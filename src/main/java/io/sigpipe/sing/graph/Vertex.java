/*
Copyright (c) 2013, Colorado State University
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

package io.sigpipe.sing.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * Provides a lightweight generic implementation of a graph vertex backed by a
 * TreeMap for extensibility.  This provides the basis of the hybrid
 * trees/graphs used in the system. Vertices store labels that extend the
 * Comparable interface to ensure they can be ordered properly in the TreeMap.
 *
 * @author malensek
 */
public class Vertex<L extends Comparable<L>> {

    protected L label;
    protected TreeMap<L, Vertex<L>> edges = new TreeMap<>();

    public Vertex() { }

    public Vertex(L label) {
        this.label = label;
    }

    public Vertex(Vertex<L> v) {
        this.label = v.label;
    }

    /**
     * Determines whether two vertices are connected.
     *
     * @param label the label of the vertex to search for
     * @return true if the Vertex label is found on a connecting edge.
     */
    public boolean connectedTo(L label) {
        return edges.containsKey(label);
    }

    /**
     * Determines whether two vertices are connected.
     *
     * @param v the vertex to search for
     * @return true if the Vertex is found on a connecting edge.
     */
    public boolean connectedTo(Vertex<L> v) {
        return edges.containsValue(v);
    }

    /**
     * Retrieve a neighboring Vertex.
     *
     * @param label Neighbor's label.
     * @return Neighbor Vertex.
     */
    public Vertex<L> getNeighbor(L label) {
        return edges.get(label);
    }

    /**
     * Retrieves the {@link NavigableMap} of neighboring vertices less than the
     * specified value.
     *
     * @param label label value to compare against
     * @param inclusive whether or not to include the label's value while doing
     *     comparisons
     * @return {@link NavigableMap} of neighboring vertices
     */
    public NavigableMap<L, Vertex<L>> getNeighborsLessThan(
            L label, boolean inclusive) {
        return edges.headMap(label, inclusive);
    }

    /**
     * Retrieves the {@link NavigableMap} of neighboring vertices greater than
     * the specified value.
     *
     * @param label label value to compare against
     * @param inclusive whether or not to include the label's value while doing
     *     comparisons
     * @return {@link NavigableMap} of neighboring vertices
     */
    public NavigableMap<L, Vertex<L>> getNeighborsGreaterThan(
            L label, boolean inclusive) {
        return edges.tailMap(label, inclusive);
    }

    /**
     * Retrieves the {@link NavigableMap} of neighboring vertices within the
     * range specified.
     *
     * @param from the beginning of the range (inclusive)
     * @param to the end of the range (exclusive)
     * @return {@link NavigableMap} of neighboring vertices in the specified
     *     range
     */
    public NavigableMap<L, Vertex<L>> getNeighborsInRange(
            L from, L to) {

        return getNeighborsInRange(from, true, to, false);
    }

    /**
     * Retrieves the {@link NavigableMap} of neighboring vertices within the
     * range specified.
     *
     * @param from the beginning of the range
     * @param fromInclusive whether to include 'from' in the range of values
     * @param to the end of the range (exclusive)
     * @param toInclusive whether to include 'to' in the range of values
     * @return {@link NavigableMap} of neighboring vertices in the specified
     *     range
     */
    public NavigableMap<L, Vertex<L>> getNeighborsInRange(
            L from, boolean fromInclusive, L to, boolean toInclusive) {

        return edges.subMap(from, fromInclusive, to, toInclusive);
    }

    /**
     * Retrieve the labels of all neighboring vertices.
     *
     * @return Neighbor Vertex labels.
     */
    public Set<L> getNeighborLabels() {
        return edges.keySet();
    }

    /**
     * Traverse all edges to return all neighboring vertices.
     *
     * @return collection of all neighboring vertices.
     */
    public Collection<Vertex<L>> getAllNeighbors() {
        return edges.values();
    }

    /**
     * Connnects two vertices.  If this vertex is already connected to the
     * provided vertex label, then the already-connected vertex is returned.
     *
     * @param vertex The vertex to connect to.
     * @return Connected vertex.
     */
    public Vertex<L> connect(Vertex<L> vertex) {
        L label = vertex.getLabel();
        Vertex<L> neighbor = getNeighbor(label);
        if (neighbor == null) {
            edges.put(label, vertex);
            return vertex;
        } else {
            return neighbor;
        }
    }

    /**
     * Removes all the edges from this Vertex, severing any connections with
     * neighboring vertices.
     */
    public void disconnectAll() {
        edges.clear();
    }

    /**
     * Add and connect a collection of vertices in the form of a traversal path.
     */
    public void addPath(Iterator<Vertex<L>> path) {
        if (path.hasNext()) {
            Vertex<L> vertex = path.next();
            Vertex<L> edge = connect(vertex);
            edge.addPath(path);
        }
    }

    /**
     * Retrieves the label associated with this vertex.
     */
    public L getLabel() {
        return label;
    }

    /**
     * Retrieves the number of descendant vertices for this {@link Vertex}.
     *
     * @return number of descendants (children)
     */
    public long numDescendants() {
        long total = this.getAllNeighbors().size();
        for (Vertex<L> child : this.getAllNeighbors()) {
            total += child.numDescendants();
        }

        return total;
    }

    /**
     * Retrieves the number of descendant edges for this {@link Vertex}.
     *
     * @return number of descendant edges.
     */
    public long numDescendantEdges() {
        long total = this.getAllNeighbors().size();
        for (Vertex<L> child : this.getAllNeighbors()) {
            total += child.numDescendantEdges();
        }

        return total;
    }

    @Override
    public String toString() {
        return "V: [" + label.toString() + "] "
            + "(" + this.getAllNeighbors().size() + ")";
    }
}
