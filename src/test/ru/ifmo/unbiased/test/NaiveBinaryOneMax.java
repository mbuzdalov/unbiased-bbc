package ru.ifmo.unbiased.test;

import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.unbiased.UnbiasedProcessor;
import ru.ifmo.unbiased.ops.Operator1;
import ru.ifmo.unbiased.ops.Operator2;
import ru.ifmo.unbiased.ops.UnbiasedOperator;
import ru.ifmo.unbiased.util.ImmutableBitArray;

public class NaiveBinaryOneMax {
    private static final ToIntFunction<ImmutableBitArray> oneMaxWithLog = ImmutableBitArray::cardinality;

    @Test
    public void smokeTest() {
        int n = 239;
        int count = 1000;
        UnbiasedProcessor processor = new UnbiasedProcessor(n, 2, oneMaxWithLog, n) {
            private Operator1 inversion = new Operator1() {
                @Override
                protected int applyUnary(int nBits) {
                    return nBits;
                }
            };

            private Operator2 flipDiffFirst = new Operator2() {
                @Override
                protected void applyBinary(int sameBits, int differentBits) {
                    flipSame(0);
                    flipDifferent(1);
                }
            };

            private Operator2 flipDiffSecond = new Operator2() {
                @Override
                protected void applyBinary(int sameBits, int differentBits) {
                    flipSame(0);
                    flipDifferent(differentBits - 1);
                }
            };

            private int firstIndex = -1, secondIndex = -1;
            private boolean lastChosenOperatorIsFlipDiffFirst;

            @Override
            protected void setup() {
                firstIndex = -1;
                secondIndex = -1;
                lastChosenOperatorIsFlipDiffFirst = false;
            }

            @Override
            protected UnbiasedOperator makeDecision(List<Integer> fitnessValues, List<Integer> selectedIndices, Random random) {
                if (fitnessValues.size() == 1) {
                    firstIndex = 0;
                    selectedIndices.add(0);
                    return inversion;
                }
                if (secondIndex == -1) {
                    secondIndex = 1;
                } else {
                    int lastIndex = fitnessValues.size() - 1;
                    if (lastChosenOperatorIsFlipDiffFirst) {
                        if (fitnessValues.get(lastIndex) > fitnessValues.get(firstIndex)) {
                            firstIndex = lastIndex;
                        }
                    } else {
                        if (fitnessValues.get(lastIndex) > fitnessValues.get(secondIndex)) {
                            secondIndex = lastIndex;
                        }
                    }
                }
                selectedIndices.add(firstIndex);
                selectedIndices.add(secondIndex);
                lastChosenOperatorIsFlipDiffFirst = random.nextBoolean();
                return lastChosenOperatorIsFlipDiffFirst ? flipDiffFirst : flipDiffSecond;
            }
        };

        int sum = 0;
        for (int i = 0; i < count; ++i) {
            int value = processor.run();
            sum += value;
        }

        double avg = (double) (sum) / count;
        System.out.println("Average = " + avg + " = n * " + (avg / n));

        if (sum > n * 2 * count * 1.05 || sum < n * 2 * count * 0.95) {
            Assert.fail("n is " + n + ", sum is " + sum + ", average is " + avg);
        }
    }
}
