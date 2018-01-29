package ru.ifmo.unbiased.misc;

import java.util.concurrent.ThreadLocalRandom;

public final class UnrestrictedOneMax {
    private final int[] individuals;
    private int individualCount;
    private final int n;

    public UnrestrictedOneMax(int n,
                              int firstIndividual, int firstFitness,
                              int secondIndividual, int secondFitness) {
        this.n = n;
        int globalMask = (1 << n) - 1;
        firstIndividual &= globalMask;
        secondIndividual &= globalMask;
        int differentBits = Integer.bitCount(firstIndividual ^ secondIndividual);
        int sameBits = n - differentBits;

        if ((firstFitness + secondFitness - differentBits) % 2 != 0) {
            throw new IllegalArgumentException("Fitness values are inconsistent");
        }
        int sameGood = (firstFitness + secondFitness - differentBits) / 2;
        int diffGoodFirst = firstFitness - sameGood;

        // now we have choose(sameBits, sameGood) * choose(differentBits, diffGoodFirst) individuals to test.
        int count = choose(sameBits, sameGood) * choose(differentBits, diffGoodFirst);
        this.individuals = new int[count];
        this.individualCount = 0;

        // shall be faster... we can do it actually.
        for (int ind = 0, indMax = 1 << n; ind < indMax; ++ind) {
            int myFirstFitness = n - Integer.bitCount(firstIndividual ^ ind);
            if (firstFitness == myFirstFitness) {
                int mySecondFitness = n - Integer.bitCount(secondIndividual ^ ind);
                if (secondFitness == mySecondFitness) {
                    individuals[individualCount++] = ind;
                }
            }
        }

        if (individualCount != count) {
            throw new AssertionError();
        }
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
        return individuals[ThreadLocalRandom.current().nextInt(individualCount)];
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
