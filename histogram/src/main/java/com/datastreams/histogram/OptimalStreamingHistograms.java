package com.datastreams.histogram;

import java.util.ArrayList;
import java.util.List;

/**
 * Algorithm from https://amplitude.com/blog/2014/08/06/optimal-streaming-histograms
 *
 * @author stefansebii@gmail.com
 */
public class OptimalStreamingHistograms {

    static class Coord implements Comparable {
        double x;
        int y;

        public Coord(double x, int y){
            this.x = x; this.y = y;
        }

        public int compareTo(Object other) {
            return Double.compare(x, ((NumericHistogram.Coord) other).x);
        }
    }

   private List<Coord> bins = new ArrayList<>();

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
            bins.add(new Coord(Math.pow(10, p), 0));
        }
    }

    public void add(double v) {
        int bin = 0;
        for(int l=0, r=bins.size(); l < r; ) {
            bin = (l+r)/2;
            if (bins.get(bin).x > v) {
                r = bin;
            } else {
                if (bins.get(bin).x < v) {
                    l = ++bin;
                } else {
                    break; // break loop on equal comparator
                }
            }
        }
        Coord coord = bins.get(bin - 1);
        coord.y += 1;
    }

    public void estimate(double a, double b) {

    }

}
