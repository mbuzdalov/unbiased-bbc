package ru.ifmo.unbiased.algo;

import ru.ifmo.unbiased.Individual;
import static ru.ifmo.unbiased.Operators.FLIP_ONE_SAME;
import static ru.ifmo.unbiased.Operators.XOR3;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.ops.Operator2;
import ru.ifmo.unbiased.ops.UnbiasedOperator;
import ru.ifmo.unbiased.util.ImmutableIntArray;

public final class OneMaxComplicated {
    private OneMaxComplicated() {}

    private static final UnbiasedOperator FLIP_TWO_SAME = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(2);
            flipDifferent(0);
        }
    };

    private static final UnbiasedOperator FLIP_THREE_SAME = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(3);
            flipDifferent(0);
        }
    };

    private static final UnbiasedOperator FLIP_ONE_WHERE_ALL_THREE_SAME = new UnbiasedOperator(3) {
        @Override
        protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
            result[0] = 1;
        }
    };

    private static final UnbiasedOperator FLIP_ONE_WHERE_SECOND_DIFFERS = new UnbiasedOperator(3) {
        @Override
        protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
            result[1] = 1;
        }
    };

    private static final UnbiasedOperator FLIP_ONE_WHERE_THIRD_DIFFERS = new UnbiasedOperator(3) {
        @Override
        protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
            result[2] = 1;
        }
    };

    private static final UnbiasedOperator FLIP_TWO_WHERE_THIRD_DIFFERS = new UnbiasedOperator(3) {
        @Override
        protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
            result[2] = 2;
        }
    };

    private static final UnbiasedOperator FLIP_ALL_WHERE_SECOND_DIFFERS_AND_ONE_WHERE_FIRST_DIFFERS = new UnbiasedOperator(3) {
        @Override
        protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
            result[1] = bitCounts.get(1);
            result[3] = 1;
        }
    };

    public static int runTernary(UnbiasedProcessor processor) {
        processor.reset();
        try {
            int n = processor.getProblemSize();
            Individual good = processor.newRandomIndividual();
            Individual bad = good;
            int sameCount = n;

            //noinspection InfiniteLoopStatement
            while (true) {
                if (sameCount == 1) {
                    if (good.fitness() == n - 1) {
                        good = processor.query(FLIP_ONE_SAME, good, bad);
                    }
                } else if (sameCount == 2) {
                    if (good.fitness() == n - 2) {
                        good = processor.query(FLIP_TWO_SAME, good, bad);
                    } else {
                        Individual flipOne = processor.query(FLIP_ONE_SAME, good, bad);
                        if (flipOne.fitness() == n - 2) {
                            good = processor.query(FLIP_ONE_WHERE_ALL_THREE_SAME, good, bad, flipOne);
                        } else {
                            good = flipOne;
                        }
                    }
                } else {
                    Individual m = processor.query(FLIP_THREE_SAME, good, bad);
                    if (m.fitness() == good.fitness() + 3) {
                        good = m;
                    } else if (m.fitness() == good.fitness() - 3) {
                        bad = processor.query(XOR3, good, bad, m);
                    } else if (m.fitness() == good.fitness() + 1) {
                        Individual p = processor.query(FLIP_TWO_WHERE_THIRD_DIFFERS, good, bad, m);
                        if (p.fitness() == good.fitness() + 2) {
                            good = p;
                        } else {
                            Individual r = processor.query(
                                    FLIP_ALL_WHERE_SECOND_DIFFERS_AND_ONE_WHERE_FIRST_DIFFERS, good, m, p);
                            if (r.fitness() == good.fitness() + 2) {
                                good = r;
                            } else {
                                good = processor.query(XOR3, good, p, r);
                            }
                        }
                        bad = processor.query(XOR3, good, bad, m);
                    } else {
                        Individual p = processor.query(FLIP_ONE_WHERE_THIRD_DIFFERS, good, bad, m);
                        if (p.fitness() == good.fitness() + 1) {
                            good = p;
                        } else {
                            Individual r = processor.query(FLIP_ONE_WHERE_SECOND_DIFFERS, good, m, p);
                            if (r.fitness() == good.fitness() + 1) {
                                good = r;
                            } else {
                                good = processor.query(XOR3, m, p, r);
                            }
                        }
                        bad = processor.query(XOR3, good, bad, m);
                    }
                    sameCount -= 3;
                }
            }
        } catch (UnbiasedProcessor.OptimumFound found) {
            return found.numberOfQueries();
        }
    }
}
