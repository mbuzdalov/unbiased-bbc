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
            for (int n = 1; n <= (arity == 6 ? 20 : 100); ++n) {
                int count = 10;
                UnbiasedProcessor processor = new UnbiasedProcessor(n, arity, ImmutableBitArray::cardinality, n);
                for (int i = 0; i < count; ++i) {
                    GenericOneMax.runGeneric(processor);
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
            sum += GenericOneMax.runGeneric(processor);
        }

        double avg = (double) (sum) / count;
        double expected = 2.0 * n;
        System.out.println("GenericOneMax, arity 3: average = " + avg + " = (2 n) * " + (avg / expected));

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
            sum += GenericOneMax.runGeneric(processor);
        }

        double avg = (double) (sum) / count;
        double expected = 37.0 * n / 34.0;
        System.out.println("GenericOneMax, arity 4: average = " + avg + " = (37/34 n) * " + (avg / expected));

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
            sum += GenericOneMax.runGeneric(processor);
        }

        double avg = (double) (sum) / count;
        double expected = 23.0 * n / 32.0;
        System.out.println("GenericOneMax, arity 5: average = " + avg + " = (23/32 n) * " + (avg / expected));

        if (sum > expected * count * 1.01) {
            Assert.fail("GREATER: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
        if (sum < expected * count * 0.99) {
            Assert.fail("LESS: n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }
}
