package io.sigpipe.sing.stat;

public class ReservoirSampler {

    private int count;
    private double[] reservoir;
    public ReservoirSampler(int size) {
        reservoir = new double[size];
    }
    }
    public int size() {
        return reservoir.length;
    }
    public double[] samples() {
        return this.reservoir;
    }
}
