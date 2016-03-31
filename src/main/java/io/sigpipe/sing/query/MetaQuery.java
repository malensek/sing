package io.sigpipe.sing.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.sigpipe.sing.dataset.feature.FeatureType;
import io.sigpipe.sing.graph.DataContainer;
import io.sigpipe.sing.graph.Vertex;
import io.sigpipe.sing.serialization.SerializationException;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.SerializationOutputStream;

public class MetaQuery extends Query {

    public DataContainer aggregateData = new DataContainer();

    public MetaQuery() {

    }

    public DataContainer result() {
        return aggregateData;
    }

    @Override
    public void execute(Vertex root)
    throws IOException, QueryException {
        this.query(root);
    }

    private void query(Vertex vertex)
    throws IOException, QueryException {
        DataContainer container = vertex.getData();
        if (container != null) {
            this.aggregateData.merge(container);
        }

        List<Expression> expList = expressions.get(vertex.getLabel().getName());
        if (expList != null) {
            Set<Vertex> matches = evaluate(vertex, expList);
            for (Vertex match : matches) {
                if (match == null) {
                    continue;
                }

                if (match.getLabel().getType() == FeatureType.NULL) {
                    continue;
                }

                query(match);
            }
        } else {
            /* No expression operates on this vertex. Consider all children. */
            for (Vertex neighbor : vertex.getAllNeighbors()) {
                query(neighbor);
            }
        }
    }

    @Deserialize
    public MetaQuery(SerializationInputStream in)
    throws IOException, SerializationException {
        int size = in.readInt();
        this.expressions = new HashMap<>(size);
        for (int i = 0; i < size; ++i) {
            int listSize = in.readInt();
            List<Expression> expList = new ArrayList<>(listSize);
            for (int j = 0; j < listSize; ++j) {
                Expression exp = new Expression(in);
                expList.add(exp);
            }
            String featureName = expList.get(0).getOperand().getName();
            this.expressions.put(featureName, expList);
        }
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        out.writeInt(this.expressions.size());
        for (List<Expression> expList : this.expressions.values()) {
            out.writeInt(expList.size());
            for (Expression expression : expList) {
                expression.serialize(out);
            }
        }
    }

}

