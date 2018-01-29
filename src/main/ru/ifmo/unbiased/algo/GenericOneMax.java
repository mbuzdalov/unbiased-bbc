package ru.ifmo.unbiased.algo;

import java.util.Arrays;

import ru.ifmo.unbiased.Individual;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.misc.UnrestrictedOneMax;
import ru.ifmo.unbiased.ops.Operator2;
import ru.ifmo.unbiased.ops.UnbiasedOperator;
import ru.ifmo.unbiased.util.ImmutableIntArray;

import static ru.ifmo.unbiased.Operators.XOR3;

public final class GenericOneMax {
    private GenericOneMax() {}

    private static final UnbiasedOperator FLIP_ONE_WHERE_ALL_THREE_SAME = new UnbiasedOperator(3) {
        @Override
        protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
            result[0] = 1;
        }
    };

    public static int runGeneric(UnbiasedProcessor processor) {
        int maxArity = processor.getMaxArity();
        if (maxArity <= 2) {
            throw new IllegalArgumentException("General algorithm works only for max arity >= 3");
        }
        if (maxArity >= 30) {
            throw new UnsupportedOperationException("Current implementation of the general algorithm cannot work with arity greater than 30");
        }
        processor.reset();
        try {
            int n = processor.getProblemSize();
            if (n <= (1 << (maxArity - 1))) {
                // The arity is enough for explicit solving in O(n / log n + log n)
                int log = 2; // this is to cover n == 1 during initialization.
                while (n > 1 << (log - 1)) {
                    log += 1;
                }
                Individual[] individuals = new Individual[log];
                int[] virtualIndividuals = new int[log];
                individuals[0] = processor.newRandomIndividual();
                virtualIndividuals[0] = 0;
                for (int i = 1; i < log; ++i) {
                    individuals[i] = processor.query(flipUpperHalf(i, 0), Arrays.copyOf(individuals, i));
                    virtualIndividuals[i] = simulateUpperHalf(n, Arrays.copyOf(virtualIndividuals, i));
                }
                UnrestrictedOneMax unrestricted = new UnrestrictedOneMax(n,
                        virtualIndividuals[0], individuals[0].fitness(),
                        virtualIndividuals[1], individuals[1].fitness());

                for (int i = 2; i < log; ++i) {
                    unrestricted.add(virtualIndividuals[i], individuals[i].fitness());
                }

                int[] indicesToSet = new int[n];
                int[] nonzeroVirtualIndividuals = Arrays.copyOfRange(virtualIndividuals, 1, log);
                for (int i = 0; i < n; ++i) {
                    indicesToSet[i] = collectBits(nonzeroVirtualIndividuals, i);
                }
                FixedQueryOperator magic = new FixedQueryOperator(log, 0);
                //noinspection InfiniteLoopStatement
                while (true) {
                    int individual = unrestricted.getIndividualToTest();
                    Arrays.fill(magic.valuesToSet, false);
                    for (int i = 0; i < n; ++i) {
                        if ((individual & (1 << i)) != 0) {
                            int indexWhere = indicesToSet[i];
                            magic.valuesToSet[indexWhere] = true;
                        }
                    }
                    Individual ind = processor.query(magic, individuals);
                    unrestricted.add(individual, ind.fitness());
                }
            } else {
                // Solve the problem by splitting it into blocks of maximum allowed size for the given arity.
                int blockSize = (1 << (maxArity - 1)) - 1;

                Individual zero = processor.newRandomIndividual();
                Individual usedMask = zero;
                Individual answer = zero;

                for (int done = 0; done < n; done += blockSize) {
                    int remaining = Math.min(blockSize, n - done);
                    if (remaining == 1) {
                        // this is the last frame, so we just do it
                        processor.query(FLIP_ONE_WHERE_ALL_THREE_SAME, answer, zero, usedMask);
                        throw new AssertionError("Optimum must have been found before");
                    } else {
                        int log = 1;
                        while (remaining > (1 << (log - 1)) - 1) {
                            log += 1;
                        }
                        if (remaining == blockSize) {
                            if (log != maxArity) {
                                throw new AssertionError("log = " + log + " maxArity = " + maxArity);
                            }
                        }

                        Individual currentMask = processor.query(flipXCoinciding(remaining), zero, usedMask);
                        Individual[] individuals = new Individual[log + 1];
                        int[] virtualIndividuals = new int[log + 1];
                        individuals[0] = zero;
                        individuals[1] = currentMask;
                        virtualIndividuals[0] = 0;
                        virtualIndividuals[1] = (1 << remaining) - 1;

                        // zero.fitness = (others) + (ones in remaining)
                        // currentMask.fitness = (others) + remaining - (ones in remaining)
                        // (others) = (zero.fitness + currentMask.fitness - remaining) / 2
                        int others = (zero.fitness() + currentMask.fitness() - remaining) / 2;

                        UnrestrictedOneMax unrestricted = null;

                        if (log < 2) {
                            // to satisfy IDEA's null inference.
                            throw new AssertionError();
                        }

                        Individual alreadyFoundDuringInit = null;

                        if (individuals[0].fitness() - others == remaining) {
                            alreadyFoundDuringInit = individuals[0];
                        } else if (individuals[1].fitness() - others == remaining) {
                            alreadyFoundDuringInit = individuals[1];
                        }

                        for (int i = 2; alreadyFoundDuringInit == null && i <= log; ++i) {
                            individuals[i] = processor.query(flipUpperHalf(i, 1), Arrays.copyOf(individuals, i));
                            virtualIndividuals[i] = simulateUpperHalf(remaining, Arrays.copyOf(virtualIndividuals, i));
                            if (i == 2) {
                                unrestricted = new UnrestrictedOneMax(remaining,
                                        virtualIndividuals[0], individuals[0].fitness() - others,
                                        virtualIndividuals[2], individuals[2].fitness() - others);
                            } else {
                                unrestricted.add(virtualIndividuals[i], individuals[i].fitness() - others);
                            }
                            if (individuals[i].fitness() - others == remaining) {
                                alreadyFoundDuringInit = individuals[i];
                            }
                        }

                        if (alreadyFoundDuringInit != null) {
                            if (alreadyFoundDuringInit != zero){
                                answer = processor.query(XOR3, answer, zero, alreadyFoundDuringInit);
                            }
                            usedMask = processor.query(XOR3, usedMask, zero, currentMask);
                        } else {
                            int[] indicesToSet = new int[remaining];
                            int[] nonTrivialVirtualIndividuals = Arrays.copyOfRange(virtualIndividuals, 2, log + 1);
                            for (int i = 0; i < remaining; ++i) {
                                indicesToSet[i] = collectBits(nonTrivialVirtualIndividuals, i);
                            }
                            FixedQueryOperator magic = new FixedQueryOperator(log, 1);
                            Individual[] notFullyInvertedIndividuals = new Individual[log];
                            notFullyInvertedIndividuals[0] = individuals[0];
                            System.arraycopy(individuals, 2, notFullyInvertedIndividuals, 1, log - 1);
                            while (true) {
                                int individual = unrestricted.getIndividualToTest();
                                Arrays.fill(magic.valuesToSet, false);
                                for (int i = 0; i < remaining; ++i) {
                                    if ((individual & (1 << i)) != 0) {
                                        int indexWhere = indicesToSet[i];
                                        magic.valuesToSet[indexWhere] = true;
                                    }
                                }
                                Individual ind = processor.query(magic, notFullyInvertedIndividuals);
                                if (unrestricted.countCompatibleIndividuals() == 1) {
                                    // this is the partial answer
                                    answer = processor.query(XOR3, answer, zero, ind);
                                    usedMask = processor.query(XOR3, usedMask, zero, currentMask);
                                    break;
                                }
                                unrestricted.add(individual, ind.fitness() - others);
                            }
                        }
                    }
                }
                throw new AssertionError("The optimum must have been found by this time");
            }
        } catch (UnbiasedProcessor.OptimumFound found) {
            return found.numberOfQueries();
        }
    }

    private static int collectBits(int[] individuals, int bit) {
        int rv = 0;
        for (int i = 0; i < individuals.length; ++i) {
            rv ^= ((individuals[i] >>> bit) & 1) << i;
        }
        return rv;
    }

    private static int simulateUpperHalf(int n, int[] individuals) {
        if (individuals[0] != 0) {
            throw new AssertionError();
        }
        int last = collectBits(individuals, 0);
        int lastIndex = 0;
        int rv = 0;
        for (int i = 1; i < n; ++i) {
            int curr = collectBits(individuals, i);
            if (curr != last) {
                // [last; curr) is the current component.
                int diff = (i - lastIndex) / 2; // rounded down
                for (int j = lastIndex + diff; j < i; ++j) {
                    rv ^= 1 << j;
                }
                last = curr;
                lastIndex = i;
            }
        }
        int diff = (n - lastIndex) / 2; // rounded down
        for (int j = lastIndex + diff; j < n; ++j) {
            rv ^= 1 << j;
        }
        return rv;
    }

    private static UnbiasedOperator flipXCoinciding(int x) {
        return new Operator2() {
            @Override
            protected void applyBinary(int sameBits, int differentBits) {
                flipSame(x);
            }
        };
    }

    private static UnbiasedOperator flipUpperHalf(int arity, int startFrom) {
        return new UnbiasedOperator(arity) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                for (int i = startFrom, iMax = bitCounts.length(); i < iMax; ++i) {
                    result[i] = bitCounts.get(i) - bitCounts.get(i) / 2;
                }
            }
        };
    }

    private static class FixedQueryOperator extends UnbiasedOperator {
        boolean[] valuesToSet;
        final int startFrom;

        FixedQueryOperator(int arity, int startFrom) {
            super(arity);
            valuesToSet = new boolean[1 << (arity - 1)];
            this.startFrom = startFrom;
        }

        @Override
        protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
            for (int i = startFrom; i < valuesToSet.length; ++i) {
                if (bitCounts.get(i) > 1) {
                    throw new AssertionError();
                }
                result[i] = valuesToSet[i] ? bitCounts.get(i) : 0;
            }
        }
    }
}
