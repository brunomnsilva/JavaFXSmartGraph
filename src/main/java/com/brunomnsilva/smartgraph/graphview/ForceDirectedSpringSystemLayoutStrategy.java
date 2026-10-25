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
        Args.requireInRange(acceleration, "acceleration", 0, 1);

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

        double vX = v.getUpdatedPositionX();
        double vY = v.getUpdatedPositionY();
        double wX = w.getUpdatedPositionX();
        double wY = w.getUpdatedPositionY();

        double deltaX = wX - vX;
        double deltaY = wY - vY;
        double rawDistance = Math.hypot(deltaX, deltaY);
        double distance = rawDistance - (v.getRadius() + w.getRadius());

        if (distance < 1) {
            distance = 1;
        }

        double directionX = rawDistance == 0 ? 0 : deltaX / rawDistance;
        double directionY = rawDistance == 0 ? 0 : deltaY / rawDistance;

        double totalForce = 0;
        if (v.isAdjacentTo(w)) {
            totalForce += attractionForce * Math.log(distance / attractionScale);
        }
        totalForce -= repulsiveForce * A_THOUSAND / (distance * distance);

        double accelerationFactor = totalForce * acceleration;
        return new Point2D(directionX * accelerationFactor, directionY * accelerationFactor);
    }

    @Override
    protected void addForceBetween(SmartGraphVertexNode<V> v, SmartGraphVertexNode<V> w,
                                   double panelWidth, double panelHeight) {
        // Keep this allocation-free version in sync with computeForceBetween().
        double vX = v.getUpdatedPositionX();
        double vY = v.getUpdatedPositionY();
        double wX = w.getUpdatedPositionX();
        double wY = w.getUpdatedPositionY();

        double deltaX = wX - vX;
        double deltaY = wY - vY;
        double rawDistance = Math.hypot(deltaX, deltaY);
        double distance = rawDistance - (v.getRadius() + w.getRadius());

        if (distance < 1) {
            distance = 1;
        }

        double directionX = rawDistance == 0 ? 0 : deltaX / rawDistance;
        double directionY = rawDistance == 0 ? 0 : deltaY / rawDistance;

        double totalForce = 0;
        if (v.isAdjacentTo(w)) {
            totalForce += attractionForce * Math.log(distance / attractionScale);
        }
        totalForce -= repulsiveForce * A_THOUSAND / (distance * distance);

        double accelerationFactor = totalForce * acceleration;
        double forceX = directionX * accelerationFactor;
        double forceY = directionY * accelerationFactor;

        v.addForceVector(forceX, forceY);
        w.addForceVector(-forceX, -forceY);
    }
}
