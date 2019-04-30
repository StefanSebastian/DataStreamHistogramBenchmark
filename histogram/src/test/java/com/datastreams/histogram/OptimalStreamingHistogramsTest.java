package com.datastreams.histogram;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author stefansebii@gmail.com
 */
public class OptimalStreamingHistogramsTest {
    private int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    @Test
    public void binCreation() {
        OptimalStreamingHistograms hist = new OptimalStreamingHistograms(0.5, 124);
        System.out.println(hist);

        hist.add(5);
        hist.add(112);
        hist.add(0.7);

        System.out.println(hist);
    }

    @Test
    public void test1000ValuesBetween0and100() {
        OptimalStreamingHistograms hist = new OptimalStreamingHistograms(0, 100);

        Map<Integer, Integer> actualValues = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            int nr = getRandomNumberInRange(0, 100);
            addActual(actualValues, nr);
            hist.add(nr);
        }

        // now generate some queries
        int lower = 40; int upper = 60;
        double actual = 0;
        for (Integer key : actualValues.keySet()) {
            if (key >= lower && key <= upper) {
                actual += actualValues.get(key);
            }
        }
        System.out.println("actual value " + actual);
        System.out.println("histogram value " + hist.estimate(lower, upper));
    }

    private void addActual(Map<Integer, Integer> actualValues, Integer nr) {
        if (actualValues.containsKey(nr)) {
            actualValues.put(nr, actualValues.get(nr) + 1);
        } else {
            actualValues.put(nr, 1);
        }
    }
}
