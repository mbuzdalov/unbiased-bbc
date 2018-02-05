package ru.ifmo.unbiased.test;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.algo.OneMaxHandCrafted;
import ru.ifmo.unbiased.util.ImmutableBitArray;

public class OneMaxHandCraftedTest {
    @Test
    public void smokeTernary() {
        for (int n = 1; n <= 100; ++n) {
            for (int t = 0; t < 10; ++t) {
                OneMaxHandCrafted.runTernary(new UnbiasedProcessor(n, 3, ImmutableBitArray::cardinality, n));
            }
        }
    }

    @Test
    public void runtimeTernary() {
        int n = 239;
        int count = 1000;
        UnbiasedProcessor processor = new UnbiasedProcessor(n, 3, ImmutableBitArray::cardinality, n);
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += OneMaxHandCrafted.runTernary(processor);
        }

        double avg = (double) (sum) / count;
        double expected = 9.0 * n / 8.0;
        System.out.println("OneMax::ternary: average = " + avg + " = (9/8 n) * " + (avg / expected));

        if (sum > expected * count * 1.05 || sum < expected * count * 0.95) {
            Assert.fail("n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }

    @Test
    public void smokeQuaternary() {
        for (int n = 1; n <= 100; ++n) {
            for (int t = 0; t < 10; ++t) {
                OneMaxHandCrafted.runQuaternary(new UnbiasedProcessor(n, 4, ImmutableBitArray::cardinality, n));
            }
        }
    }

    @Test
    public void runtimeQuaternary() {
        int n = 239;
        int count = 1000;
        UnbiasedProcessor processor = new UnbiasedProcessor(n, 4, ImmutableBitArray::cardinality, n);
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += OneMaxHandCrafted.runQuaternary(processor);
        }

        double avg = (double) (sum) / count;
        double expected = 0.922 * n;
        System.out.println("OneMax::quaternary: average = " + avg + " = (0.922 n) * " + (avg / expected));

        if (sum > expected * count * 1.05 || sum < expected * count * 0.95) {
            Assert.fail("n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }
}
