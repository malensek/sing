package io.sigpipe.sing.stat;

import org.apache.commons.math3.util.FastMath;

public class RunningStatisticsND {

    private long n;

    private double[] mean;
    private double[] m2;
    private double[] min;
    private double[] max;

    private double[] ss;

    public RunningStatisticsND(int size) {
        this.mean = new double[size];
        this.m2 = new double[size];
        this.min = new double[size];
        this.max = new double[size];

        for (int d = 0; d < size; ++d) {
            this.min[d] = Double.MAX_VALUE;
            this.max[d] = Double.MIN_VALUE;
        }

        ss = new double[size * (size - 1) / 2];
    }

    /**
     * Add a new sample to the running statistics.
     */
    public void put(double... samples) {
        if (samples.length != this.dimensions()) {
            throw new IllegalArgumentException("Input dimension mismatch: "
                    + samples.length + " =/= " + this.dimensions());
        }

        n++;

        for (int i = 0; i < this.dimensions() - 1; ++i) {
            for (int j = i + 1; j < this.dimensions(); ++j) {
                double dx = samples[i] - mean[i];
                double dy = samples[j] - mean[j];
                int index = index1D(i, j);
                ss[index] += dx * dy * n() / (n() + 1);
            }
        }

        for (int d = 0; d < this.dimensions(); ++d) {
            double delta = samples[d] - mean[d];
            mean[d] = mean[d] + delta / n;
            m2[d] = m2[d] + delta * (samples[d] - mean[d]);

            min[d] = FastMath.min(min[d], samples[d]);
            max[d] = FastMath.max(max[d], samples[d]);
        }
    }

    private int index1D(int i, int j) {
        int dims = this.dimensions();
        return (dims * (dims - 1) / 2)
            - (dims - i) * ((dims - i) - 1) / 2 + j - i - 1;
    }

    public int dimensions() {
        return mean.length;
    }

    public long n() {
        return this.n;
    }

}
