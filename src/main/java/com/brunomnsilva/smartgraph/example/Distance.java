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

package com.brunomnsilva.smartgraph.example;

import com.brunomnsilva.smartgraph.graphview.SmartLabelSource;

/**
 * A simple class to represent a distance in an example usage of the library.
 * @author brunomnsilva
 */
public class Distance {
    private int distance;

    /**
     * Constructor for Distance instances.
     * @param distance the distance
     */
    public Distance(int distance) {
        this.distance = distance;
    }

    /**
     * Returns the distance.
     * @return the distance
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Setter for the distance.
     * @param distance the distance.
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * Establishes the text representation in the graph.
     * @return the text representation of the distance
     */
    @SmartLabelSource
    public String getDisplayDistance() {
        /* If the above annotation is not present, the toString()
        will be used as the edge label. */
        
        return distance + " km";
    }

    @Override
    public String toString() {
        return "Distance{" + "distance=" + distance + '}';
    }

}
