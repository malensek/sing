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


    public Quantizer(Feature start, Feature end, Feature step) {
        this.start = start;
        this.end = end;

        Feature tick = new Feature(start);
        while (tick.less(end)) {
            insertTick(tick);
            tick = tick.add(step);
        }
    }

    public void insertTick(Feature tick) {
        ticks.add(tick);
    }

    public void removeTick(Feature tick) {
        ticks.remove(tick);
    }

    public Feature quantize(Feature feature) {
        Feature result = ticks.floor(feature);
        //TODO if this is null, handle the special case
        return result;
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
