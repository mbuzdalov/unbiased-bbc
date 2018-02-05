package ru.ifmo.unbiased;

import java.io.IOException;
import java.io.PrintWriter;
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
            n -> OneMaxHandCrafted.runQuaternary(new UnbiasedProcessor(n, 4, ImmutableBitArray::cardinality, n)),
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
            "custom arity=4",
            "generic arity=3 pure",
            "generic arity=3 hack",
            "generic arity=4 pure",
            "generic arity=4 hack",
            "generic arity=5 pure",
            "generic arity=5 hack",
            "generic arity=6 pure",
            "generic arity=6 hack",
    };

    private static String[] algorithmTexNames = {
            "ArityOne",
            "ArityTwo",
            "CustomArityThree",
            "CustomArityFour",
            "GenericArityThreePure",
            "GenericArityThreeHack",
            "GenericArityFourPure",
            "GenericArityFourHack",
            "GenericArityFivePure",
            "GenericArityFiveHack",
            "GenericAritySixPure",
            "GenericAritySixHack",
    };

    public static void main(String[] args) throws IOException {
        try (PrintWriter plots = new PrintWriter("measurements.tex");
             PrintWriter logs = new PrintWriter("measurements.log")) {
            for (int i = 0; i < algorithms.length; ++i) {
                plots.println("\\pgfplotstableread{");
                plots.println("  x y dev");
                logs.println(algorithmNames[i]);
                System.out.println(algorithmNames[i] + ":");
                for (int n = 100; n <= 2000; n += 100) {
                    System.out.println("  n = " + n);
                    System.out.print("  ");
                    logs.print("  n = " + n + ":");
                    int sum = 0;
                    int measurements = 100;
                    int[] results = new int[measurements];
                    for (int j = 0; j < results.length; ++j) {
                        results[j] = algorithms[i].applyAsInt(n);
                        System.out.print(" " + results[j]);
                        sum += results[j];
                        if ((j + 1) % 25 == 0) {
                            System.out.println();
                            System.out.print("  ");
                        }
                    }
                    Arrays.sort(results);
                    for (int result : results) {
                        logs.print(" " + result);
                    }
                    logs.println();
                    double mean = (double) (sum) / measurements;
                    double sumDiffSquares = 0;
                    for (int result : results) {
                        sumDiffSquares += (mean - result) * (mean - result);
                    }
                    double deviation = Math.sqrt(sumDiffSquares / (measurements - 1));
                    plots.println("    " + n + " " + mean + " " + deviation);
                    System.out.println("=> " + mean + " +- " + deviation);
                }
                plots.println("}{\\" + algorithmTexNames[i] + "}");
                plots.flush();
                logs.flush();
            }
        }
    }
}
