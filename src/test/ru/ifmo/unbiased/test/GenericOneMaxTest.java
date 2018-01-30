package ru.ifmo.unbiased.test;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.algo.GenericOneMax;
import ru.ifmo.unbiased.util.ImmutableBitArray;

public class GenericOneMaxTest {
    @Test
    public void smokeGeneric() {
        for (int arity = 3; arity <= 6; ++arity) {
            // The tricky bound on n is because arity 6 is currently quite slow
            for (int n = 1; n <= (arity == 6 ? 50 : 100); ++n) {
                int count = arity == 6 ? 1 : 10;
                UnbiasedProcessor processor = new UnbiasedProcessor(n, arity, ImmutableBitArray::cardinality, n);
                for (int i = 0; i < count; ++i) {
                    GenericOneMax.runGeneric(processor, false);
                }
            }
        }
    }

    @Test
    public void smokeGenericPure() {
        for (int arity = 3; arity <= 6; ++arity) {
            // The tricky bound on n is because arity 6 is currently quite slow
            for (int n = 1; n <= (arity == 6 ? 50 : 100); ++n) {
                int count = arity == 6 ? 1 : 10;
                UnbiasedProcessor processor = new UnbiasedProcessor(n, arity, ImmutableBitArray::cardinality, n);
                for (int i = 0; i < count; ++i) {
                    GenericOneMax.runGeneric(processor, true);
                }
            }
        }
    }

    @Test
    public void runtimeGeneric3() {
        int n = 239;
        int count = 100;
        UnbiasedProcessor processor = new UnbiasedProcessor(n, 3, ImmutableBitArray::cardinality, n);
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += GenericOneMax.runGeneric(processor, false);
        }

        double avg = (double) (sum) / count;
        double expected = 53.0 * n / 41.0;
        System.out.println("GenericOneMax, arity 3: average = " + avg + " = (53/41 n) * " + (avg / expected));

        if (sum > expected * count * 1.01) {
            Assert.fail("GREATER: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
        if (sum < expected * count * 0.99) {
            Assert.fail("LESS: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }

    @Test
    public void runtimeGeneric4() {
        int n = 239;
        int count = 100;
        UnbiasedProcessor processor = new UnbiasedProcessor(n, 4, ImmutableBitArray::cardinality, n);
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += GenericOneMax.runGeneric(processor, false);
        }

        double avg = (double) (sum) / count;
        double expected = 29.0 * n / 31.0;
        System.out.println("GenericOneMax, arity 4: average = " + avg + " = (29/31 n) * " + (avg / expected));

        if (sum > expected * count * 1.01) {
            Assert.fail("GREATER: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
        if (sum < expected * count * 0.99) {
            Assert.fail("LESS: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }

    @Test
    public void runtimeGeneric5() {
        int n = 239;
        int count = 100;
        UnbiasedProcessor processor = new UnbiasedProcessor(n, 5, ImmutableBitArray::cardinality, n);
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += GenericOneMax.runGeneric(processor, false);
        }

        double avg = (double) (sum) / count;
        double expected = 13.0 * n / 20.0;
        System.out.println("GenericOneMax, arity 5: average = " + avg + " = (13/20 n) * " + (avg / expected));

        if (sum > expected * count * 1.01) {
            Assert.fail("GREATER: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
        if (sum < expected * count * 0.99) {
            Assert.fail("LESS: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }


    @Test
    public void runtimeGeneric6() {
        int n = 239;
        int count = 100;
        UnbiasedProcessor processor = new UnbiasedProcessor(n, 6, ImmutableBitArray::cardinality, n);
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            int value = GenericOneMax.runGeneric(processor, false);
            sum += value;
            System.out.println("    [arity 6, " + (i + 1) + "/" + count + "]: " + value);
        }

        double avg = (double) (sum) / count;
        double expected = 37.0 * n / 77.0;
        System.out.println("GenericOneMax, arity 6: average = " + avg + " = (37/77 n) * " + (avg / expected));

        if (sum > expected * count * 1.01) {
            Assert.fail("GREATER: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
        if (sum < expected * count * 0.99) {
            Assert.fail("LESS: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }
}
