package io.sigpipe.sing.graph;

import java.io.IOException;

import io.sigpipe.sing.serialization.ByteSerializable;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.SerializationOutputStream;
import io.sigpipe.sing.stat.RunningStatistics2D;
import io.sigpipe.sing.stat.RunningStatisticsND;

public class ContainerEntry implements ByteSerializable {

    public RunningStatisticsND stats;

    public ContainerEntry() {

    }

    @Deserialize
    public ContainerEntry(SerializationInputStream in)
    throws IOException {
        //stats = new RunningStatistics2D(in);
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        //stats.serialize(out);
    }

}

