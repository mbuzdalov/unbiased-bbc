package ru.ifmo.unbiased.test;

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
                int sum = 0;
                for (int i = 0; i < count; ++i) {
                    sum += GenericOneMax.runGeneric(processor);
                }

                double avg = (double) (sum) / count;

                System.out.println("OneMax::generic(n = " + n + ", arity = " + arity + "): average = " + avg);
            }
        }
    }
}
