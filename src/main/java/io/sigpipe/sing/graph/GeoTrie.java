package io.sigpipe.sing.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.sigpipe.sing.dataset.feature.Feature;

public class GeoTrie {

    private Vertex root = new Vertex();

    public GeoTrie() {

    }

    public void addHash(String geohash, CountContainer payload) {
        List<Vertex> path = hashToPath(geohash.toLowerCase());
        path.get(path.size() - 1).setData(payload);
        root.addPath(path.iterator());
    }

    public CountContainer query(String geohash) {
        List<Vertex> path = hashToPath(geohash.toLowerCase());
        CountContainer cc = new CountContainer();
        query(root, path.iterator(), cc);
        return cc;
    }

    private void query(
            Vertex vertex, Iterator<Vertex> path, DataContainer container) {
        if (path.hasNext()) {
            Vertex queryVertex = path.next();
            Vertex neighbor = vertex.getNeighbor(queryVertex.getLabel());
            if (neighbor == null) {
                /* Specified hash character wasn't found, there are no matches
                 * for this query. */
                return;
            } else {
                query(neighbor, path, container);
            }
        } else {
            for (Vertex v : vertex.getAllNeighbors()) {
                query(v, path, container);
            }
        }

        if (vertex.hasData()) {
            container.merge(vertex.getData());
        }
    }

    private List<Vertex> hashToPath(String geohash) {
        List<Vertex> path = new ArrayList<>(geohash.length());
        for (char c : geohash.toCharArray()) {
            path.add(
                    new Vertex(
                        new Feature("geohash", String.valueOf(c))));
        }
        return path;
    }

    public static void main(String[] args) throws Exception {

        GeoTrie gt = new GeoTrie();
        gt.addHash("9xqj", new CountContainer(1, 1));
        gt.addHash("9xq2", new CountContainer(1, 1));
        gt.addHash("9xd3", new CountContainer(1, 0));
        gt.addHash("djjs", new CountContainer(0, 1));
        gt.addHash("djj9", new CountContainer(1, 1));
        gt.addHash("djj2", new CountContainer(1, 1));
        CountContainer cc = gt.query("9");

        System.out.println(gt.root.numDescendants());
        System.out.println(cc.a);
        System.out.println(cc.b);
    }
}
