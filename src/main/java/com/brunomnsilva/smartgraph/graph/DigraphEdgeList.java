/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2023-2024  brunomnsilva@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.brunomnsilva.smartgraph.graph;

import java.util.*;

/**
 * Implementation of a digraph that adheres to the {@link Digraph} interface.
 * <br>
 * Does not allow duplicates of stored elements through <b>equals</b> criteria.
 * <br>
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 * 
 * @author brunomnsilva
 */
public class DigraphEdgeList<V, E> implements Digraph<V, E> {

     /* inner classes are defined at the end of the class, so are the auxiliary methods 
     */
    private final Map<V, Vertex<V>> vertices;
    private final Map<E, Edge<E, V>> edges;

    /**
     * Default constructor that initializes an empty digraph.
     */
    public DigraphEdgeList() {
        this.vertices = new HashMap<>();
        this.edges = new HashMap<>();
    }

    @Override
    public synchronized Collection<Edge<E, V>> incidentEdges(Vertex<V> inbound) throws InvalidVertexException {
        checkVertex(inbound);

        List<Edge<E, V>> incidentEdges = new ArrayList<>();
        for (Edge<E, V> edge : edges.values()) {

            if (((MyEdge) edge).getInbound() == inbound) {
                incidentEdges.add(edge);
            }
        }
        return incidentEdges;
    }
    
    @Override
    public synchronized Collection<Edge<E, V>> outboundEdges(Vertex<V> outbound) throws InvalidVertexException {
        checkVertex(outbound);

        List<Edge<E, V>> outboundEdges = new ArrayList<>();
        for (Edge<E, V> edge : edges.values()) {

            if (((MyEdge) edge).getOutbound() == outbound) {
                outboundEdges.add(edge);
            }
        }
        return outboundEdges;
    }

    @Override
    public boolean areAdjacent(Vertex<V> outbound, Vertex<V> inbound) throws InvalidVertexException {
        //we allow loops, so we do not check if outbound == inbound
        checkVertex(outbound);
        checkVertex(inbound);

        /* find and edge that goes outbound ---> inbound */
        for (Edge<E, V> edge : edges.values()) {
            if (((MyEdge) edge).getOutbound() == outbound && ((MyEdge) edge).getInbound() == inbound) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized Edge<E, V> insertEdge(Vertex<V> outbound, Vertex<V> inbound, E edgeElement) throws InvalidVertexException, InvalidEdgeException {
        if (existsEdgeWith(edgeElement)) {
            throw new InvalidEdgeException("There's already an edge with this element.");
        }

        MyVertex outVertex = checkVertex(outbound);
        MyVertex inVertex = checkVertex(inbound);

        MyEdge newEdge = new MyEdge(edgeElement, outVertex, inVertex);

        edges.put(edgeElement, newEdge);

        return newEdge;
    }

    @Override
    public synchronized Edge<E, V> insertEdge(V outboundElement, V inboundElement, E edgeElement) throws InvalidVertexException, InvalidEdgeException {
        if (existsEdgeWith(edgeElement)) {
            throw new InvalidEdgeException("There's already an edge with this element.");
        }

        if (!existsVertexWith(outboundElement)) {
            throw new InvalidVertexException("No vertex contains " + outboundElement);
        }
        if (!existsVertexWith(inboundElement)) {
            throw new InvalidVertexException("No vertex contains " + inboundElement);
        }

        MyVertex outVertex = vertexOf(outboundElement);
        MyVertex inVertex = vertexOf(inboundElement);

        MyEdge newEdge = new MyEdge(edgeElement, outVertex, inVertex);

        edges.put(edgeElement, newEdge);

        return newEdge;
    }

    @Override
    public int numVertices() {
        return vertices.size();
    }

    @Override
    public int numEdges() {
        return edges.size();
    }

    @Override
    public synchronized Collection<Vertex<V>> vertices() {
        return new ArrayList<>(vertices.values());
    }

    @Override
    public synchronized Collection<Edge<E, V>> edges() {
        return new ArrayList<>(edges.values());
    }

    @Override
    public synchronized Vertex<V> opposite(Vertex<V> v, Edge<E, V> e) throws InvalidVertexException, InvalidEdgeException {
        checkVertex(v);
        MyEdge edge = checkEdge(e);

        if (!edge.contains(v)) {
            return null; /* this edge does not connect vertex v */
        }

        if (edge.vertices()[0] == v) {
            return edge.vertices()[1];
        } else {
            return edge.vertices()[0];
        }

    }

    @Override
    public synchronized Vertex<V> insertVertex(V vElement) throws InvalidVertexException {
        if (existsVertexWith(vElement)) {
            throw new InvalidVertexException("There's already a vertex with this element.");
        }

        MyVertex newVertex = new MyVertex(vElement);

        vertices.put(vElement, newVertex);

        return newVertex;
    }

    @Override
    public synchronized V removeVertex(Vertex<V> v) throws InvalidVertexException {
        checkVertex(v);

        V element = v.element();

        //remove incident edges
        Collection<Edge<E, V>> inOutEdges = incidentEdges(v);
        inOutEdges.addAll(outboundEdges(v));
        
        for (Edge<E, V> edge : inOutEdges) {
            edges.remove(edge.element());
        }

        vertices.remove(v.element());

        return element;
    }

    @Override
    public synchronized E removeEdge(Edge<E, V> e) throws InvalidEdgeException {
        checkEdge(e);

        E element = e.element();
        edges.remove(e.element());

        return element;
    }

    @Override
    public V replace(Vertex<V> v, V newElement) throws InvalidVertexException {
        if (existsVertexWith(newElement)) {
            throw new InvalidVertexException("There's already a vertex with this element.");
        }

        MyVertex vertex = checkVertex(v);

        V oldElement = vertex.element;
        vertex.element = newElement;

        return oldElement;
    }

    @Override
    public E replace(Edge<E, V> e, E newElement) throws InvalidEdgeException {
        if (existsEdgeWith(newElement)) {
            throw new InvalidEdgeException("There's already an edge with this element.");
        }

        MyEdge edge = checkEdge(e);

        E oldElement = edge.element;
        edge.element = newElement;

        return oldElement;
    }

    private MyVertex vertexOf(V vElement) {
        for (Vertex<V> v : vertices.values()) {
            if (v.element().equals(vElement)) {
                return (MyVertex) v;
            }
        }
        return null;
    }

    private boolean existsVertexWith(V vElement) {
        return vertices.containsKey(vElement);
    }

    private boolean existsEdgeWith(E edgeElement) {
        return edges.containsKey(edgeElement);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                String.format("Graph with %d vertices and %d edges:\n", numVertices(), numEdges())
        );

        sb.append("--- Vertices: \n");
        for (Vertex<V> v : vertices.values()) {
            sb.append("\t").append(v.toString()).append("\n");
        }
        sb.append("\n--- Edges: \n");
        for (Edge<E, V> e : edges.values()) {
            sb.append("\t").append(e.toString()).append("\n");
        }
        return sb.toString();
    }
    
    private class MyVertex implements Vertex<V> {

        V element;

        public MyVertex(V element) {
            this.element = element;
        }

        @Override
        public V element() {
            return this.element;
        }

        @Override
        public String toString() {
            return "Vertex{" + element + '}';
        }
    }

    private class MyEdge implements Edge<E, V> {

        E element;
        Vertex<V> vertexOutbound;
        Vertex<V> vertexInbound;

        public MyEdge(E element, Vertex<V> vertexOutbound, Vertex<V> vertexInbound) {
            this.element = element;
            this.vertexOutbound = vertexOutbound;
            this.vertexInbound = vertexInbound;
        }

        @Override
        public E element() {
            return this.element;
        }

        public boolean contains(Vertex<V> v) {
            return (vertexOutbound == v || vertexInbound == v);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Vertex<V>[] vertices() {
            Vertex<V>[] vertices = new Vertex[2];
            vertices[0] = vertexOutbound;
            vertices[1] = vertexInbound;

            return vertices;
        }

        @Override
        public String toString() {
            return "Edge{{" + element + "}, vertexOutbound=" + vertexOutbound.toString()
                    + ", vertexInbound=" + vertexInbound.toString() + '}';
        }
        
        public Vertex<V> getOutbound() {
            return vertexOutbound;
        }
        
        public Vertex<V> getInbound() {
            return vertexInbound;
        }
    }

    /**
     * Checks whether a given vertex is valid and belongs to this graph.
     *
     * @param v the vertex to check
     * @return the reference of the vertex, with cast to the underlying implementation of {@link Vertex}
     * @throws InvalidVertexException if the vertex is <code>null</code> or does not belong to this graph
     */
    private MyVertex checkVertex(Vertex<V> v) throws InvalidVertexException {
        if(v == null) throw new InvalidVertexException("Null vertex.");
        
        MyVertex vertex;
        try {
            vertex = (MyVertex) v;
        } catch (ClassCastException e) {
            throw new InvalidVertexException("Not a vertex.");
        }

        if (!vertices.containsKey(vertex.element)) {
            throw new InvalidVertexException("Vertex does not belong to this graph.");
        }

        return vertex;
    }

    private MyEdge checkEdge(Edge<E, V> e) throws InvalidEdgeException {
        if(e == null) throw new InvalidEdgeException("Null edge.");
        
        MyEdge edge;
        try {
            edge = (MyEdge) e;
        } catch (ClassCastException ex) {
            throw new InvalidVertexException("Not an adge.");
        }

        if (!edges.containsKey(edge.element)) {
            throw new InvalidEdgeException("Edge does not belong to this graph.");
        }

        return edge;
    }
    
}
