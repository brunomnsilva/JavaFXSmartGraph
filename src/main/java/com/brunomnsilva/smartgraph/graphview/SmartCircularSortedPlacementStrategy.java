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

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Places vertices around a circle, ordered by the underlying
 * vertices {@code element.toString() value}.
 * 
 * @see SmartPlacementStrategy
 * 
 * @author brunomnsilva
 */
public class SmartCircularSortedPlacementStrategy implements SmartPlacementStrategy {

    private static final int RADIUS_PADDING = 4;

    @Override
    public <V, E> void place(double width, double height, SmartGraphPanel<V, E> smartGraphPanel) {
        // Sort vertices by their label
        List<SmartGraphVertex<V>> vertices = new ArrayList<>(smartGraphPanel.getSmartVertices());

        vertices.sort((v1, v2) -> {
            V e1 = v1.getUnderlyingVertex().element();
            V e2 = v2.getUnderlyingVertex().element();
            return smartGraphPanel.getVertexLabelFor(e1).compareTo(smartGraphPanel.getVertexLabelFor(e2));
        });

        //place first vertex at north position, others in clockwise manner
        Point2D center = new Point2D(width / 2, height / 2);
        int N = vertices.size();
        double angleIncrement = -360f / N;
        boolean first = true;
        Point2D p = null;

        for (SmartGraphVertex<V> vertex : vertices) {
            
            if (first) {
                //verify the smallest width and height.
                if(width > height)
                    p = new Point2D(center.getX(),
                            center.getY() - height / 2 + vertex.getRadius() * RADIUS_PADDING);
                else
                    p = new Point2D(center.getX(),
                            center.getY() - width / 2 + vertex.getRadius() * RADIUS_PADDING);

                first = false;
            } else {
                p = UtilitiesPoint2D.rotate(p, center, angleIncrement);
            }

            vertex.setPosition(p.getX(), p.getY());
        }
    }

}
