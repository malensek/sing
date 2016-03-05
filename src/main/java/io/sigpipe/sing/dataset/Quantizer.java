package io.sigpipe.sing.dataset;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import io.sigpipe.sing.dataset.feature.Feature;

public class Quantizer {

    public static void main(String[] args) {
        Quantizer q = new Quantizer(new Feature(28.3), 2.8);
        System.out.println(q);

        System.out.println(q.quantize(new Feature(1.1)));
        System.out.println(q.quantize(new Feature(2.8)));
        System.out.println(q.quantize(new Feature(-100.0)));
        System.out.println(q.quantize(new Feature(80.0)));
        System.out.println(q.quantize(new Feature(100.0)));

    }

    private NavigableSet<Feature> ticks = new TreeSet<>();

    public <T> Quantizer(Feature interval, T step) {
//        if (interval.isInterval() == false) {
//            return;
//        }

        Double start = 0.0;
        Double finish = 100.0;
        Double stepSz = 2.8;

        for (Double i = start; i < finish; i += stepSz) {
            Feature f = new Feature(i);
            insertTick(f);
        }
    }

    public void insertTick(Feature tick) {
        ticks.add(tick);
    }

    public void removeTick(Feature tick) {
        ticks.remove(tick);
    }

    public Feature quantize(Feature feature) {
        return ticks.floor(feature);
    }

    @Override
    public String toString() {
        String output = "";
        for (Feature f : ticks) {
            output += f.getString() + System.lineSeparator();
        }
        return output;
    }

}
