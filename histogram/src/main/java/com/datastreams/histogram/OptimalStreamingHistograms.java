package com.datastreams.histogram;

import java.util.HashMap;

/**
 * Algorithm from https://amplitude.com/blog/2014/08/06/optimal-streaming-histograms
 *
 * @author stefansebii@gmail.com
 */
public class OptimalStreamingHistograms {

   private HashMap<Double, Integer> bins = new HashMap<>();

    public OptimalStreamingHistograms(double minbound, double maxbound) {
        int p1 = -1;
        while (Math.pow(10, p1) < minbound) {
            p1++;
        }
        p1--;

        int p2 = -1;
        while (Math.pow(10, p2) < maxbound) {
            p2++;
        }

        for (int p = p1; p <= p2; p++) {
            bins.put(Math.pow(10, p), 0);
        }
    }

}
