/*
Copyright (c) 2016, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package io.sigpipe.sing.stat;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import io.sigpipe.sing.dataset.feature.Feature;

public class SquaredError {

    private RunningStatistics sqErrs = new RunningStatistics();
    private RunningStatistics actualStats = new RunningStatistics();
    private RunningStatistics predictedStats = new RunningStatistics();

    public SquaredError() {

    }

    public SquaredError(List<Feature> actual, List<Feature> predicted) {
        if (actual.size() != predicted.size()) {
            throw new IllegalArgumentException(
                    "List sizes must be equal");
        }

        for (int i = 0; i < actual.size(); ++i) {
            Feature a = actual.get(i);
            Feature p = predicted.get(i);
            this.put(a, p);
        }
    }

    public void put(Feature actual, Feature predicted) {
        this.put(actual.getDouble(), predicted.getDouble());
    }

    public void put(double actual, double predicted) {
        double err = actual - predicted;
        double p = FastMath.pow(err, 2.0);
        sqErrs.put(p);
        actualStats.put(actual);
        predictedStats.put(predicted);
    }

    public double RMSE() {
        return FastMath.sqrt(sqErrs.mean());
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
