package ru.ifmo.unbiased.test;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.algo.OneMaxComplicated;
import ru.ifmo.unbiased.util.ImmutableBitArray;

public class OneMaxComplicatedTest {
    @Test
    public void testTernary() {
        int n = 239;
        int count = 1000;
        UnbiasedProcessor processor = new UnbiasedProcessor(n, 3, ImmutableBitArray::cardinality, n);
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += OneMaxComplicated.runTernary(processor);
        }

        double avg = (double) (sum) / count;
        double expected = 9.0 * n / 8.0;
        System.out.println("OneMax::ternary: average = " + avg + " = (9/8 n) * " + (avg / expected));

        if (sum > expected * count * 1.05 || sum < expected * count * 0.95) {
            Assert.fail("n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }

    @Test
    public void testQuaternary1() {
        int n = 239;
        int count = 1000;
        UnbiasedProcessor processor = new UnbiasedProcessor(n, 4, ImmutableBitArray::cardinality, n);
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += OneMaxComplicated.runQuaternary1(processor);
        }

        double avg = (double) (sum) / count;
        double expected = 9.0 * n / 8.0 * 19 / 20;
        System.out.println("OneMax::quaternary1: average = " + avg + " = ((9*19) / (8*20) n) * " + (avg / expected));

        if (sum > expected * count * 1.05 || sum < expected * count * 0.95) {
            Assert.fail("n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }
}
