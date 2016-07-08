package io.sigpipe.sing.stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Reservoir<T extends Comparable<T>> {

    private int count;
    private int size;
    private List<Entry> reservoir;
    private Random random = new Random();

    private class Entry implements Comparable<Entry> {
        public double key;
        public T value;
        public int id;

        public Entry(double key, T value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(Entry that) {
            return Double.compare(this.key, that.key);
        }

        @Override
        public String toString() {
            return id + " [" + key + "] -> " + value;
        }
    }

    public Reservoir(int size) {
        this.size = size;
        reservoir = new ArrayList<>(size);
    }

    public void put(Iterable<T> items) {
        for (T item : items) {
            put(item);
        }
    }

    public void put(T item) {
        double key = random.nextDouble();
        Entry e = new Entry(key, item);
        e.id = count;
        if (count < this.size()) {
            reservoir.add(count, e);
        } else {
            if (key < ((double) this.size() / (count + 1))) {
                int position = random.nextInt(this.size());
                reservoir.set(position, e);
            }
        }

        count++;
    }

//    public void merge(Reservoir<T> res, int size) {
//
//    }
//

    public int size() {
        return this.size;
    }

    public List<Entry> entries() {
        return new ArrayList<>(this.reservoir);
    }

    public List<T> samples() {
        return new ArrayList<>(reservoir);
    }

    private double[] keys() {
        return this.keys;
    }

    public static void main(String[] args) {
        Reservoir<Double> rs = new Reservoir<>(20);

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
