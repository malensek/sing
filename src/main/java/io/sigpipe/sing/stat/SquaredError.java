package io.sigpipe.sing.stat;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import io.sigpipe.sing.dataset.feature.Feature;

public class SquaredError {

    private RunningStatistics sqErrs = new RunningStatistics();
    private RunningStatistics actualStats = new RunningStatistics();
    private RunningStatistics predictedStats = new RunningStatistics();

    public SquaredError(List<Feature> actual, List<Feature> predicted) {
        if (actual.size() != predicted.size()) {
            throw new IllegalArgumentException(
                    "List sizes must be equal");
        }

        for (int i = 0; i < actual.size(); ++i) {
            Feature a = actual.get(i);
            Feature b = predicted.get(i);
            Feature err = a.subtract(b);
            double p = Math.pow(err.getDouble(), 2.0);

            sqErrs.put(p);
            actualStats.put(a.getDouble());
            predictedStats.put(b.getDouble());
        }
    }

    public double RMSE() {
        return Math.sqrt(sqErrs.mean());
    }

    public double NRMSE() {
        return RMSE() / (actualStats.max() - actualStats.min());
    }

    public double CVRMSE() {
        return RMSE() / actualStats.mean();
    }

    public SummaryStatistics actualSummary() {
        return new SummaryStatistics(actualStats);
    }

    public SummaryStatistics predictedSummary() {
        return new SummaryStatistics(predictedStats);
    }

}
