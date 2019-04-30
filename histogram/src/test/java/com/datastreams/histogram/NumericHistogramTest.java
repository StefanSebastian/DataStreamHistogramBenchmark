package com.datastreams.histogram;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author stefansebii@gmail.com
 */
public class NumericHistogramTest {

    private int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    // performs poorly
    @Test
    public void testNotFull() {
        NumericHistogram nrHist = new NumericHistogram();
        nrHist.allocate(20);
        nrHist.add(10);
        nrHist.add(12);

        // using prior knowledge must add boundaries
        nrHist.add(0);
        nrHist.add(100);

        System.out.println("actual " + 2);
        System.out.println("histogram " + nrHist.estimate(9, 13));
    }

    // decent results already
    @Test
    public void test1000ValuesBetween0and100() {
        NumericHistogram nrHist = new NumericHistogram();
        nrHist.allocate(20);
        Map<Integer, Integer> actualValues = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            int nr = getRandomNumberInRange(0, 100);
            addActual(actualValues, nr);
            nrHist.add(nr);
        }

        nrHist.add(0);
        nrHist.add(100);

        // now generate some queries
        int lower = 40; int upper = 60;
        double actual = 0;
        for (Integer key : actualValues.keySet()) {
            if (key >= lower && key <= upper) {
                actual += actualValues.get(key);
            }
        }
        System.out.println("actual value " + actual);
        System.out.println("histogram value " + nrHist.estimate(lower, upper));
    }

    private void addActual(Map<Integer, Integer> actualValues, Integer nr) {
        if (actualValues.containsKey(nr)) {
            actualValues.put(nr, actualValues.get(nr) + 1);
        } else {
            actualValues.put(nr, 1);
        }
    }
}
