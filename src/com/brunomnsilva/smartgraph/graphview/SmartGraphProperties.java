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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Properties used by {@link SmartGraphPanel}. Default file is given by
 * the {@link #DEFAULT_FILE} property.
 *
 * @see SmartGraphPanel
 * @see SmartGraphVertex
 * @see SmartGraphEdge
 * 
 * @author brunomnsilva
 */
public class SmartGraphProperties {

    private static final boolean DEFAULT_VERTEX_ALLOW_USER_MOVE = true;
    private static final String PROPERTY_VERTEX_ALLOW_USER_MOVE = "vertex.allow-user-move";
    
    private static final double DEFAULT_VERTEX_RADIUS = 5;
    private static final String PROPERTY_VERTEX_RADIUS = "vertex.radius";

    private static final boolean DEFAULT_VERTEX_USE_TOOLTIP = true;
    private static final String PROPERTY_VERTEX_USE_TOOLTIP = "vertex.tooltip";
    
    private static final boolean DEFAULT_VERTEX_USE_LABEL = false;
    private static final String PROPERTY_VERTEX_USE_LABEL = "vertex.label";

    private static final boolean DEFAULT_EDGE_USE_TOOLTIP = true;
    private static final String PROPERTY_EDGE_USE_TOOLTIP = "edge.tooltip";
    
    private static final boolean DEFAULT_EDGE_USE_LABEL = true;
    private static final String PROPERTY_EDGE_USE_LABEL = "edge.label";
    
    private static final boolean DEFAULT_EDGE_USE_ARROW = true;
    private static final String PROPERTY_EDGE_USE_ARROW = "edge.arrow";

    private static final double DEFAULT_REPULSION_FORCE = 1000;
    private static final String PROPERTY_REPULSION_FORCE = "layout.repulsive-force";

    private static final double DEFAULT_REPULSION_SCALE = 1;
    private static final String PROPERTY_REPULSION_SCALE = "layout.repulsive-scale";
    
    private static final double DEFAULT_ATTRACTION_FORCE = 20;
    private static final String PROPERTY_ATTRACTION_FORCE = "layout.attraction-force";

    private static final double DEFAULT_ATTRACTION_SCALE = 1;
    private static final String PROPERTY_ATTRACTION_SCALE = "layout.attraction-scale";

    private static final String DEFAULT_FILE = "smartgraph.properties";
    private Properties properties;
    
    /**
     * Uses default properties file.
     */
    public SmartGraphProperties() {
        properties = new Properties();
        
        try {
            properties.load(new FileInputStream(DEFAULT_FILE));
        } catch (IOException ex) {
            String msg = String.format("The default %s was not found. Using default values.", DEFAULT_FILE);
            Logger.getLogger(SmartGraphProperties.class.getName()).log(Level.WARNING, msg);
        }
    }
    
    /**
     * Reads properties from the desired input stream.
     * 
     * @param inputStream   input stream from where to read the properties
     */
    public SmartGraphProperties(InputStream inputStream) {
        properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException ex) {
            String msg = "The file provided by the input stream does not exist. Using default values.";
            Logger.getLogger(SmartGraphProperties.class.getName()).log(Level.WARNING, msg);
        }
    }
    
    /**
     * Returns a property that indicates whether a vertex can be moved freely
     * by the user.
     * 
     * @return corresponding property value
     */
    public boolean getVertexAllowUserMove() {
        return getBooleanProperty(PROPERTY_VERTEX_ALLOW_USER_MOVE, DEFAULT_VERTEX_ALLOW_USER_MOVE);
    }
    
    /**
     * Returns a property that indicates the radius of each vertex.
     * 
     * @return corresponding property value
     */
    public double getVertexRadius() {
        return getDoubleProperty(PROPERTY_VERTEX_RADIUS, DEFAULT_VERTEX_RADIUS);
    }
    
    /**
     * Returns a property that indicates the repulsion force to use in the
     * automatic force-based layout.
     * 
     * @return corresponding property value
     */
    public double getRepulsionForce() {
        return getDoubleProperty(PROPERTY_REPULSION_FORCE, DEFAULT_REPULSION_FORCE);
    }
        
    /**
     * Returns a property that indicates the repulsion scale to use in the
     * automatic force-based layout.
     * 
     * @return corresponding property value
     */
    public double getRepulsionScale() {
        return getDoubleProperty(PROPERTY_REPULSION_SCALE, DEFAULT_REPULSION_SCALE);
    }

    /**
     * Returns a property that indicates the attraction force to use in the
     * automatic force-based layout.
     * 
     * @return corresponding property value
     */
    public double getAttractionForce() {
        return getDoubleProperty(PROPERTY_ATTRACTION_FORCE, DEFAULT_ATTRACTION_FORCE);
    }
    
    /**
     * Returns a property that indicates the attraction scale to use in the
     * automatic force-based layout.
     * 
     * @return corresponding property value
     */
    public double getAttractionScale() {
        return getDoubleProperty(PROPERTY_ATTRACTION_SCALE, DEFAULT_ATTRACTION_SCALE);
    }
    
    /**
     * Returns a property that indicates whether a vertex has a tooltip installed.
     * 
     * @return corresponding property value
     */
    public boolean getUseVertexTooltip() {
        return getBooleanProperty(PROPERTY_VERTEX_USE_TOOLTIP, DEFAULT_VERTEX_USE_TOOLTIP);
    }
    
    /**
     * Returns a property that indicates whether a vertex has a {@link SmartLabel}
     * attached to it.
     * 
     * @return corresponding property value
     */
    public boolean getUseVertexLabel() {
        return getBooleanProperty(PROPERTY_VERTEX_USE_LABEL, DEFAULT_VERTEX_USE_LABEL);
    }
    
    /**
     * Returns a property that indicates whether an edge has a tooltip installed.
     * 
     * @return corresponding property value
     */
    public boolean getUseEdgeTooltip() {
        return getBooleanProperty(PROPERTY_EDGE_USE_TOOLTIP, DEFAULT_EDGE_USE_TOOLTIP);
    }
    
    /**
     * Returns a property that indicates whether an edge has a {@link SmartLabel}
     * attached to it.
     * 
     * @return corresponding property value
     */
    public boolean getUseEdgeLabel() {
        return getBooleanProperty(PROPERTY_EDGE_USE_LABEL, DEFAULT_EDGE_USE_LABEL);
    }
    
    /**
     * Returns a property that indicates whether a {@link SmartArrow} should be
     * attached to an edge.
     * 
     * @return corresponding property value
     */
    public boolean getUseEdgeArrow() {
        return getBooleanProperty(PROPERTY_EDGE_USE_ARROW, DEFAULT_EDGE_USE_ARROW);
    }
    
    
    private double getDoubleProperty(String propertyName, double defaultValue) {
        String p = properties.getProperty(propertyName, Double.toString(defaultValue));
        try {
            return Double.valueOf(p);
        } catch (NumberFormatException e) {
            System.err.printf("Error in reading property %s: %s", propertyName, e.getMessage());
            return defaultValue;
        }
        
    }
    
    private boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        String p = properties.getProperty(propertyName, Boolean.toString(defaultValue));
        try {
            return Boolean.valueOf(p);
        } catch (NumberFormatException e) {
            System.err.printf("Error in reading property %s: %s", propertyName, e.getMessage());
            return defaultValue;
        }        
    }
    
    
    public static void main(String[] args) {
        SmartGraphProperties props = new SmartGraphProperties();
        System.out.println("Prop vertex radius: " + props.getVertexRadius());
        System.out.println("Prop vertex use label: " + props.getUseVertexLabel());
    }
}
