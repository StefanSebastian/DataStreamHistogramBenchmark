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
            double rangeStart = Math.pow(10, p);
            double increment = rangeStart / 10;
            double rangeEnd = Math.pow(10, p + 1);
            while (rangeStart < rangeEnd) {
                bins.add(new Coord(rangeStart, 0));
                rangeStart += increment;
            }
        }
    }

    public void add(double v) {
        int bin = getBin(v);
        Coord coord = bins.get(bin);
        coord.y += 1;
    }

    public double estimate(double a, double b) {
        double estimate = 0;

        int bin1 = getBin(a);
        int bin2 = getBin(b);

        Coord c1 = bins.get(bin1);
        double upperBound = c1.x * 10;
        double lowerBound = c1.x;
        double ratio = (upperBound - a) / (upperBound - lowerBound);
        double aToNext = ratio * c1.y;
        estimate += aToNext;

        bin1++;
        while (bin1 < bin2) {
            estimate += bins.get(bin1).y;
            bin1++;
        }

        Coord c2 = bins.get(bin2);
        upperBound = c2.x * 10;
        lowerBound = c2.x;
        ratio =  (b - lowerBound) / (upperBound - lowerBound);
        double prevToB = ratio * c2.y;
        estimate += prevToB;

        return estimate;
    }



    /**
     * Gets bin which contains given value
     */
    private int getBin(double v) {
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
        return bin > 0 ?  bin - 1 : 0;
    }

}
