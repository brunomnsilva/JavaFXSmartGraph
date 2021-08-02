/* 
 * The MIT License
 *
 * Copyright 2021
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Implementation of {@link SmartGraphVertexNode} class.
 * <br>
 * Rather than visualize a vertex just as a {@link Circle}, this uses
 * {@link StackPane} as a vertex node. If the type of the underlying vertex is
 * an instance of {@link Node} it will be added to the vertex node.
 * <br>
 * The vertex internally deals with mouse drag events that visually move it in
 * the {@link SmartGraphPanel} when displayed, if parameterized to do so.
 *
 *
 *
 * @param <T> the type of the underlying vertex
 *
 * @author pantape.k@gmail.com
 */
public class SmartGraphVertexNodeBasic<T> implements SmartGraphVertexNode<T> {

    protected Vertex<T> underlyingVertex;

    /* Critical for performance, so we don't rely on the efficiency of the Graph.areAdjacent method */
    protected final Set<SmartGraphVertexNode<T>> adjacentVertices;

    protected SmartLabel attachedLabel = null;
    protected boolean isDragging = false;

    protected DoubleProperty radiusProperty;
    protected DoubleProperty widthProperty;
    protected DoubleProperty heightProperty;
    protected DoubleProperty centerXProperty;
    protected DoubleProperty centerYProperty;
    protected BooleanProperty visibleProperty;

    /* Styling proxy */
    protected SmartStyleProxy styleProxy;

    // Node container
    protected StackPane node;

    protected SmartGraphVertexNodeBasic() {
        this.underlyingVertex = null;
        this.attachedLabel = null;
        this.isDragging = false;
        this.styleProxy = null;
        this.adjacentVertices = new HashSet<>();
        this.widthProperty = new SimpleDoubleProperty();
        this.heightProperty = new SimpleDoubleProperty();
        this.centerXProperty = new SimpleDoubleProperty();
        this.centerYProperty = new SimpleDoubleProperty();
        this.radiusProperty = new SimpleDoubleProperty();
        this.visibleProperty = new SimpleBooleanProperty();
        this.visibleProperty.set(true);
        this.node = new StackPane();
        this.node.visibleProperty().bind(this.visibleProperty);
    }

    /**
     * Constructor which sets the instance attributes
     *
     * @param v the underlying vertex
     * @param allowMove should the vertex able to be dragged with the mouse
     */
    public SmartGraphVertexNodeBasic(Vertex<T> v, boolean allowMove) {
        this();
        this.underlyingVertex = v;
        Node element = null;
        if (this.underlyingVertex.element() instanceof Node) {
            element = (Node) this.underlyingVertex.element();
        }
        if (element != null) {
            this.node.getChildren().add(element);
            this.styleProxy = new SmartStyleProxy(element);
        } else {
            this.styleProxy = new SmartStyleProxy(this.node);
        }
        this.styleProxy.addStyleClass("vertex");

        this.node.boundsInLocalProperty().addListener((obs, ov, nv) -> {
            Bounds bounds = (Bounds) nv;
            double width, height, radius;
            width = bounds.getWidth();
            height = bounds.getHeight();
            radius = (width > height ? width : height) / 2;//(Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2))) / 2;
            this.widthProperty.set(width);
            this.heightProperty.set(height);
            this.radiusProperty.set(radius);
        });
        this.centerXProperty.bind(this.node.layoutXProperty().add(this.widthProperty.divide(2)));
        this.centerYProperty.bind(this.node.layoutYProperty().add(this.heightProperty.divide(2)));

        if (allowMove) {
            this.enableMouseEvent();
        }
    }

    /**
     * Adds a vertex to the internal list of adjacent vertices.
     *
     * @param v vertex to add
     */
    @Override
    public void addAdjacentVertex(SmartGraphVertexNode<T> v) {
        this.adjacentVertices.add(v);
    }

    @Override
    public Set<SmartGraphVertexNode<T>> getAdjacentVertices() {
        return this.adjacentVertices;
    }

    /**
     * Removes a vertex from the internal list of adjacent vertices.
     *
     * @param v vertex to remove
     * @return true if <code>v</code> existed; false otherwise.
     */
    @Override
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
    @Override
    public boolean isAdjacentTo(SmartGraphVertexNode<T> v) {
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
    
    @Override
    public BooleanProperty visibleProperty() {
        return this.visibleProperty;
    }    

    /**
     * Make a node movable by dragging it around with the mouse primary button.
     */
    protected void enableMouseEvent() {
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

    protected double boundCenterCoordinate(double value, double min, double max) {
        double radius = this.getRadius();

        if (value < min + radius) {
            return min + radius;
        } else if (value > max - radius) {
            return max - radius;
        } else {
            return value;
        }
    }

    /**
     * Internal representation of a 2D point or vector for quick access to its
     * attributes.
     */
    protected class PointVector {

        double x, y;

        public PointVector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
