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

    private static final UnbiasedOperator FLIP_FOUR_DIFFERENT = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(0);
            flipDifferent(4);
        }
    };

    private static final UnbiasedOperator FLIP_THREE_SAME = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(3);
            flipDifferent(0);
        }
    };

    private static final UnbiasedOperator FLIP_SEVEN_SAME = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(7);
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

    public static int runQuaternary(UnbiasedProcessor processor) {
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
                                    first = processor.query(Operators.ternary1SS, first, second, flipOne);
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
                                Individual p = processor.query(Operators.ternary2SD, first, second, m);
                                if (p.fitness() == first.fitness() + 2) {
                                    first = p;
                                    second = processor.query(XOR3, first, second, m);
                                } else {
                                    Individual r = processor.query(Operators.ternaryDS1DD, first, m, p);
                                    if (r.fitness() == first.fitness() + 2) {
                                        first = r;
                                        second = processor.query(XOR3, first, second, m);
                                    } else {
                                        first = processor.query(XOR3, first, p, r);
                                        second = processor.query(XOR3, first, second, m);
                                    }
                                }
                            } else {
                                Individual p = processor.query(Operators.ternary1SD, first, second, m);
                                if (p.fitness() == first.fitness() + 1) {
                                    first = p;
                                    second = processor.query(XOR3, first, second, m);
                                } else {
                                    Individual r = processor.query(Operators.ternary1DS, first, m, p);
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
                else {
                    Individual a = processor.query(FLIP_SEVEN_SAME, first, second);
                    if (a.fitness() == first.fitness() + 7) {      // 0000000->1111111
                        first = a;
                    } else if (a.fitness() == first.fitness() - 7) {  // 1111111->0000000
                        second = processor.query(Operators.ternarySD, second, first, a);
                    } else {
                        if (a.fitness() - first.fitness() < 0) {
                            Individual c = a;
                            a = first;
                            first = c;
                            second = processor.query(XOR3, second, first, a);
                        }
                        Individual b = processor.query(FLIP_FOUR_DIFFERENT, first, a);
                        if (a.fitness() == first.fitness() + 5) {
                            if (b.fitness() == first.fitness() + 2) {
                                Individual c = processor.query(Operators.ternary2SD, a, b, first);
                                if (c.fitness() == a.fitness()) {
                                    Individual d = processor.query(FLIP_ONE_DIFFERENT, c, a);
                                    if (d.fitness() == a.fitness() + 1) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        first = processor.query(Operators.ternarySD, a, d, c);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                } else {
                                    Individual d = processor.query(Operators.qua1SDS, a, b, first, c);
                                    if (d.fitness() == a.fitness() + 1) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        first = processor.query(Operators.quaSDSDSD, d, c, first, b);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                }
                            } else if (b.fitness() == first.fitness() + 4) {
                                Individual c = processor.query(Operators.ternary2SD, b, first, a);
                                if (c.fitness() == b.fitness() + 2) {
                                    first = c;
                                    second = processor.query(XOR3, second, first, a);
                                } else {
                                    Individual d = processor.query(Operators.ternary1DS, a, b, c);
                                    if (d.fitness() == a.fitness() + 1) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        first = processor.query(Operators.quaDSS, a, b, c ,d);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                }
                            }
                        } else if (a.fitness() == first.fitness() + 3) {
                            if (b.fitness() == first.fitness()) {
                                Individual c = processor.query(Operators.ternary2SD, a, b, first);
                                if (c.fitness() == a.fitness() + 2) {
                                    first = c;
                                    second = processor.query(XOR3, second, first, a);
                                } else if (c.fitness() == a.fitness()){
                                    Individual d = processor.query(Operators.qua1DSS1DDS, a, first, c, b);
                                    if (d.fitness() == a.fitness() + 2) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else if (d.fitness() == a.fitness()){
                                        Individual e = processor.query(XOR3, d, c, a);
                                        if (e.fitness() == a.fitness() +2) {
                                            first = e;
                                            second = processor.query(XOR3, second, first, a);
                                        } else {
                                            first = processor.query(Operators.quaDSS, a, first, e, b);
                                            second = processor.query(XOR3, second, first, a);
                                        }
                                    } else if (d.fitness() == a.fitness() - 2) {
                                        first = processor.query(Operators.quaDSS, a, first, b, d);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                } else if (c.fitness() == a.fitness() - 2) {
                                    first = processor.query(Operators.quaDSS, a, first, b, c);
                                    second = processor.query(XOR3, second, first, a);
                                }
                            } else if (b.fitness() == first.fitness() + 2) {
                                Individual c = processor.query(Operators.ternary2DS2SD, b, first, a);
                                if (c.fitness() == b.fitness()) {
                                    Individual d = processor.query(Operators.ternary1DS1SD, a, b, c);
                                    if (d.fitness() == a.fitness() + 2) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else if(d.fitness() == a.fitness()) {
                                        Individual f = processor.query(Operators.quaSDDDSS, a, b, c, d);
                                        if (f.fitness() == a.fitness() + 2) {
                                            first = f;
                                            second = processor.query(XOR3, second, first, a);
                                        } else {
                                            first = processor.query(Operators.quaSDSDSD, a, b, c, d);
                                            second = processor.query(XOR3, second, first, a);
                                        }
                                    } else if (d.fitness() == a.fitness() - 2) {
                                        Individual f = processor.query(Operators.quaSDSDSS, a, b, c, d);
                                        if (f.fitness() == a.fitness() + 2) {
                                            first = f;
                                            second = processor.query(XOR3, second, first, a);
                                        } else {
                                            Individual x = processor.query(Operators.ternary1DSSD, b, first, c);
                                            if (x.fitness() == a.fitness() + 2) {
                                                first = x;
                                                second = processor.query(XOR3, second, first, a);
                                            } else {
                                                first = processor.query(Operators.quaSDDDSS, b, first, c, x);
                                                second = processor.query(XOR3, second, first, a);
                                            }
                                        }
                                    }
                                } else if (c.fitness() == b.fitness() - 2) {
                                    Individual e = processor.query(Operators.qua1DSS1DDS, a, first, b, c);
                                    if (e.fitness() == a.fitness() + 2) {
                                        first = e;
                                        second = processor.query(XOR3, second, first, a);
                                    } else if (e.fitness() == a.fitness() - 2) {
                                        first = processor.query(Operators.quaDSS, a, first, c, e);
                                        second = processor.query(XOR3, second, first, a);
                                    } else if (e.fitness() == a.fitness()) {
                                        Individual f = processor.query(Operators.quaDSSSSD, a, b, c, e);
                                        if (f.fitness() == a.fitness() + 2) {
                                            first = f;
                                            second = processor.query(XOR3, second, first, a);
                                        } else {
                                            first = processor.query(Operators.quaDSS, a, first, c, f);
                                            second = processor.query(XOR3, second, first, a);
                                        }
                                    }
                                } else if (c.fitness() == b.fitness() + 2) {
                                    Individual d = processor.query(Operators.ternary1SD, c, first, b);
                                    if (d.fitness() == a.fitness() + 2) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        first = processor.query(Operators.quaSDS, c, first, b, d);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                }
                            } else if (b.fitness() == first.fitness() + 4) {
                                Individual c = processor.query(Operators.ternary2DD, a, first, b);
                                if (c.fitness() == a.fitness() + 2) {
                                    first = c;
                                    second = processor.query(XOR3, second, first, a);
                                } else {
                                    Individual d = processor.query(Operators.ternary1DS, b, a, c);
                                    if (d.fitness() == a.fitness() + 2) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        first = processor.query(Operators.quaDSS, b, a, c, d);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                }
                            }
                        } else if (a.fitness() == first.fitness() + 1) {
                            if (b.fitness() == first.fitness() + 4) {
                                first = b;
                                second = processor.query(XOR3, second, first, a);
                            } else if (b.fitness() == first.fitness() + 2) {
                                Individual c = processor.query(Operators.ternary2DS2DD, a, first, b);
                                if (c.fitness() == a.fitness()) {
                                    Individual d = processor.query(Operators.qua1DSSDDD, a, first, b, c);
                                    if (d.fitness() == a.fitness() + 3) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else if (d.fitness() == a.fitness() + 1) {
                                        first = processor.query(Operators.quaDSSSDD, d, first, c, b);
                                        second = processor.query(XOR3, second, first, a);
                                    } else if (d.fitness() == a.fitness() - 1) {
                                        Individual e = processor.query(Operators.ternary1DS1SD, b, a, c);
                                        if (e.fitness() == b.fitness() + 2) {
                                            first = e;
                                            second = processor.query(XOR3, second, first, a);
                                        } else if (e.fitness() == b.fitness()) {
                                            Individual f = processor.query(Operators.quaSDSDSD, b, e, c, a);
                                            if (f.fitness() == b.fitness() + 2) {
                                                first = f;
                                                second = processor.query(XOR3, second, first, a);
                                            } else {
                                                first = processor.query(Operators.quaSDSDSD, b, e, a, c);
                                                second = processor.query(XOR3, second, first, a);
                                            }
                                        } else if (e.fitness() == b.fitness() - 2) {
                                            first = processor.query(Operators.quaDSSSSD, b, a, e, c);
                                            second = processor.query(XOR3, second, first, a);
                                        }
                                    }
                                } else if (c.fitness() == a.fitness() - 2) {
                                    Individual d = processor.query(Operators.qua1DSS1SSD, b, a, c, first);
                                    if (d.fitness() == b.fitness() + 2) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else if (d.fitness() == b.fitness()) {
                                        Individual e = processor.query(Operators.quaDSSSSD, b, first, c, d);
                                        if (e.fitness() == b.fitness() + 2) {
                                            first = e;
                                            second = processor.query(XOR3, second, first, a);
                                        } else {
                                            first = processor.query(Operators.quaDSSSSD, b, d, c, a);
                                            second = processor.query(XOR3, second, first, a);
                                        }
                                    } else if (d.fitness() == b.fitness() - 2) {
                                        Individual e = processor.query(Operators.quaDSS, b, first, c, d);
                                        first = processor.query(Operators.quaDSS, e, a, c, d);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                } else if (c.fitness() == a.fitness() + 2) {
                                    Individual d = processor.query(Operators.ternary1DD, c, a, b);
                                    if (d.fitness() == a.fitness() + 3) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        first = processor.query(Operators.quaDDS, c, a, b, d);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                }
                            } else if (b.fitness() == first.fitness()) {
                                Individual c = processor.query(Operators.ternary2DS2SD, b, first, a);
                                if (c.fitness() == b.fitness() +4) {
                                    first = c;
                                    second = processor.query(XOR3, second, first, a);
                                } else if (c.fitness() == b.fitness() + 2) {
                                    Individual d = processor.query(Operators.quaDSDDDS, a, first, b, c);
                                    if (d.fitness() == a.fitness() + 2) {
                                        Individual e = processor.query(Operators.ternary1DS, d, a, b);
                                        if (e.fitness() == a.fitness() + 3) {
                                            first = e;
                                            second = processor.query(XOR3, second, first, a);
                                        } else {
                                            first = processor.query(Operators.quaDSS, d, a, b, e);
                                            second = processor.query(XOR3, second, first, a);
                                        }
                                    } else if (d.fitness() == a.fitness() - 2) {
                                        Individual e = processor.query(Operators.ternary1DS1SD, c, first, b);
                                        if (e.fitness() == a.fitness() + 3) {
                                            first = e;
                                            second = processor.query(XOR3, second, first, a);
                                        } else if(e.fitness() == c.fitness()) {
                                            Individual f = processor.query(Operators.quaSDSDSD, e, a, d, b);
                                            if (f.fitness() == a.fitness() + 3) {
                                                first = f;
                                                second = processor.query(XOR3, second, first, a);
                                            } else if(f.fitness() == e.fitness() - 2) {
                                                first = processor.query(Operators.quaDSSSDD, e, first, d, b);
                                                second = processor.query(XOR3, second, first, a);
                                            }
                                        } else if(e.fitness() == c.fitness() - 2) {
                                            first = processor.query(Operators.quaSDSDSS, c, first, b, e);
                                            second = processor.query(XOR3, second, first, a);
                                        }
                                    }
                                } else if (c.fitness() == b.fitness()) {
                                    Individual d = processor.query(Operators.quaDSSDDD, a, first, b, c);
                                    if (d.fitness() == a.fitness() + 3) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        Individual e = processor.query(Operators.qua1DSS1SSD1SDS, a, b, c, d);
                                        if (e.fitness() == a.fitness() + 3) {
                                            first = e;
                                            second = processor.query(XOR3, second, first, a);
                                        } else if(e.fitness() == a.fitness() + 1) {
                                            Individual f = processor.query(Operators.quaSSDDDS, e, a, b, d);
                                            if (f.fitness() == a.fitness() + 3) {
                                                first = f;
                                                second = processor.query(XOR3, second, first, a);
                                            } else {
                                                f = processor.query(Operators.quaDDSSSD, e, a, b, c);
                                                if (f.fitness() == a.fitness() + 3) {
                                                    first = f;
                                                    second = processor.query(XOR3, second, first, a);
                                                } else {
                                                    first = processor.query(Operators.quaSDSDSD, e, a, b, d);
                                                    second = processor.query(XOR3, second, first, a);
                                                }
                                            }
                                        } else if (e.fitness() == a.fitness() - 1) {
                                            Individual f = processor.query(Operators.quaDSDSDSSSDDDS, e, a, b, d);
                                            if (f.fitness() == a.fitness() + 3) {
                                                first = f;
                                                second = processor.query(XOR3, second, first, a);
                                            } else {
                                                f = processor.query(Operators.quaDSDSDSSSDDDS, e, a, b, c);
                                                if (f.fitness() == a.fitness() + 3) {
                                                    first = f;
                                                    second = processor.query(XOR3, second, first, a);
                                                } else {
                                                    first = processor.query(Operators.quaDSDSDSSSDDDS, e, a, c, d);
                                                    second = processor.query(XOR3, second, first, a);
                                                }
                                            }
                                        } else if(e.fitness() == a.fitness() - 3) {
                                            first = processor.query(Operators.quaDSSSDSSSDDDS, b, d, c, e);
                                            second = processor.query(XOR3, second, first, a);
                                        }
                                    }
                                } else if (c.fitness() == b.fitness() - 2) {
                                    Individual d = processor.query(Operators.qua1DDSSDS, a, b, first, c);
                                    if (d.fitness() == a.fitness() + 3) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        first = processor.query(Operators.quaSDDDSS, d, c, b ,first);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                }
                            } else if (b.fitness() == first.fitness() - 2) {
                                Individual c = processor.query(Operators.ternary2SD, a, b, first);
                                if (c.fitness() == a.fitness() + 2) {
                                    Individual d = processor.query(Operators.ternary1SD, c, b, first);
                                    if (d.fitness() == a.fitness() + 3) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        first = processor.query(Operators.quaSDS, c, b, first, d);
                                        second = processor.query(XOR3, second, first, a);
                                    }
                                } else {
                                    Individual d = processor.query(Operators.ternary1DSSD, c, b, first);
                                    if (d.fitness() == a.fitness() + 3) {
                                        first = d;
                                        second = processor.query(XOR3, second, first, a);
                                    } else {
                                        first = processor.query(Operators.quaDSSSDD, c, b, first, d);
                                        second = processor.query(XOR3, second, first, a);
                                    }

                                }
                            }
                        }
                    }
                    sameCount -=7;
                }
            }
        } catch (UnbiasedProcessor.OptimumFound found) {
            return found.numberOfQueries();
        }
    }

    private static class Operators {
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

        static final UnbiasedOperator ternary1SS = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 1;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 0;
                result[2] = 0;
                result[3] = 0;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternarySD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 0;
                result[2] = bitCounts.get(2);
                result[3] = 0;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternary2SD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 0;
                result[2] = 2;
                result[3] = 0;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternaryDS1DD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = bitCounts.get(1);
                result[2] = 0;
                result[3] = 1;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternary1DS1SD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 1;
                result[2] = 1;
                result[3] = 0;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternary1SD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 0;
                result[2] = 1;
                result[3] = 0;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternary1DS = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 1;
                result[2] = 0;
                result[3] = 0;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternary1DSSD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 1;
                result[2] = bitCounts.get(2);
                result[3] = 0;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternary2DS2SD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 2;
                result[2] = 2;
                result[3] = 0;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternary2DS2DD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 2;
                result[2] = 0;
                result[3] = 2;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternary2DD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 0;
                result[2] = 0;
                result[3] = 2;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator ternary1DD = new UnbiasedOperator(3) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
                result[1] = 0;
                result[2] = 0;
                result[3] = 1;                // flip nothing    where (first != second), (first != third)  => 11
            }
        };

        static final UnbiasedOperator qua1SDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 0; // DSS
                result[2] = 1; // SDS
                result[3] = 0; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaDSDDDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 0; // DSS
                result[2] = 0; // SDS
                result[3] = bitCounts.get(3); // DDS
                result[4] = 0; // SSD
                result[5] = bitCounts.get(5); // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaDSSSDD = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = bitCounts.get(1); // DSS
                result[2] = 0; // SDS
                result[3] = 0; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = bitCounts.get(6); // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator qua1DDSSDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 0; // DSS
                result[2] = bitCounts.get(2); // SDS
                result[3] = 1; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaSDSDSS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = bitCounts.get(1); // DSS
                result[2] = bitCounts.get(2); // SDS
                result[3] = 0; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaSDDDSS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = bitCounts.get(1); // DSS
                result[2] = 0; // SDS
                result[3] = 0; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = bitCounts.get(6); // SDD
            }
        };

        static final UnbiasedOperator quaSDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 0; // DSS
                result[2] = bitCounts.get(2); // SDS
                result[3] = 0; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator qua1DSS1SSD = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 1; // DSS
                result[2] = 0; // SDS
                result[3] = 0; // DDS
                result[4] = 1; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaSDSDSD = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 0; // DSS
                result[2] = bitCounts.get(2); // SDS
                result[3] = 0; // DDS
                result[4] = 0; // SSD
                result[5] = bitCounts.get(5); // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator qua1DSS1DDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 1; // DSS
                result[2] = 0; // SDS
                result[3] = 1; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaDSSSSD = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = bitCounts.get(1); // DSS
                result[2] = 0; // SDS
                result[3] = 0; // DDS
                result[4] = bitCounts.get(4); // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaSSDDDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 0; // DSS
                result[2] = 0; // SDS
                result[3] = bitCounts.get(3); // DDS
                result[4] = bitCounts.get(4); // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator qua1DSSDDD = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 1; // DSS
                result[2] = 0; // SDS
                result[3] = 0; // DDS
                result[4] = 0; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = bitCounts.get(7); // DDD
            }
        };

        static final UnbiasedOperator quaDDSSSD = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 0; // DSS
                result[2] = 0; // SDS
                result[3] = bitCounts.get(3); // DDS
                result[4] = bitCounts.get(4); // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator qua1DSS1SSD1SDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 1; // DSS
                result[2] = 1; // SDS
                result[3] = 0; // DDS
                result[4] = 1; // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaDSDSDSSSDDDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = 0; // DSS
                result[2] = bitCounts.get(2); // SDS
                result[3] = bitCounts.get(3); // DDS
                result[4] = bitCounts.get(4); // SSD
                result[5] = bitCounts.get(5); // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };

        static final UnbiasedOperator quaDSSSDSSSDDDS = new UnbiasedOperator(4) {
            @Override
            protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
                result[0] = 0; // SSS
                result[1] = bitCounts.get(1); // DSS
                result[2] = bitCounts.get(2); // SDS
                result[3] = bitCounts.get(3); // DDS
                result[4] = bitCounts.get(4); // SSD
                result[5] = 0; // DSD
                result[6] = 0; // SDD
                result[7] = 0; // DDD
            }
        };
    }
}
