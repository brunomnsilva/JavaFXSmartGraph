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

/**
 * A stylable node can have its css properties changed at runtime.
 * <br>
 * All Java FX nodes used by {@link SmartGraphPanel} to represent graph entities
 * should implement this interface.
 * 
 * @see SmartGraphPanel
 * 
 * @author brunomnsilva
 */
public interface SmartStylableNode {    
    
    /**
     * Applies the <code>css</code> styles to the node.
     * 
     * Note that JavaFX styles are cumulative.
     * 
     * @param css styles
     */
    public void setStyle(String css);
    
    /**
     * Applies the CSS styling defined in class selector <code>cssClass</code>.
     * 
     * The <code>cssClass</code> string must not contain a preceding dot, e.g.,
     * "myClass" instead of ".myClass".
     * 
     * The CSS Class must be defined in <code>smartpgraph.css</code> file.
     * 
     * @param cssClass name of the CSS class.
     */
    public void setStyleClass(String cssClass);
}
