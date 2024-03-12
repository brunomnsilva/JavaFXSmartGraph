/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2019-2024  brunomnsilva@gmail.com
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

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;

/**
 * Abstracts the internal representation and behavior of a visualized graph vertex.
 * 
 * @param <V> Type stored in the underlying vertex
 * 
 * @see SmartGraphPanel
 * 
 * @author brunomnsilva
 */
public interface SmartGraphVertex<V> extends SmartStylableNode {
    
    /**
     * Returns the underlying (stored reference) graph vertex.
     * 
     * @return vertex reference 
     * 
     * @see Graph
     */
    Vertex<V> getUnderlyingVertex();
    
    /**
     * Sets the position of this vertex in panel coordinates. 
     * <br/>
     * Apart from its usage in the {@link SmartGraphPanel}, this method
     * should only be called when implementing {@link SmartPlacementStrategy}.
     * 
     * @param x     x-coordinate for the vertex
     * @param y     y-coordinate for the vertex
     */
    void setPosition(double x, double y);
    
    /**
     * Return the center x-coordinate of this vertex in panel coordinates.
     * 
     * @return     x-coordinate of the vertex 
     */
    double getPositionCenterX();
    
    /**
     * Return the center y-coordinate of this vertex in panel coordinates.
     * 
     * @return     y-coordinate of the vertex 
     */
    double getPositionCenterY();
    
    /**
     * Returns the circle radius used to represent this vertex.
     * 
     * @return      circle radius
     */
    double getRadius();
    
    /**
     * Returns the label node for further styling.
     * 
     * @return the label node.
     */
    SmartStylableNode getStylableLabel();
}
