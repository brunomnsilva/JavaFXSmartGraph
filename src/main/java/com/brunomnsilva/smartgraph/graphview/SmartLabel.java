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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.Text;

/**
 * A label contains text and can be attached to any {@link SmartLabelledNode}.
 * <br>
 * This class extends from {@link Text} and is allowed any corresponding
 * css formatting.
 * 
 * @author brunomnsilva
 */
public class SmartLabel extends Text implements SmartStylableNode {
    /** Proxy used for node styling */
    private final SmartStyleProxy styleProxy;

    /** The width used for layout calculations. */
    private final DoubleProperty layoutWidth;

    /** The height used for layout calculations. */
    private final DoubleProperty layoutHeight;


    /**
     * Default constructor.
     * @param text the text of the SmartLabel.
     */
    public SmartLabel(String text) {
        this(0, 0, text);
    }

    /**
     * Constructor that accepts an initial position.
     * @param x initial x coordinate
     * @param y initial y coordinate
     * @param text the text of the SmartLabel.
     */
    public SmartLabel(double x, double y, String text) {
        super(x, y, text);
        styleProxy = new SmartStyleProxy(this);

        this.layoutWidth = new SimpleDoubleProperty(  );
        this.layoutHeight = new SimpleDoubleProperty(  );

        layoutBoundsProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null) {
                if(Double.compare(layoutWidth.doubleValue(), newValue.getWidth()) != 0) {
                    layoutWidth.set(newValue.getWidth());
                }
                if(Double.compare(layoutHeight.doubleValue(), newValue.getHeight()) != 0) {
                    layoutHeight.set(newValue.getHeight());
                }
            }
        });
    }


    /**
     * Returns the read-only property representing the layout width of this label.
     *
     * @return the read-only property representing the layout width
     */
    public ReadOnlyDoubleProperty layoutWidthProperty() {
        return layoutWidth;
    }

    /**
     * Returns the read-only property representing the layout height of this label.
     *
     * @return the read-only property representing the layout height
     */
    public ReadOnlyDoubleProperty layoutHeightProperty() {
        return layoutHeight;
    }

    /**
     * Use instead of {@link #setText(String)} to allow for correct layout adjustments and label placement.
     * @param text the text to display on the label
     */
    public void setText_(String text) {
        if(getText().compareTo(text) != 0) {
            setText(text);
        }
    }

    @Override
    public void setStyleInline(String css) {
        styleProxy.setStyleInline(css);
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
