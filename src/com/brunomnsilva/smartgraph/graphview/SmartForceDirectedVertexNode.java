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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import com.brunomnsilva.smartgraph.graph.Vertex;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

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
public class SmartForceDirectedVertexNode<T> extends StackPane implements SmartGraphVertex<T>, SmartLabelledNode {

    private final Vertex<T> underlyingVertex;
    /* Critical for performance, so we don't rely on the efficiency of the Graph.areAdjacent method */
    private final Set<SmartForceDirectedVertexNode<T>> adjacentVertices;

    private SmartLabel attachedLabel = null;
    private boolean isDragging = false;

    private DoubleProperty radiusProperty;

    /*
    Automatic layout functionality members
     */
    private final PointVector forceVector = new PointVector(0, 0);
    private final PointVector updatedPosition = new PointVector(0, 0);

    /* Styling proxy */
    private final SmartStyleProxy styleProxy;

    /**
     * Constructor which sets the instance attributes.
     *
     * @param v the underlying vertex
     * @param allowMove should the vertex able to be dragged with the mouse
     */
    public SmartForceDirectedVertexNode(Vertex<T> v, boolean allowMove) {
        this.underlyingVertex = v;
        this.attachedLabel = null;
        this.isDragging = false;

        this.radiusProperty = new SimpleDoubleProperty();
        this.radiusProperty.set(20);
//        this.radiusProperty.bind(Bindings
//                .when(this.widthProperty().greaterThan(this.heightProperty()))
//                .then(this.widthProperty().divide(2))
//                .otherwise(this.heightProperty().divide(2)));

        this.adjacentVertices = new HashSet<>();

        styleProxy = new SmartStyleProxy(this);
        styleProxy.addStyleClass("vertex");

        if (allowMove) {
            this.enableDrag();
        }
    }

    /**
     * Adds a vertex to the internal list of adjacent vertices.
     *
     * @param v vertex to add
     */
    public void addAdjacentVertex(SmartForceDirectedVertexNode<T> v) {
        this.adjacentVertices.add(v);
    }

    /**
     * Removes a vertex from the internal list of adjacent vertices.
     *
     * @param v vertex to remove
     * @return true if <code>v</code> existed; false otherwise.
     */
    public boolean removeAdjacentVertex(SmartForceDirectedVertexNode<T> v) {
        return this.adjacentVertices.remove(v);
    }

    /**
     * Removes a collection of vertices from the internal list of adjacent
     * vertices.
     *
     * @param col collection of vertices
     * @return true if any vertex was effectively removed
     */
    public boolean removeAdjacentVertices(Collection<SmartForceDirectedVertexNode<T>> col) {
        return this.adjacentVertices.removeAll(col);
    }

    /**
     * Checks whether <code>v</code> is adjacent this instance.
     *
     * @param v vertex to check
     * @return true if adjacent; false otherwise
     */
    public boolean isAdjacentTo(SmartForceDirectedVertexNode<T> v) {
        return this.adjacentVertices.contains(v);
    }

    /**
     * Returns the current position of the instance in pixels.
     *
     * @return the x,y coordinates in pixels
     */
    public Point2D getPosition() {
        return new Point2D(this.getLayoutX(), this.getLayoutY());
    }

    /**
     * Sets the position of the instance in pixels.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    @Override
    public void setPosition(double x, double y) {
        if (isDragging) {
            return;
        }

        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    @Override
    public double getPositionCenterX() {
        return this.getPosition().getX();// + this.getWidth() / 2;
    }

    @Override
    public double getPositionCenterY() {
        return this.getPosition().getY();// + this.getHeight() / 2;
    }

    /**
     * Sets the position of the instance in pixels.
     *
     * @param p coordinates
     */
    public void setPosition(Point2D p) {
        this.setPosition(p.getX(), p.getY());
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
        double height = this.getParent().getLayoutBounds().getHeight();
        double width = this.getParent().getLayoutBounds().getWidth();

        this.updatedPosition.x = this.boundCenterCoordinate(this.updatedPosition.x, 0, width);
        this.updatedPosition.y = boundCenterCoordinate(this.updatedPosition.y, 0, height);

        this.setPosition(this.updatedPosition.x, this.updatedPosition.y);
    }

    /**
     * Make a node movable by dragging it around with the mouse primary button.
     */
    private void enableDrag() {
        final PointVector dragDelta = new PointVector(0, 0);

        this.setOnMousePressed((MouseEvent mouseEvent) -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = mouseEvent.getX();
                dragDelta.y = mouseEvent.getY();
                this.isDragging = true;

                mouseEvent.consume();
            }

        });

        this.setOnMouseReleased((MouseEvent mouseEvent) -> {
            this.getScene().setCursor(Cursor.HAND);
            this.isDragging = false;

            mouseEvent.consume();
        });

        this.setOnDragDetected((MouseEvent mouseEvent) -> {
            this.getScene().setCursor(Cursor.MOVE);
        });

        this.setOnMouseDragged((MouseEvent mouseEvent) -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                double newX = this.getLayoutX() + mouseEvent.getX() - dragDelta.x;
                double newY = this.getLayoutY() + mouseEvent.getY() - dragDelta.y;

                double x = this.boundCenterCoordinate(newX, 0, getParent().getLayoutBounds().getWidth());
                double y = this.boundCenterCoordinate(newY, 0, getParent().getLayoutBounds().getHeight());

                this.setLayoutX(x);
                this.setLayoutY(y);

                mouseEvent.consume();
            }

        });

        setOnMouseEntered((MouseEvent mouseEvent) -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                this.getScene().setCursor(Cursor.HAND);
            }

        });

        setOnMouseExited((MouseEvent mouseEvent) -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                this.getScene().setCursor(Cursor.DEFAULT);
            }

        });
    }

    private double boundCenterCoordinate(double value, double min, double max) {
        double radius = this.getRadius();

        if (value < min + radius) {
            return min + radius;
        } else if (value > max - radius) {
            return max - radius;
        } else {
            return value;
        }
    }

    @Override
    public void attachLabel(SmartLabel label) {
        this.attachedLabel = label;
        label.xProperty().bind(this.layoutXProperty().add(this.widthProperty().divide(2)).subtract(label.getLayoutBounds().getWidth() / 2));
        label.yProperty().bind(this.layoutYProperty().add(this.heightProperty()).add(label.getLayoutBounds().getHeight()));
    }

    @Override
    public SmartLabel getAttachedLabel() {
        return this.attachedLabel;
    }

    @Override
    public Vertex<T> getUnderlyingVertex() {
        return this.underlyingVertex;
    }

    @Override
    public void setStyleClass(String cssClass) {
        this.styleProxy.setStyleClass(cssClass);
    }

    @Override
    public void addStyleClass(String cssClass) {
        this.styleProxy.addStyleClass(cssClass);
    }

    @Override
    public boolean removeStyleClass(String cssClass) {
        return styleProxy.removeStyleClass(cssClass);
    }

    @Override
    public double getRadius() {
        return this.radiusProperty.get();
    }

    /**
     * Internal representation of a 2D point or vector for quick access to its
     * attributes.
     */
    private class PointVector {

        double x, y;

        public PointVector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public Set<SmartForceDirectedVertexNode<T>> getAdjacentVertices() {
        return this.adjacentVertices;
    }

    public DoubleProperty radiusProperty() {
        return this.radiusProperty;
    }
}
