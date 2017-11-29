package ru.ifmo.unbiased.util;

public final class ImmutableIntArray {
    private final int[] data;

    public ImmutableIntArray(int[] data) {
        this.data = data;
    }

    public int length() {
        return data.length;
    }

    public int get(int index) {
        return data[index];
    }
}
