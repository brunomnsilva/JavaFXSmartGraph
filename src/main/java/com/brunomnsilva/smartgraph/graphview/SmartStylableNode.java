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
     * Applies cumulatively the <code>css</code> inline styles to the node.
     * <br/>
     * These inline JavaFX styles have higher priority and are not overwritten by
     * any css classes set by {@link SmartStylableNode#addStyleClass(java.lang.String) }.
     * But will be discarded if you use  {@link SmartStylableNode#setStyleClass(java.lang.String) }
     * <br/>
     * If you need to clear any previously set inline styles, use 
     * <code>.setStyleInline(null)</code>. Not that this will clear all inline styles previously applied.
     * 
     * @param css styles
     */
    void setStyleInline(String css);
    
    /**
     * Applies the CSS styling defined in class selector <code>cssClass</code>.
     * <br/>
     * The <code>cssClass</code> string must not contain a preceding dot, e.g.,
     * "myClass" instead of ".myClass".
     * <br/>
     * The CSS Class must be defined in <code>smartgraph.css</code> file or
     * in the custom provided stylesheet.
     * <br/>
     * The expected behavior is to remove all current styling before 
     * applying the class css.
     * 
     * @param cssClass name of the CSS class.
     */
    void setStyleClass(String cssClass);
    
    /**
     * Applies cumulatively the CSS styling defined in class selector 
     * <code>cssClass</code>.
     * <br/>
     * The CSS Class must be defined in <code>smartgraph.css</code> file or
     * in the custom provided stylesheet.
     * <br/>
     * The cumulative operation will overwrite any existing styling elements
     * previously defined for previous classes.
     * 
     * @param cssClass name of the CSS class.
     */
    void addStyleClass(String cssClass);
    
    /**
     * Removes a previously <code>cssClass</code> existing CSS styling.
     * <br/>
     * Given styles can be added sequentially, the removal of a css class
     * will be a removal that keeps the previous ordering of kept styles.
     * 
     * @param cssClass name of the CSS class.
     * 
     * @return true if successful; false if <code>cssClass</code> wasn't 
     * previously set.
     */
    boolean removeStyleClass(String cssClass);
}
