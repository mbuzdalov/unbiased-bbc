package ru.ifmo.unbiased.ops;

import ru.ifmo.unbiased.util.ImmutableIntArray;

public abstract class Operator1 extends UnbiasedOperator {
    protected Operator1() {
        super(1);
    }

    protected abstract int applyUnary(int nBits);

    @Override
    protected final void applyImpl(ImmutableIntArray bitCounts, int[] result) {
        result[0] = applyUnary(bitCounts.get(0));
    }
}
