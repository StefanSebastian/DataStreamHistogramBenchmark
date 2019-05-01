package com.datastreams.histogram;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author stefansebii@gmail.com
 */
@Controller
public class HistogramController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private NumericHistogram nrHist;
    private OptimalStreamingHistograms osHist;

    public class Bounds {
        double lowerBound;
        double upperBound;
    }

    @PostMapping("/init")
    void init(@RequestBody Bounds bounds) {
        logger.info("Init called with bounds: " + bounds);
        nrHist = new NumericHistogram(bounds.lowerBound, bounds.upperBound);
        osHist = new OptimalStreamingHistograms(bounds.lowerBound, bounds.upperBound);
    }

    @PostMapping("/nrh/add")
    void nrHistAdd(double value) {
        nrHist.add(value);
    }

    @PostMapping("/osh/add")
    void osHistAdd(double value) {
        osHist.add(value);
    }

    @GetMapping("/nrh/estimate")
    Double nrHistEstimate(@RequestBody Bounds bounds) {
        return nrHist.estimate(bounds.lowerBound, bounds.upperBound);
    }

    @PostMapping("/osh/estimate")
    Double osHistEstimate(@RequestBody Bounds bounds) {
        return osHist.estimate(bounds.lowerBound, bounds.upperBound);
    }

}
