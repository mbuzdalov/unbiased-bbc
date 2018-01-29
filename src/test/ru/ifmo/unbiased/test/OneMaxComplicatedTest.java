package ru.ifmo.unbiased.test;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.algo.OneMaxComplicated;
import ru.ifmo.unbiased.util.ImmutableBitArray;

public class OneMaxComplicatedTest {
    @Test
    public void smokeTernary() {
        for (int n = 1; n <= 100; ++n) {
            for (int t = 0; t < 10; ++t) {
                OneMaxComplicated.runTernary(new UnbiasedProcessor(n, 3, ImmutableBitArray::cardinality, n));
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
    public void smokeQuaternary1() {
        for (int n = 1; n <= 100; ++n) {
            for (int t = 0; t < 10; ++t) {
                OneMaxComplicated.runQuaternary1(new UnbiasedProcessor(n, 4, ImmutableBitArray::cardinality, n));
            }
        }
    }

    @Test
    public void runtimeQuaternary1() {
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

    @Test
    public void smokeGeneric() {
        for (int arity = 4; arity <= 6; ++arity) {
            // The tricky bound on n is because arity 6 is currently quite slow
            for (int n = 1; n <= (arity == 6 ? 20 : 100); ++n) {
                int count = 100;
                UnbiasedProcessor processor = new UnbiasedProcessor(n, arity, ImmutableBitArray::cardinality, n);
                int sum = 0;
                for (int i = 0; i < count; ++i) {
                    sum += OneMaxComplicated.runGeneric(processor);
                }

                double avg = (double) (sum) / count;

                System.out.println("OneMax::generic(n = " + n + ", arity = " + arity + "): average = " + avg);
            }
        }
    }
}
