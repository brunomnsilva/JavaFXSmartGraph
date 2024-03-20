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
package com.brunomnsilva.smartgraph.containers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.net.MalformedURLException;

/**
 * This class provides zooming and panning for any JavaFX Node.
 * <br/>
 * Reacts to mouse scrolls and right-click mouse dragging (panning).
 * <br/>
 * The content node is out forward in the z-index, so it can react to mouse
 * events first. The node should consume any event not meant to propagate to
 * this pane.
 *
 * @author brunomnsilva
 */
public class ContentZoomPane extends BorderPane {
    /** Minimum scale factor */
    public static final double MIN_SCALE = 1;
    /** Maximum scale factor */
    public static final double MAX_SCALE = 5;
    /** Scroll delta to apply to scale factor */
    public static final double SCROLL_DELTA = 0.25;

    private final Node content;
    private final DoubleProperty scaleFactorProperty;
    private final double minScaleFactor, maxScaleFactor, deltaScaleFactor;


    /**
     * Creates a new instance of ContentZoomPane.
     * @param content pane to zoom and pan.
     */
    public ContentZoomPane(Node content) {
        this(content, MIN_SCALE, MAX_SCALE, SCROLL_DELTA);
    }

    /**
     * Creates a new instance of ContentZoomPane.
     * @param content pane to zoom and pan.
     * @param minScaleFactor minimum scale factor for zoom
     * @param maxScaleFactor maximum scale factor for zoom
     * @param deltaScaleFactor delta scaling factor applied when zooming with the mouse
     */
    public ContentZoomPane(Node content, double minScaleFactor, double maxScaleFactor, double deltaScaleFactor) {
        if (content == null)
            throw new IllegalArgumentException("Content cannot be null.");
        if (minScaleFactor <= 0 || maxScaleFactor <= 0 || deltaScaleFactor <= 0)
            throw new IllegalArgumentException("Scale factors must be >= 0.");
        if(minScaleFactor >= maxScaleFactor)
            throw new IllegalArgumentException("Requirement: minScaleFactor < maxScaleFactor.");

        content.toFront();

        // Apply same background color of graph to mask the "panning"
        // This is a project-specific "hack"
        try {
            File f = new File("smartgraph.css");
            String css = f.toURI().toURL().toExternalForm();
            getStylesheets().add(css);
            this.getStyleClass().add("graph");
        } catch (MalformedURLException e) {
            // do nothing
        }

        this.minScaleFactor = minScaleFactor;
        this.maxScaleFactor = maxScaleFactor;
        this.deltaScaleFactor = deltaScaleFactor;

        this.scaleFactorProperty  = new ReadOnlyDoubleWrapper(minScaleFactor);

        setCenter(this.content = content);
        enablePanAndZoom();
        enableResizeListener();
    }

    /**
     * Scale (zoom) factor property. Can be bound to control the zoom of the panel.
     * @return the scale factor property
     */
    public DoubleProperty scaleFactorProperty() {
        return scaleFactorProperty;
    }

    /**
     * Gets the minimum scaling factor allowed for zooming.
     *
     * @return the minimum scaling factor
     */
    public double getMinScaleFactor() {
        return minScaleFactor;
    }

    /**
     * Gets the maximum scaling factor allowed for zooming.
     *
     * @return the maximum scaling factor
     */
    public double getMaxScaleFactor() {
        return maxScaleFactor;
    }

    /**
     * Gets the delta scaling factor applied when zooming with the mouse.
     *
     * @return the delta scaling factor
     */
    public double getDeltaScaleFactor() {
        return deltaScaleFactor;
    }

    /**
     * Sets the center pivot point for (un)zooming.
     * @param x x coordinate
     * @param y y coordinate
     */
    private void setContentPivot(double x, double y) {
        content.setTranslateX(content.getTranslateX() - x);
        content.setTranslateY(content.getTranslateY() - y);
    }

    /**
     * Helper method to keep a value within bounds
     * @param value the value to check
     * @param min minimum value
     * @param max maximum value
     * @return a value that is kept within [min, max]
     */
    private static double boundValue(double value, double min, double max) {

        if (Double.compare(value, min) < 0) {
            return min;
        }

        if (Double.compare(value, max) > 0) {
            return max;
        }

        return value;
    }

    /**
     * Binds the necessary properties to update the clipping area.
     */
    private void enableResizeListener() {
        this.widthProperty().addListener((observableValue, oldValue, newValue) -> clipArea());
        this.heightProperty().addListener((observableValue, oldValue, newValue) -> clipArea());
    }

    /**
     * Processes mouse scroll-wheel and right-click drags to achieve the intended purposes.
     */
    private void enablePanAndZoom() {

        setOnScroll((ScrollEvent event) -> {

            double direction = event.getDeltaY() >= 0 ? 1 : -1;

            double currentScale = scaleFactorProperty.getValue();
            double computedScale = currentScale + direction * deltaScaleFactor;

            computedScale = boundValue(computedScale, minScaleFactor, maxScaleFactor);

            if (currentScale != computedScale) {

                content.setScaleX(computedScale);
                content.setScaleY(computedScale);

                if (computedScale == 1) {
                    content.setTranslateX(-getTranslateX());
                    content.setTranslateY(-getTranslateY());
                } else {

                    Bounds bounds = content.localToScene(content.getBoundsInLocal());
                    double f = (computedScale / currentScale) - 1;
                    double dx = (event.getX() - (bounds.getWidth() / 2 + bounds.getMinX()));
                    double dy = (event.getY() - (bounds.getHeight() / 2 + bounds.getMinY()));

                    setContentPivot(f * dx, f * dy);

                    clipArea();
                }
                scaleFactorProperty.setValue(computedScale);

            }
            //do not propagate event
            event.consume();
        });

        final DragContext sceneDragContext = new DragContext();

        setOnMousePressed((MouseEvent event) -> {

            if (event.isSecondaryButtonDown()) {
                getScene().setCursor(Cursor.MOVE);

                sceneDragContext.mouseAnchorX = event.getX();
                sceneDragContext.mouseAnchorY = event.getY();

                sceneDragContext.translateAnchorX = content.getTranslateX();
                sceneDragContext.translateAnchorY = content.getTranslateY();
            }

        });

        setOnMouseReleased((MouseEvent event) -> getScene().setCursor(Cursor.DEFAULT));

        setOnMouseDragged((MouseEvent event) -> {
            if (event.isSecondaryButtonDown()) {
                
                content.setTranslateX(sceneDragContext.translateAnchorX + event.getX() - sceneDragContext.mouseAnchorX);
                content.setTranslateY(sceneDragContext.translateAnchorY + event.getY() - sceneDragContext.mouseAnchorY);

                clipArea();
            }
        });

    }

    /**
     * Clips the necessary region. The underlying pane, when zoomed, will not extend the region of this pane.
     */
    private void clipArea() {
        double height = getHeight();
        double width = getWidth();
        setClip(new Rectangle(width,height));
    }

    /**
     * Keeps track of mouse drag action.
     */
    private static class DragContext {
        double mouseAnchorX;
        double mouseAnchorY;

        double translateAnchorX;
        double translateAnchorY;
    }

}
