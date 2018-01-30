package ru.ifmo.unbiased;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

import ru.ifmo.unbiased.algo.GenericOneMax;
import ru.ifmo.unbiased.algo.OneMaxHandCrafted;
import ru.ifmo.unbiased.algo.OneMaxSimple;
import ru.ifmo.unbiased.util.ImmutableBitArray;

public class BigMeasurements {
    private static IntUnaryOperator[] algorithms = {
            n -> OneMaxSimple.runUnary(new UnbiasedProcessor(n, 1, ImmutableBitArray::cardinality, n)),
            n -> OneMaxSimple.runBinary(new UnbiasedProcessor(n, 2, ImmutableBitArray::cardinality, n)),
            n -> OneMaxHandCrafted.runTernary(new UnbiasedProcessor(n, 3, ImmutableBitArray::cardinality, n)),
            n -> OneMaxHandCrafted.runQuaternary1(new UnbiasedProcessor(n, 4, ImmutableBitArray::cardinality, n)),
            n -> GenericOneMax.runGeneric(new UnbiasedProcessor(n, 3, ImmutableBitArray::cardinality, n), true),
            n -> GenericOneMax.runGeneric(new UnbiasedProcessor(n, 3, ImmutableBitArray::cardinality, n), false),
            n -> GenericOneMax.runGeneric(new UnbiasedProcessor(n, 4, ImmutableBitArray::cardinality, n), true),
            n -> GenericOneMax.runGeneric(new UnbiasedProcessor(n, 4, ImmutableBitArray::cardinality, n), false),
            n -> GenericOneMax.runGeneric(new UnbiasedProcessor(n, 5, ImmutableBitArray::cardinality, n), true),
            n -> GenericOneMax.runGeneric(new UnbiasedProcessor(n, 5, ImmutableBitArray::cardinality, n), false),
            n -> GenericOneMax.runGeneric(new UnbiasedProcessor(n, 6, ImmutableBitArray::cardinality, n), true),
            n -> GenericOneMax.runGeneric(new UnbiasedProcessor(n, 6, ImmutableBitArray::cardinality, n), false),
    };

    private static String[] algorithmNames = {
            "arity=1",
            "arity=2",
            "custom arity=3",
            "custom arity=4 v1",
            "generic arity=3 pure",
            "generic arity=3 hack",
            "generic arity=4 pure",
            "generic arity=4 hack",
            "generic arity=5 pure",
            "generic arity=5 hack",
            "generic arity=6 pure",
            "generic arity=6 hack",
    };

    public static void main(String[] args) {
        for (int n : new int[] { 1000 }) {
            System.out.println("n = " + n);
            for (int i = 0; i < algorithms.length; ++i) {
                System.out.print("  " + algorithmNames[i] + ":");
                int sum = 0;
                int[] results = new int[25];
                for (int j = 0; j < results.length; ++j) {
                    results[j] = algorithms[i].applyAsInt(n);
                    sum += results[j];
                }
                Arrays.sort(results);
                for (int r : results) {
                    System.out.print(" " + r);
                }
                System.out.println(" => " + ((double)sum / results.length));
            }
        }
    }
}
