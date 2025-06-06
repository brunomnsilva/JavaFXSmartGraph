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
import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Point2D;
import javafx.scene.shape.CubicCurve;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Concrete implementation of an edge.
 * <br>
 * The edge's start and end points are calculated to coincide with the
 * boundaries of the <code>outbound</code> and <code>inbound</code>
 * {@link SmartGraphVertexNode}s, respectively. The curve is updated
 * automatically as the vertices move or their radii change.
 * <br>
 * The <code>multiplicityIndex</code> parameter in the constructor allows for
 * different visual representations for multiple edges connecting the same two vertices:
 * <ul>
 *     <li>Index 0: A straight line.</li>
 *     <li>Index > 0: Curved lines, alternating sides for odd/even indices.</li>
 * </ul>
 * For self-loops, the control points defining the loop shape are calculated relative
 * to the vertex center. The edge then starts and ends at the intersection of lines
 * (from vertex center towards these control points) with the vertex boundary.
 *
 * @param <E> Type stored in the underlying edge
 * @param <V> Type of connecting vertex
 *
 * @author brunomnsilva
 */
public class SmartGraphEdgeNode<E, V> extends CubicCurve implements SmartGraphEdge<E, V>, SmartLabelledNode {

    // For self-loops
    public static final int LOOP_RADIUS_FACTOR = 3;
    public static final double LOOP_SIZE_INCREMENT_PER_INDEX = 0.5;
    public static final double LOOP_ANGULAR_OFFSET_DEG_PER_INDEX = 30.0;

    final double SELF_LOOP_ARROW_Y_ADJUST = 0;
    final double ARROW_OFFSET_FROM_BOUNDARY = 0.5;

    // For normal edges
    public static final int CURVE_DISTANCE_THRESHOLD = 400;
    public static final double CURVE_MAX_OFFSET_PX = 60.0;
    public static final double CURVE_MIN_OFFSET_PX = 20.0;
    public static final double CURVE_ADDITIONAL_OFFSET_PER_PAIR_PX = 20.0;

    private volatile int multiplicityIndex;
    private final Edge<E, V> underlyingEdge;
    private final SmartGraphVertexNode<V> inbound;
    private final SmartGraphVertexNode<V> outbound;
    private SmartLabel attachedLabel = null;
    private SmartArrow attachedArrow = null;
    private final SmartStyleProxy styleProxy;
    private Translate arrowVisualOffsetTransform = new Translate();

    public SmartGraphEdgeNode(Edge<E, V> edge, SmartGraphVertexNode<V> inbound, SmartGraphVertexNode<V> outbound) {
        this(edge, inbound, outbound, 0);
    }

    public SmartGraphEdgeNode(Edge<E, V> edge, SmartGraphVertexNode<V> inbound, SmartGraphVertexNode<V> outbound, int multiplicityIndex) {
        Args.requireNonNegative(multiplicityIndex, "multiplicityIndex");

        this.inbound = inbound;
        this.outbound = outbound;
        this.underlyingEdge = edge;
        this.multiplicityIndex = multiplicityIndex;

        styleProxy = new SmartStyleProxy(this);
        styleProxy.addStyleClass("edge");

        updateCurveGeometry();
        enableListeners();
        propagateHoverEffectToAttachments();
    }

    public int getMultiplicityIndex() {
        return multiplicityIndex;
    }

    public void setMultiplicityIndex(int multiplicityIndex) {
        Args.requireNonNegative(multiplicityIndex, "multiplicityIndex");
        if (this.multiplicityIndex == multiplicityIndex) return;

        this.multiplicityIndex = multiplicityIndex;
        updateCurveGeometry();
    }

    @Override
    public SmartGraphVertex<V> getInbound() {
        return inbound;
    }

    @Override
    public SmartGraphVertex<V> getOutbound() {
        return outbound;
    }

    private void updateCurveGeometry() {
        double outCenterX = outbound.getCenterX();
        double outCenterY = outbound.getCenterY();
        double outRadius = outbound.getRadius();

        double inCenterX = inbound.getCenterX();
        double inCenterY = inbound.getCenterY();
        double inRadius = inbound.getRadius();

        if (inbound == outbound) {
            // SELF-LOOP
            // Control points (P1, P2) are calculated relative to the vertex CENTER.
            Point2D vertexCenter = new Point2D(outCenterX, outCenterY);

            double effectiveLoopSizeFactor = LOOP_RADIUS_FACTOR + (multiplicityIndex * LOOP_SIZE_INCREMENT_PER_INDEX);
            double controlPointOffsetMagnitude = outRadius * effectiveLoopSizeFactor;

            double angleDegrees = multiplicityIndex * LOOP_ANGULAR_OFFSET_DEG_PER_INDEX;
            double angleRadians = Math.toRadians(angleDegrees);
            double cosAngle = Math.cos(angleRadians);
            double sinAngle = Math.sin(angleRadians);

            // Unrotated relative positions for control points from the vertex center.
            // These determine the loop's shape and orientation.
            double unrotatedRelCp1X = -controlPointOffsetMagnitude; // e.g., top-left of center
            double unrotatedRelCp1Y = -controlPointOffsetMagnitude;
            double unrotatedRelCp2X = controlPointOffsetMagnitude;  // e.g., top-right of center
            double unrotatedRelCp2Y = -controlPointOffsetMagnitude;

            // Rotated relative control point positions
            double rotatedRelCp1X = unrotatedRelCp1X * cosAngle - unrotatedRelCp1Y * sinAngle;
            double rotatedRelCp1Y = unrotatedRelCp1X * sinAngle + unrotatedRelCp1Y * cosAngle;
            double rotatedRelCp2X = unrotatedRelCp2X * cosAngle - unrotatedRelCp2Y * sinAngle;
            double rotatedRelCp2Y = unrotatedRelCp2X * sinAngle + unrotatedRelCp2Y * cosAngle;

            // Absolute positions of control points
            double cp1x = outCenterX + rotatedRelCp1X;
            double cp1y = outCenterY + rotatedRelCp1Y;
            double cp2x = outCenterX + rotatedRelCp2X;
            double cp2y = outCenterY + rotatedRelCp2Y;

            setControlX1(cp1x);
            setControlY1(cp1y);
            setControlX2(cp2x);
            setControlY2(cp2y);

            // Start point (P0) is intersection of (vertexCenter -> P1) with boundary
            Point2D p0 = calculateIntersectionPoint(vertexCenter, outRadius, new Point2D(cp1x, cp1y));
            setStartX(p0.getX());
            setStartY(p0.getY());

            // End point (P3) is intersection of (vertexCenter -> P2) with boundary
            // For many loop configurations, P2 is used as the target from center.
            // Alternatively, if the loop is meant to be "symmetric" and close perfectly,
            // P3 might be calculated based on a reflection or by ensuring the tangent at P3
            // smoothly enters the vertex. For simplicity here, we aim towards P2 from center.
            Point2D p3 = calculateIntersectionPoint(vertexCenter, outRadius, new Point2D(cp2x, cp2y));
            setEndX(p3.getX());
            setEndY(p3.getY());

        } else {
            // EDGE BETWEEN DISTINCT VERTICES
            Point2D outboundCenterPt = new Point2D(outCenterX, outCenterY);
            Point2D inboundCenterPt = new Point2D(inCenterX, inCenterY);

            double p1TempX, p1TempY, p2TempX, p2TempY;

            if (multiplicityIndex == 0) {
                p1TempX = outboundCenterPt.getX() + (inboundCenterPt.getX() - outboundCenterPt.getX()) / 3.0;
                p1TempY = outboundCenterPt.getY() + (inboundCenterPt.getY() - outboundCenterPt.getY()) / 3.0;
                p2TempX = outboundCenterPt.getX() + 2.0 * (inboundCenterPt.getX() - outboundCenterPt.getX()) / 3.0;
                p2TempY = outboundCenterPt.getY() + 2.0 * (inboundCenterPt.getY() - outboundCenterPt.getY()) / 3.0;
            } else {
                Point2D canonicalStartPos, canonicalEndPos;
                if (System.identityHashCode(outbound) < System.identityHashCode(inbound)) {
                    canonicalStartPos = outboundCenterPt; canonicalEndPos = inboundCenterPt;
                } else if (System.identityHashCode(outbound) > System.identityHashCode(inbound)) {
                    canonicalStartPos = inboundCenterPt; canonicalEndPos = outboundCenterPt;
                } else {
                    canonicalStartPos = outboundCenterPt; canonicalEndPos = inboundCenterPt;
                }

                double distance = outboundCenterPt.distance(inboundCenterPt);
                double basePerpendicularOffset = linearDecay(CURVE_MAX_OFFSET_PX, CURVE_MIN_OFFSET_PX,
                        distance, CURVE_DISTANCE_THRESHOLD);
                int pairRankForCurves = (int) Math.floor((multiplicityIndex - 1) / 2.0);
                double totalPerpendicularOffset = basePerpendicularOffset +
                        pairRankForCurves * CURVE_ADDITIONAL_OFFSET_PER_PAIR_PX;
                int directionSign = (multiplicityIndex % 2 != 0) ? 1 : -1;
                double signedPerpendicularOffset = directionSign * totalPerpendicularOffset;

                double midX = (outboundCenterPt.getX() + inboundCenterPt.getX()) / 2.0;
                double midY = (outboundCenterPt.getY() + inboundCenterPt.getY()) / 2.0;

                if (distance < 1e-6) {
                    p1TempX = midX + signedPerpendicularOffset;
                    p1TempY = midY;
                } else {
                    double dxCanonical = canonicalEndPos.getX() - canonicalStartPos.getX();
                    double dyCanonical = canonicalEndPos.getY() - canonicalStartPos.getY();
                    double normPerpX = -dyCanonical / distance;
                    double normPerpY = dxCanonical / distance;
                    p1TempX = midX + signedPerpendicularOffset * normPerpX;
                    p1TempY = midY + signedPerpendicularOffset * normPerpY;
                }
                p2TempX = p1TempX;
                p2TempY = p1TempY;
            }

            setControlX1(p1TempX);
            setControlY1(p1TempY);
            setControlX2(p2TempX);
            setControlY2(p2TempY);

            Point2D targetForP0 = (multiplicityIndex == 0) ? inboundCenterPt : new Point2D(p1TempX, p1TempY);
            Point2D adjustedStartPt = calculateIntersectionPoint(outboundCenterPt, outRadius, targetForP0);

            Point2D targetForP3 = (multiplicityIndex == 0) ? outboundCenterPt : new Point2D(p2TempX, p2TempY);
            Point2D adjustedEndPt = calculateIntersectionPoint(inboundCenterPt, inRadius, targetForP3);

            setStartX(adjustedStartPt.getX());
            setStartY(adjustedStartPt.getY());
            setEndX(adjustedEndPt.getX());
            setEndY(adjustedEndPt.getY());
        }
    }

    private Point2D calculateIntersectionPoint(Point2D circleCenter, double radius, Point2D lineTargetPoint) {
        double dx = lineTargetPoint.getX() - circleCenter.getX();
        double dy = lineTargetPoint.getY() - circleCenter.getY();
        double distanceToTarget = Math.sqrt(dx * dx + dy * dy);

        if (distanceToTarget < 1e-9) { // Target is effectively at the center or extremely close.
            // Return a default point on the circle, e.g., (center.X + radius, center.Y)
            // This prevents division by zero and provides a fallback.
            return new Point2D(circleCenter.getX() + radius, circleCenter.getY());
        }

        double intersectionX = circleCenter.getX() + (dx / distanceToTarget) * radius;
        double intersectionY = circleCenter.getY() + (dy / distanceToTarget) * radius;

        return new Point2D(intersectionX, intersectionY);
    }

    private static double linearDecay(double initialValue, double finalValue, double distance, double distanceThreshold) {
        if (distance <= 0) return initialValue;
        if (distance >= distanceThreshold) return finalValue;
        return initialValue + (finalValue - initialValue) * (distance / distanceThreshold);
    }

    private void enableListeners() {
        InvalidationListener updateListener = (observable) -> updateCurveGeometry();

        outbound.centerXProperty().addListener(updateListener);
        outbound.centerYProperty().addListener(updateListener);
        outbound.radiusProperty().addListener(updateListener);

        inbound.centerXProperty().addListener(updateListener);
        inbound.centerYProperty().addListener(updateListener);
        inbound.radiusProperty().addListener(updateListener);
    }

    @Override
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

    @Override
    public void attachLabel(SmartLabel label) {
        this.attachedLabel = label;
        DoubleBinding midCurveXBinding = new DoubleBinding() {
            { super.bind(startXProperty(), controlX1Property(), controlX2Property(), endXProperty(), label.layoutWidthProperty()); }
            @Override protected double computeValue() {
                double p0x = startXProperty().get(); double p1x = controlX1Property().get();
                double p2x = controlX2Property().get(); double p3x = endXProperty().get();
                return (0.125 * p0x + 0.375 * p1x + 0.375 * p2x + 0.125 * p3x) - (label.layoutWidthProperty().get() / 2.0);
            }
        };
        label.xProperty().bind(midCurveXBinding);

        DoubleBinding midCurveYBinding = new DoubleBinding() {
            { super.bind(startYProperty(), controlY1Property(), controlY2Property(), endYProperty(), label.layoutHeightProperty()); }
            @Override protected double computeValue() {
                double p0y = startYProperty().get(); double p1y = controlY1Property().get();
                double p2y = controlY2Property().get(); double p3y = endYProperty().get();
                return (0.125 * p0y + 0.375 * p1y + 0.375 * p2y + 0.125 * p3y) - (label.layoutHeightProperty().get() / 2.0);
            }
        };
        label.yProperty().bind(midCurveYBinding);
    }

    @Override
    public SmartLabel getAttachedLabel() {
        return attachedLabel;
    }

    @Override
    public Edge<E, V> getUnderlyingEdge() {
        return underlyingEdge;
    }

    public void attachArrow(SmartArrow arrow) {
        this.attachedArrow = arrow;

        arrow.translateXProperty().bind(endXProperty());
        arrow.translateYProperty().bind(endYProperty());

        Rotate rotation = new Rotate();
        rotation.pivotXProperty().set(0);
        rotation.pivotYProperty().set(0);
        DoubleBinding angleBinding = UtilitiesBindings.toDegrees(
                UtilitiesBindings.atan2(endYProperty().subtract(controlY2Property()),
                        endXProperty().subtract(controlX2Property()))
        );
        rotation.angleProperty().bind(angleBinding);
        arrow.getTransforms().add(rotation);

        arrow.getTransforms().remove(arrowVisualOffsetTransform);
        arrowVisualOffsetTransform.setX(-ARROW_OFFSET_FROM_BOUNDARY);
        arrowVisualOffsetTransform.setY(0);

        if (inbound == outbound) {
            arrowVisualOffsetTransform.setY(SELF_LOOP_ARROW_Y_ADJUST);
        }
        arrow.getTransforms().add(arrowVisualOffsetTransform);
    }

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

    private void propagateHoverEffectToAttachments() {
        this.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(attachedArrow != null) {
                if(newValue) UtilitiesJavaFX.triggerMouseEntered(attachedArrow);
                else UtilitiesJavaFX.triggerMouseExited(attachedArrow);
            }
            if(attachedLabel != null) {
                if(newValue) UtilitiesJavaFX.triggerMouseEntered(attachedLabel);
                else UtilitiesJavaFX.triggerMouseExited(attachedLabel);
            }
        });
    }
}