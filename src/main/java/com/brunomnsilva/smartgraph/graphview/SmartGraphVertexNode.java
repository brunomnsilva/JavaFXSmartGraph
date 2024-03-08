/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2018-2024  brunomnsilva@gmail.com
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

import com.brunomnsilva.smartgraph.graph.Vertex;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Internal implementation of a graph vertex for the {@link SmartGraphPanel}
 * class.
 * <br>
 * Visually it depicts a vertex as a circle, extending from {@link Circle}.
 * <br>
 * The vertex internally deals with mouse drag events that visually move
 * it in the {@link SmartGraphPanel} when displayed, if parameterized to do so.
 *
 * @param <T> the type of the underlying vertex
 *
 * @see SmartGraphPanel
 *
 * @author brunomnsilva
 */
public class SmartGraphVertexNode<T> extends Circle implements SmartGraphVertex<T>, SmartLabelledNode {

    private final Vertex<T> underlyingVertex;

    /* Critical for performance, so we don't rely on the efficiency of the Graph.areAdjacent method */
    private final Set<SmartGraphVertexNode<T>> adjacentVertices;

    private SmartLabel attachedLabel = null;
    private boolean isDragging = false;

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
     * @param x initial x position on the parent pane
     * @param y initial y position on the parent pane
     * @param radius radius of this vertex representation, i.e., a circle
     * @param allowMove should the vertex be draggable with the mouse
     */
    public SmartGraphVertexNode(Vertex<T> v, double x, double y, double radius, boolean allowMove) {
        super(x, y, radius);

        this.underlyingVertex = v;
        this.attachedLabel = null;
        this.isDragging = false;

        this.adjacentVertices = new HashSet<>();

        styleProxy = new SmartStyleProxy(this);
        styleProxy.addStyleClass("vertex");

        if (allowMove) {
            enableDrag();
        }
    }
    
    /**
     * Adds a vertex to the internal list of adjacent vertices.
     *
     * @param v vertex to add
     */
    public void addAdjacentVertex(SmartGraphVertexNode<T> v) {
        this.adjacentVertices.add(v);
    }

    /**
     * Removes a vertex from the internal list of adjacent vertices.
     *
     * @param v vertex to remove
     * @return true if <code>v</code> existed; false otherwise.
     */
    public boolean removeAdjacentVertex(SmartGraphVertexNode<T> v) {
        return this.adjacentVertices.remove(v);
    }

    /**
     * Removes a collection of vertices from the internal list of adjacent
     * vertices.
     *
     * @param col collection of vertices
     * @return true if any vertex was effectively removed
     */
    public boolean removeAdjacentVertices(Collection<SmartGraphVertexNode<T>> col) {
        return this.adjacentVertices.removeAll(col);
    }

    /**
     * Checks whether <code>v</code> is adjacent this instance.
     *
     * @param v vertex to check
     * @return true if adjacent; false otherwise
     */
    public boolean isAdjacentTo(SmartGraphVertexNode<T> v) {
        return this.adjacentVertices.contains(v);
    }

    /**
     * Returns the number of adjacent vertices.
     * @return the number of adjacent vertices
     */
    public int neighborhoodSize() {
        return this.adjacentVertices.size();
    }
    /**
     * Returns the current position of the instance in pixels.
     *
     * @return the x,y coordinates in pixels
     */
    public Point2D getPosition() {
        return new Point2D(getCenterX(), getCenterY());
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

        setCenterX(x);
        setCenterY(y);
    }
    
     @Override
    public double getPositionCenterX() {
        return getCenterX();
    }

    @Override
    public double getPositionCenterY() {
        return getCenterY();
    }


    /**
     * Sets the position of the instance in pixels.
     *
     * @param p coordinates
     */
    public void setPosition(Point2D p) {
        setPosition(p.getX(), p.getY());
    }

    /**
     * Resets the current computed external force vector.
     *
     */
    public void resetForces() {
        forceVector.x = forceVector.y = 0;
        updatedPosition.x = getCenterX();
        updatedPosition.y = getCenterY();
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
        forceVector.x += x;
        forceVector.y += y;
    }

    /**
     * Returns the current external force vector.
     *
     * @return force vector
     */
    public Point2D getForceVector() {
        return new Point2D(forceVector.x, forceVector.y);
    }

    /**
     * Returns the future position of the vertex.
     *
     * @return future position
     */
    public Point2D getUpdatedPosition() {
        return new Point2D(updatedPosition.x, updatedPosition.y);
    }

    /**
     * Updates the future position according to the current internal force
     * vector.
     *
     */
    public void updateDelta() {
        updatedPosition.x = updatedPosition.x /* + speed*/ + forceVector.x;
        updatedPosition.y = updatedPosition.y + forceVector.y;
    }

    /**
     * Moves the vertex position to the computed future position.
     * <p>
     * Moves are constrained within the parent pane dimensions.
     *
     */
    public void moveFromForces() {

        //limit movement to parent bounds
        double height = getParent().getLayoutBounds().getHeight();
        double width = getParent().getLayoutBounds().getWidth();

        updatedPosition.x = boundCenterCoordinate(updatedPosition.x, 0, width);
        updatedPosition.y = boundCenterCoordinate(updatedPosition.y, 0, height);

        setPosition(updatedPosition.x, updatedPosition.y);
    }

    /**
     * Make a node movable by dragging it around with the mouse primary button.
     */
    private void enableDrag() {
        final PointVector dragDelta = new PointVector(0, 0);

        setOnMousePressed((MouseEvent mouseEvent) -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = getCenterX() - mouseEvent.getX();
                dragDelta.y = getCenterY() - mouseEvent.getY();
                getScene().setCursor(Cursor.MOVE);
                isDragging = true;

                mouseEvent.consume();
            }

        });

        setOnMouseReleased((MouseEvent mouseEvent) -> {
            getScene().setCursor(Cursor.HAND);
            isDragging = false;

            mouseEvent.consume();
        });

        setOnMouseDragged((MouseEvent mouseEvent) -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                double newX = mouseEvent.getX() + dragDelta.x;
                double x = boundCenterCoordinate(newX, 0, getParent().getLayoutBounds().getWidth());
                setCenterX(x);

                double newY = mouseEvent.getY() + dragDelta.y;
                double y = boundCenterCoordinate(newY, 0, getParent().getLayoutBounds().getHeight());
                setCenterY(y);
                mouseEvent.consume();
            }

        });

        setOnMouseEntered((MouseEvent mouseEvent) -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.HAND);
            }

        });

        setOnMouseExited((MouseEvent mouseEvent) -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.DEFAULT);
            }

        });
    }

    private double boundCenterCoordinate(double value, double min, double max) {
        double radius = getRadius();

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
        label.xProperty().bind(centerXProperty().subtract(label.getLayoutBounds().getWidth() / 2.0));
        label.yProperty().bind(centerYProperty().add(getRadius() + label.getLayoutBounds().getHeight()));
    }

    @Override
    public SmartLabel getAttachedLabel() {
        return attachedLabel;
    }

    @Override
    public Vertex<T> getUnderlyingVertex() {
        return underlyingVertex;
    }

     
    @Override
    public void setStyleClass(String cssClass) {
        styleProxy.setStyleClass(cssClass);
    }

    @Override
    public void addStyleClass(String cssClass) {
        styleProxy.addStyleClass(cssClass);
    }

    @Override
    public boolean removeStyleClass(String cssClass) {
        return styleProxy.removeStyleClass(cssClass);
    }

    @Override
    public SmartStylableNode getStylableLabel() {
        return this.attachedLabel;
    }

    /**
     * Internal representation of a 2D point or vector for quick access to its
     * attributes.
     */
    private static class PointVector {

        double x, y;

        public PointVector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
