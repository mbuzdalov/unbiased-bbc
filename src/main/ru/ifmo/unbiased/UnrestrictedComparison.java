package ru.ifmo.unbiased;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import ru.ifmo.unbiased.misc.UnrestrictedOneMax;

public class UnrestrictedComparison {
    private static int run(int n, boolean pure) {
        Random random = ThreadLocalRandom.current();
        int first = random.nextInt() >>> (32 - n);
        int firstFitness = Integer.bitCount(first);
        if (firstFitness == n) {
            return 1;
        }
        int second = random.nextInt() >>> (32 - n);
        int secondFitness = Integer.bitCount(second);
        if (secondFitness == n) {
            return 2;
        }
        UnrestrictedOneMax oneMax = new UnrestrictedOneMax(n, pure, first, firstFitness, second, secondFitness);
        int count = 2;
        while (true) {
            int current = oneMax.getIndividualToTest();
            int currentFitness = Integer.bitCount(current);
            ++count;
            if (currentFitness == n) {
                return count;
            }
            oneMax.add(current, currentFitness);
        }
    }

    private static final BiFunction<Integer, Integer, Integer> plusOne = (key, old) -> old == null ? 1 : old + 1;

    public static void main(String[] args) {
        List<String> summary = new ArrayList<>();
        List<String> stats = new ArrayList<>();
        for (int n = 2; n <= 32; ++n) {
            int sumPure = 0, sumHack = 0;
            int nTimes = 10000;
            double[] valuesPure = new double[nTimes];
            double[] valuesHack = new double[nTimes];
            Map<Integer, Integer> countsPure = new TreeMap<>();
            Map<Integer, Integer> countsHack = new TreeMap<>();
            for (int t = 0; t < nTimes; ++t) {
                int runPure = run(n, true);
                int runHack = run(n, false);
                countsPure.compute(runPure, plusOne);
                countsHack.compute(runHack, plusOne);
                valuesPure[t] = runPure;
                valuesHack[t] = runHack;
                sumPure += runPure;
                sumHack += runHack;
                System.out.println("n = " + n + ", run = " + (t + 1) + ":"
                        + " pure " + runPure + " (" + sumPure + ")"
                        + " hack " + runHack + " (" + sumHack + ")");
            }
            stats.add(n + " => pure " + countsPure + ", hack " + countsHack);
            double meanPure = (double) (sumPure) / nTimes;
            double meanHack = (double) (sumHack) / nTimes;
            double sumSqPure = 0;
            double sumSqHack = 0;
            for (int i = 0; i < nTimes; ++i) {
                sumSqPure += (valuesPure[i] - meanPure) * (valuesPure[i] - meanPure);
                sumSqHack += (valuesHack[i] - meanHack) * (valuesHack[i] - meanHack);
            }
            double devPure = Math.sqrt(sumSqPure / (nTimes - 1));
            double devHack = Math.sqrt(sumSqHack / (nTimes - 1));
            double pValue = new MannWhitneyUTest().mannWhitneyUTest(valuesPure, valuesHack);
            summary.add(n + " => pure " + meanPure + " +- " + devPure
                    + ", hack " + meanHack + " +- " + devHack
                    + ", p-value " + pValue);
        }
        for (String line : stats) {
            System.out.println(line);
        }
        for (String line : summary) {
            System.out.println(line);
        }
    }
}
