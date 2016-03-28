package io.sigpipe.sing.query;

import java.io.IOException;

import io.sigpipe.sing.graph.Vertex;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.SerializationOutputStream;

public class RelationalQuery implements Query {

    public RelationalQuery() {

    }

    public void addOperator() { 

    }

    public void execute(Vertex root, SerializationOutputStream out)
    throws IOException, QueryException {

    }

    @Deserialize
    public RelationalQuery(SerializationInputStream in)
    throws IOException {

    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {

    }

}
