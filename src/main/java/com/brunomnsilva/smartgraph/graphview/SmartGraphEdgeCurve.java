/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2019-2024  brunomnsilva@gmail.com
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

import com.brunomnsilva.smartgraph.graph.Edge;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.CubicCurve;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Concrete implementation of a curved edge.
 * <br>
 * The edge binds its start point to the <code>outbound</code>
 * {@link SmartGraphVertexNode} center and its end point to the
 * <code>inbound</code> {@link SmartGraphVertexNode} center. As such, the curve
 * is updated automatically as the vertices move.
 * <br>
 * Given there can be several curved edges connecting two vertices, when calling
 * the constructor {@link #SmartGraphEdgeCurve(Edge,
 * SmartGraphVertexNode,
 * SmartGraphVertexNode, int) } the <code>edgeIndex</code>
 * can be specified as to create non-overlapping curves.
 *
 * @param <E> Type stored in the underlying edge
 * @param <V> Type of connecting vertex
 *
 * @author brunomnsilva
 */
public class SmartGraphEdgeCurve<E, V> extends CubicCurve implements SmartGraphEdgeBase<E, V> {

    private static final double MAX_EDGE_CURVE_ANGLE = 45;
    private static final double MIN_EDGE_CURVE_ANGLE = 3;

    /** Distance (in pixels) that establishes the maximum curve threshold */
    public static final int DISTANCE_THRESHOLD = 400;

    /** Radius applied to loop curves */
    public static final int LOOP_RADIUS_FACTOR = 4;

    private final Edge<E, V> underlyingEdge;

    private final SmartGraphVertexNode<V> inbound;
    private final SmartGraphVertexNode<V> outbound;

    private SmartLabel attachedLabel = null;
    private SmartArrow attachedArrow = null;

    private double randomAngleFactor;
    
    /* Styling proxy */
    private final SmartStyleProxy styleProxy;

    /**
     * Constructs a SmartGraphEdgeCurve representing a curved edge between two SmartGraphVertexNodes.
     *
     * @param edge     the edge associated with this curve
     * @param inbound  the inbound SmartGraphVertexNode
     * @param outbound the outbound SmartGraphVertexNode
     */
    public SmartGraphEdgeCurve(Edge<E, V> edge, SmartGraphVertexNode<V> inbound, SmartGraphVertexNode<V> outbound) {
        this(edge, inbound, outbound, 0);
    }

    /**
     * Constructs a SmartGraphEdgeCurve representing an edge curve between two SmartGraphVertexNodes.
     *
     * @param edge     the edge associated with this curve
     * @param inbound  the inbound SmartGraphVertexNode
     * @param outbound the outbound SmartGraphVertexNode
     * @param edgeIndex the edge index (>=0)
     */
    public SmartGraphEdgeCurve(Edge<E, V> edge, SmartGraphVertexNode<V> inbound, SmartGraphVertexNode<V> outbound, int edgeIndex) {
        this.inbound = inbound;
        this.outbound = outbound;

        this.underlyingEdge = edge;

        styleProxy = new SmartStyleProxy(this);
        styleProxy.addStyleClass("edge");

        //bind start and end positions to vertices centers through properties
        this.startXProperty().bind(outbound.centerXProperty());
        this.startYProperty().bind(outbound.centerYProperty());
        this.endXProperty().bind(inbound.centerXProperty());
        this.endYProperty().bind(inbound.centerYProperty());

        //TODO: improve this solution taking into account even indices, etc.
        randomAngleFactor = edgeIndex == 0 ? 0 : 1.0 / edgeIndex; //Math.random();

        // Initial placement of control points for the curve
        update();

        enableListeners();

        propagateHoverEffectToArrow();
    }

    public void setStyleInline(String css) {
        styleProxy.setStyleInline(css);
        if(attachedArrow != null) {
            attachedArrow.setStyleInline(css);
        }
    }

    @Override
    public void setStyleClass(String cssClass) {
        styleProxy.setStyleClass(cssClass);
        if(attachedArrow != null) {
            attachedArrow.setStyleClass(cssClass);
        }
    }

    @Override
    public void addStyleClass(String cssClass) {
        styleProxy.addStyleClass(cssClass);
        if(attachedArrow != null) {
            attachedArrow.addStyleClass(cssClass);
        }
    }

    @Override
    public boolean removeStyleClass(String cssClass) {
        boolean result = styleProxy.removeStyleClass(cssClass);
        if(attachedArrow != null) {
            attachedArrow.removeStyleClass(cssClass);
        }
        return result;
    }
    
    private void update() {                
        if (inbound == outbound) {
            /* Make a loop using the control points proportional to the vertex radius */
            
            //TODO: take into account several "self-loops" with randomAngleFactor
            double midpointX1 = outbound.getCenterX() - inbound.getRadius() * LOOP_RADIUS_FACTOR;
            double midpointY1 = outbound.getCenterY() - inbound.getRadius() * LOOP_RADIUS_FACTOR;

            double midpointX2 = outbound.getCenterX() + inbound.getRadius() * LOOP_RADIUS_FACTOR;
            double midpointY2 = outbound.getCenterY() - inbound.getRadius() * LOOP_RADIUS_FACTOR;

            setControlX1(midpointX1);
            setControlY1(midpointY1);
            setControlX2(midpointX2);
            setControlY2(midpointY2);
            
        } else {
            /* Make a curved edge. The curvature is bounded and proportional to the distance;
                higher curvature for closer vertices  */

            Point2D startpoint = new Point2D(inbound.getCenterX(), inbound.getCenterY());
            Point2D endpoint = new Point2D(outbound.getCenterX(), outbound.getCenterY());

            double distance = startpoint.distance(endpoint);

            double angle = linearDecay(MAX_EDGE_CURVE_ANGLE, MIN_EDGE_CURVE_ANGLE, distance, DISTANCE_THRESHOLD);

            Point2D midpoint = UtilitiesPoint2D.calculateTriangleBetween(startpoint, endpoint,
                    (-angle) + randomAngleFactor * 2 * angle);

            setControlX1(midpoint.getX());
            setControlY1(midpoint.getY());
            setControlX2(midpoint.getX());
            setControlY2(midpoint.getY());
        }
    }

    /**
     * Provides the decreasing linear function decay.
     * @param initialValue initial value
     * @param finalValue maximum value
     * @param distance current distance
     * @param distanceThreshold distance threshold (maximum distance -> maximum value)
     * @return the decay function value for <code>distance</code>
     */
    private static double linearDecay(double initialValue, double finalValue, double distance, double distanceThreshold) {
        //Args.requireNonNegative(distance, "distance");
        //Args.requireNonNegative(distanceThreshold, "distanceThreshold");
        // Parameters are internally guaranteed to be positive. We avoid two method calls.

        if(distance >= distanceThreshold) return finalValue;

        return initialValue + (finalValue - initialValue) * distance / distanceThreshold;
    }

    private void enableListeners() {
        // With a curved edge we need to continuously update the control points.
        // TODO: Maybe we can achieve this solely with bindings? Maybe there's no performance gain in doing so.

        this.startXProperty().addListener((ov, oldValue, newValue) -> {
            update();
        });
        this.startYProperty().addListener((ov, oldValue, newValue) -> {
            update();
        });
        this.endXProperty().addListener((ov, oldValue, newValue) -> {
            update();
        });
        this.endYProperty().addListener((ov, oldValue, newValue) -> {
            update();
        });
    }

    @Override
    public void attachLabel(SmartLabel label) {
        this.attachedLabel = label;

        // Place the label between the control points, offset by half the label dimensions
        label.xProperty()
                .bind(
                        controlX1Property()
                            .add(controlX2Property())
                            .divide(2)
                            .subtract(Bindings.divide(label.layoutWidthProperty(),2))
                );

        label.yProperty()
                .bind(
                        controlY1Property()
                                .add(controlY2Property())
                                .divide(2)
                                .add(Bindings.divide(label.layoutHeightProperty(), 2))
                );
    }

    @Override
    public SmartLabel getAttachedLabel() {
        return attachedLabel;
    }

    @Override
    public Edge<E, V> getUnderlyingEdge() {
        return underlyingEdge;
    }

    @Override
    public void attachArrow(SmartArrow arrow) {
        this.attachedArrow = arrow;

        // Bind arrow's main position to the curve's endpoint (inbound vertex center)
        arrow.translateXProperty().bind(endXProperty());
        arrow.translateYProperty().bind(endYProperty());

        // Create and configure the rotation transform for the arrow */
        Rotate rotation = new Rotate();

        // Set the pivot point for the rotation IN THE ARROW'S LOCAL COORDINATE SYSTEM.
        // SmartArrow is designed with its tip at its local (0,0).
        rotation.setPivotX(0);
        rotation.setPivotY(0);

        // Bind the rotation angle to the tangent of the curve at its endpoint.
        // The tangent is determined by the vector from ControlPoint2 to the EndPoint.
        rotation.angleProperty().bind(UtilitiesBindings.toDegrees(
                UtilitiesBindings.atan2(endYProperty().subtract(controlY2Property()),
                        endXProperty().subtract(controlX2Property()))
        ));

        // Add transforms to the arrow. Order matters. */
        // 1. Apply the rotation.
        arrow.getTransforms().add(rotation);

        /* 2. Apply a translation to "pull back" the arrow so its tip touches
              the inbound vertex's boundary instead of its center.
              This translation occurs along the arrow's (now rotated) local X-axis.
              With SmartArrow's geometry points along its positive X-axis from tail to tip,
              a negative X translation moves it backward.
         */
        Translate pullbackTranslation = new Translate(); //(0,0)
        pullbackTranslation.xProperty().bind(inbound.radiusProperty().negate());

        arrow.getTransforms().add(pullbackTranslation);
    }

    @Override
    public SmartArrow getAttachedArrow() {
        return this.attachedArrow;
    }
    
    @Override
    public SmartStylableNode getStylableArrow() {
        return this.attachedArrow;
    }

    @Override
    public SmartStylableNode getStylableLabel() {
        return this.attachedLabel;
    }

    private void propagateHoverEffectToArrow() {
        this.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(attachedArrow != null && newValue) {

                attachedArrow.fireEvent(new MouseEvent(MouseEvent.MOUSE_ENTERED, 0, 0, 0, 0, MouseButton.NONE, 0, true, true, true, true, true, true, true, true, true, true, null));

            } else if(attachedArrow != null) { //newValue is false, hover ended

                attachedArrow.fireEvent(new MouseEvent(MouseEvent.MOUSE_EXITED, 0, 0, 0, 0, MouseButton.NONE, 0, true, true, true, true, true, true, true, true, true, true, null));

            }
        });
    }
}
