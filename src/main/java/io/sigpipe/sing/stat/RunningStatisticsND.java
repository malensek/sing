package io.sigpipe.sing.stat;

import java.io.IOException;

import org.apache.commons.math3.util.FastMath;

import io.sigpipe.sing.serialization.ByteSerializable;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.SerializationOutputStream;

public class RunningStatisticsND implements ByteSerializable {

    private long n;

    private double[] mean;
    private double[] m2;
    private double[] min;
    private double[] max;

    private double[] ss;

    public RunningStatisticsND(int dimensions) {
        this.mean = new double[dimensions];
        this.m2 = new double[dimensions];
        this.min = new double[dimensions];
        this.max = new double[dimensions];

        for (int d = 0; d < dimensions; ++d) {
            this.min[d] = Double.MAX_VALUE;
            this.max[d] = Double.MIN_VALUE;
        }

        this.ss = new double[dimensions * (dimensions - 1) / 2];
    }

    public RunningStatisticsND(double... samples) {
        this(samples.length);
        put(samples);
    }

    /**
     * Add a new set of samples to the running statistics.
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
                ss[index] += dx * dy * n / (n + 1);
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

    /**
     * Converts a 2D matrix index (i, j) to a 1D array position.
     *
     * @return corresponding array position.
     */
    private int index1D(int i, int j) {
        int dims = this.dimensions();
        return (dims * (dims - 1) / 2)
            - (dims - i) * ((dims - i) - 1) / 2 + j - i - 1;
    }

    public void merge(RunningStatisticsND that) {

    }

    public void clear() {

    }

    public int dimensions() {
        return mean.length;
    }

    public long count() {
        return this.n;
    }

    @Deserialize
    public RunningStatisticsND(SerializationInputStream in)
    throws IOException {
        int dimensions = in.readInt();
        this.mean = new double[dimensions];
        this.m2 = new double[dimensions];
        this.min = new double[dimensions];
        this.max = new double[dimensions];
        this.ss = new double[dimensions * (dimensions - 1) / 2];

        this.n = in.readLong();

        for (int i = 0; i < dimensions; ++i) {
            mean[i] = in.readDouble();
            m2[i] = in.readDouble();
            min[i] = in.readDouble();
            max[i] = in.readDouble();
        }

        for (int i = 0; i < this.ss.length; ++i) {
            ss[i] = in.readDouble();
        }
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        out.writeInt(this.dimensions());

        out.writeLong(n);

        for (int i = 0; i < this.dimensions(); ++i) {
            out.writeDouble(mean[i]);
            out.writeDouble(m2[i]);
            out.writeDouble(min[i]);
            out.writeDouble(max[i]);
        }

        for (int i = 0; i < ss.length; ++i) {
            out.writeDouble(ss[i]);
        }
    }

}
