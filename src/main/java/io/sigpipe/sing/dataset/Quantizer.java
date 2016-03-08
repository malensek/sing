package io.sigpipe.sing.dataset;

import java.util.NavigableSet;
import java.util.TreeSet;

import io.sigpipe.sing.dataset.feature.Feature;

public class Quantizer {

    private NavigableSet<Feature> ticks = new TreeSet<>();

    public Quantizer(Object start, Object end, Object step) {
        this(
                Feature.fromPrimitiveType(start),
                Feature.fromPrimitiveType(end),
                Feature.fromPrimitiveType(step));
    }

    public Quantizer(Feature start, Feature end, Feature step) {
        if (start.sameType(end) == false || start.sameType(step) == false) {
            throw new IllegalArgumentException(
                    "All feature types must be the same");
        }

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
        if (result == null) {
            return ticks.first();
        }
        return result;
    }

    public Feature nextTick(Feature feature) {
        Feature result = ticks.higher(feature);
        //TODO this will return null when feature > highest tick, but should
        //actually return 'end'
        return result;
    /**
     * Retrieves the tick mark value preceding the given Feature. In other
     * words, this method will return the bucket before the given Feature's
     * bucket. If there is no previous tick mark (the specified Feature's bucket
     * is at the beginning of the range) then this method returns null.
     *
     * @param feature Feature to use to locate the previous tick mark bucket in
     *     the range.
     * @return Next tick mark, or null if the end of the range has been reached.
     */
    public Feature prevTick(Feature feature) {
        return ticks.lower(feature);
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
