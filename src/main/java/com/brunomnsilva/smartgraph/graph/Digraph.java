/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2019-2023  brunomnsilva@gmail.com
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

import java.util.Collection;

/**
 * A directed graph (or digraph) is a graph that is made up of a set of vertices 
 * connected by edges, where the edges have a direction associated with them.
 * <br>
 * An directed-edge leaves the <i>outbound vertex</i>
 * towards the <i>inbound vertex</i> and this changes the reasoning behind some
 * methods of the {@link Graph} interface, which are overridden in this interface
 * so as to provide different documentation of expected behavior.
 * 
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 * 
 * @see Graph
 * @see Edge
 * @see Vertex
 */
public interface Digraph<V, E> extends Graph<V, E> {
    
    /**
     * Returns a vertex's <i>incident</i> edges as a collection.
     * <br/>
     * Incident edges are all edges that have vertex <code>inbound</code> as the
     * <i>inbound vertex</i>, i.e., the edges "entering" vertex <code>inbound</code>.
     * If there are no incident edges, e.g., an isolated vertex, 
     * returns an empty collection.
     * 
     * @param inbound     vertex for which to obtain the incident edges
     * 
     * @return            collection of edges
     */
    @Override
    Collection<Edge<E, V>> incidentEdges(Vertex<V> inbound)
            throws InvalidVertexException;

    /**
     * Returns a vertex's <i>outbound</i> edges as a collection.
     * <br/>
     * Incident edges are all edges that have vertex <code>outbound</code> as the
     * <i>outbound vertex</i>, i.e., the edges "leaving" vertex <code>outbound</code>.
     * If there are no outbound edges, e.g., an isolated vertex, 
     * returns an empty collection.
     * 
     * @param outbound     vertex for which to obtain the outbound edges
     * 
     * @return            collection of edges
     */
    Collection<Edge<E, V>> outboundEdges(Vertex<V> outbound)
            throws InvalidVertexException;
    
    
    /**
     * Evaluates whether two vertices are adjacent, i.e., there exists some
     * directed-edge connecting <code>outbound</code> and <code>inbound</code>.
     * <br/>
     * The existing edge must be directed as <code>outbound --&gt; inbound</code>.
     * <br/>
     * If, for example, there exists only an edge <code>outbound &lt;-- inbound</code>,
     * they are not considered adjacent.
     * 
     * @param outbound    outbound vertex
     * @param inbound     inbound vertex
     * 
     * @return      true if they are adjacent, false otherwise.
     * 
     * @exception InvalidVertexException    if <code>outbound</code> or 
     *                                      <code>inbound</code>
     *                                      are invalid vertices for the graph
     */
    @Override
    boolean areAdjacent(Vertex<V> outbound, Vertex<V> inbound)
            throws InvalidVertexException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     * 
     * @param outbound      outbound vertex
     * @param inbound       inbound vertex
     * @param edgeElement   the element to store in the new edge
     * 
     * @return              the reference for the newly created edge
     * 
     * @exception InvalidVertexException    if <code>outbound</code> or 
     *                                      <code>inbound</code>
     *                                      are invalid vertices for the graph
     * 
     * @exception InvalidEdgeException    if there already exists an edge
     *                                      containing <code>edgeElement</code>
     *                                      according to the equality of
     *                                      {@link Object#equals(java.lang.Object)} 
     *                                      method.
     */
    @Override
    Edge<E, V> insertEdge(Vertex<V> outbound, Vertex<V> inbound, E edgeElement)
            throws InvalidVertexException, InvalidEdgeException;

    
    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     * 
     * @param outboundElement  outbound vertex's stored element 
     * @param inboundElement   inbound vertex's stored element 
     * @param edgeElement      element to store in the new edge
     * 
     * @return              the reference for the newly created edge
     * 
     * @exception InvalidVertexException    if <code>outboundElement</code> or 
     *                                      <code>inboundElement</code>
     *                                      are not found in any vertices of the graph
     *                                      according to the equality of
     *                                      {@link Object#equals(java.lang.Object) }
     *                                      method.
     * 
     * @exception InvalidEdgeException    if there already exists an edge
     *                                      containing <code>edgeElement</code>
     *                                      according to the equality of
     *                                      {@link Object#equals(java.lang.Object) }
     *                                      method.
     */
    @Override
    Edge<E, V> insertEdge(V outboundElement, V inboundElement, E edgeElement)
            throws InvalidVertexException, InvalidEdgeException;;

    
    
}
