package com.datastreams.histogram.benchmark;

/**
 * @author stefansebii@gmail.com
 */
public class Query {
    private int lowerBound;
    private int upperBound;
    private double duration;
    private double estimated;
    private double actual;

    public Query(int lowerBound, int upperBound, double duration, double estimated, double actual) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.duration = duration;
        this.estimated = estimated;
        this.actual = actual;
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

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getEstimated() {
        return estimated;
    }

    public void setEstimated(double estimated) {
        this.estimated = estimated;
    }

    public double getActual() {
        return actual;
    }

    public void setActual(double actual) {
        this.actual = actual;
    }

    @Override
    public String toString() {
        return "Query{" +
                "lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                ", duration=" + duration +
                ", estimated=" + estimated +
                ", actual=" + actual +
                '}';
    }
}
