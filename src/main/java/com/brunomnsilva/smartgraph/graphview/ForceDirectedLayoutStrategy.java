/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2024  brunomnsilva@gmail.com
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
import java.util.Collection;
import java.util.List;

/**
 * A representation of a force directed layout "strategy" used during automatic layout of nodes in a {@link SmartGraphPanel}.
 * <br/>
 * Implementing classes should compute attractive and repulsive forces according to some algorithm.
 * Typically, if two graph nodes are not adjacent the force should be dominated by the repulsive force.
 * <br/>
 * See: <a href="https://en.wikipedia.org/wiki/Force-directed_graph_drawing">Wikipedia - Force-directed graph drawing</a>
 *
 * @param <V> The generic type of {@link SmartGraphVertexNode}, i.e., the nodes of a {@link SmartGraphPanel}.
 */
public abstract class ForceDirectedLayoutStrategy<V> {

    /**
     * This method must compute forces between all graph nodes. Typically, repelling forces exist between all nodes (similarly to particles
     * with the same polarity), but attractive forces only exist between adjacent nodes (nodes that are connected).
     * <br/>
     * The default behavior is to iterate over all distinct pairs of nodes and compute
     * their combined forces (attractive and repulsive), by calling {@link #computeForceBetween(SmartGraphVertexNode, SmartGraphVertexNode, double, double)}.
     * <br/>
     * Other strategies that rely on some link of global metrics should override this method.
     *
     * @param nodes       the current nodes of the graph
     * @param panelWidth    the graph panel's width
     * @param panelHeight   the graph panel's height
     */
    public void computeForces(Collection<SmartGraphVertexNode<V>> nodes, double panelWidth, double panelHeight) {
        List<SmartGraphVertexNode<V>> nodeList = (nodes instanceof List) ? (List<SmartGraphVertexNode<V>>) nodes : new ArrayList<>(nodes);
        int size = nodeList.size();

        for (int i = 0; i < size; i++) {
            SmartGraphVertexNode<V> v = nodeList.get(i);
            for (int j = i + 1; j < size; j++) {
                SmartGraphVertexNode<V> w = nodeList.get(j);

                Point2D force = computeForceBetween(v, w, panelWidth, panelHeight);
                v.addForceVector(force.getX(), force.getY());
                w.addForceVector(-force.getX(), -force.getY()); // Newton’s third law
            }
        }
    }

    /**
     * Computes a force vector between two nodes. The force vector is the result of the attractive and repulsive force between the two.
     *
     * @param v           a node
     * @param w           another node
     * @param panelWidth    the graph panel's width
     * @param panelHeight   the graph panel's height
     * @return the force vector
     */
    protected abstract Point2D computeForceBetween(SmartGraphVertexNode<V> v, SmartGraphVertexNode<V> w, double panelWidth, double panelHeight);
}
