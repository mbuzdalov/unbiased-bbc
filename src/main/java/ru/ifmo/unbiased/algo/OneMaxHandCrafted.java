package ru.ifmo.unbiased.algo;

import ru.ifmo.unbiased.Individual;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.ops.Operator2;
import ru.ifmo.unbiased.ops.UnbiasedOperator;
import ru.ifmo.unbiased.util.ImmutableIntArray;

import static ru.ifmo.unbiased.Operators.*;

public final class OneMaxHandCrafted {
    private OneMaxHandCrafted() {}

    private static final UnbiasedOperator FLIP_TWO_SAME = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(2);
            flipDifferent(0);
        }
    };

    private static final UnbiasedOperator FLIP_TWO_DIFFERENT = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(0);
            flipDifferent(2);
        }
    };

    private static final UnbiasedOperator FLIP_THREE_SAME = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(3);
            flipDifferent(0);
        }
    };

    private static final UnbiasedOperator FLIP_FOUR_SAME = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(4);
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

    private static final UnbiasedOperator FLIP_ONE_WHERE_FIRST_DIFFERS = new UnbiasedOperator(3) {
        @Override
        protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
            result[3] = 1;
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

    public static int runQuaternary1(UnbiasedProcessor processor) {
        processor.reset();
        try {
            Individual first = processor.newRandomIndividual();
            Individual second = first;
            int sameCount = processor.getProblemSize();
            int n = sameCount;

            //noinspection InfiniteLoopStatement
            while (true) {
                if (sameCount < 7) {
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        if (sameCount == 1) {
                            if (first.fitness() == n - 1) {
                                first = processor.query(FLIP_ONE_SAME, first, second);
                            }
                        } else if (sameCount == 2) {
                            if (first.fitness() == n - 2) {
                                first = processor.query(FLIP_TWO_SAME, first, second);
                            } else {
                                Individual flipOne = processor.query(FLIP_ONE_SAME, first, second);
                                if (flipOne.fitness() == n - 2) {
                                    first = processor.query(FLIP_ONE_WHERE_ALL_THREE_SAME, first, second, flipOne);

                                } else {
                                    first = flipOne;
                                }
                            }
                        } else {
                            Individual m = processor.query(FLIP_THREE_SAME, first, second);
                            if (m.fitness() == first.fitness() + 3) {
                                first = m;
                            } else if (m.fitness() == first.fitness() - 3) {
                                second = processor.query(XOR3, first, second, m);
                            } else if (m.fitness() == first.fitness() + 1) {


                                Individual p = processor.query(FLIP_TWO_WHERE_THIRD_DIFFERS, first, second, m);
                                if (p.fitness() == first.fitness() + 2) {
                                    first = p;
                                    second = processor.query(XOR3, first, second, m);
                                } else {
                                    Individual r = processor.query(FLIP_ALL_WHERE_SECOND_DIFFERS_AND_ONE_WHERE_FIRST_DIFFERS, first, m, p);

                                    if (r.fitness() == first.fitness() + 2) {
                                        first = r;
                                        second = processor.query(XOR3, first, second, m);
                                    } else {
                                        first = processor.query(XOR3, first, p, r);
                                        second = processor.query(XOR3, first, second, m);
                                    }
                                }
                            } else {
                                Individual p = processor.query(FLIP_ONE_WHERE_THIRD_DIFFERS, first, second, m);
                                if (p.fitness() == first.fitness() + 1) {
                                    first = p;
                                    second = processor.query(XOR3, first, second, m);
                                } else {
                                    Individual r = processor.query(FLIP_ONE_WHERE_SECOND_DIFFERS, first, m, p);
                                    if (r.fitness() == first.fitness() + 1) {
                                        first = r;
                                        second = processor.query(XOR3, first, second, m);
                                    } else {
                                        first = processor.query(XOR3, m, p, r);
                                        second = processor.query(XOR3, first, second, m);
                                    }
                                }
                            }
                            sameCount -= 3;

                        }
                    }
                } else {
                    Individual b = processor.query(FLIP_FOUR_SAME, first, second);
                    //fi = +++++++
                    //se = +++++++
                    //b  = ----+++
                    if (b.fitness() == first.fitness() + 4) {
                        //first = 0000
                        //b = 1111;
                        first = b;
                    } else if (b.fitness() == first.fitness() - 4) {
                        //first = 1111
                        //b = 0000
                        second = processor.query(XOR3, second, first, b);
                    } else if (first.fitness() == b.fitness()) {
                        //first = 1100
                        //b = 0011
                        Individual d = processor.query(FLIP_TWO_DIFFERENT, first, b);

                        if (d.fitness() == first.fitness() + 2) {
                            //first = 1100
                            //b = 0011
                            //d = 11|11|
                            first = d;
                            second = processor.query(XOR3, second, first, b);
                        } else if (d.fitness() == first.fitness()) {
                            //first = 1100
                            //b = 0011
                            //d = 1|01|0
                            Individual e = processor.query(Operators.ternary1DD1DS, first, b, d);
                            //f = 1100
                            //b = 0011
                            //d = 1010 - one of two outer and one of two inner
                            if (e.fitness() == first.fitness() + 2) {
                                //e = 1111
                                first = e;
                                second = processor.query(XOR3, second, first, b);
                            } else if (e.fitness() == first.fitness()) {
                                //e = 0110
                                //e = 1001
                                Individual f = processor.query(Operators.quaDSSDDD, first, b, d, e);
                                //f = 11|00|
                                //b = 00|11|
                                //d = 10|10|
                                //e = 01|10|
                                if (f.fitness() == first.fitness() + 2) {
                                    //f = 1111
                                    first = f;
                                    second = processor.query(XOR3, second, first, b);
                                } else if (f.fitness() == first.fitness() - 2) {
                                    //f = 11|00
                                    //b = 00|11
                                    //d = 10|10
                                    //e = 10|01
                                    //f = 0000
                                    first = processor.query(XOR3, first, b, f);
                                    second = processor.query(XOR3, second, first, b);
                                }
                            } else if (e.fitness() == first.fitness() - 2) {
                                //f = 1100
                                //b = 0011
                                //e = 0000
                                first = processor.query(XOR3, first, b, e);
                                second = processor.query(XOR3, second, first, b);
                            }
                        } else if (d.fitness() == first.fitness() - 2) {
                            //d = 0000
                            first = processor.query(XOR3, first, b, d);
                            second = processor.query(XOR3, second, first, b);
                        }
                    } else if (b.fitness() == first.fitness() - 2) {
                        //f = 0111
                        //b = 1000
                        Individual d = processor.query(FLIP_TWO_DIFFERENT, first, b);
                        if (d.fitness() == first.fitness()) {
                            //d = 1011
                            Individual e = processor.query(FLIP_ONE_WHERE_FIRST_DIFFERS, first, b, d);
                            //f = |01|11
                            //b = |10|00
                            //d = |10|11
                            if (e.fitness() == first.fitness() + 1) {
                                //e = 1111
                                first = e;
                                second = processor.query(XOR3, second, first, b);
                            } else if (e.fitness() == first.fitness() - 1) {
                                //e = 0011
                                first = processor.query(Operators.quaDDS, first, b, d, e);
                                //f = 0111
                                //b = 1000
                                //d = 1011
                                //e = 0011
                                second = processor.query(XOR3, second, first, b);
                            }
                        } else if (d.fitness() == first.fitness() - 2) {
                            //f = |01|11
                            //b = |10|00
                            //d = |01|00
                            Individual e = processor.query(FLIP_ONE_WHERE_SECOND_DIFFERS, first, b, d);
                            if (e.fitness() == first.fitness() + 1) {
                                //e = 1111
                                first = e;
                                second = processor.query(XOR3, second, first, b);
                            } else if (e.fitness() == first.fitness() - 1) {
                                //f = |0|111
                                //b = |1|000
                                //d = |0|100
                                //e = |0|011
                                first = processor.query(Operators.quaDSS, first, b, d, e);
                                second = processor.query(XOR3, second, first, b);
                            }
                        }
                    } else if (b.fitness() == first.fitness() + 2) {
                        //b = 1110
                        //f = 0001
                        Individual d = processor.query(FLIP_TWO_DIFFERENT, b, first);
                        if (d.fitness() == b.fitness()) {
                            //d = 1101
                            Individual e = processor.query(FLIP_ONE_WHERE_FIRST_DIFFERS, b, first, d);
                            //b = 11|10|
                            //f = 00|01|
                            //d = 11|01|
                            if (e.fitness() == b.fitness() + 1) {
                                //e = 1111
                                first = e;
                                second = processor.query(XOR3, second, first, b);
                            } else if (e.fitness() == b.fitness() - 1) {
                                //b = 111|0|
                                //f = 000|1|
                                //d = 110|1|
                                //e = 110|0|
                                first = processor.query(Operators.quaDDS, b, first, d, e);
                                second = processor.query(XOR3, second, first, b);
                            }
                        } else if (d.fitness() == b.fitness() - 2) {
                            //d = 0010
                            Individual e = processor.query(FLIP_ONE_WHERE_SECOND_DIFFERS, b, first, d);
                            //b = 11|10|
                            //f = 00|01|
                            //d = 00|10|
                            if (e.fitness() == b.fitness() + 1) {
                                //e = 1111
                                first = e;
                                second = processor.query(XOR3, second, first, b);
                            } else if (e.fitness() == b.fitness() - 1) {
                                //e = 1100
                                first = processor.query(Operators.quaDSS, b, first, d, e);
                                second = processor.query(XOR3, second, first, b);
                            }
                        }

                    }
                    sameCount -= 4;

                    if (sameCount >= 3) {
                        Individual m = processor.query(FLIP_THREE_SAME, first, second);
                        if (m.fitness() == first.fitness() + 3) {
                            first = m;
                        } else if (m.fitness() == first.fitness() - 3) {
                            second = processor.query(XOR3, second, first, m);
                        } else if (m.fitness() == first.fitness() + 1) {
                            Individual p = processor.query(FLIP_TWO_WHERE_THIRD_DIFFERS, first, second, m);
                            if (p.fitness() == first.fitness() + 2) {
                                first = p;
                                second = processor.query(XOR3, first, second, m);
                            } else {
                                Individual r = processor.query(FLIP_ALL_WHERE_SECOND_DIFFERS_AND_ONE_WHERE_FIRST_DIFFERS, first, m, p);

                                if (r.fitness() == first.fitness() + 2) {
                                    first = r;
                                    second = processor.query(XOR3, first, second, m);
                                } else {
                                    first = processor.query(XOR3, first, p, r);
                                    second = processor.query(XOR3, first, second, m);
                                }
                            }
                        } else {
                            Individual p = processor.query(FLIP_ONE_WHERE_THIRD_DIFFERS, first, second, m);
                            if (p.fitness() == first.fitness() + 1) {
                                first = p;
                                second = processor.query(XOR3, first, second, m);
                            } else {
                                Individual r = processor.query(FLIP_ONE_WHERE_SECOND_DIFFERS, first, m, p);
                                if (r.fitness() == first.fitness() + 1) {
                                    first = r;
                                    second = processor.query(XOR3, first, second, m);
                                } else {
                                    first = processor.query(XOR3, m, p, r);
                                    second = processor.query(XOR3, first, second, m);
                                }
                            }
                        }
                        sameCount -= 3;

                    }
                }
            }
        } catch (UnbiasedProcessor.OptimumFound found) {
            return found.numberOfQueries();
        }
    }

    private static class Operators {
        static final UnbiasedOperator ternary1DD1DS = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 1;
                result[2] = 0;
                result[3] = 1;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator quaDDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 0; // DSS
                result[2] = 0; // SDS
                result[3] = bitCounts.get(3); // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaDSS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = bitCounts.get(1); // DSS
                result[2] = 0; // SDS
                result[3] = 0; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaDSSDDD = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = bitCounts.get(1); // DSS
                result[2] = 0; // SDS
                result[3] = 0; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = bitCounts.get(7); // DDD
            }
        };
    }
}
