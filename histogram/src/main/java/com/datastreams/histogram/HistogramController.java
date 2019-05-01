package com.datastreams.histogram;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author stefansebii@gmail.com
 */
@Controller
public class HistogramController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private NumericHistogram nrHist;
    private OptimalStreamingHistograms osHist;

    @PostMapping("/init")
    @ResponseBody void init(@RequestBody Bounds bounds) {
        logger.info("Init called with bounds: " + bounds);
        nrHist = new NumericHistogram(bounds.getLowerBound(), bounds.getUpperBound());
        osHist = new OptimalStreamingHistograms(bounds.getLowerBound(), bounds.getUpperBound());
    }

    @PostMapping("/nrh/add")
    @ResponseBody void nrHistAdd(double value) {
        nrHist.add(value);
    }

    @PostMapping("/osh/add")
    @ResponseBody void osHistAdd(double value) {
        osHist.add(value);
    }

    @GetMapping("/nrh/estimate")
    @ResponseBody Double nrHistEstimate(@RequestBody Bounds bounds) {
        return nrHist.estimate(bounds.getLowerBound(), bounds.getUpperBound());
    }

    @PostMapping("/osh/estimate")
    @ResponseBody Double osHistEstimate(@RequestBody Bounds bounds) {
        return osHist.estimate(bounds.getLowerBound(), bounds.getUpperBound());
    }

}
