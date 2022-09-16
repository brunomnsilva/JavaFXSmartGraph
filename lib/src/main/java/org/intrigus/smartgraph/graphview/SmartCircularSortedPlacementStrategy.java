/*
 * The MIT License
 *
 * Copyright 2019 brunomnsilva.
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
package org.intrigus.smartgraph.graphview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.intrigus.smartgraph.graph.Graph;

import javafx.geometry.Point2D;

/**
 * Places vertices around a circle, ordered by the underlying
 * vertices {@code element.toString() value}.
 * 
 * @see SmartPlacementStrategy
 * 
 * @author brunomnsilva
 */
public class SmartCircularSortedPlacementStrategy implements SmartPlacementStrategy {

    @Override
    public <V, E> void place(double width, double height, Graph<V, E> theGraph, Collection<? extends SmartGraphVertex<V>> vertices) {
        Point2D center = new Point2D(width / 2, height / 2);
        int N = vertices.size();
        double angleIncrement = -360f / N;
        
        //place first vertice north position, others in clockwise manner
        boolean first = true;
        Point2D p = null;
        for (SmartGraphVertex<V> vertex : sort(vertices)) {
            
            if (first) {
                //verifiy smaller width and height.
                if(width > height)
                    p = new Point2D(center.getX(),
                            center.getY() - height / 2 + vertex.getRadius() * 2);
                else
                    p = new Point2D(center.getX(),
                            center.getY() - width / 2 + vertex.getRadius() * 2);
                
        
                first = false;
            } else {
                p = UtilitiesPoint2D.rotate(p, center, angleIncrement);
            }

            vertex.setPosition(p.getX(), p.getY());
            
        }
    }
    
    protected <V> Collection<SmartGraphVertex<V>> sort(Collection<? extends SmartGraphVertex<V>> vertices) {
        
        List<SmartGraphVertex<V>> list = new ArrayList<>();
        list.addAll(vertices);
        
        Collections.sort(list, new Comparator<SmartGraphVertex<V>>() {
            @Override
            public int compare(SmartGraphVertex<V> t, SmartGraphVertex<V> t1) {
                return t.getUnderlyingVertex().element().toString().compareToIgnoreCase(t1.getUnderlyingVertex().element().toString());
            }
        });
        
        return list;
    }
}
