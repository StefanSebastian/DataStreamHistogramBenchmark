package com.datastreams.histogram.benchmark.model;

/**
 * @author stefansebii@gmail.com
 */
public class BenchmarkAction {
    public enum Type {
        ADD, QUERY
    }

    private Type type;
    private int value;
    private int lowerBound;
    private int upperBound;

    public BenchmarkAction() {
    }

    public BenchmarkAction(int value) {
        this.type = Type.ADD;
        this.value = value;
    }

    public BenchmarkAction(int lowerBound, int upperBound) {
        this.type = Type.QUERY;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
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
