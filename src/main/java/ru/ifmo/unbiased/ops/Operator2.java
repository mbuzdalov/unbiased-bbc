package ru.ifmo.unbiased.ops;

import ru.ifmo.unbiased.util.ImmutableIntArray;

public abstract class Operator2 extends UnbiasedOperator {
    private int flipSame, flipDifferent;

    protected Operator2() {
        super(2);
    }

    protected final void flipSame(int howMuch) {
        this.flipSame = howMuch;
    }

    protected final void flipDifferent(int howMuch) {
        this.flipDifferent = howMuch;
    }

    protected abstract void applyBinary(int sameBits, int differentBits);

    @Override
    protected final void applyImpl(ImmutableIntArray bitCounts, int[] result) {
        flipSame = 0;
        flipDifferent = 0;
        applyBinary(bitCounts.get(0), bitCounts.get(1));
        result[0] = flipSame;
        result[1] = flipDifferent;
    }
}
