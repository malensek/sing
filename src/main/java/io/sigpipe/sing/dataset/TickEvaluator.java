package io.sigpipe.sing.dataset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.sigpipe.sing.dataset.feature.Feature;
import io.sigpipe.sing.stat.RunningStatistics;

public class TickEvaluator {

    private Quantizer quantizer;
    private Map<Feature, RunningStatistics> statMap = new HashMap<>();

    public TickEvaluator(Quantizer quantizer) {
        this.quantizer = quantizer;
    }

    public void train(List<Feature> features) {
        for (Feature feature : features) {
            Feature q = quantizer.quantize(feature);
            RunningStatistics rs = statMap.get(q);
            if (rs == null) {
                rs = new RunningStatistics();
                statMap.put(q, rs);
            }
            rs.put(feature.getDouble());
        }
    }

    public void evaluate(List<Feature> features) {

    }
}
