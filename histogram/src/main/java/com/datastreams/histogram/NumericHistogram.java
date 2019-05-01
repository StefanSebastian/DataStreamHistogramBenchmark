package com.datastreams.histogram;

import java.util.ArrayList;
import java.util.Random;

/**
 * The algorithm is taken from the hive implementation of the following paper:
 * Yael Ben-Haim and Elad Tom-Tov, "A streaming parallel decision tree algorithm",
 * J. Machine Learning Research 11 (2010), pp. 849--872.
 *
 * The sum method was implemented as described in the paper.
 *
 * @author stefansebii@gmail.com
 */
public class NumericHistogram {

    static class Coord implements Comparable {
        double x;
        double y;

        public int compareTo(Object other) {
            return Double.compare(x, ((Coord) other).x);
        }
    }

    private int nbins;
    private int nusedbins;
    private ArrayList<Coord> bins;
    private Random prng;

    public NumericHistogram() {
        nbins = 0;
        nusedbins = 0;
        bins = null;
        prng = new Random();
    }

    /**
     * Initialize histogram, add bounds in order to provide estimates for queries outside the range of the given
     * values. Allocates a number of buckets that falls within the recommended values.
     */
    public NumericHistogram(double lowerBound, double upperBound) {
        this();
        allocate(40); // a number in the recommended range for the amount of bins
        add(lowerBound);
        add(upperBound);
    }

    /**
     * Sets the number of histogram bins to use for approximating data.
     *
     * @param num_bins Number of non-uniform-width histogram bins to use
     */
    public void allocate(int num_bins) {
        nbins = num_bins;
        bins = new ArrayList<Coord>();
        nusedbins = 0;
    }

    /**
     * Adds a new data point to the histogram approximation. Make sure you have
     * called either allocate() or merge() first. This method implements Algorithm #1
     * from Ben-Haim and Tom-Tov, "A Streaming Parallel Decision Tree Algorithm", JMLR 2010.
     *
     * @param v The data point to add to the histogram approximation.
     */
    public synchronized void add(double v) {
        // Binary search to find the closest bucket that v should go into.
        // 'bin' should be interpreted as the bin to shift right in order to accomodate
        // v. As a result, bin is in the range [0,N], where N means that the value v is
        // greater than all the N bins currently in the histogram. It is also possible that
        // a bucket centered at 'v' already exists, so this must be checked in the next step.
        int bin = 0;
        for(int l=0, r=nusedbins; l < r; ) {
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

        // If we found an exact bin match for value v, then just increment that bin's count.
        // Otherwise, we need to insert a new bin and trim the resulting histogram back to size.
        // A possible optimization here might be to set some threshold under which 'v' is just
        // assumed to be equal to the closest bin -- if fabs(v-bins[bin].x) < THRESHOLD, then
        // just increment 'bin'. This is not done now because we don't want to make any
        // assumptions about the range of numeric data being analyzed.
        if (bin < nusedbins && bins.get(bin).x == v) {
            bins.get(bin).y++;
        } else {
            Coord newBin = new Coord();
            newBin.x = v;
            newBin.y = 1;
            bins.add(bin, newBin);

            // Trim the bins down to the correct number of bins.
            if (++nusedbins > nbins) {
                trim();
            }
        }

    }

    /**
     * Trims a histogram down to 'nbins' bins by iteratively merging the closest bins.
     * If two pairs of bins are equally close to each other, decide uniformly at random which
     * pair to merge, based on a PRNG.
     */
    private void trim() {
        while(nusedbins > nbins) {
            // Find the closest pair of bins in terms of x coordinates. Break ties randomly.
            double smallestdiff = bins.get(1).x - bins.get(0).x;
            int smallestdiffloc = 0, smallestdiffcount = 1;
            for(int i = 1; i < nusedbins-1; i++) {
                double diff = bins.get(i+1).x - bins.get(i).x;
                if(diff < smallestdiff)  {
                    smallestdiff = diff;
                    smallestdiffloc = i;
                    smallestdiffcount = 1;
                } else {
                    if(diff == smallestdiff && prng.nextDouble() <= (1.0/++smallestdiffcount) ) {
                        smallestdiffloc = i;
                    }
                }
            }

            // Merge the two closest bins into their average x location, weighted by their heights.
            // The height of the new bin is the sum of the heights of the old bins.
            // double d = bins[smallestdiffloc].y + bins[smallestdiffloc+1].y;
            // bins[smallestdiffloc].x *= bins[smallestdiffloc].y / d;
            // bins[smallestdiffloc].x += bins[smallestdiffloc+1].x / d *
            //   bins[smallestdiffloc+1].y;
            // bins[smallestdiffloc].y = d;

            double d = bins.get(smallestdiffloc).y + bins.get(smallestdiffloc+1).y;
            Coord smallestdiffbin = bins.get(smallestdiffloc);
            smallestdiffbin.x *= smallestdiffbin.y / d;
            smallestdiffbin.x += bins.get(smallestdiffloc+1).x / d * bins.get(smallestdiffloc+1).y;
            smallestdiffbin.y = d;
            // Shift the remaining bins left one position
            bins.remove(smallestdiffloc+1);
            nusedbins--;
        }
    }

    private double sum(double v) {
        int bin = 0;
        for(int l=0, r=nusedbins; l < r; ) {
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
        int i = bin - 1;

        double mb = bins.get(i).y + ((bins.get(i + 1).y - bins.get(i).y) / (bins.get(i + 1).x - bins.get(i).x))
                * (v - bins.get(i).x);
        double s = ((bins.get(i + 1).y + mb) / 2) * ((v - bins.get(i).x) / (bins.get( + 1).x - bins.get(i).x));
        for (int j = 0; j < i; j++) {
            s += bins.get(j).y;
        }
        s = s + bins.get(i).y / 2;
        return s;
    }

    public synchronized double estimate(double a, double b) {
        return sum(b) - sum(a);
    }

}
