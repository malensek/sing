package io.sigpipe.sing.stat;

import java.util.Random;

public class ReservoirSampler {

    private int count;
    private double[] reservoir;
    private double[] keys;
    private Random random = new Random();

    public ReservoirSampler(int size) {
        reservoir = new double[size];
        keys = new double[size];
    }
    }
    public int size() {
        return reservoir.length;
    }
    public double[] samples() {
        return this.reservoir;
    }
}
