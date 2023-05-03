/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2019-2023  brunomnsilva@gmail.com
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

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * A shape of an arrow to be attached to a {@link SmartGraphEdge}.
 * 
 * @author brunomnsilva
 */
public class SmartArrow extends Path implements SmartStylableNode {
    
    /* Styling proxy */
    private final SmartStyleProxy styleProxy;
    
    /**
     * Constructor
     * 
     * @param size determines the size of the arrow (side of the triangle in pixels)
     */
    public SmartArrow(double size) {
        
        /* Create this arrow shape */
        getElements().add(new MoveTo(0, 0));  
        getElements().add(new LineTo(-size, size));
        getElements().add(new MoveTo(0, 0));        
        getElements().add(new LineTo(-size, -size));    
        
        /* Add the corresponding css class */
        styleProxy = new SmartStyleProxy(this);
        styleProxy.addStyleClass("arrow");      
    }

    @Override
    public void setStyleClass(String cssClass) {
        styleProxy.setStyleClass(cssClass);
    }

    @Override
    public void addStyleClass(String cssClass) {
        styleProxy.addStyleClass(cssClass);
    }

    @Override
    public boolean removeStyleClass(String cssClass) {
        return styleProxy.removeStyleClass(cssClass);
    }
    
}
