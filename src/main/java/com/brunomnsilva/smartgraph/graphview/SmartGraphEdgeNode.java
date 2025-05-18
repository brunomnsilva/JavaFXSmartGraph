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
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.CubicCurve;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Concrete implementation of an edge.
 * <br>
 * The edge binds its start point to the <code>outbound</code>
 * {@link SmartGraphVertexNode} center and its end point to the
 * <code>inbound</code> {@link SmartGraphVertexNode} center. As such, the curve
 * is updated automatically as the vertices move.
 * <br>
 * The <code>multiplicityIndex</code> parameter in the constructor allows for
 * different visual representations for multiple edges connecting the same two vertices:
 * <ul>
 *     <li>Index 0: A straight line.</li>
 *     <li>Index > 0: Curved lines, alternating sides for odd/even indices.</li>
 * </ul>
 *
 * @param <E> Type stored in the underlying edge
 * @param <V> Type of connecting vertex
 *
 * @author brunomnsilva
 */
public class SmartGraphEdgeNode<E, V> extends CubicCurve implements SmartGraphEdge<E, V>, SmartLabelledNode {

    // For self-loops
    public static final int LOOP_RADIUS_FACTOR = 4;
    /** Increment to effective radius factor for each unit of multiplicityIndex, making subsequent loops larger. */
    public static final double LOOP_SIZE_INCREMENT_PER_INDEX = 0;
    /** Angular offset in degrees applied per unit of multiplicityIndex, rotating subsequent loops around the vertex. */
    public static final double LOOP_ANGULAR_OFFSET_DEG_PER_INDEX = 30.0;

    /** Heuristic value for arrow head adjustment on loops */
    final double SELF_LOOP_ARROW_Y_ADJUST = 2.0;

    // For normal edges

    /** Distance threshold for varying curvature of the first curved edge pair. */
    public static final int CURVE_DISTANCE_THRESHOLD = 400;
    /** Maximum base offset for the first curved edge pair (indices 1, 2) when nodes are close. In pixels. */
    public static final double CURVE_MAX_OFFSET_PX = 80.0;
    /** Minimum base offset for the first curved edge pair (indices 1, 2) when nodes are distant. In pixels. */
    public static final double CURVE_MIN_OFFSET_PX = 20.0;
    /** Additional perpendicular offset added for each subsequent pair of curved edges (e.g., indices 3/4, 5/6). In pixels. */
    public static final double CURVE_ADDITIONAL_OFFSET_PER_PAIR_PX = 20.0;

    /** Multiplicity index, see class description  */
    private volatile int multiplicityIndex;

    /** Reference to the underlying Edge  */
    private final Edge<E, V> underlyingEdge;

    /** Reference to the (inbound) vertex  */
    private final SmartGraphVertexNode<V> inbound;

    /** Reference to the (outbound) vertex  */
    private final SmartGraphVertexNode<V> outbound;

    /** Reference to the attached label, if any  */
    private SmartLabel attachedLabel = null;

    /** Reference to the attached arrow, if any  */
    private SmartArrow attachedArrow = null;

    /** Proxy used for node styling */
    private final SmartStyleProxy styleProxy;

    /**
     * Constructs a SmartGraphEdgeNode representing an edge between two SmartGraphVertexNodes.
     * Defaults to multiplicityIndex = 0 (straight line).
     *
     * @param edge     the edge associated with this node
     * @param inbound  the inbound SmartGraphVertexNode
     * @param outbound the outbound SmartGraphVertexNode
     */
    public SmartGraphEdgeNode(Edge<E, V> edge, SmartGraphVertexNode<V> inbound, SmartGraphVertexNode<V> outbound) {
        this(edge, inbound, outbound, 0);
    }

    /**
     * Constructs a SmartGraphEdgeNode representing an edge between two SmartGraphVertexNodes.
     *
     * @param edge             the edge associated with this node
     * @param inbound          the inbound SmartGraphVertexNode
     * @param outbound         the outbound SmartGraphVertexNode
     * @param multiplicityIndex the multiplicity index (0 for straight line, >0 for curved lines)
     */
    public SmartGraphEdgeNode(Edge<E, V> edge, SmartGraphVertexNode<V> inbound, SmartGraphVertexNode<V> outbound, int multiplicityIndex) {
        Args.requireNonNegative(multiplicityIndex, "multiplicityIndex");

        this.inbound = inbound;
        this.outbound = outbound;
        this.underlyingEdge = edge;
        this.multiplicityIndex = multiplicityIndex;

        styleProxy = new SmartStyleProxy(this);
        styleProxy.addStyleClass("edge");

        // Bind start and end positions to vertices centers through properties
        this.startXProperty().bind(outbound.centerXProperty());
        this.startYProperty().bind(outbound.centerYProperty());
        this.endXProperty().bind(inbound.centerXProperty());
        this.endYProperty().bind(inbound.centerYProperty());

        // Initial placement of control points for the curve/line.
        update();

        enableListeners();
        propagateHoverEffectToArrow();
    }

    /**
     * Returns the current multiplicity index of the edge.
     * @return the current multiplicity index of the edge.
     */
    public int getMultiplicityIndex() {
        return multiplicityIndex;
    }

    /**
     * Sets the multiplicity index of the edge. This impacts the curve of the edge.
     * @param multiplicityIndex the new multiplicity index.
     */
    public void setMultiplicityIndex(int multiplicityIndex) {
        Args.requireNonNegative(multiplicityIndex, "multiplicityIndex");

        this.multiplicityIndex = multiplicityIndex;

        // Changing the multiplicity will change the curve of the edge, so update the control points.
        update();
    }

    @Override
    public SmartGraphVertex<V> getInbound() {
        return inbound;
    }

    @Override
    public SmartGraphVertex<V> getOutbound() {
        return outbound;
    }

    private void update() {
        if (inbound == outbound) {
            // Self-loop: Create a loop shape, varying size and orientation by multiplicityIndex.
            double centerX = outbound.getCenterX();
            double centerY = outbound.getCenterY();
            double vertexRadius = inbound.getRadius(); // Same as outbound.getRadius()

            // 1. Calculate effective size factor for the loop's control points.
            // Loops get progressively larger with multiplicityIndex.
            double effectiveLoopSizeFactor = LOOP_RADIUS_FACTOR + (multiplicityIndex * LOOP_SIZE_INCREMENT_PER_INDEX);
            double controlPointOffsetMagnitude = vertexRadius * effectiveLoopSizeFactor;

            // 2. Calculate angular offset for rotating the loop's control points.
            // Loops are rotated around the vertex for differentiation.
            double angleDegrees = multiplicityIndex * LOOP_ANGULAR_OFFSET_DEG_PER_INDEX;
            double angleRadians = Math.toRadians(angleDegrees);
            double cosAngle = Math.cos(angleRadians);
            double sinAngle = Math.sin(angleRadians);

            // 3. Define unrotated relative positions for control points.
            // These would create a loop primarily "above" the vertex.
            // CP1 is top-left-ish, CP2 is top-right-ish relative to vertex center if angle is 0.
            double unrotatedRelCp1X = -controlPointOffsetMagnitude;
            double unrotatedRelCp1Y = -controlPointOffsetMagnitude;
            double unrotatedRelCp2X =  controlPointOffsetMagnitude;
            double unrotatedRelCp2Y = -controlPointOffsetMagnitude;

            // 4. Rotate these relative control point positions.
            double rotatedRelCp1X = unrotatedRelCp1X * cosAngle - unrotatedRelCp1Y * sinAngle;
            double rotatedRelCp1Y = unrotatedRelCp1X * sinAngle + unrotatedRelCp1Y * cosAngle;

            double rotatedRelCp2X = unrotatedRelCp2X * cosAngle - unrotatedRelCp2Y * sinAngle;
            double rotatedRelCp2Y = unrotatedRelCp2X * sinAngle + unrotatedRelCp2Y * cosAngle;

            // 5. Set the absolute control point positions.
            setControlX1(centerX + rotatedRelCp1X);
            setControlY1(centerY + rotatedRelCp1Y);
            setControlX2(centerX + rotatedRelCp2X);
            setControlY2(centerY + rotatedRelCp2Y);

        } else {
            // Edge between distinct vertices
            Point2D actualStartVertexPos = new Point2D(outbound.getCenterX(), outbound.getCenterY());
            Point2D actualEndVertexPos = new Point2D(inbound.getCenterX(), inbound.getCenterY());

            if (multiplicityIndex == 0) {
                // Straight line
                setControlX1(actualStartVertexPos.getX() + (actualEndVertexPos.getX() - actualStartVertexPos.getX()) / 3.0);
                setControlY1(actualStartVertexPos.getY() + (actualEndVertexPos.getY() - actualStartVertexPos.getY()) / 3.0);
                setControlX2(actualStartVertexPos.getX() + 2.0 * (actualEndVertexPos.getX() - actualStartVertexPos.getX()) / 3.0);
                setControlY2(actualStartVertexPos.getY() + 2.0 * (actualEndVertexPos.getY() - actualStartVertexPos.getY()) / 3.0);
            } else {
                // Curved line (multiplicityIndex > 0)

                // Determine canonical start and end points for consistent perpendicular vector calculation,
                // so it doesn't matter which vertex is the outbound and inbound.

                Point2D canonicalStartPos, canonicalEndPos;
                // A simple way to canonicalize: use identity hash codes.
                if (System.identityHashCode(outbound) < System.identityHashCode(inbound)) {
                    canonicalStartPos = actualStartVertexPos;
                    canonicalEndPos = actualEndVertexPos;
                } else if (System.identityHashCode(outbound) > System.identityHashCode(inbound)) {
                    canonicalStartPos = actualEndVertexPos;
                    canonicalEndPos = actualStartVertexPos;
                } else {
                    // Extremely rare case: same hash code for different objects. Fallback or use another property.
                    // For simplicity, we'll just pick one if hash codes are equal but objects differ.
                    // If they are the same object, it's a self-loop, handled above.
                    // This fallback ensures dx/dy are not zero unless vertices are at the same position.
                    canonicalStartPos = actualStartVertexPos;
                    canonicalEndPos = actualEndVertexPos;
                }

                double distance = actualStartVertexPos.distance(actualEndVertexPos); // Distance is always positive

                double basePerpendicularOffset = linearDecay(CURVE_MAX_OFFSET_PX, CURVE_MIN_OFFSET_PX,
                        distance, CURVE_DISTANCE_THRESHOLD);
                int pairRankForCurves = (int) Math.floor((multiplicityIndex - 1) / 2.0);
                double totalPerpendicularOffset = basePerpendicularOffset +
                        pairRankForCurves * CURVE_ADDITIONAL_OFFSET_PER_PAIR_PX;
                int directionSign = (multiplicityIndex % 2 != 0) ? 1 : -1; // 1 for odd, -1 for even index
                double signedPerpendicularOffset = directionSign * totalPerpendicularOffset;

                // Midpoint is the same regardless of actual start/end or canonical start/end
                double midX = (actualStartVertexPos.getX() + actualEndVertexPos.getX()) / 2.0;
                double midY = (actualStartVertexPos.getY() + actualEndVertexPos.getY()) / 2.0;
                double controlX, controlY;

                if (distance < 1e-6) {
                    // Vertices are virtually coincident (should have been caught by self-loop or means an issue)
                    // Fallback: offset minimally from the midpoint along an arbitrary axis (e.g., x-axis)
                    controlX = midX + signedPerpendicularOffset;
                    controlY = midY;
                } else {
                    // Vector from CANONICAL start to CANONICAL end vertex
                    double dxCanonical = canonicalEndPos.getX() - canonicalStartPos.getX();
                    double dyCanonical = canonicalEndPos.getY() - canonicalStartPos.getY();

                    // Normalized perpendicular vector based on CANONICAL direction
                    double normPerpX = -dyCanonical / distance;
                    double normPerpY = dxCanonical / distance;

                    // Calculate the control point by offsetting from the midpoint
                    controlX = midX + signedPerpendicularOffset * normPerpX;
                    controlY = midY + signedPerpendicularOffset * normPerpY;
                }
                setControlX1(controlX);
                setControlY1(controlY);
                setControlX2(controlX);
                setControlY2(controlY);
            }
        }
    }

    /**
     * Provides the decreasing linear function decay for pixel offsets.
     * @param initialValue initial offset value (for close distances)
     * @param finalValue   final offset value (for distant distances)
     * @param distance     current distance between vertices
     * @param distanceThreshold distance beyond which finalValue is used
     * @return the decay function value for <code>distance</code>
     */
    private static double linearDecay(double initialValue, double finalValue, double distance, double distanceThreshold) {
        if (distance <= 0) return initialValue; // Avoid division by zero or negative distances
        if (distance >= distanceThreshold) return finalValue;

        return initialValue + (finalValue - initialValue) * (distance / distanceThreshold);
    }

    private void enableListeners() {
        // Update control points if start/end vertices move
        this.startXProperty().addListener((ov, oldValue, newValue) -> update());
        this.startYProperty().addListener((ov, oldValue, newValue) -> update());
        this.endXProperty().addListener((ov, oldValue, newValue) -> update());
        this.endYProperty().addListener((ov, oldValue, newValue) -> update());

        // For now, existing bindings handle arrow updates if connected vertices' radius change.
        // inbound.radiusProperty().addListener((ov, oldValue, newValue) -> update());
        // outbound.radiusProperty().addListener((ov, oldValue, newValue) -> update());
        // Currently, only inbound.radiusProperty is used for arrow pullback.
    }

    // --- Styling Methods ---
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

    // --- Attachment Methods (Label and Arrow) ---
    @Override
    public void attachLabel(SmartLabel label) {
        this.attachedLabel = label;

        // General formula for a point on a Cubic Bézier curve at t=0.5:
        // M(0.5) = (1/8)*P0 + (3/8)*P1 + (3/8)*P2 + (1/8)*P3
        // P0 = start point (startXProperty, startYProperty)
        // P1 = control point 1 (controlX1Property, controlY1Property)
        // P2 = control point 2 (controlX2Property, controlY2Property)
        // P3 = end point (endXProperty, endYProperty)

        DoubleBinding midCurveXBinding = new DoubleBinding() {
            {
                super.bind(startXProperty(), controlX1Property(), controlX2Property(), endXProperty(),
                        label.layoutWidthProperty());
            }

            @Override
            protected double computeValue() {
                double p0x = startXProperty().get();
                double p1x = controlX1Property().get();
                double p2x = controlX2Property().get();
                double p3x = endXProperty().get();
                double labelWidth = label.layoutWidthProperty().get();

                double curveMidX = 0.125 * p0x + 0.375 * p1x + 0.375 * p2x + 0.125 * p3x;
                return curveMidX - (labelWidth / 2.0);
            }
        };
        label.xProperty().bind(midCurveXBinding);

        DoubleBinding midCurveYBinding = new DoubleBinding() {
            {
                super.bind(startYProperty(), controlY1Property(), controlY2Property(), endYProperty(),
                        label.layoutHeightProperty());
            }

            @Override
            protected double computeValue() {
                double p0y = startYProperty().get();
                double p1y = controlY1Property().get();
                double p2y = controlY2Property().get();
                double p3y = endYProperty().get();
                double labelHeight = label.layoutHeightProperty().get();

                double curveMidY = 0.125 * p0y + 0.375 * p1y + 0.375 * p2y + 0.125 * p3y;
                // Assuming label y-coordinate refers to its top edge,
                // subtracting half height centers it vertically.
                return curveMidY - (labelHeight / 2.0);
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

    /**
     * Attaches a {@link SmartArrow} to this edge, binding its position/rotation.
     *
     * @param arrow     arrow to attach
     */
    public void attachArrow(SmartArrow arrow) {
        this.attachedArrow = arrow;

        arrow.translateXProperty().bind(endXProperty());
        arrow.translateYProperty().bind(endYProperty());

        Rotate rotation = new Rotate();
        rotation.setPivotX(0);
        rotation.setPivotY(0);

        // This binding gives the tangent at t=1.0 of the cubic curve
        DoubleBinding angleBinding = UtilitiesBindings.toDegrees(
                UtilitiesBindings.atan2(endYProperty().subtract(controlY2Property()),
                        endXProperty().subtract(controlX2Property()))
        );
        rotation.angleProperty().bind(angleBinding);

        arrow.getTransforms().add(rotation);

        Translate pullbackTranslation = new Translate();
        // Standard pullback along the arrow's new local X-axis
        pullbackTranslation.xProperty().bind(inbound.radiusProperty().negate());

        if (inbound == outbound) {
            pullbackTranslation.setY(SELF_LOOP_ARROW_Y_ADJUST); // Positive Y shifts arrow "down" in its local coords
        }

        arrow.getTransforms().add(pullbackTranslation);
    }

    /**
     * Returns the attached {@link SmartArrow}, if any.
     *
     * @return      reference of the attached arrow; null if none.
     */
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

    // --- Event Handling ---
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