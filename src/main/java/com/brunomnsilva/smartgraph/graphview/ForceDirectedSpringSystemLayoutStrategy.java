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

/**
 * An implementation of a spring system layout strategy. This strategy allows to freely move the graph along
 * the panel, but if you have a bipartite graph, the sub-graphs will repel each other to the edges of the panel.
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
 *
 * @param <V> The generic type of {@link SmartGraphVertexNode}, i.e., the nodes of a {@link SmartGraphPanel}.
 */
public class ForceDirectedSpringSystemLayoutStrategy<V> extends ForceDirectedLayoutStrategy<V> {

    private final double repulsiveForce;
    private final double attractionForce;
    private final double attractionScale;
    private final double acceleration;

    /* just a scaling factor so all parameters are, at most, two-digit numbers. */
    private static final double A_THOUSAND = 1000;

    private static final double MIN_DISTANCE = 1; // in pixels, between vertex boundaries
    private static final double MAX_REPULSIVE_FACTOR = 50;
    private static final double OVERLAP_THRESHOLD = 5; // in pixels

    /**
     * Constructs a new instance of ForceDirectedSpringGravityLayoutStrategy with default parameters, namely:
     * <br/>
     * repulsiveForce = 25, attractionForce = 3, attractionScale = 10 and acceleration = 0.8.
     */
    public ForceDirectedSpringSystemLayoutStrategy() {
        this.repulsiveForce = 25;
        this.attractionForce = 3;
        this.attractionScale = 10;
        this.acceleration = 0.8;
    }

    /**
     * Constructs a new instance of ForceDirectedSpringGravityLayoutStrategy with the specified parameters.
     *
     * @param repulsiveForce The strength of the repulsive force between nodes. Higher values result in greater repulsion.
     * @param attractionForce The strength of the attractive force between connected nodes. Higher values result in stronger attraction.
     * @param attractionScale The scale factor for attraction. It determines the effectiveness of the attraction force based on the distance between connected nodes.
     * @param acceleration The acceleration factor applied to node movements. Higher values result in faster movements.
     */
    public ForceDirectedSpringSystemLayoutStrategy(double repulsiveForce, double attractionForce, double attractionScale, double acceleration) {
        Args.requireGreaterThan(repulsiveForce, "repulsiveForce", 0);
        Args.requireGreaterThan(attractionForce, "attractionForce", 0);
        Args.requireGreaterThan(attractionScale, "attractionScale", 0);
        Args.requireGreaterThan(acceleration, "acceleration", 0);

        this.repulsiveForce = repulsiveForce;
        this.attractionForce = attractionForce;
        this.attractionScale = attractionScale;
        this.acceleration = acceleration;
    }

    @Override
    protected Point2D computeForceBetween(SmartGraphVertexNode<V> v, SmartGraphVertexNode<V> w, double panelWidth, double panelHeight) {
        // The panel's width and height are not used in this strategy
        // This allows to freely move the graph to a particular region in the panel;
        // On the other hand, e.g., in a bipartite graph the two sub-graphs will repel each other to the edges of the panel

        Point2D vPosition = v.getUpdatedPosition();
        Point2D wPosition = w.getUpdatedPosition();

        double distance = vPosition.distance(wPosition) - (v.getRadius() + w.getRadius());
        distance = Math.max(distance, MIN_DISTANCE);

        Point2D forceDirection = wPosition.subtract(vPosition).normalize();

        double attractionFactor = 0;
        if (v.isAdjacentTo(w)) {
            attractionFactor = attractionForce * Math.log(distance / attractionScale);
        }
        Point2D attraction = forceDirection.multiply(attractionFactor);

        double repulsiveFactor = (distance < OVERLAP_THRESHOLD)
                ? repulsiveForce * (OVERLAP_THRESHOLD - distance)    // Linear when overlapping
                : repulsiveForce * A_THOUSAND / (distance * distance);

        repulsiveFactor = Math.min(repulsiveFactor, MAX_REPULSIVE_FACTOR); // Cap
        Point2D repulsion = forceDirection.multiply(-repulsiveFactor);

        return attraction.add(repulsion).multiply(acceleration); // total force
    }
}
