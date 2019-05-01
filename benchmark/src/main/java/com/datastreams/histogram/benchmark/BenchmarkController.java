package com.datastreams.histogram.benchmark;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author stefansebii@gmail.com
 */
@Controller
public class BenchmarkController {

    @Autowired
    private BenchmarkEngine benchmarkEngine;

    @GetMapping("/uniform")
    public @ResponseBody String balancedDistribution() {
        benchmarkEngine.uniformBenchmark();
        return "benchmark executed";
    }
}
