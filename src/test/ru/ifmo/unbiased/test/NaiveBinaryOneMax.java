package ru.ifmo.unbiased.test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;

import ru.ifmo.unbiased.Individual;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.ops.Operator1;
import ru.ifmo.unbiased.ops.Operator2;
import ru.ifmo.unbiased.util.ImmutableBitArray;

public class NaiveBinaryOneMax {
    private final Operator1 inversion = new Operator1() {
        @Override
        protected int applyUnary(int nBits) {
            return nBits;
        }
    };

    private final Operator2 flipDiff = new Operator2() {
        @Override
        protected void applyBinary(int sameBits, int differentBits) {
            flipSame(0);
            flipDifferent(1);
        }
    };

    private int runOneMax(UnbiasedProcessor processor) {
        processor.reset();
        try {
            Random random = ThreadLocalRandom.current();
            Individual first = processor.newRandomIndividual();
            Individual second = processor.query(inversion, first);

            while (true) {
                if (random.nextBoolean()) {
                    Individual newFirst = processor.query(flipDiff, first, second);
                    if (newFirst.fitness() > first.fitness()) {
                        first = newFirst;
                    }
                } else {
                    Individual newSecond = processor.query(flipDiff, second, first);
                    if (newSecond.fitness() > second.fitness()) {
                        second = newSecond;
                    }
                }
            }
        } catch (UnbiasedProcessor.OptimumFound found) {
            return found.numberOfQueries();
        }
    }

    @Test
    public void smokeTest() {
        int n = 239;
        int count = 1000;

        UnbiasedProcessor processor = new UnbiasedProcessor(n, 2, ImmutableBitArray::cardinality, n);

        int sum = 0;
        for (int i = 0; i < count; ++i) {
            int value = runOneMax(processor);
            sum += value;
        }

        double avg = (double) (sum) / count;
        System.out.println("Average = " + avg + " = n * " + (avg / n));

        if (sum > n * 2 * count * 1.05 || sum < n * 2 * count * 0.95) {
            Assert.fail("n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }
}
