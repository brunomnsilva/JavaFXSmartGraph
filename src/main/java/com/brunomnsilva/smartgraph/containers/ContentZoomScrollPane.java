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
import javafx.scene.shape.Rectangle;

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

    /** Minimum scale factor. Doesn't make sense to allow &lt; 1 */
    public static final double MIN_SCALE = 1;

    /** Maximum scale factor */
    public static final double MAX_SCALE = 5;
    /** Scroll delta to apply to scale factor */
    public static final double SCROLL_DELTA = 0.25;

    /** The pane content to be displayed, scaled and paned */
    private final Pane content;

    /** Current scaling factor. */
    private final DoubleProperty scaleFactorProperty;

    /** Scaling factor interval and delta. */
    private final double minScaleFactor, maxScaleFactor, deltaScaleFactor;

    /** Content preferred bounds, if set. */
    private PreferredSize contentPreferredSize;

    /**
     * Creates a new {@code ContentZoomScrollPane} with configurable zooming, panning, and scrollbars.
     * <p>
     * This constructor allows full control over the pane's interactive features.
     * The content will be scaled using zooming gestures, panned with mouse drag (if enabled),
     * and scrollbars can optionally be shown.
     * <p>
     * The minimum scale factor is fixed at 1. The {@code maxScaleFactor} must be ≥ 1 and
     * {@code deltaScaleFactor} must be > 0. {@code maxScaleFactor} should ideally be a multiple
     * of {@code deltaScaleFactor} for smoother zooming steps.
     *
     * @param content the {@link Pane} to be displayed inside the scroll pane. Cannot be {@code null}.
     * @param maxScaleFactor the maximum zoom level allowed (e.g., 5.0 for 500% zoom). Must be ≥ 1.
     * @param deltaScaleFactor the zoom increment applied per zoom gesture (e.g., 0.25). Must be > 0.
     * @param enableZoom whether zooming is enabled via mouse scroll.
     * @param enablePanning whether panning is enabled via mouse drag.
     * @param enableScrollbars whether scrollbars are shown.
     * @param enableClipping whether the panel clips overflows of its contents.
     * @throws IllegalArgumentException if {@code content} is {@code null}, {@code maxScaleFactor} < 1,
     *         or {@code deltaScaleFactor} ≤ 0.
     */
    public ContentZoomScrollPane(Pane content, double maxScaleFactor, double deltaScaleFactor,
                                 boolean enableZoom, boolean enablePanning, boolean enableScrollbars, boolean enableClipping) {
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

        // Always set
        enableContentResize();

        // Optional
        if(enableZoom) {
            enableZoom();
        }
        if(enablePanning) {
            enablePanning();
        }

        enableScrollbars(enableScrollbars);

        if(enableClipping) {
            enableClipping();
        }
    }

    /**
     * Creates a new {@code ContentZoomScrollPane} with zooming and panning enabled by default,
     * scrollbars disabled and clipping enabled.
     * <p>
     * This is a convenience constructor that sets:
     * <ul>
     *   <li>{@code enableZoom = true}</li>
     *   <li>{@code enablePanning = true}</li>
     *   <li>{@code enableScrollbars = false}</li>
     *   <li>{@code enableClipping = true}</li>
     * </ul>
     * The minimum scale factor is fixed at 1. The {@code maxScaleFactor} must be ≥ 1 and
     * {@code deltaScaleFactor} must be > 0. {@code maxScaleFactor} should ideally be a multiple
     * of {@code deltaScaleFactor} for smoother zooming steps.
     *
     * @param content the {@link Pane} to be displayed inside the scroll pane. Cannot be {@code null}.
     * @param maxScaleFactor the maximum zoom level allowed (e.g., 5.0 for 500% zoom). Must be ≥ 1.
     * @param deltaScaleFactor the zoom increment applied per zoom gesture (e.g., 0.25). Must be > 0.
     * @throws IllegalArgumentException if {@code content} is {@code null}, {@code maxScaleFactor} < 1,
     *         or {@code deltaScaleFactor} ≤ 0.
     */
    public ContentZoomScrollPane(Pane content, double maxScaleFactor, double deltaScaleFactor) {
        this(content, maxScaleFactor, deltaScaleFactor, true, true, false, true);
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
     * Resize the content pane if this panel is resized.
     */
    private void enableScrollbars(boolean enable) {
        if(enable) {
            setVbarPolicy(ScrollBarPolicy.ALWAYS);
            setHbarPolicy(ScrollBarPolicy.ALWAYS);
        } else {
            setVbarPolicy(ScrollBarPolicy.NEVER);
            setHbarPolicy(ScrollBarPolicy.NEVER);
        }
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
    }

    /*
     * Clips the content against any overflows.
     */
    private void enableClipping() {
        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(this.content.widthProperty());
        clipRect.heightProperty().bind(this.content.heightProperty());
        this.content.setClip(clipRect);
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
        double oldScale = scaleFactorProperty.doubleValue();
        double targetScaleTotal = oldScale + direction.getValue() * deltaScaleFactor;

        // Clamp target scale to min/max bounds
        targetScaleTotal = Math.max(minScaleFactor, Math.min(maxScaleFactor, targetScaleTotal));

        if (targetScaleTotal == oldScale) {
            return; // No change in scale (already at min/max or delta is too small)
        }

        Bounds viewportBounds = getViewportBounds();
        Bounds contentUnscaledBounds = content.getLayoutBounds(); // Use layout bounds for true unscaled size

        // Convert pivot point from ScrollPane coordinates to content's local (unscaled) coordinates
        Point2D pivotInScrollPane = new Point2D(pivotX, pivotY);
        Point2D pivotInScene = this.localToScene(pivotInScrollPane);
        Point2D pivotInContentLocal = content.sceneToLocal(pivotInScene);

        // Apply scaling transform to the content
        content.setScaleX(targetScaleTotal);
        content.setScaleY(targetScaleTotal);
        scaleFactorProperty.set(targetScaleTotal); // Update the scale property

        // Calculate new scroll values to keep the pivot point stationary in the viewport
        double newScaledContentWidth = contentUnscaledBounds.getWidth() * targetScaleTotal;
        double newScaledContentHeight = contentUnscaledBounds.getHeight() * targetScaleTotal;

        // Calculate the scroll extent (how much the content exceeds the viewport)
        double newHorizontalScrollExtent = Math.max(0, newScaledContentWidth - viewportBounds.getWidth());
        double newVerticalScrollExtent = Math.max(0, newScaledContentHeight - viewportBounds.getHeight());

        // The desired position (in pixels) of the top-left of the viewport relative to the top-left of the scaled content
        double newScrollXPixels = (pivotInContentLocal.getX() * targetScaleTotal) - pivotX;
        double newScrollYPixels = (pivotInContentLocal.getY() * targetScaleTotal) - pivotY;

        // Convert pixel scroll values to ScrollPane's HValue/VValue (0-1 range)
        if (newHorizontalScrollExtent == 0) {
            setHvalue(0); // Content fits or is smaller, so align to left.
        } else {
            setHvalue(newScrollXPixels / newHorizontalScrollExtent);
        }

        if (newVerticalScrollExtent == 0) {
            setVvalue(0); // Content fits or is smaller, so align to top.
        } else {
            setVvalue(newScrollYPixels / newVerticalScrollExtent);
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
