package ru.ifmo.unbiased.util;

import java.util.concurrent.ThreadLocalRandom;

public final class ImmutableBitArray {
    private final long[] data;
    private final int length;

    private ImmutableBitArray(long[] data, int length) {
        this.data = data;
        this.length = length;
        int lengthTop = data.length << 6;
        for (int i = lengthTop - 1; i >= length; --i) {
            if (((data[i >>> 6] >>> i) & 1) == 1) {
                throw new AssertionError("bit at " + i + " is set for length " + length);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            sb.append(getBit(i) ? '1' : '0');
        }
        return sb.toString();
    }

    public boolean getBit(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("index " + index + ", length " + length);
        }
        return ((data[index >>> 6] >>> index) & 1) == 1;
    }

    public int cardinality() {
        int result = 0;
        int bitSize = (length + 63) >>> 6;
        for (int i = 0; i < bitSize; ++i) {
            result += Long.bitCount(data[i]);
        }
        return result;
    }

    public static ImmutableBitArray random(int length) {
        long[] data = new long[(length + 63) >>> 6];
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0, iMax = data.length; i < iMax; ++i) {
            data[i] = rng.nextLong();
        }
        if ((data.length << 6) != length) {
            data[data.length - 1] &= (1L << length) - 1;
        }
        return new ImmutableBitArray(data, length);
    }

    public ImmutableBitArray flip(int[] indices, int howMuch) {
        long[] newData = data.clone();
        for (int i = 0; i < howMuch; ++i) {
            int ii = indices[i];
            newData[ii >>> 6] ^= 1L << ii;
        }
        return new ImmutableBitArray(newData, length);
    }
}
