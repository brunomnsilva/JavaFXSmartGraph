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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
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
public class SmartForceDirectedVertexNode<T> implements SmartGraphVertexNode<T> {

    private final Vertex<T> underlyingVertex;

    /* Critical for performance, so we don't rely on the efficiency of the Graph.areAdjacent method */
    private final Set<SmartGraphVertexNode<T>> adjacentVertices;

    private SmartLabel attachedLabel = null;
    private boolean isDragging = false;

    private DoubleProperty radiusProperty;
    private DoubleProperty widthProperty;
    private DoubleProperty heightProperty;
    private DoubleProperty centerXProperty;
    private DoubleProperty centerYProperty;

    /*
    Automatic layout functionality members
     */
    private final PointVector forceVector = new PointVector(0, 0);
    private final PointVector updatedPosition = new PointVector(0, 0);

    /* Styling proxy */
    private final SmartStyleProxy styleProxy;

    private StackPane node;

    /**
     * Constructor which sets the instance attributes and having {@link Circle}
     * as a vertex node
     *
     * @param v the underlying vertex
     * @param allowMove should the vertex able to be dragged with the mouse
     */
    public SmartForceDirectedVertexNode(Vertex<T> v, boolean allowMove) {
        this(null, v, allowMove);
    }

    /**
     * Constructor which sets the instance attributes and having the specified
     * {@link Node} as a vertex node
     *
     * @param node {@link Node}
     * @param v the underlying vertex
     * @param allowMove should the vertex able to be dragged with the mouse
     */
    public SmartForceDirectedVertexNode(Node node, Vertex<T> v, boolean allowMove) {
        this.underlyingVertex = v;
        this.attachedLabel = null;
        this.isDragging = false;
        this.adjacentVertices = new HashSet<>();
        this.widthProperty = new SimpleDoubleProperty();
        this.heightProperty = new SimpleDoubleProperty();
        this.centerXProperty = new SimpleDoubleProperty();
        this.centerYProperty = new SimpleDoubleProperty();
        this.radiusProperty = new SimpleDoubleProperty();

        this.centerXProperty.bind(this.widthProperty.divide(2));
        this.centerYProperty.bind(this.heightProperty.divide(2));

        this.node = new StackPane();
        Node element = null;
        if (node == null) {
            if (this.underlyingVertex.element() instanceof Node) {
                element = (Node) this.underlyingVertex.element();
            }           
        } else {
            element = node;
        }
        if(element != null){
        this.node.getChildren().add(element);
            this.styleProxy = new SmartStyleProxy(element);
        }else{
            this.styleProxy = new SmartStyleProxy(this.node);
        }
        this.styleProxy.addStyleClass("vertex");
                
        this.node.boundsInLocalProperty().addListener((obs, ov, nv) -> {
            Bounds bounds = (Bounds) nv;
            double width, height, radius;
            width = bounds.getWidth();
            height = bounds.getHeight();
            radius = (Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2)))/2;
            this.widthProperty.set(width);
            this.heightProperty.set(height);
            this.radiusProperty.set(radius);
        });
        this.centerXProperty.bind(this.node.layoutXProperty().add(this.widthProperty.divide(2)));
        this.centerYProperty.bind(this.node.layoutYProperty().add(this.heightProperty.divide(2)));


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
        return new Point2D(this.node.getLayoutX(), this.node.getLayoutY());
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

        this.node.setLayoutX(x);
        this.node.setLayoutY(y);
    }

    @Override
    public double getPositionCenterX() {
        return this.centerXProperty.get();
    }

    @Override
    public double getPositionCenterY() {
        return this.centerYProperty.get();
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
        double height = this.node.getParent().getLayoutBounds().getHeight();
        double width = this.node.getParent().getLayoutBounds().getWidth();

        this.updatedPosition.x = this.boundCenterCoordinate(this.updatedPosition.x, 0, width);
        this.updatedPosition.y = boundCenterCoordinate(this.updatedPosition.y, 0, height);

        this.setPosition(this.updatedPosition.x, this.updatedPosition.y);
    }

    /**
     * Make a node movable by dragging it around with the mouse primary button.
     */
    private void enableDrag() {
        final PointVector dragDelta = new PointVector(0, 0);

        this.node.setOnMousePressed((MouseEvent mouseEvent) -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = mouseEvent.getX();
                dragDelta.y = mouseEvent.getY();
                this.isDragging = true;

                mouseEvent.consume();
            }

        });

        this.node.setOnMouseReleased((MouseEvent mouseEvent) -> {
            this.node.getScene().setCursor(Cursor.HAND);
            this.isDragging = false;

            mouseEvent.consume();
        });

        this.node.setOnDragDetected((MouseEvent mouseEvent) -> {
            this.node.getScene().setCursor(Cursor.MOVE);
        });

        this.node.setOnMouseDragged((MouseEvent mouseEvent) -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                double newX = this.node.getLayoutX() + mouseEvent.getX() - dragDelta.x;
                double newY = this.node.getLayoutY() + mouseEvent.getY() - dragDelta.y;

                double x = this.boundCenterCoordinate(newX, 0, this.node.getParent().getLayoutBounds().getWidth());
                double y = this.boundCenterCoordinate(newY, 0, this.node.getParent().getLayoutBounds().getHeight());

                this.node.setLayoutX(x);
                this.node.setLayoutY(y);

                mouseEvent.consume();
            }

        });

        this.node.setOnMouseEntered((MouseEvent mouseEvent) -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                this.node.getScene().setCursor(Cursor.HAND);
            }

        });

        this.node.setOnMouseExited((MouseEvent mouseEvent) -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                this.node.getScene().setCursor(Cursor.DEFAULT);
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
        label.xProperty().bind(this.centerXProperty().subtract(label.getLayoutBounds().getWidth() / 2));
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

    @Override
    public Node getNode() {
        return this.node;
    }

    @Override
    public void setStyle(String css) {
        this.styleProxy.setStyle(css);
    }

    @Override
    public DoubleProperty widthProperty() {
        return this.widthProperty;
    }

    @Override
    public DoubleProperty heightProperty() {
        return this.heightProperty;
    }

    @Override
    public DoubleProperty centerXProperty() {
        return this.centerXProperty;
    }

    @Override
    public DoubleProperty centerYProperty() {
        return this.centerYProperty;
    }

    @Override
    public DoubleProperty layoutXProperty() {
        return this.node.layoutXProperty();
    }

    @Override
    public DoubleProperty layoutYProperty() {
        return this.node.layoutYProperty();
    }

    @Override
    public DoubleProperty radiusProperty() {
        return this.radiusProperty;
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

    @Override
    public Set<SmartGraphVertexNode<T>> getAdjacentVertices() {
        return this.adjacentVertices;
    }
}
