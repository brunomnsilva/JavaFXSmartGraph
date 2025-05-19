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
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
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
public class SmartGraphVertexNode<T> extends Group implements SmartGraphVertex<T>, SmartLabelledNode {

    public static final int ATTACHED_LABEL_OFFSET = 5;

    /** The underlying vertex in the graph model represented by this visual node. */
    private final Vertex<T> underlyingVertex;

    /** Whether this node is currently being dragged by the user. */
    private boolean isDragging;

    /** Whether this node can be moved (e.g., by user interaction or layout). */
    private boolean allowMove;

    /** Adjacent visual vertex nodes, cached for performance. */
    private final Set<SmartGraphVertexNode<T>> adjacentVertices;

    /** Force vector used in automatic layout calculations. */
    private final PointVector forceVector = new PointVector(0, 0);

    /** Updated position during automatic layout steps. */
    private final PointVector updatedPosition = new PointVector(0, 0);

    /** X coordinate of the node's center. */
    private final DoubleProperty centerX;

    /** Y coordinate of the node's center. */
    private final DoubleProperty centerY;

    /** Radius of the node's visual representation. */
    private final DoubleProperty radius;


    /** Proxy used for node styling */
    private final SmartStyleProxy styleProxy;

    /** Reference to the attached label, if any  */
    private SmartLabel attachedLabel;

    /*
    *  Shape proxy and related properties used to represent the underlying vertex.
    *  We will allow to change the shape at runtime, but other elements (e.g., lines/arrows)
    *  when created will bind to the vertex's location and radius values.
    *  Hence, we need separate properties here to be bound; later we'll bind and unbind these
    *  to the concrete shape being used.
    */

    /** Shape proxy used to represent the visual appearance of the vertex. */
    private ShapeWithRadius<?> shapeProxy;

    /** Name of the shape currently being used as a proxy. */
    private String shapeProxyName;

    /** Reference to the parent panel managing this node. */
    private final SmartGraphPanel<T, ?> parent;

    /** Bounding box covering the shape and its optional label. */
    private Bounds boundingBox;

    /**
     * Constructor which sets the instance attributes.
     *
     * @param parent the panel where this node is placed
     * @param v the underlying vertex
     * @param x initial x position on the parent pane
     * @param y initial y position on the parent pane
     * @param radius radius of this vertex representation
     * @param shapeType type of the shape to represent this vertex, see {@link ShapeFactory}
     * @param allowMove should the vertex be draggable with the mouse
     * @throws IllegalArgumentException if <code>shapeType</code> is invalid or if <code>x</code> or <code>y</code> or
     * <code>radius</code> are negative.
     */
    public SmartGraphVertexNode(SmartGraphPanel<T, ?> parent, Vertex<T> v, double x, double y, double radius, String shapeType, boolean allowMove) {
        this.parent = parent;
        this.underlyingVertex = v;
        this.adjacentVertices = new HashSet<>();

        this.attachedLabel = null;
        this.isDragging = false;

        /* Shape proxy */
        this.centerX = new SimpleDoubleProperty();
        this.centerY = new SimpleDoubleProperty();
        this.radius = new SimpleDoubleProperty();

        this.shapeProxy = ShapeFactory.create(shapeType, x, y, radius);
        this.shapeProxyName = shapeType;

        bindShapeProperties(this.shapeProxy);

        this.getChildren().add(this.shapeProxy.getShape());

        /* Styling proxy */
        styleProxy = new SmartStyleProxy(this.shapeProxy.getShape());
        styleProxy.addStyleClass("vertex");

        this.allowMove = allowMove;
        /* Enable dragging */
        if (allowMove) {
            enableDrag();
        }

        updateBoundingBox();
        propagateHoverEffectToAttachments();
    }

    /**
     * Returns the x-coordinate of the center of this node.
     *
     * @return the x-coordinate of the center of this node
     */
    public double getCenterX() {
        return centerX.doubleValue();
    }

    /**
     * Sets the x-coordinate of the center of this node.
     *
     * @param x the x-coordinate of the center of this node
     */
    public void setCenterX(double x) {
        centerX.set(x);
    }

    /**
     * Returns the y-coordinate of the center of this node.
     *
     * @return the y-coordinate of the center of this node
     */
    public double getCenterY() {
        return centerY.doubleValue();
    }

    /**
     * Sets the y-coordinate of the center of this node.
     *
     * @param y the y-coordinate of the center of this node
     */
    public void setCenterY(double y) {
        centerY.set(y);
    }

    /**
     * Returns the property representing the x-coordinate of the center of this node.
     *
     * @return the property representing the x-coordinate of the center of this node
     */
    public DoubleProperty centerXProperty() {
        return centerX;
    }

    /**
     * Returns the property representing the y-coordinate of the center of this node.
     *
     * @return the property representing the y-coordinate of the center of this node
     */
    public DoubleProperty centerYProperty() {
        return centerY;
    }

    /**
     * Returns the property representing the radius of this node.
     *
     * @return the property representing the radius of this node
     */
    public ReadOnlyDoubleProperty radiusProperty() {
        return radius;
    }

    /**
     * Returns the radius of this node.
     *
     * @return the radius of this node
     */
    public double getRadius() {
        return radius.doubleValue();
    }

    /**
     * Sets the radius of this node.
     * Since it is a bound value, the value will only be updated if it has changed.
     *
     * @param radius the new radius of this node
     */
    public void setRadius(double radius) {
        if (Double.compare(getRadius(), radius) != 0) {
            this.radius.set(radius);

            updateBoundingBox();
        }
    }

    /**
     * Changes the shape used to represent this node.
     * <br/>
     * When "swapping" shapes, the new shape will retain the positioning, radius and styling of the previous shape.
     *
     * @param shapeType the shape type name. See {@link ShapeFactory}.
     */
    public void setShapeType(String shapeType) {
        // If the shape is the same, no need to change it
        if(shapeProxyName.compareToIgnoreCase(shapeType) == 0) return;

        ShapeWithRadius<?> newShapeProxy = ShapeFactory.create(shapeType, getCenterX(), getCenterY(), getRadius());
        // Shape correctly instantiated, i.e., 'shapeType' is valid, proceed...

        // Style copying and proxy set
        SmartStyleProxy.copyStyling(this.shapeProxy.getShape(), newShapeProxy.getShape());
        styleProxy.setClient(newShapeProxy.getShape());

        this.shapeProxy = newShapeProxy;
        this.shapeProxyName = shapeType;

        bindShapeProperties(newShapeProxy);

        this.getChildren().clear();
        this.getChildren().add(newShapeProxy.getShape());
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
     * Sets the position (in relative pixels coordinates of the panel) of the node.
     * <br/>
     * If the entered position falls outside the bounds of the panel, it will be bounded to the dimensions of the panel.
     * @param p coordinates
     */
    public void setPosition(Point2D p) {
        setPosition(p.getX(), p.getY());
    }
    
    /**
     * Sets the position (in relative pixels coordinates of the panel) of the node.
     * <br/>
     * If the entered position falls outside the bounds of the panel, it will be bounded to the dimensions of the panel.
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
     */
    public void moveFromForces() {

        //limit movement to parent bounds
        double height = getParent().getLayoutBounds().getHeight();
        double width = getParent().getLayoutBounds().getWidth();

        updatedPosition.x = boundVertexNodeXPositioning(updatedPosition.x, 0, width);
        updatedPosition.y = boundVertexNodeYPositioning(updatedPosition.y, 0, height);

        setPosition(updatedPosition.x, updatedPosition.y);
    }

    @Override
    public void attachLabel(SmartLabel label) {
        this.attachedLabel = label;

        // The label's (0,0) coordinate is the top-left corner.
        // Center horizontally relative to the vertex
        label.xProperty().bind(centerXProperty().subtract(Bindings.divide( label.layoutWidthProperty(), 2.0)));

        // Put below the vertex, by the specified offset
        label.yProperty().bind(centerYProperty().add(Bindings.add( shapeProxy.radiusProperty(), ATTACHED_LABEL_OFFSET)));

        updateBoundingBox();
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
    public void setStyleInline(String css) {
        styleProxy.setStyleInline(css);
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

    private boolean hasLabel() {
        return attachedLabel != null;
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

                isDragging = true;

                // Bring the node to the front, together with the label
                toFront();
                if(hasLabel()) {
                    getAttachedLabel().toFront();
                }

                mouseEvent.consume();
            }
        });

        setOnMouseReleased((MouseEvent mouseEvent) -> {
            if(allowMove) { // necessary after a possible drag operation
                setCursor(Cursor.HAND);
            }

            isDragging = false;

            mouseEvent.consume();
        });

        setOnMouseDragged((MouseEvent mouseEvent) -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                if(allowMove && getCursor() != Cursor.MOVE) {
                    setCursor(Cursor.MOVE);
                }

                double newX = mouseEvent.getX() + dragDelta.x;
                double x = boundVertexNodeXPositioning(newX, 0, getParent().getLayoutBounds().getWidth());
                setCenterX(x);

                double newY = mouseEvent.getY() + dragDelta.y;
                double y = boundVertexNodeYPositioning(newY, 0, getParent().getLayoutBounds().getHeight());
                setCenterY(y);

                mouseEvent.consume();
            }
        });

        setOnMouseEntered((MouseEvent mouseEvent) -> {
            if (allowMove && !mouseEvent.isPrimaryButtonDown()) {
                setCursor(Cursor.HAND);
            }
        });

        setOnMouseExited((MouseEvent mouseEvent) -> {
            if (allowMove && !mouseEvent.isPrimaryButtonDown()) {
                setCursor(Cursor.DEFAULT);
            }
        });
    }

    /*
     * Bounds the positioning of this vertex node within bounds.
     * It takes into account the overall size of the node.
     */
    private double boundVertexNodeXPositioning(double xCoord, double minCoordValue, double maxCoordValue) {
        // The shape and (possibly attached) label are centered, so its bounds are equals for each side
        double lengthToSide = this.boundingBox.getWidth() / 2;

        if (xCoord < minCoordValue + lengthToSide) {
            return minCoordValue + lengthToSide;
        } else if (xCoord > maxCoordValue - lengthToSide) {
            return maxCoordValue - lengthToSide;
        } else {
            return xCoord;
        }
    }

    private double boundVertexNodeYPositioning(double yCoord, double minCoordValue, double maxCoordValue) {
        // The length to the top from the center point is the radius of the surrogate shape
        // The length to the bottom from the center point is the radius of the surrogate shape, plus the label offset and height
        double lengthToTop = getRadius();
        double lengthToBottom = getRadius() + (attachedLabel != null ? attachedLabel.layoutHeightProperty().get() : 0);

        if (yCoord < minCoordValue + lengthToTop) {
            return minCoordValue + lengthToTop;
        } else if (yCoord > maxCoordValue - lengthToBottom) {
            return maxCoordValue - lengthToBottom;
        } else {
            return yCoord;
        }
    }

    /**
     * Updates the bounding box that encloses this node (including its label).
     */
    private void updateBoundingBox() {
        if(shapeProxy == null || shapeProxy.getShape() == null) return;

        this.boundingBox = this.shapeProxy.getShape().getLayoutBounds();

        if(hasLabel()) {
            this.boundingBox = UtilitiesJavaFX.union(this.boundingBox, attachedLabel.getLayoutBounds());
        }
    }

    /*
     * (re)Bind properties of the exposed properties and the underlying shape.
     */
    private void bindShapeProperties(ShapeWithRadius<?> shape) {
        if( this.shapeProxy != null && this.centerX.isBound() ) {
            this.centerX.unbindBidirectional(this.shapeProxy.centerXProperty());
        }

        if( this.shapeProxy != null && this.centerY.isBound() ) {
            this.centerY.unbindBidirectional(this.shapeProxy.centerYProperty());
        }

        if( this.shapeProxy != null && this.radius.isBound() ) {
            this.radius.unbindBidirectional(this.shapeProxy.radiusProperty());
        }

        this.centerX.bindBidirectional(shape.centerXProperty());
        this.centerY.bindBidirectional(shape.centerYProperty());
        this.radius.bindBidirectional(shape.radiusProperty());
    }

    /*
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

    private void propagateHoverEffectToAttachments() {
        this.hoverProperty().addListener((observable, oldValue, newValue) -> {

            // Propagate to label
            if(attachedLabel != null && newValue) {
                UtilitiesJavaFX.triggerMouseEntered(attachedLabel);

            } else if(attachedLabel != null) { //newValue is false, hover ended
                UtilitiesJavaFX.triggerMouseExited(attachedLabel);
            }
        });
    }
}
