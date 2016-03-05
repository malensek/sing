package io.sigpipe.sing.dataset;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import io.sigpipe.sing.dataset.feature.Feature;

public class Quantizer {

    private NavigableSet<Feature> ticks = new TreeSet<>();

    public Quantizer() {

    }

    public void insertTick(Feature tick) {
        ticks.add(tick);
    }

    public void removeTick(Feature tick) {
        ticks.remove(tick);
    }

    public Feature quantize(Feature feature) {
        return ticks.ceiling(feature);
    }

    @Override
    public String toString() {
        String output = "";
        Iterator<Feature> it = ticks.iterator();
        while (it.hasNext()) {
            Feature f = it.next();
            output += f.getString() + System.lineSeparator();
        }
        return output;
    }

}
