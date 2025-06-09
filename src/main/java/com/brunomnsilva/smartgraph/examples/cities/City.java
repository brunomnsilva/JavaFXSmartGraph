/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2023-2025  brunomnsilva@gmail.com
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

package com.brunomnsilva.smartgraph.examples.cities;

import com.brunomnsilva.smartgraph.graphview.SmartLabelSource;
import com.brunomnsilva.smartgraph.graphview.SmartRadiusSource;
import com.brunomnsilva.smartgraph.graphview.SmartShapeTypeSource;

import java.util.Objects;

/**
 * A simple class to represent a city in an example usage of the library.
 * @author brunomnsilva
 */
public class City {
    private String name;
    private float population;

    /**
     * Constructor for City instances.
     * @param name name of the city
     * @param population population (in millions)
     */
    public City(String name, float population) {
        this.name = name;
        this.population = population;
    }

    /**
     * Returns the name of the city.
     * @return the name of the city
     */
    @SmartLabelSource
    public String getName() {
        return name;
    }

    /**
     * Setter for the name of the city.
     * @param name the name of the city
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the population of the city.
     * @return the population of the city
     */
    public float getPopulation() {
        return population;
    }

    /**
     * Setter for the population of the city.
     * @param population the population of the city
     */
    public void setPopulation(float population) {
        this.population = population;
    }

    @Override
    public String toString() {
        return "City{" + "name=" + name + ", population=" + population + '}';
    }

    /**
     * Establishes the shape of the vertex to use when representing this city.
     * @return the name of the shape, see {@link com.brunomnsilva.smartgraph.graphview.ShapeFactory}
     */
    @SmartShapeTypeSource
    public String modelShape() {
        if(Objects.equals(this.name, "Tokyo")) {
            return "star";
        }

        return "circle";
    }

    /**
     * Returns the radius of the vertex when representing this city.
     * @return the radius of the vertex
     */
    @SmartRadiusSource
    public Double modelRadius() {
        return convertToLogScale(Double.parseDouble(String.valueOf(this.population)));
    }

    private static double convertToLogScale(double value) {
        // Define input range
        double minValue = 1;
        double maxValue = 40;

        // Define output range
        double minOutputValue = 15;
        double maxOutputValue = 40;

        // Map the input value to the output range using logarithmic function
        double mappedValue = (Math.log(value) - Math.log(minValue)) / (Math.log(maxValue) - Math.log(minValue));

        // Map the mapped value to the output range
        return minOutputValue + mappedValue * (maxOutputValue - minOutputValue);
    }
}
