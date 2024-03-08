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

import java.util.Collection;

/**
 * An implementation of a spring system layout strategy with gravity towards the center.
 * <br/>
 * Applies the same spring system as {@link ForceDirectedSpringSystemLayoutStrategy} but with added gravitational pull
 * towards the center of the panel. Even with bipartite graphs, they will not repel each other to the edges of the panel.
 * <br/>
 * Parameters:
 * <br/>
 * Repulsive force (> 0): Recommended [1, 50]. Default 25. The strength of the repulsive force between nodes.
 * Higher values result in greater repulsion.
 * <br/>
 * Attraction force (> 0): Recommended [1, 5]. Default 3. The strength of the attractive force between connected nodes.
 * Higher values result in stronger attraction. Careful, because larger values may not produce stable states.
 * <br/>
 * Attraction scale (> 0): Recommended [1, ?]. Default 10. The scale factor for attraction.
 * It determines the effectiveness of the attraction force based on the distance between connected nodes.
 * <br/>
 * Acceleration: Mandatory ]0, 1]. Default 0.8. The acceleration factor applied to node movements.
 * Higher values result in faster movements.
 * <br/>
 * Gravity (> 0): Recommended ]0, 0.1]. Default 0.01. The higher the value, the more dominant the gravitation "pull" is.
 * Careful, because larger values may not produce stable states. The gravity force is applied after
 * the attraction/repulsive forces between nodes are computed. We don't want this force to dominate the layout placement.
 *
 * @param <V> The generic type of {@link SmartGraphVertexNode}, i.e., the nodes of a {@link SmartGraphPanel}.
 */
public class ForceDirectedSpringGravityLayoutStrategy<V> extends ForceDirectedSpringSystemLayoutStrategy<V> {

    private final double gravity;

    /**
     * Constructs a new instance of ForceDirectedSpringGravityLayoutStrategy with default parameters, namely:
     * <br/>
     * repulsiveForce = 25, attractionForce = 3, attractionScale = 10, acceleration = 0.8 and gravity = 0.01.
     */
    public ForceDirectedSpringGravityLayoutStrategy() {
        super();
        this.gravity = 0.01;
    }

    /**
     * Constructs a new instance of ForceDirectedSpringGravityLayoutStrategy with the specified parameters.
     *
     * @param repulsiveForce The strength of the repulsive force between nodes. Higher values result in greater repulsion.
     * @param attractionForce The strength of the attractive force between connected nodes. Higher values result in stronger attraction.
     * @param attractionScale The scale factor for attraction. It determines the effectiveness of the attraction force based on the distance between connected nodes.
     * @param acceleration The acceleration factor applied to node movements. Higher values result in faster movements.
     * @param gravity The strength of the gravity force applied to all nodes, attracting them towards the center of the layout area.
     */
    public ForceDirectedSpringGravityLayoutStrategy(double repulsiveForce, double attractionForce, double attractionScale,
                                                    double acceleration, double gravity) {
        super(repulsiveForce, attractionForce, attractionScale, acceleration);

        Args.requireGreaterThan(gravity, "gravity", 0);
        Args.requireInRange(gravity, "gravity", 0, 1);
        this.gravity = gravity;
    }

    @Override
    public void computeForces(Collection<SmartGraphVertexNode<V>> nodes, double panelWidth, double panelHeight) {
        // Attractive and repulsive forces
        for (SmartGraphVertexNode<V> v : nodes) {
            for (SmartGraphVertexNode<V> w : nodes) {
                if(v == w) continue;

                Point2D force = computeForceBetween(v, w, panelWidth, panelHeight);
                v.addForceVector(force.getX(), force.getY());
            }
        }

        // Gravitational pull towards the center for all nodes
        double centerX = panelWidth / 2;
        double centerY = panelHeight / 2;

        for (SmartGraphVertexNode<V> v : nodes) {
            Point2D curPosition = v.getUpdatedPosition();
            Point2D forceCenter = new Point2D(centerX - curPosition.getX(), centerY - curPosition.getY())
                    .multiply(gravity);

            v.addForceVector(forceCenter.getX(), forceCenter.getY());
        }
    }
}
