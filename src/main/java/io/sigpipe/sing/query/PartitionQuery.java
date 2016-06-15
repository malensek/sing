/*
Copyright (c) 2016, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package io.sigpipe.sing.query;

import java.io.IOException;
import java.util.Iterator;

import io.sigpipe.sing.graph.Vertex;
import io.sigpipe.sing.serialization.SerializationOutputStream;

/**
 * Based on a {@RelationalQuery}, this query selects portions of the graph for
 * removal and transfer to another node or graph instance. This query physically
 * removes vertices (if possible).
 *
 * @author malensek
 */
public class PartitionQuery extends RelationalQuery {

    @Override
    public void serializeResults(
            Vertex vertex, SerializationOutputStream out)
    throws IOException {
        serializeAndDeleteResults(vertex, out);
    }

    /**
     * @return true if the Vertex this method was called on can be deleted;
     * Vertices are deletable if the query matched it, AND all of its children.
     */
    private boolean serializeAndDeleteResults(
            Vertex vertex, SerializationOutputStream out)
    throws IOException {
        if (pruned.contains(vertex)) {
            /* A pruned (non-matching) vertex cannot be deleted */
            return false;
        }

        vertex.getLabel().serialize(out);
        out.writeBoolean(vertex.hasData());
        if (vertex.hasData() == true) {
            vertex.getData().serialize(out);
        }

        /* How many neighbors are still valid after the pruning process? */
        int validNeighbors = 0;
        for (Vertex v : vertex.getAllNeighbors()) {
            if (pruned.contains(v) == false) {
                validNeighbors++;
            }
        }
        out.writeInt(validNeighbors);

        boolean deletable = true;
        if (validNeighbors != vertex.numNeighbors()) {
            /* If some of this vertex's children didn't match the query, it
             * cannot be deleted */
            deletable = false;
        }

        Iterator<Vertex> it = vertex.getAllNeighbors().iterator();
        while (it.hasNext()) {
            Vertex v = it.next();
            if (pruned.contains(v) == false) {
                if (serializeAndDeleteResults(v, out) == true) {
                    if (this.metrics != null) {
                        this.metrics.removeVertex();
                        if (v.hasData()) {
                            this.metrics.removeLeaf();
                        }
                    }
                    it.remove();
                } else {
                    deletable = false;
                }
            }
        }

        return deletable;
    }
}

