package ru.ifmo.unbiased.test;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.algo.OneMaxSimple;
import ru.ifmo.unbiased.util.ImmutableBitArray;

public class OneMaxSimpleTest {
    @Test
    public void smokeSimpleUnary() {
        for (int n = 1; n <= 100; ++n) {
            for (int t = 0; t < 10; ++t) {
                OneMaxSimple.runUnary(new UnbiasedProcessor(n, 1, ImmutableBitArray::cardinality, n));
            }
        }
    }

    @Test
    public void runtimeSimpleUnary() {
        int n = 239;
        int count = 300;

        UnbiasedProcessor processor = new UnbiasedProcessor(n, 1, ImmutableBitArray::cardinality, n);

        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += OneMaxSimple.runUnary(processor);
        }
        double expected = 0;
        for (int i = n / 2; i < n; ++i) {
            expected += (double) (n) / (n - i);
        }

        double avg = (double) (sum) / count;
        System.out.println("OneMax::simpleUnary: average = " + avg + " = (n * (H(n) - H(n/2)) * " + (avg / expected));

        if (sum > expected * count * 1.05 || sum < expected * count * 0.95) {
            Assert.fail("n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }

    @Test
    public void smokeSimpleBinary() {
        for (int n = 1; n <= 100; ++n) {
            for (int t = 0; t < 10; ++t) {
                OneMaxSimple.runBinary(new UnbiasedProcessor(n, 2, ImmutableBitArray::cardinality, n));
            }
        }
    }

    @Test
    public void runtimeSimpleBinary() {
        int n = 239;
        int count = 1000;

        UnbiasedProcessor processor = new UnbiasedProcessor(n, 2, ImmutableBitArray::cardinality, n);

        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += OneMaxSimple.runBinary(processor);
        }

        double avg = (double) (sum) / count;
        double expected = 2 * n;
        System.out.println("OneMax::simpleBinary: average = " + avg + " = (n * 2) * " + (avg / expected));

        if (sum > expected * count * 1.05 || sum < expected * count * 0.95) {
            Assert.fail("n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }

    @Test
    public void smokeSimpleBinaryDeterministic() {
        for (int n = 1; n <= 100; ++n) {
            for (int t = 0; t < 10; ++t) {
                OneMaxSimple.runBinaryDeterministic(new UnbiasedProcessor(n, 2, ImmutableBitArray::cardinality, n));
            }
        }
    }

    @Test
    public void runtimeSimpleBinaryDeterministic() {
        int n = 239;
        int count = 1000;

        UnbiasedProcessor processor = new UnbiasedProcessor(n, 2, ImmutableBitArray::cardinality, n);

        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += OneMaxSimple.runBinaryDeterministic(processor);
        }

        double avg = (double) (sum) / count;
        double expected = 2 * n;
        System.out.println("OneMax::simpleBinaryDeterministic: average = " + avg + " = (n * 2) * " + (avg / expected));

        if (sum > expected * count * 1.05 || sum < expected * count * 0.95) {
            Assert.fail("n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }
}
