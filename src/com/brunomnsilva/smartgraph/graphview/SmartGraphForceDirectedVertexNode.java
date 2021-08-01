/* 
 * The MIT License
 *
 * Copyright 2021 pantape.k@gmail.com.
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
import javafx.scene.shape.Circle;
import com.brunomnsilva.smartgraph.graph.Vertex;
import javafx.scene.Node;

/**
 * An adaptation implementation from {@link SmartGraphVertexNode} class.
 * <br>
 * Rather than visualize a vertex just as a {@link Circle}, this will enable any
 * {@link Node} be a vertex node.
 * <br>
 * The vertex internally deals with mouse drag events that visually move it in
 * the {@link SmartGraphPanel} when displayed, if parameterized to do so.
 *
 *
 *
 * @param <T> the type of the underlying vertex
 *
 * @see SmartGraphPanel
 *
 * @author brunomnsilva
 */
public class SmartGraphForceDirectedVertexNode<T> extends SmartGraphVertexNodeBasic<T> {

    /*
    Automatic layout functionality members
     */
    private final PointVector forceVector = new PointVector(0, 0);
    private final PointVector updatedPosition = new PointVector(0, 0);


    /**
     * Constructor which sets the instance attributes and having the specified
     * {@link Node} as a vertex node
     *
     * @param v the underlying vertex
     * @param allowMove should the vertex able to be dragged with the mouse
     */
    public SmartGraphForceDirectedVertexNode(Vertex<T> v, boolean allowMove) {
        super(v, allowMove);
    }

    /**
     * Resets the current computed external force vector.
     *
     */
    public void resetForces() {
        this.forceVector.x = this.forceVector.y = 0;
        this.updatedPosition.x = this.getPosition().getX();
        this.updatedPosition.y = this.getPosition().getY();
    }

    /**
     * Adds the vector represented by <code>(x,y)</code> to the current external
     * force vector.
     *
     * @param x x-component of the force vector
     * @param y y-component of the force vector
     *
     */
    public void addForceVector(double x, double y) {
        this.forceVector.x += x;
        this.forceVector.y += y;
    }

    /**
     * Returns the current external force vector.
     *
     * @return force vector
     */
    public Point2D getForceVector() {
        return new Point2D(this.forceVector.x, this.forceVector.y);
    }

    /**
     * Returns the future position of the vertex.
     *
     * @return future position
     */
    public Point2D getUpdatedPosition() {
        return new Point2D(this.updatedPosition.x, this.updatedPosition.y);
    }

    /**
     * Updates the future position according to the current internal force
     * vector.
     *
     * @see SmartGraphPanel#updateForces()
     */
    public void updateDelta() {
        this.updatedPosition.x += this.forceVector.x;
        this.updatedPosition.y += this.forceVector.y;
    }

    /**
     * Moves the vertex position to the computed future position.
     * <p>
     * Moves are constrained within the parent pane dimensions.
     *
     * @see SmartGraphPanel#applyForces()
     */
    public void moveFromForces() {
        //limit movement to parent bounds
        double height = this.node.getParent().getLayoutBounds().getHeight();
        double width = this.node.getParent().getLayoutBounds().getWidth();

        this.updatedPosition.x = this.boundCenterCoordinate(this.updatedPosition.x, 0, width);
        this.updatedPosition.y = boundCenterCoordinate(this.updatedPosition.y, 0, height);

        this.setPosition(this.updatedPosition.x, this.updatedPosition.y);
    }
}
