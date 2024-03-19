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

/**
 * A factory class for creating instances of shapes with a specified center coordinates and radius.
 *
 * @author brunomnsilva
 */
public class ShapeFactory {

    /**
     * Creates a new instance of a shape with the specified type, center coordinates, and radius.
     *
     * @param type   The type of shape to create. Supported types are "star", "circle", "triangle",
     *               "square", "pentagon", "hexagon", "heptagon", "octagon", "nonagon", "decagon",
     *               "hendecagon", and "dodecagon".
     * @param x      The center X coordinate of the shape.
     * @param y      The center Y coordinate of the shape.
     * @param radius The radius of the shape.
     * @return An instance of a shape with the specified parameters.
     * @throws IllegalArgumentException If the provided type is not recognized or if the center coordinates
     *                                  or radius are negative.
     */
    public static ShapeWithRadius<?> create(String type, double x, double y, double radius) {
        Args.requireNonNegative(x, "x");
        Args.requireNonNegative(y, "y");
        Args.requireNonNegative(radius, "radius");

        type = type.trim().toLowerCase();

        switch(type) {
            case "star": return new ShapeStar(x, y, radius);
            case "circle": return new ShapeCircle(x, y, radius);
            case "triangle": return new ShapeRegularPolygon(x, y, radius, 3);
            case "square": return new ShapeRegularPolygon(x, y, radius, 4);
            case "pentagon": return new ShapeRegularPolygon(x, y, radius, 5);
            case "hexagon": return new ShapeRegularPolygon(x, y, radius, 6);
            case "heptagon": return new ShapeRegularPolygon(x, y, radius, 7);
            case "octagon": return new ShapeRegularPolygon(x, y, radius, 8);
            case "nonagon": return new ShapeRegularPolygon(x, y, radius, 9);
            case "decagon": return new ShapeRegularPolygon(x, y, radius, 10);
            case "hendecagon": return new ShapeRegularPolygon(x, y, radius, 11);
            case "dodecagon": return new ShapeRegularPolygon(x, y, radius, 12);

            default: throw new IllegalArgumentException("Invalid shape type. See javadoc for available shapes.");
        }
    }
}
