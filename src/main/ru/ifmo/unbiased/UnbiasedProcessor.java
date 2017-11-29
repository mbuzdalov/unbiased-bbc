package ru.ifmo.unbiased;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;

import ru.ifmo.unbiased.ops.UnbiasedOperator;
import ru.ifmo.unbiased.util.ImmutableBitArray;
import ru.ifmo.unbiased.util.ImmutableIntArray;

public abstract class UnbiasedProcessor {
    private final int n;
    private final int maxArity;
    private final ToIntFunction<ImmutableBitArray> fitness;
    private final int maxFitness;

    protected UnbiasedProcessor(int problemSize, int maxArity, ToIntFunction<ImmutableBitArray> fitness, int maxFitness) {
        this.n = problemSize;
        this.maxArity = maxArity;
        this.fitness = fitness;
        this.maxFitness = maxFitness;
    }

    public final int run() {
        final List<Individual> queriedIndividuals = new ArrayList<>();
        final ListView view = new ListView(queriedIndividuals);
        final List<Integer> selectedIndices = new ArrayList<>();

        setup();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        ImmutableBitArray q0 = ImmutableBitArray.random(n);
        queriedIndividuals.add(new Individual(q0, fitness.applyAsInt(q0)));

        ImmutableBitArray[] selected = new ImmutableBitArray[maxArity];
        int[][] counts = new int[maxArity][];
        int[][] results = new int[maxArity][];
        ImmutableIntArray[] countsView = new ImmutableIntArray[maxArity];
        for (int i = 0; i < maxArity; ++i) {
            counts[i] = new int[1 << i];
            results[i] = new int[1 << i];
            countsView[i] = new ImmutableIntArray(counts[i]);
        }

        int[] toFlip = new int[n];
        Index[] indices = new Index[n];
        for (int i = 0; i < n; ++i) {
            indices[i] = new Index(i);
        }

        while (queriedIndividuals.get(queriedIndividuals.size() - 1).fitness != maxFitness) {
            selectedIndices.clear();
            UnbiasedOperator operator = makeDecision(view, selectedIndices, random);
            int arity = operator.getArity();
            if (arity > maxArity) {
                throw new UnbiasedProtocolException("Maximum allowed arity is " + maxArity
                        + ", an operator with arity " + arity + " is used");
            }
            if (arity != selectedIndices.size()) {
                throw new UnbiasedProtocolException("An operator with arity " + arity
                        + " is used, but " + selectedIndices.size() + " arguments are provided");
            }
            for (int i = 0; i < arity; ++i) {
                int indIndex = selectedIndices.get(i);
                if (indIndex >= queriedIndividuals.size()) {
                    throw new UnbiasedProtocolException("Individual #" + indIndex + " is requested, but we have "
                            + queriedIndividuals.size() + " in total");
                }
                selected[i] = queriedIndividuals.get(indIndex).bits;
            }
            ImmutableBitArray result;
            if (arity == 0) {
                result = ImmutableBitArray.random(n);
            } else {
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
            queriedIndividuals.add(new Individual(result, fitness.applyAsInt(result)));
        }

        return queriedIndividuals.size();
    }

    protected abstract void setup();

    protected abstract UnbiasedOperator makeDecision(List<Integer> fitnessValues, List<Integer> selectedIndices, Random random);

    private static final class Individual {
        private final ImmutableBitArray bits;
        private final int fitness;

        private Individual(ImmutableBitArray bits, int fitness) {
            this.bits = bits;
            this.fitness = fitness;
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

    private static final class ListView extends AbstractList<Integer> {
        private final List<Individual> individuals;

        private ListView(List<Individual> individuals) {
            this.individuals = individuals;
        }

        @Override
        public Integer get(int index) {
            return individuals.get(index).fitness;
        }

        @Override
        public int size() {
            return individuals.size();
        }
    }
}
