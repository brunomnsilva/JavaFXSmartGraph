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
 * The Args class provides a collection of static methods for checking method parameters and throwing
 * IllegalArgumentExceptions if the parameters do not meet the specified requirements.
 *
 * @author brunomnsilva
 */
public final class Args {

    /**
     * Checks if the specified parameter is null and throws an IllegalArgumentException if it is.
     *
     * @param param the parameter to check for null
     * @param name the name of the parameter being checked
     * @throws IllegalArgumentException if the specified parameter is null
     */
    public static void requireNotNull(Object param, String name) {
        if (param == null) {
            throw new IllegalArgumentException(String.format("Require '%s' to be not null.", name));
        }
    }

    /**
     * Checks if a specified double value is greater than a specified minimum value and throws an IllegalArgumentException
     * if it is not. Uses a scaled comparison to avoid floating-point precision errors.
     *
     * @param value the double value to check
     * @param name the name of the double value being checked
     * @param minValue the minimum value that the specified double value must be greater than
     * @throws IllegalArgumentException if the specified double value is less than or equal to the specified minimum value
     */
    public static void requireGreaterThan(double value, String name, double minValue) {
        if(value <= minValue) {
            throw new IllegalArgumentException(String.format("Require '%s' (%f) to be greater than %f.", name, value, minValue));
        }
    }

    /**
     * Checks if a specified double value is non-negative and throws an IllegalArgumentException if it is not.
     *
     * @param value the double value to check
     * @param name the name of the double value being checked
     * @throws IllegalArgumentException if the specified double value is negative
     */
    public static void requireNonNegative(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(String.format("Require '%s' (%f) to be non-negative.", name, value));
        }
    }

    /**
     * This method checks if a value falls within a specified range.
     * If the value is less than the lower bound or greater than the upper bound, an IllegalArgumentException is thrown.
     * @param value the value to check
     * @param name the name of the value being checked
     * @param lowerBound the lower bound of the range (inclusive)
     * @param upperBound the upper bound of the range (inclusive)
     * @throws IllegalArgumentException if the value is outside the specified range
     */
    public static void requireInRange(double value, String name, double lowerBound, double upperBound) {
        if (value < lowerBound || value > upperBound) {
            throw new IllegalArgumentException(String.format("Require '%s' (%f) to be in range [%f, %f].", name, value, lowerBound, upperBound));
        }
    }

    /**
     * Checks if the given double value is finite (i.e., not NaN or infinite).
     * @param value the double value to check for finiteness
     * @param name the name of the value being checked
     * @throws IllegalArgumentException if the value is not finite
     */
    public static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(String.format("Require '%s' (%f) to be finite.", name, value));
        }
    }
}
