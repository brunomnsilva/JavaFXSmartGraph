/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2024  brunomnsilva@gmail.com
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
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * This class provides zooming and panning for any JavaFX Pane.
 * <br/>
 * Reacts to mouse scrolls and mouse dragging (panning).
 * <br/>
 * The content node is out forward in the z-index, so it can react to mouse
 * events first. The node should consume any event not meant to propagate to
 * this pane.
 *
 * @author brunomnsilva
 */
public class ContentZoomScrollPane extends ScrollPane {

    /** Minimum scale factor. Doesn't make sense to allow < 1 */
    public static final double MIN_SCALE = 1;

    /** Maximum scale factor */
    public static final double MAX_SCALE = 5;
    /** Scroll delta to apply to scale factor */
    public static final double SCROLL_DELTA = 0.25;

    // The pane content to be displayed, scaled and paned
    private final Pane content;

    private final DoubleProperty scaleFactorProperty;
    private final double minScaleFactor, maxScaleFactor, deltaScaleFactor;

    // Content preferred bounds, if set.
    private PreferredSize contentPreferredSize;

    /**
     * Creates a new instance of ContentZoomScrollPane.
     * <br/>
     * The minimum scale factor is 1. So <code>maxScaleFactor</code> should be &gt; 1 and
     * <code>deltaScaleFactor</code> should be a value such that <code>maxScaleFactor</code> is a multiple
     * of it, so the scale is utilized fully.
     *
     * @param content pane to zoom and pan.
     * @param maxScaleFactor maximum scale factor for zoom, e.g., 5x.
     * @param deltaScaleFactor delta scaling factor applied when zooming with the mouse, e.g., steps of 0.25x.
     */
    public ContentZoomScrollPane(Pane content, double maxScaleFactor, double deltaScaleFactor) {
        if (content == null)
            throw new IllegalArgumentException("Content cannot be null.");
        if (maxScaleFactor < 1)
            throw new IllegalArgumentException("Maximum scale factor must be >= 1.");
        if (deltaScaleFactor <= 0)
            throw new IllegalArgumentException("Delta scale factor must be > 0.");

        this.content = content;

        // we need to add content to a group for the scrollpane to take into account the scaling of the content
        Group contentGroup = new Group();
        contentGroup.getChildren().add(this.content);
        setContent(contentGroup);

        this.minScaleFactor = MIN_SCALE;
        this.maxScaleFactor = maxScaleFactor;
        this.deltaScaleFactor = deltaScaleFactor;

        this.scaleFactorProperty  = new ReadOnlyDoubleWrapper(minScaleFactor);

        enableContentResize();
        enableZoom();
        enablePanning();
    }

    /**
     * Creates a new instance of ContentZoomScrollPane.
     * <br/>
     * The minimum scale factor is 1. The default maximum scale factor is 5 and the default delta scale factor is 0.25.
     *
     * @param content pane to zoom and pan.
     */
    public ContentZoomScrollPane(Pane content) {
        this(content, MAX_SCALE, SCROLL_DELTA);
    }

    /**
     * Returns the scale (zoom) factor property. Can be bound to control the zoom of the panel.
     * @return the scale factor property
     */
    public DoubleProperty scaleFactorProperty() {
        return scaleFactorProperty;
    }

    /**
     * Returns the minimum scaling factor allowed for zooming.
     *
     * @return the minimum scaling factor
     */
    public double getMinScaleFactor() {
        return minScaleFactor;
    }

    /**
     *  Returns the maximum scaling factor allowed for zooming.
     *
     * @return the maximum scaling factor
     */
    public double getMaxScaleFactor() {
        return maxScaleFactor;
    }

    /**
     * Returns the delta scaling factor applied when zooming with the mouse.
     *
     * @return the delta scaling factor
     */
    public double getDeltaScaleFactor() {
        return deltaScaleFactor;
    }

    private void enableContentResize() {

        // Get the content's preferred size values and, if set, respect them.
        // Otherwise, set the size of the content to match the size of the scrollpane's viewport.

        this.contentPreferredSize = new PreferredSize( this.content.getPrefWidth(), this.content.getPrefHeight() );

        this.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {

            if(!contentPreferredSize.isWidthSet() && newValue.getWidth() > 0) {
                this.content.setPrefWidth(newValue.getWidth());
            }

            if(!contentPreferredSize.isHeightSet() && newValue.getHeight() > 0) {
                this.content.setPrefHeight(newValue.getHeight());
            }
        });
    }

    /*
     * Method to add panning behavior to the ScrollPane
     */
    private void enablePanning() {
        setPannable(true);
    }

    /*
     * Method to add zoom behavior to the ScrollPane
     */
    private void enableZoom() {
        this.addEventFilter(ScrollEvent.ANY, event -> {
            if (event.getDeltaY() > 0) {
                zoomIn(event);
            } else {
                zoomOut(event);
            }
            event.consume();
        });

        setVbarPolicy(ScrollBarPolicy.NEVER);
        setHbarPolicy(ScrollBarPolicy.NEVER);
    }

    /*
     * Performs zooming in.
     */
    private void zoomOut(ScrollEvent event) {
        zoomContent(event.getX(), event.getY(), ZoomDirection.OUT);
    }

    /*
     * Performs zooming out.
     */
    private void zoomIn(ScrollEvent event) {
        zoomContent(event.getX(), event.getY(), ZoomDirection.IN);
    }

    /*
     * Performs the zoom (scaling) functionality.
     */
    private void zoomContent(double pivotX, double pivotY, ZoomDirection direction) {
        double previousScale = scaleFactorProperty.doubleValue();
        double nextScale = previousScale + direction.getValue() * deltaScaleFactor;

        double scaleFactor = nextScale / previousScale;

        double scaleTotal = scaleFactorProperty.doubleValue() * scaleFactor;

        if (scaleTotal >= minScaleFactor && scaleTotal <= maxScaleFactor) {

            Bounds viewPort = getViewportBounds();
            Bounds contentSize = content.getBoundsInParent();

            // Convert mouse pivot points to content coordinates, even with scaling and panning.
            Point2D zoomCenter =  content.sceneToLocal(pivotX, pivotY);

            double centerPosX = (contentSize.getWidth() - viewPort.getWidth()) * getHvalue() + zoomCenter.getX();
            double centerPosY = (contentSize.getHeight() - viewPort.getHeight()) * getVvalue() + zoomCenter.getY();

            content.setScaleX(scaleTotal);
            content.setScaleY(scaleTotal);

            double newCenterX = centerPosX * scaleFactor;
            double newCenterY = centerPosY * scaleFactor;

            setHvalue( (newCenterX - zoomCenter.getX()) / (contentSize.getWidth() * scaleFactor - viewPort.getWidth()) );
            setVvalue( (newCenterY - zoomCenter.getY()) / (contentSize.getHeight() * scaleFactor - viewPort.getHeight()) );

            scaleFactorProperty.set(scaleTotal);
        }
    }

    /**
     * Enum type to specify the zoom direction.
     */
    private enum ZoomDirection {
        IN(1), OUT(-1);

        private final int value;

        ZoomDirection(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static class PreferredSize {
        public double width;
        public double height;

        public PreferredSize(double width, double height) {
            this.width = width;
            this.height = height;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        public boolean isWidthSet() {
            return getWidth() != Region.USE_COMPUTED_SIZE;
        }

        public boolean isHeightSet() {
            return getHeight() != Region.USE_COMPUTED_SIZE;
        }
    }

}
