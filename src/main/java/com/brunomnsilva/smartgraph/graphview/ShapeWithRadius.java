/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2024  brunomnsilva@gmail.com
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

import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.Shape;

/**
 * This interface represents a shape with a radius, providing methods to access and modify
 * properties related to the center coordinates and radius of the shape.
 *
 * @param <T> The type of the concrete underlying shape.
 *
 * @author brunomnsilva
 */
public interface ShapeWithRadius<T extends Shape> {

    /**
     * Returns the shape instance associated with this object.
     *
     * @return The shape instance.
     */
    Shape getShape();

    /**
     * Returns the property representing the center X coordinate of the shape.
     *
     * @return The property representing the center X coordinate.
     */
    DoubleProperty centerXProperty();

    /**
     * Returns the property representing the center Y coordinate of the shape.
     *
     * @return The property representing the center Y coordinate.
     */
    DoubleProperty centerYProperty();

    /**
     * Returns the property representing the radius of the shape.
     *
     * @return The property representing the radius of the shape.
     */
    DoubleProperty radiusProperty();

    /**
     * Returns the radius of the shape.
     *
     * @return The radius of the shape.
     */
    double getRadius();

    /**
     * Sets the radius of the shape to the specified value.
     *
     * @param radius The new radius value.
     */
    void setRadius(double radius);
}
