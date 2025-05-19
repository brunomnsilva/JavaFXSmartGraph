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
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * A label contains text and can be attached to any {@link SmartLabelledNode}.
 * <br>
 * This class extends from {@link Text} and is allowed any corresponding
 * css formatting.
 * 
 * @author brunomnsilva
 */
public class SmartLabel extends StackPane implements SmartStylableNode {

    private final Text text;

    /** Proxy used for node styling */
    private final SmartStyleProxy paneStyleProxy;
    private final SmartStyleProxy textStyleProxy;

    /** The width used for layout calculations. */
    private final DoubleProperty layoutWidth;

    /** The height used for layout calculations. */
    private final DoubleProperty layoutHeight;


    /**
     * Default constructor.
     * @param label the text of the SmartLabel.
     */
    public SmartLabel(String label) {
        this(0, 0, label);
    }

    /**
     * Constructor that accepts an initial position.
     * @param x initial x coordinate
     * @param y initial y coordinate
     * @param label the text of the SmartLabel.
     */
    public SmartLabel(double x, double y, String label) {
        super();

        this.text = new Text(label);
        this.textStyleProxy = new SmartStyleProxy(text);
        getChildren().add(text);

        //Set initial position
        setLayoutX(x);
        setLayoutY(y);

        this.paneStyleProxy = new SmartStyleProxy(this);

        this.layoutWidth = new SimpleDoubleProperty(  );
        this.layoutHeight = new SimpleDoubleProperty(  );

        enableBoundsListener();
        propagateHoverEffectToAttachments();
    }

    /**
     * Defines the X coordinate of the SmartLabel's top-left corner in its parent's coordinate space.
     * This property is analogous to the 'x' property of a standalone Text node for positioning
     * and directly manipulates the {@code layoutXProperty} of this StackPane.
     *
     * @return the property for the x coordinate.
     */
    public final DoubleProperty xProperty() {
        return layoutXProperty();
    }

    /**
     * Defines the Y coordinate of the SmartLabel's top-left corner in its parent's coordinate space.
     * This property is analogous to the 'y' property of a standalone Text node for positioning
     * and directly manipulates the {@code layoutYProperty} of this StackPane.
     * Note: For Text, 'y' often refers to the baseline; here it's the top edge.
     *
     * @return the property for the y coordinate.
     */
    public final DoubleProperty yProperty() {
        return layoutYProperty();
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
     * Gets the text content of this SmartLabel.
     * @return the text content.
     */
    public String getText() {
        return text.getText();
    }

    /**
     * Use instead of {@link #setText(String)} to allow for correct layout adjustments and label placement.
     * @param newLabel the text to display on the label
     * @deprecated Since version 2.2.0. Keeping for compatibility until removed.
     */
    public void setText_(String newLabel) {
        setText(newLabel);
    }

    /**
     * Sets the text content of this SmartLabel.
     * @param newLabel the new text content.
     */
    public void setText(String newLabel) {
        if(this.text.getText().compareTo(newLabel) != 0) {
            this.text.setText(newLabel);
        }
    }

    @Override
    public void setStyleInline(String css) {
        textStyleProxy.setStyleInline(css);
        paneStyleProxy.setStyleInline(css);
    }

    @Override
    public void setStyleClass(String cssClass) {
        textStyleProxy.setStyleClass(cssClass);
        paneStyleProxy.setStyleClass(cssClass);
    }

    @Override
    public void addStyleClass(String cssClass) {
        textStyleProxy.addStyleClass(cssClass);
        paneStyleProxy.addStyleClass(cssClass);
    }

    @Override
    public boolean removeStyleClass(String cssClass) {
        boolean res1 =  paneStyleProxy.removeStyleClass(cssClass);
        boolean res2 = paneStyleProxy.removeStyleClass(cssClass);
        return (res1 || res2);
    }

    private void enableBoundsListener() {
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

    private void propagateHoverEffectToAttachments() {
        this.hoverProperty().addListener((observable, oldValue, newValue) -> {

            // Propagate to text node
            if(text != null && newValue) {
                UtilitiesJavaFX.triggerMouseEntered(text);

            } else if(text != null) { //newValue is false, hover ended
                UtilitiesJavaFX.triggerMouseExited(text);
            }
        });
    }

}
