package ru.ifmo.unbiased.ops;

import ru.ifmo.unbiased.UnbiasedProtocolException;
import ru.ifmo.unbiased.util.ImmutableIntArray;

public abstract class UnbiasedOperator {
    private final int arity;

    protected UnbiasedOperator(int arity) {
        this.arity = arity;
    }

    public int getArity() {
        return arity;
    }

    public void apply(ImmutableIntArray bitCounts, int[] result) {
        if (arity == 0) {
            return;
        }
        int expectedLength = 1 << (arity - 1);
        if (result.length != expectedLength || bitCounts.length() != expectedLength) {
            throw new IllegalArgumentException("Lengths of arguments do not match with the arity: arity " + arity
                    + ", expected argument length " + expectedLength
                    + ", bitCounts.length = " + bitCounts.length() + ", result.length = " + result.length);
        }
        for (int i = 0; i < result.length; ++i) {
            int v = bitCounts.get(i);
            if (v < 0) {
                throw new IllegalArgumentException("bitCounts[" + i + "] is negative");
            }
        }
        applyImpl(bitCounts, result);
        for (int i = 0; i < result.length; ++i) {
            if (result[i] < 0 || result[i] > bitCounts.get(i)) {
                throw new UnbiasedProtocolException("Post-condition failed: for i = " + i
                        + " result[i] = " + result[i] + " with bitCounts[i] = " + bitCounts.get(i));
            }
        }
    }

    protected abstract void applyImpl(ImmutableIntArray bitCounts, int[] result);
}
