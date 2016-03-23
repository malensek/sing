package io.sigpipe.sing.graph;

import java.io.IOException;

import io.sigpipe.sing.serialization.ByteSerializable;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.SerializationOutputStream;

public class DataContainer implements ByteSerializable {

    public ContainerEntry entry;

    public DataContainer() {

    }

    public void merge(DataContainer container) {

    }

    public void clear() {

    }

    @Deserialize
    public DataContainer(SerializationInputStream in)
    throws IOException {

    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {

    }
}
