/* 
 * The MIT License
 *
 * Copyright 2021 brunomnsilva@gmail.com.
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
package com.brunomnsilva.smartgraph.graphview;

import java.util.Collection;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;


/**
 * Abstracts the visualized graph vertex
 *
 * @param <T> type of the underlying vertex
 *
 * @author pantape.k
 */
public interface SmartGraphVertexNode<T> extends SmartGraphVertex<T>, SmartLabelledNode {
    /**
     * Gets {@link Node}
     * @return {@link Node}
     */
    public Node getNode();
    /**
     * Gets node radius property
     * @return radius property
     */
    public DoubleProperty radiusProperty();
    /**
     * Gets node width property
     * @return width property
     */
    public DoubleProperty widthProperty();
    /**
     * Gets node height property
     * @return height property
     */
    public DoubleProperty heightProperty();
    /**
     * Gets node center x property
     * @return center x property
     */
    public DoubleProperty centerXProperty();
    /**
     * Gets node center y property
     * @return center y property
     */
    public DoubleProperty centerYProperty();
    /**
     * Gets node layout x property
     * @return layout x property
     */
    public DoubleProperty layoutXProperty();
    /**
     * Gets node layout y property
     * @return layout y property
     */
    public DoubleProperty layoutYProperty();
    /**
     * Gets node adjacent vertices
     * @return node adjacent vertices
     */
    public Set<SmartGraphVertexNode<T>> getAdjacentVertices();
    /**
     * Adds a vertex to the internal list of adjacent vertices.
     *
     * @param v vertex to add
     */    
    public void addAdjacentVertex(SmartGraphVertexNode<T> v);
    /**
     * Removes a vertex from the internal list of adjacent vertices.
     *
     * @param v vertex to remove
     * @return true if <code>v</code> existed; false otherwise.
     */    
    public boolean removeAdjacentVertex(SmartGraphVertexNode<T> v);
    /**
     * Checks whether <code>v</code> is adjacent this instance.
     *
     * @param v vertex to check
     * @return true if adjacent; false otherwise
     */
    public boolean isAdjacentTo(SmartGraphVertexNode<T> v);
    /**
     * Gets all edges that connected to this node
     * @return a set of edges ({@link  SmartGraphEdgeBase)}
     */
    public Set<SmartGraphEdgeBase> getEdges();
    /**
     * Adds edge to node
     * @param edge edge to be added
     */
    public void addEdge(SmartGraphEdgeBase edge);
    /**
     * Removes edge from this node
     * @param edge edge to be removed
     */
    public void removeEdge(SmartGraphEdgeBase edge);
    /**
     * Removes edge from this node
     * @param edges edges to be removed
     */
    public void removeEdges(Collection<SmartGraphEdgeBase> edges);
    /**
     * Gets node visible property.
     *
     * @return node visible property.
     */
    public BooleanProperty visibleProperty();

}
