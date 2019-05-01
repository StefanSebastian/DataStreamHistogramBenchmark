package com.datastreams.histogram.benchmark.model;

/**
 * Benchmark results
 *
 * @author stefansebii@gmail.com
 */
public class BenchmarkReport {
    private String algorithm;
    private int nrQueries;
    private double averageReqTime;
    private double meanAbsError;
    private double meanSquaredError;

    public BenchmarkReport() {
    }

    public BenchmarkReport(String algorithm, int nrQueries, double averageReqTime, double meanAbsError, double squaredAbsError) {
        this.algorithm = algorithm;
        this.nrQueries = nrQueries;
        this.averageReqTime = averageReqTime;
        this.meanAbsError = meanAbsError;
        this.meanSquaredError = squaredAbsError;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getNrQueries() {
        return nrQueries;
    }

    public void setNrQueries(int nrQueries) {
        this.nrQueries = nrQueries;
    }

    public double getAverageReqTime() {
        return averageReqTime;
    }

    public void setAverageReqTime(double averageReqTime) {
        this.averageReqTime = averageReqTime;
    }

    public double getMeanAbsError() {
        return meanAbsError;
    }

    public void setMeanAbsError(double meanAbsError) {
        this.meanAbsError = meanAbsError;
    }

    public double getMeanSquaredError() {
        return meanSquaredError;
    }

    public void setMeanSquaredError(double meanSquaredError) {
        this.meanSquaredError = meanSquaredError;
    }
}
