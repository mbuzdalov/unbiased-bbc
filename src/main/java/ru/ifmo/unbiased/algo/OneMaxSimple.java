package ru.ifmo.unbiased.algo;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import ru.ifmo.unbiased.Individual;
import ru.ifmo.unbiased.Operators;
import ru.ifmo.unbiased.UnbiasedProcessor;

public final class OneMaxSimple {
    private OneMaxSimple() {}

    public static int runUnary(UnbiasedProcessor processor) {
        processor.reset();
        try {
            Individual current = processor.newRandomIndividual();

            //noinspection InfiniteLoopStatement
            while (true) {
                Individual next = processor.query(Operators.FLIP_ONE, current);
                if (next.fitness() > current.fitness()) {
                    current = next;
                }
            }
        } catch (UnbiasedProcessor.OptimumFound found) {
            return found.numberOfQueries();
        }
    }

    public static int runBinary(UnbiasedProcessor processor) {
        processor.reset();
        try {
            Random random = ThreadLocalRandom.current();
            Individual first = processor.newRandomIndividual();
            Individual second = processor.query(Operators.FLIP_ALL, first);

            //noinspection InfiniteLoopStatement
            while (true) {
                if (random.nextBoolean()) {
                    Individual newFirst = processor.query(Operators.FLIP_ONE_DIFFERENT, first, second);
                    if (newFirst.fitness() > first.fitness()) {
                        first = newFirst;
                    }
                } else {
                    Individual newSecond = processor.query(Operators.FLIP_ONE_DIFFERENT, second, first);
                    if (newSecond.fitness() > second.fitness()) {
                        second = newSecond;
                    }
                }
            }
        } catch (UnbiasedProcessor.OptimumFound found) {
            return found.numberOfQueries();
        }
    }
}
