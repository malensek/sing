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

    public void put(double... items) {
        for (double item : items) {
            put(item);
        }
    }

    public void put(double item) {
        if (count < this.size()) {
            reservoir[count] = item;
        } else {
            double r = random.nextDouble();
            if (r < ((double) this.size() / (count + 1))) {
                int i = random.nextInt(this.size());
                reservoir[i] = item;
                keys[i] = r;
            }
        }

        count++;
    }

    public void merge(ReservoirSampler res, int size) {

    }

    public int size() {
        return reservoir.length;
    }

    public double[] samples() {
        return this.reservoir;
    }

    private double[] keys() {
        return this.keys;
    }

    public static void main(String[] args) {
        ReservoirSampler rs = new ReservoirSampler(20);

        Random r = new Random();
        r.doubles(1000).filter(val -> val < 0.5).forEach(rs::put);

        RunningStatistics stats = new RunningStatistics();
        for (double d : rs.samples()) {
            System.out.println(d);
            stats.put(d);
        }
        System.out.println(stats);

        for (double d : rs.keys()) {
            System.out.println(d);
        }

    }
}
