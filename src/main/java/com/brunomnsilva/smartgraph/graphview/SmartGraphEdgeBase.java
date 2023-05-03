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

/**
 * Used as a super-type for all different concrete edge implementations by
 * {@link SmartGraphPanel}, e.g., line and curves.
 * <br>
 * An edge can have an attached arrow.
 * 
 * @param <E> Type stored in the underlying edge
 * @param <V> Type of connecting vertex
 * 
 * @see SmartArrow
 * @see SmartGraphEdge
 * @see SmartLabelledNode
 * @see SmartGraphPanel
 * 
 * @author brunomnsilva
 */
public interface SmartGraphEdgeBase<E, V> extends SmartGraphEdge<E, V>, SmartLabelledNode {
    
    /**
     * Attaches a {@link SmartArrow} to this edge, binding its position/rotation.
     * 
     * @param arrow     arrow to attach
     */
    void attachArrow(SmartArrow arrow);
    
    /**
     * Returns the attached {@link SmartArrow}, if any.
     * 
     * @return      reference of the attached arrow; null if none.
     */
    SmartArrow getAttachedArrow();
    
}
