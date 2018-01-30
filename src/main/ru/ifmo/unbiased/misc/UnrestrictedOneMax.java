package ru.ifmo.unbiased.misc;

import java.util.concurrent.ThreadLocalRandom;

public final class UnrestrictedOneMax {
    private final int[] individuals;
    private int individualCount;
    private final int n;
    private final boolean pure;

    public UnrestrictedOneMax(int n, boolean pure,
                              int firstIndividual, int firstFitness,
                              int secondIndividual, int secondFitness) {
        if (n > 32 || n < 1) {
            throw new IllegalArgumentException("n cannot be " + n + ", it must be in [1; 32]");
        }
        this.n = n;
        this.pure = pure;
        int globalMask = n == 32 ? -1 : (1 << n) - 1;
        firstIndividual &= globalMask;
        secondIndividual &= globalMask;

        int diffMask = firstIndividual ^ secondIndividual;
        int sameMask = globalMask ^ diffMask;

        int differentBits = Integer.bitCount(diffMask);
        int sameBits = n - differentBits;

        if ((firstFitness + secondFitness - differentBits) % 2 != 0) {
            throw new IllegalArgumentException("Fitness values are inconsistent");
        }
        int sameGood = (firstFitness + secondFitness - differentBits) / 2;
        int diffGoodFirst = firstFitness - sameGood;

        // now we have choose(sameBits, sameGood) * choose(differentBits, diffGoodFirst) individuals to test.
        int chooseDiff = choose(differentBits, diffGoodFirst);
        int count = choose(sameBits, sameGood) * chooseDiff;

        int[] individuals = new int[count];
        int individualCount = 0;

        int[] diffMaskTails = getMaskTails(diffMask);
        int[] sameMaskTails = getMaskTails(sameMask);

        int[] diffs = new int[chooseDiff];
        for (int diff = diffMaskTails[diffGoodFirst], i = 0; diff != -1; diff = nextSubset(diffMask, diff, diffMaskTails), ++i) {
            diffs[i] = diff;
        }

        for (int same = sameMaskTails[sameGood]; same != -1; same = nextSubset(sameMask, same, sameMaskTails)) {
            for (int diff : diffs) {
                int fromFirst = same ^ diff;
                int value = (firstIndividual & fromFirst) ^ (~firstIndividual & ~fromFirst & globalMask);
                individuals[individualCount++] = value;
            }
        }

        if (individualCount != count) {
            throw new AssertionError();
        }

        this.individuals = individuals;
        this.individualCount = individualCount;
    }

    private static int[] getMaskTails(int mask) {
        int[] rv = new int[Integer.bitCount(mask) + 1];
        for (int i = rv.length - 1; i > 0; --i) {
            rv[i] = mask;
            mask ^= 1 << (31 - Integer.numberOfLeadingZeros(mask));
        }
        return rv;
    }

    private static int nextSubset(int mask, int curr, int[] maskTails) {
        if (curr == 0) {
            return -1;
        }
        int rawZeroBitCount = Integer.numberOfTrailingZeros(mask & curr);
        int rawOneBitCount = Integer.numberOfTrailingZeros((mask ^ curr) >>> rawZeroBitCount);
        if (rawOneBitCount == Integer.SIZE) {
            return -1; // the current subset was the last one
        }
        int leadingChangingBitIndex = rawOneBitCount + rawZeroBitCount;
        int replacementMask = (1 << leadingChangingBitIndex) - 1;
        int sizeOfBlockOfOnes = Integer.bitCount(curr & replacementMask);
        return (curr & ~replacementMask) ^ (1 << leadingChangingBitIndex) ^ maskTails[sizeOfBlockOfOnes - 1];
    }

    public int countCompatibleIndividuals() {
        return individualCount;
    }

    public void add(int individual, int fitness) {
        int newCount = 0;

        for (int i = 0; i < individualCount; ++i) {
            int ii = individuals[i];
            int myFitness = n - Integer.bitCount(individual ^ ii);
            if (myFitness == fitness) {
                individuals[newCount++] = ii;
            }
        }

        individualCount = newCount;
    }

    // Must return the remaining compatible individual when countCompatibleIndividuals() returns 1.
    public int getIndividualToTest() {
        if (individualCount == 1) {
            return individuals[0];
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (pure) {
            return random.nextInt() >>> (32 - n);
        } else {
            return individuals[random.nextInt(individualCount)];
        }
    }

    private static int choose(int n, int k) {
        int result = 1;
        for (int i = 0; i < k; ++i) {
            result *= n - i;
            result /= i + 1;
        }
        return result;
    }
}
