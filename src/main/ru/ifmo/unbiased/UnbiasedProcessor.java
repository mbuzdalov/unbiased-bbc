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

    private final int[] toFlip;
    private final Index[] indices;

    public UnbiasedProcessor(int problemSize, int maxArity, ToIntFunction<ImmutableBitArray> fitness, int maxFitness) {
        this.n = problemSize;
        this.maxArity = maxArity;
        this.fitness = fitness;
        this.maxFitness = maxFitness;

        selected = new ImmutableBitArray[maxArity];
        counts = new int[maxArity][];
        results = new int[maxArity][];
        countsView = new ImmutableIntArray[maxArity];

        toFlip = new int[n];
        indices = new Index[n];

        for (int i = 0; i < maxArity; ++i) {
            counts[i] = new int[1 << i];
            results[i] = new int[1 << i];
            countsView[i] = new ImmutableIntArray(counts[i]);
        }
        for (int i = 0; i < n; ++i) {
            indices[i] = new Index(i);
        }
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

            Arrays.sort(indices, indexComparator);
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
                indices[i].mask = mask;
                ++xCounts[mask];
            }
            int maxMask = 1 << (arity - 1);
            operator.apply(xView, xResults);
            Arrays.sort(indices, maskComparator);
            int totalToFlip = 0;
            for (int mask = 0, start = 0; mask < maxMask; ++mask) {
                int finish = start;
                while (finish < n && indices[finish].mask == mask) {
                    ++finish;
                }
                for (int t = 0; t < xResults[mask]; ++t) {
                    int index = random.nextInt(finish - start - t) + start;
                    Index chosen = indices[index];
                    toFlip[totalToFlip++] = chosen.index;
                    indices[index] = indices[finish - 1 - t];
                    indices[finish - 1 - t] = chosen;
                    if (finish - 1 - t < start) {
                        throw new AssertionError();
                    }
                }
                start = finish;
            }
            result = selected[0].flip(toFlip, totalToFlip);
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

    private static final class Index {
        final int index;
        int mask;

        private Index(int index) {
            this.index = index;
        }
    }

    private static final Comparator<Index> indexComparator = Comparator.comparingInt(o -> o.index);
    private static final Comparator<Index> maskComparator = Comparator.comparingInt(o -> o.mask);

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
