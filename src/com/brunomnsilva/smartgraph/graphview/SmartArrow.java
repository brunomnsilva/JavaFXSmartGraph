/*
 * The MIT License
 *
 * Copyright 2019 Bruno Silva.
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

import javafx.scene.layout.Region;

/**
 * A shape of an arrow to be attached to a {@link SmartGraphEdge}.
 * 
 * @author brunomnsilva
 */
public class SmartArrow extends Region implements SmartStylableNode {
    
    /* Styling proxy */
    private final SmartStyleProxy styleProxy;
    
    public SmartArrow() {
        
        /* Create this arrow shape */
//        getElements().add(new MoveTo(0, 0));  
//        getElements().add(new LineTo(-6, 3));
//        //getElements().add(new MoveTo(0, 0));        
//        getElements().add(new LineTo(-6, -3));    
//        getElements().add(new LineTo(0, 0));    
        
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
