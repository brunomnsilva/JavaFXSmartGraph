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
 * com.brunomnsilva.smartgraph.graphview.SmartGraphVertexNode, int) } the <code>edgeIndex</code>
 * can be specified as to create non-overlaping curves.
 *
 * @param <E> Type stored in the underlying edge
 * @param <V> Type of connecting vertex
 *
 * @author brunomnsilva
 */
public class SmartGraphEdgeCurve<E, V> extends CubicCurve implements SmartGraphEdgeBase<E, V> {

    private static final double MAX_EDGE_CURVE_ANGLE = 20;

    private final Edge<E, V> underlyingEdge;

    private final SmartGraphVertexNode<V> inbound;
    private final SmartGraphVertexNode<V> outbound;

    private SmartLabel attachedLabel = null;
    private SmartArrow attachedArrow = null;

    private double randomAngleFactor = 0;

    public SmartGraphEdgeCurve(Edge<E, V> edge, SmartGraphVertexNode inbound, SmartGraphVertexNode outbound) {
        this(edge, inbound, outbound, 0);
    }

    public SmartGraphEdgeCurve(Edge<E, V> edge, SmartGraphVertexNode inbound, SmartGraphVertexNode outbound, int edgeIndex) {
        this.inbound = inbound;
        this.outbound = outbound;

        this.underlyingEdge = edge;

        getStyleClass().add("edge");

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
    public void setStyleClass(String cssClass) {
        getStyleClass().add(cssClass);
    }
    
    private void update() {                
        if (inbound == outbound) {
            /* Make a loop using the control points proportional to the vertex radius */
            
            //TODO: take into account several "self-loops" with randomAngleFactor
            double midpointX1 = outbound.getCenterX() - inbound.getRadius() * 5;
            double midpointY1 = outbound.getCenterY() - inbound.getRadius() * 2;
            
            double midpointX2 = outbound.getCenterX() + inbound.getRadius() * 5;
            double midpointY2 = outbound.getCenterY() - inbound.getRadius() * 2;
            
            setControlX1(midpointX1);
            setControlY1(midpointY1);
            setControlX2(midpointX2);
            setControlY2(midpointY2);
            
        } else {          
            /* Make a curved edge. The curve is proportional to the distance  */
            double midpointX = (outbound.getCenterX() + inbound.getCenterX()) / 2;
            double midpointY = (outbound.getCenterY() + inbound.getCenterY()) / 2;

            Point2D midpoint = new Point2D(midpointX, midpointY);

            Point2D startpoint = new Point2D(inbound.getCenterX(), inbound.getCenterY());
            Point2D endpoint = new Point2D(outbound.getCenterX(), outbound.getCenterY());

            //TODO: improvement lower max_angle_placement according to distance between vertices
            double angle = MAX_EDGE_CURVE_ANGLE;

            double distance = startpoint.distance(endpoint);

            //TODO: remove "magic number" 1500 and provide a distance function for the 
            //decreasing angle with distance
            angle = angle - (distance / 1500 * angle);

            midpoint = UtilitiesPoint2D.rotate(midpoint,
                    startpoint,
                    (-angle) + randomAngleFactor * (angle - (-angle)));

            setControlX1(midpoint.getX());
            setControlY1(midpoint.getY());
            setControlX2(midpoint.getX());
            setControlY2(midpoint.getY());
        }

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
        Translate t = new Translate(-outbound.getRadius(), 0);
        arrow.getTransforms().add(t);
    }

    @Override
    public SmartArrow getAttachedArrow() {
        return this.attachedArrow;
    }
}
