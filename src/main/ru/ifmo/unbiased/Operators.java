package ru.ifmo.unbiased;

import ru.ifmo.unbiased.ops.Operator1;
import ru.ifmo.unbiased.ops.Operator2;
import ru.ifmo.unbiased.ops.UnbiasedOperator;
import ru.ifmo.unbiased.util.ImmutableIntArray;

public final class Operators {
    private Operators() {}

    public static final UnbiasedOperator FLIP_ALL = new Operator1() {
        @Override
        protected int applyUnary(int nBits) {
            return nBits;
        }
    };

    public static final UnbiasedOperator FLIP_ONE = new Operator1() {
        @Override
        protected int applyUnary(int nBits) {
            return 1;
        }
    };

    public static final UnbiasedOperator FLIP_ONE_DIFFERENT = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(0);
            flipDifferent(1);
        }
    };

    public static final UnbiasedOperator XOR3 = new UnbiasedOperator(3) {
        @Override
        protected void applyImpl(ImmutableIntArray bitCounts, int[] result) {
            result[0] = 0;                // flip nothing    where (first == second), (first == third)  => 00
            result[1] = bitCounts.get(1); // flip everything where (first != second), (first == third)  => 10
            result[2] = bitCounts.get(2); // flip everything where (first == second), (first != third)  => 01
            result[3] = 0;                // flip nothing    where (first != second), (first != third)  => 11
        }
    };
}
