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
import javafx.geometry.Point2D;
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
    public static final int DISTANCE_THRESHOLD = 400;
    public static final int LOOP_RADIUS_FACTOR = 4;

    private final Edge<E, V> underlyingEdge;

    private final SmartGraphVertexNode<V> inbound;
    private final SmartGraphVertexNode<V> outbound;

    private SmartLabel attachedLabel = null;
    private SmartArrow attachedArrow = null;

    private double randomAngleFactor;
    
    /* Styling proxy */
    private final SmartStyleProxy styleProxy;

    public SmartGraphEdgeCurve(Edge<E, V> edge, SmartGraphVertexNode<V> inbound, SmartGraphVertexNode<V> outbound) {
        this(edge, inbound, outbound, 0);
    }

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

        //update();
        enableListeners();
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
        label.xProperty().bind(controlX1Property().add(controlX2Property()).divide(2).subtract(label.getLayoutBounds().getWidth() / 2));
        label.yProperty().bind(controlY1Property().add(controlY2Property()).divide(2).add(label.getLayoutBounds().getHeight() / 2));
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

        /* attach arrow to line's endpoint */
        arrow.translateXProperty().bind(endXProperty());
        arrow.translateYProperty().bind(endYProperty());

        /* rotate arrow around itself based on this line's angle */
        Rotate rotation = new Rotate();
        rotation.pivotXProperty().bind(translateXProperty());
        rotation.pivotYProperty().bind(translateYProperty());
        rotation.angleProperty().bind(UtilitiesBindings.toDegrees(
                UtilitiesBindings.atan2(endYProperty().subtract(controlY2Property()),
                        endXProperty().subtract(controlX2Property()))
        ));

        arrow.getTransforms().add(rotation);

        /* add translation transform to put the arrow touching the circle's bounds */
        Translate t = new Translate(-inbound.getRadius(), 0);
        arrow.getTransforms().add(t);
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
}
