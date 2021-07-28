/*
 * The MIT License
 *
 * Copyright 2019 pantape.k@gmail.com.
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
import java.util.Random;
import com.brunomnsilva.smartgraph.graph.Graph;

/**
 * Scatters the vertices randomly.
 *
 * @see SmartPlacementStrategy
 *
 * @author pantape.k@gmail.com
 */
public class SmartRandomNearCenterPlacementStrategy implements SmartPlacementStrategy {

    @Override
    public <V, E> void place(double width, double height, Graph<V, E> theGraph, Collection<? extends SmartGraphVertex<V>> vertices) {

        Random rand = new Random();
        int maxRadius = (int)(width < height ? width : height)/4;
        for (SmartGraphVertex<V> vertex : vertices) {
            int radius = rand.nextInt(maxRadius);
            int angle = (int)(2 * Math.PI * rand.nextDouble());
            int x = (int)(width / 2 + radius * Math.cos(angle));
            int y = (int)(height / 2 + radius * Math.sin(angle));
            vertex.setPosition(x, y);
        }
    }

}
