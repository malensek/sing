package io.sigpipe.sing.query;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.sigpipe.sing.dataset.feature.FeatureType;
import io.sigpipe.sing.graph.Vertex;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.SerializationOutputStream;

public class RelationalQuery extends Query {

    private Set<Vertex> pruned;

    public RelationalQuery() {

    }

    @Override
    public void execute(Vertex root)
    throws IOException, QueryException {
        this.pruned = new HashSet<Vertex>();
        System.out.println("expressions: " + expressions.size());
        prune(root, 0);
        System.out.println("Pruned " + pruned.size() + " vertices");
    }

    private void prune(Vertex vertex, int expressionsEvaluated)
    throws QueryException {
        if (expressionsEvaluated == this.expressions.size()) {
            return;
        }

        String childFeature = vertex.getFirstNeighbor().getLabel().getName();
        List<Expression> expList = this.expressions.get(childFeature);
        if (expList != null) {
            Set<Vertex> allNeighbors = new HashSet<>(vertex.getAllNeighbors());
            Set<Vertex> matches = evaluate(vertex, expList);
            for (Vertex match : matches) {
                if (match == null) {
                    continue;
                }

                if (match.getLabel().getType() == FeatureType.NULL) {
                    continue;
                }

                prune(match, expressionsEvaluated + 1);
            }

            allNeighbors.removeAll(matches);
            for (Vertex nonMatch : allNeighbors) {
                pruned.add(nonMatch);
            }
        } else {
            /* No expression operates on this vertex. Consider all children. */
            for (Vertex neighbor : vertex.getAllNeighbors()) {
                prune(neighbor, expressionsEvaluated);
            }
        }
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
