package com.datastreams.histogram;

import org.junit.Test;

/**
 * @author stefansebii@gmail.com
 */
public class OptimalStreamingHistogramsTest {
    @Test
    public void binCreation() {
        OptimalStreamingHistograms hist = new OptimalStreamingHistograms(0.5, 124);
        System.out.println(hist);

        hist.add(5);
        hist.add(112);
        hist.add(0.7);

        System.out.println(hist);
    }
}
