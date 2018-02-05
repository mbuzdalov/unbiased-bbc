package ru.ifmo.unbiased;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;

import ru.ifmo.unbiased.ops.UnbiasedOperator;
import ru.ifmo.unbiased.util.ImmutableBitArray;
import ru.ifmo.unbiased.util.ImmutableIntArray;

public class UnbiasedProcessor {
    private final int n;
    private final int maxArity;
    private final ToIntFunction<ImmutableBitArray> fitness;
    private final int maxFitness;
    private final List<IndividualImpl> queriedIndividuals = new ArrayList<>();

    private final ImmutableBitArray[] selected;
    private final int[][] counts;
    private final int[][] results;
    private final ImmutableIntArray[] countsView;

    private final int[] nextToFlip;
    private final int[] whatToFlip;
    private final int[] firstToFlip;

    public UnbiasedProcessor(int problemSize, int maxArity, ToIntFunction<ImmutableBitArray> fitness, int maxFitness) {
        this.n = problemSize;
        this.maxArity = maxArity;
        this.fitness = fitness;
        this.maxFitness = maxFitness;

        selected = new ImmutableBitArray[maxArity];
        counts = new int[maxArity][];
        results = new int[maxArity][];
        countsView = new ImmutableIntArray[maxArity];

        nextToFlip = new int[n];
        whatToFlip = new int[n];
        firstToFlip = new int[1 << (maxArity - 1)];

        for (int i = 0; i < maxArity; ++i) {
            counts[i] = new int[1 << i];
            results[i] = new int[1 << i];
            countsView[i] = new ImmutableIntArray(counts[i]);
        }
    }

    public int getProblemSize() {
        return n;
    }

    public int getMaxArity() {
        return maxArity;
    }

    public void reset() {
        for (IndividualImpl i : queriedIndividuals) {
            i.processor = null;
        }
        queriedIndividuals.clear();
    }

    public Individual newRandomIndividual() throws OptimumFound {
        ImmutableBitArray q0 = ImmutableBitArray.random(n);
        IndividualImpl rv = new IndividualImpl(this, q0, fitness.applyAsInt(q0));
        queriedIndividuals.add(rv);
        if (rv.fitness == maxFitness) {
            throw new OptimumFound(queriedIndividuals.size());
        } else {
            return rv;
        }
    }

    private Individual queryImpl(UnbiasedOperator operator) throws OptimumFound {
        int arity = operator.getArity();
        ImmutableBitArray result;
        if (arity == 0) {
            result = ImmutableBitArray.random(n);
        } else {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int[] xCounts = counts[arity - 1];
            int[] xResults = results[arity - 1];
            ImmutableIntArray xView = countsView[arity - 1];

            Arrays.fill(firstToFlip, -1);
            Arrays.fill(xCounts, 0);
            Arrays.fill(xResults, 0);
            for (int i = 0; i < n; ++i) {
                int mask = 0;
                boolean b0 = selected[0].getBit(i);
                for (int j = 1; j < arity; ++j) {
                    if (selected[j].getBit(i) != b0) {
                        mask |= 1 << (j - 1);
                    }
                }
                nextToFlip[i] = firstToFlip[mask];
                firstToFlip[mask] = i;
                ++xCounts[mask];
            }
            int maxMask = 1 << (arity - 1);
            operator.apply(xView, xResults);

            int totalToFlip = 0;
            for (int mask = 0; mask < maxMask; ++mask) {
                int current = totalToFlip;
                int countThisMask = 0;
                for (int e = firstToFlip[mask]; e != -1; e = nextToFlip[e]) {
                    whatToFlip[current++] = e;
                    ++countThisMask;
                }
                if (countThisMask != xCounts[mask]) {
                    throw new AssertionError();
                }
                for (int t = 0; t < xResults[mask]; ++t) {
                    int index = random.nextInt(countThisMask - t) + totalToFlip;
                    int tmp = whatToFlip[totalToFlip];
                    whatToFlip[totalToFlip] = whatToFlip[index];
                    whatToFlip[index] = tmp;
                    ++totalToFlip;
                }
            }
            result = selected[0].flip(whatToFlip, totalToFlip);
        }
        IndividualImpl rv = new IndividualImpl(this, result, fitness.applyAsInt(result));
        queriedIndividuals.add(rv);
        if (rv.fitness == maxFitness) {
            throw new OptimumFound(queriedIndividuals.size());
        } else {
            return rv;
        }
    }

    public Individual query(UnbiasedOperator operator, Individual... individuals) throws OptimumFound {
        int arity = operator.getArity();
        if (arity > maxArity) {
            throw new UnbiasedProtocolException("Maximum allowed arity is " + maxArity
                    + ", an operator with arity " + arity + " is used");
        }
        if (arity != individuals.length) {
            throw new UnbiasedProtocolException("An operator with arity " + arity
                    + " is used, but " + individuals.length + " arguments are provided");
        }

        for (int i = 0; i < arity; ++i) {
            if (individuals[i] instanceof IndividualImpl) {
                IndividualImpl ii  = (IndividualImpl) (individuals[i]);
                if (ii.processor != this) {
                    throw new IllegalArgumentException("Individual is supplied which is unknown to the processor");
                }
                selected[i] = ii.bits;
            } else {
                throw new IllegalArgumentException("Individual is supplied which is unknown to the processor");
            }
        }

        return queryImpl(operator);
    }

    private static final class IndividualImpl implements Individual {
        private UnbiasedProcessor processor;
        private final ImmutableBitArray bits;
        private final int fitness;

        private IndividualImpl(UnbiasedProcessor processor, ImmutableBitArray bits, int fitness) {
            this.processor = processor;
            this.bits = bits;
            this.fitness = fitness;
        }

        @Override
        public int fitness() {
            return fitness;
        }
    }

    public static class OptimumFound extends Exception {
        private final int nQueries;

        OptimumFound(int nQueries) {
            this.nQueries = nQueries;
        }

        public int numberOfQueries() {
            return nQueries;
        }
    }
}
