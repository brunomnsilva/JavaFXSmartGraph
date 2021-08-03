/*
 * The MIT License
 *
 * Copyright 2019 brunomnsilva.
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

import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import com.brunomnsilva.smartgraph.graph.Edge;
import java.util.Iterator;

/**
 * Concrete implementation of a curved edge.
 * <br>
 * The edge binds its start point to the <code>outbound</code>
 * {@link SmartGraphVertexNode} center and its end point to the
 * <code>inbound</code> {@link SmartGraphVertexNode} center. As such, the curve
 * is updated automatically as the vertices move.
 * <br>
 * Given there can be several curved edges connecting two vertices, when calling
 * the constructor {@link #SmartGraphEdgeCurve(com.brunomnsilva.smartgraph.graph.Edge,
 * com.brunomnsilva.smartgraph.graphview.SmartGraphVertexNode,
 * com.brunomnsilva.smartgraph.graphview.SmartGraphVertexNode, int) } the
 * <code>edgeIndex</code> can be specified as to create non-overlaping curves.
 *
 * @param <E> Type stored in the underlying edge
 * @param <V> Type of connecting vertex
 *
 * @author brunomnsilva
 */
public class SmartGraphEdgeCurve<E, V> extends CubicCurve implements SmartGraphEdgeBase<E, V> {

    private static final double MAX_EDGE_CURVE_ANGLE = 75;

    private final Edge<E, V> underlyingEdge;

    private final SmartGraphVertexNode<V> inbound;
    private final SmartGraphVertexNode<V> outbound;

    private SmartLabel attachedLabel = null;
    private SmartArrow attachedArrow = null;

    private int edgeIndex = 0;

    /* Styling proxy */
    private final SmartStyleProxy styleProxy;

    public SmartGraphEdgeCurve(Edge<E, V> edge, SmartGraphVertexNode inbound, SmartGraphVertexNode outbound) {
        this(edge, inbound, outbound, 0);
    }

    public SmartGraphEdgeCurve(Edge<E, V> edge, SmartGraphVertexNode inbound, SmartGraphVertexNode outbound, int edgeIndex) {
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

        this.edgeIndex = edgeIndex;

        //update();
        enableListeners();
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
            Point2D startpoint = new Point2D(inbound.getPositionCenterX(), inbound.getPositionCenterY());

            /* Make a loop to be on the ohter side of those adjecent vertices */
            int x = 0, y = 0;
            Iterator<SmartGraphVertexNode<V>> it = inbound.getAdjacentVertices().iterator();
            while (it.hasNext()) {
                SmartGraphVertexNode<V> vertex = it.next();
                if (vertex.visibleProperty().get()) {
                    x += (int) (startpoint.getX() - vertex.getPositionCenterX());
                    y += (int) (startpoint.getY() - vertex.getPositionCenterY());
                }
            }

            // Make the loop length
            double distance = inbound.getRadius() * 2;
            distance = distance < 100 ? 100 : distance;

            // Calculate the loop angle
            double angle = Math.atan2(y, x) * 180 / Math.PI;

            // Calculate control points
            int angleFactor = 15;
            int newEdgeIndex = edgeIndex % 2 == 0 ? edgeIndex * 2 : (edgeIndex * 2) + 1;
            Point2D endpoint = new Point2D(inbound.getPositionCenterX() + (distance * Math.cos(angle * Math.PI / 180)),
                    inbound.getPositionCenterY() + (distance * Math.sin(angle * Math.PI / 180)));
            double angle1 = getAngle(newEdgeIndex == 0 ? 1 : newEdgeIndex - 2, angleFactor);
            double angle2 = getAngle(newEdgeIndex + 2, angleFactor);
            Point2D midpoint1 = UtilitiesPoint2D.rotate(endpoint, startpoint, edgeIndex % 2 == 0 ? angle1 : angle2);
            Point2D midpoint2 = UtilitiesPoint2D.rotate(endpoint, startpoint, edgeIndex % 2 == 0 ? angle2 : angle1);

            // Set
            setControlX1(midpoint1.getX());
            setControlY1(midpoint1.getY());
            setControlX2(midpoint2.getX());
            setControlY2(midpoint2.getY());

        } else {
            Point2D startpoint = new Point2D(inbound.getPositionCenterX(), inbound.getPositionCenterY());
            Point2D endpoint = new Point2D(outbound.getPositionCenterX(), outbound.getPositionCenterY());

            Point2D midpoint = getCurveControlPoint(startpoint, endpoint, edgeIndex, MAX_EDGE_CURVE_ANGLE);

            setControlX1(midpoint.getX());
            setControlY1(midpoint.getY());
            setControlX2(midpoint.getX());
            setControlY2(midpoint.getY());
        }
    }

    private Point2D getCurveControlPoint(Point2D startpoint, Point2D endpoint, int edgeIndex, double maxAngle) {
        /* Make a curved edge. The curve is proportional to the distance  */
        double midpointX = (endpoint.getX() + startpoint.getX()) / 2;
        double midpointY = (endpoint.getY() + startpoint.getY()) / 2;

        Point2D midpoint = new Point2D(midpointX, midpointY);

        double lineAngle;
        double rotationAngle = Math.atan2(endpoint.getY() - startpoint.getY(), endpoint.getX() - startpoint.getX()) * 180.0 / Math.PI;
        double distance = startpoint.distance(endpoint);

        midpoint = UtilitiesPoint2D.rotate(midpoint, startpoint, -rotationAngle);

        // make angle denpends on edge index and distance
        double controlLength = 100;
        int angleFactor = 20;
        lineAngle = getAngle(edgeIndex, 15);
        if (Math.abs(lineAngle) >= maxAngle) {
            lineAngle = (lineAngle % maxAngle) + getAngle(1, angleFactor) / (1 + (int) (lineAngle / maxAngle));
        }
        lineAngle = distance > controlLength ? lineAngle * (controlLength / distance) : lineAngle;

        double y = (distance / 2.0) * Math.tan(lineAngle * Math.PI / 180.0);
        midpoint = new Point2D(midpoint.getX(), midpoint.getY() + y);
        midpoint = UtilitiesPoint2D.rotate(midpoint, startpoint, rotationAngle);

        return midpoint;
    }

    private double getAngle(int index, int angleFactor) {
        double offset = (index % 2) + (index / 2);
        double angle = angleFactor * offset * (index % 2 > 0 ? -1 : 1);
        return angle;
    }

    /*
    With a curved edge we need to continuously update the control points.
    TODO: Maybe we can achieve this solely with bindings.
     */
    private void enableListeners() {
        this.startXProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            update();
        });
        this.startYProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            update();
        });
        this.endXProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            update();
        });
        this.endYProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
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
        arrow.layoutXProperty().bind(this.endXProperty());
        arrow.layoutYProperty().bind(this.endYProperty());

        /* rotate arrow around itself based on this line's angle */
        Rotate rotation = new Rotate();
        rotation.pivotXProperty().bind(translateXProperty());
        rotation.pivotYProperty().bind(translateYProperty());
        rotation.angleProperty().bind(
                UtilitiesBindings.toDegrees(
                        UtilitiesBindings.atan2(
                                endYProperty().subtract(controlY2Property()),
                                endXProperty().subtract(controlX2Property())
                        )
                )
        );

        arrow.getTransforms().add(rotation);

        /* add translation transform to put the arrow touching the circle's bounds */
        Translate translate = new Translate();
        translate.xProperty().bind(arrow.widthProperty().multiply(-1).subtract(this.inbound.radiusProperty()));
        translate.yProperty().bind(arrow.heightProperty().divide(2.0).multiply(-1));
        arrow.getTransforms().add(translate);
    }

    @Override
    public SmartArrow getAttachedArrow() {
        return this.attachedArrow;
    }
}
