package io.sigpipe.sing.stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Implements Reservoir Sampling, which maintains a representative, random
 * sample of a given size as data points are streamed in. Reservoirs are useful
 * when the number of data points is not known ahead of time or the entire
 * dataset cannot fit into memory. See
 * https://en.wikipedia.org/wiki/Reservoir_sampling for more information.
 */
public class Reservoir<T> {

    private long count;
    private int size;
    private List<Entry> reservoir;
    private Random random = new Random();

    private class Entry implements Comparable<Entry> {
        public double key;
        public T value;

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
            return "[" + key + "] -> " + value;
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

        if (count < this.size()) {
            /* The reservoir has not been filled yet; add the item immediately.
             * Note: we can cast the count to an integer here because the size
             * of the reservoir is limited to the capacity of a single int. */
            reservoir.add((int) count, e);
        } else {
            if (key < ((double) this.size() / (count + 1))) {
                int position = random.nextInt(this.size());
                reservoir.set(position, e);
            }
        }

        count++;
    }

    public void merge(Reservoir<T> that, int size) {
        List<Entry> combinedEntries = new ArrayList<>(size);
        combinedEntries.addAll(this.reservoir);
        combinedEntries.addAll(that.reservoir);
        Collections.sort(combinedEntries);

        this.reservoir = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            this.reservoir.add(combinedEntries.get(i));
        }
    }

    public void merge(Reservoir<T> that) {
        merge(that, this.size());
    }

    /**
     * Retrieves the total number of items observed by the reservoir. Note that
     * this is different from the size of the reservoir, which represents the
     * number of items in the sample.
     *
     * @return the total number of items observed by this reservoir instance.
     */
    public long count() {
        return this.count;
    }

    /**
     * Retrieves the size of the reservoir, which is the number of items
     * contained in the sample.
     *
     * @return reservoir sample size.
     */
    public int size() {
        return this.size;
    }

    public List<Entry> entries() {
        return new ArrayList<>(this.reservoir);
    }

    public List<T> sample() {
        List<T> l = new ArrayList<>(this.size());
        for (Entry e : this.reservoir) {
            l.add(e.value);
        }
        return l;
    }

    public double[] keys() {
        double[] k = new double[this.size()];
        for (int i = 0; i < this.size(); ++i) {
            k[i] = this.reservoir.get(i).key;
        }
        return k;
    }

    public static void main(String[] args) {
        Reservoir<Double> rs = new Reservoir<>(20);
        Reservoir<Double> r2 = new Reservoir<>(20);

        Random r = new Random();
        r.doubles(10000).filter(val -> val < 0.5).forEach(rs::put);
        r.doubles(10000).filter(val -> val < 0.10).forEach(r2::put);

        RunningStatistics stats = new RunningStatistics();
        for (Reservoir<Double>.Entry e : rs.entries()) {
            System.out.println(e);
            stats.put(e.value);
        }
        System.out.println(stats);

        rs.merge(r2);
        stats = new RunningStatistics();
        for (Reservoir<Double>.Entry e : rs.entries()) {
            System.out.println(e);
            stats.put(e.value);
        }
        System.out.println(stats);
    }
}
