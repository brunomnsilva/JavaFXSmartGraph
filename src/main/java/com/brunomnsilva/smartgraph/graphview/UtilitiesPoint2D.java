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

import javafx.geometry.Point2D;

/**
 * Class with utility methods for Point2D instances.
 * 
 * @author brunomnsilva
 */
public class UtilitiesPoint2D {
    
    /**
     * Rotate a point around a pivot point by a specific degrees amount
     * @param point point to rotate
     * @param pivot pivot point
     * @param angleDegrees rotation degrees
     * @return rotated point
     */
    public static Point2D rotate(final Point2D point, final Point2D pivot, final double angleDegrees) {
        double angleRadians = Math.toRadians(angleDegrees); // Convert angle to radians

        double sin = Math.sin(angleRadians);
        double cos = Math.cos(angleRadians);

        // Translate the point relative to the pivot
        double translatedX = point.getX() - pivot.getX();
        double translatedY = point.getY() - pivot.getY();

        // Apply rotation using trigonometric functions
        double rotatedX = translatedX * cos - translatedY * sin;
        double rotatedY = translatedX * sin + translatedY * cos;

        // Translate the rotated point back to the original position
        rotatedX += pivot.getX();
        rotatedY += pivot.getY();

        return new Point2D(rotatedX, rotatedY);
    }

    /**
     * Calculates the third vertex point that forms a triangle with segment AB as the base and C equidistant to A and B;
     * <code>angleDegrees</code> is the angle formed between A and C.
     *
     * @param pointA the point a
     * @param pointB the point b
     * @param angleDegrees desired angle (in degrees)
     * @return the point c
     */
    public static Point2D calculateTriangleBetween(final Point2D pointA, final Point2D pointB, final double angleDegrees) {
        // Calculate the midpoint of AB
        Point2D midpointAB = pointA.midpoint(pointB);

        // Calculate the perpendicular bisector of AB
        double slopeAB = (pointB.getY() - pointA.getY()) / (pointB.getX() - pointA.getX());
        double perpendicularSlope = -1 / slopeAB;

        // Handle special cases where the perpendicular bisector is vertical or horizontal
        if (Double.isInfinite(perpendicularSlope)) {
            double yC = midpointAB.getY() + Math.tan(Math.toRadians(angleDegrees)) * midpointAB.getX();
            return new Point2D(midpointAB.getX(), yC);
        } else if (perpendicularSlope == 0) {
            return new Point2D(pointA.getX(), midpointAB.getY());
        }

        // Calculate the angle between AB and the x-axis
        double angleAB = Math.toDegrees(Math.atan2(pointB.getY() - pointA.getY(), pointB.getX() - pointA.getX()));

        // Calculate the angle between AB and AC
        double angleAC = angleAB + angleDegrees;

        // Calculate the coordinates of point C
        double distanceAC = pointA.distance(midpointAB) / Math.cos(Math.toRadians(angleDegrees));
        double xC = pointA.getX() + distanceAC * Math.cos(Math.toRadians(angleAC));
        double yC = perpendicularSlope * (xC - midpointAB.getX()) + midpointAB.getY();

        return new Point2D(xC, yC);
    }

}
