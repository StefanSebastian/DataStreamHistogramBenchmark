package com.datastreams.histogram.benchmark.model;

/**
 * Params for performing benchmark
 *
 * @author stefansebii@gmail.com
 */
public class BenchmarkStats {
    private int nrOps;
    private double queryChance;
    private int lowerBound;
    private int upperBound;

    public BenchmarkStats() {
    }

    public BenchmarkStats(int nrOps, double queryChance, int lowerBound, int upperBound) {
        this.nrOps = nrOps;
        this.queryChance = queryChance;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public int getNrOps() {
        return nrOps;
    }

    public void setNrOps(int nrOps) {
        this.nrOps = nrOps;
    }

    public double getQueryChance() {
        return queryChance;
    }

    public void setQueryChance(double queryChance) {
        this.queryChance = queryChance;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }
}
