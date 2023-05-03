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
package com.brunomnsilva.smartgraph.graphview;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;

/**
 * A graph edge visually connects two {@link Vertex} of type <code>V</code>.
 * <br>
 * Concrete edge implementations used by {@link SmartGraphPanel} should
 * implement this interface as this type is the only one exposed to the user.
 * 
 * @param <E> Type stored in the underlying edge
 * @param <V> Type of connecting vertex
 *
 * @see Vertex
 * @see SmartGraphPanel
 * 
 * @author brunomnsilva
 */
public interface SmartGraphEdge<E, V> extends SmartStylableNode {
    
     /**
     * Returns the underlying (stored reference) graph edge.
     * 
     * @return edge reference 
     * 
     * @see SmartGraphPanel
     */
    Edge<E, V> getUnderlyingEdge();
    
    /**
     * Returns the attached arrow of the edge, for styling purposes.
     * <br/>
     * The arrows are only used with directed graphs.
     * 
     * @return arrow reference; null if does not exist.
     */
    SmartStylableNode getStylableArrow();
    
    /**
     * Returns the label node for further styling.
     * 
     * @return the label node.
     */
    SmartStylableNode getStylableLabel();
}
