package io.sigpipe.sing.graph;

public class GraphMetrics implements Cloneable {

    private long vertices;
    private long leaves;

    public GraphMetrics() {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public GraphMetrics(int vertices, int leaves) {
        this.vertices = vertices;
        this.leaves = leaves;
    }

    public void setVertexCount(long vertices) {
        this.vertices = vertices;
    }

    public void setLeafCount(long leaves) {
        this.leaves = leaves;
    }

    public void addVertex() {
        this.vertices++;
    }

    public void addVertices(long vertices) {
        this.vertices += vertices;
    }

    public void addLeaf() {
        this.leaves++;
    }

    public void addLeaves(long leaves) {
        this.leaves += leaves;
    }

    public long getVertexCount() {
        return this.vertices;
    }

    public long getLeafCount() {
        return this.leaves;
    }

    public String toString() {
        return "V: " + this.vertices + ", L: " + this.leaves;
    }

}
