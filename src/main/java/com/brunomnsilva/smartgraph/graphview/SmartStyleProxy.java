/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2023-2024  brunomnsilva@gmail.com
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

import javafx.scene.Node;
import javafx.scene.shape.Shape;

/**
 * This class acts as a proxy for styling of nodes.
 * <br/>
 * It essentially groups all the logic, avoiding code duplicate.
 * <br/>
 * Classes that have this behavior can delegate the method calls to an instance
 * of this class.
 * 
 * @author brunomnsilva
 */
public class SmartStyleProxy implements SmartStylableNode {

    private Node client;

    /**
     * Creates a new style proxy for a shape client.
     * @param client the shape client
     */
    public SmartStyleProxy(Node client) {
        this.client = client;
    }

    /**
     * Changes the shape client of this proxy.
     * @param client the new shape client
     */
    public void setClient(Node client) {
        this.client = client;
    }
    
    @Override
    public void setStyleInline(String css) {
        client.setStyle(css);
    }

    @Override
    public void setStyleClass(String cssClass) {
        client.getStyleClass().clear();
        client.setStyle(null);
        client.getStyleClass().add(cssClass);
    }

    @Override
    public void addStyleClass(String cssClass) {
        client.getStyleClass().add(cssClass);
    }

    @Override
    public boolean removeStyleClass(String cssClass) {
        return client.getStyleClass().remove(cssClass);
    }

    /**
     * Copies all the styles and classes (currently applied) of <code>source</code> to <code>destination</code>.
     * @param source the shape whose styles are to be copied
     * @param destination the shape that receives the copied styles
     */
    protected static void copyStyling(Shape source, Shape destination) {
        destination.setStyle(source.getStyle());
        destination.getStyleClass().addAll(source.getStyleClass());
    }

    /*

    // This may be used in the future.

    public void removeStyleInlineProperty(String cssProperty) {
        // Get the current inline style
        String currentStyle = client.getStyle();

        // Split the style into individual property declarations
        String[] styleProperties = currentStyle.split(";");

        // Reconstruct the style without the -fx-fill property
        StringBuilder newStyle = new StringBuilder();
        for (String property : styleProperties) {
            // Split each property into key-value pair
            String[] keyValue = property.split(":");
            if (keyValue.length == 2) {
                // Check if the property is -fx-fill, if not, add it to the new style
                String key = keyValue[0].trim();
                if (!key.equals(cssProperty)) {
                    newStyle.append(property).append(";");
                }
            }
        }

        // Apply the modified inline style
        client.setStyle(newStyle.toString());
    }*/
}
