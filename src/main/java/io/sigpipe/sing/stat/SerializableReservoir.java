package io.sigpipe.sing.stat;

import io.sigpipe.sing.serialization.ByteSerializable;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.SerializationOutputStream;

public class SerializableReservoir<T extends ByteSerializable>
extends Reservoir<T> implements ByteSerializable {

    @Deserialize
    public SerializableReservoir(SerializationInputStream in) {
        super(0);
    }

    @Override
    public void serialize(SerializationOutputStream out) {

    }
}
