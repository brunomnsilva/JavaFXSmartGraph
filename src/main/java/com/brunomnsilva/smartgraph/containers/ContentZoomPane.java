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
 * This class provides zooming and panning for a JavaFX node.
 * <br/>
 * Reacts to mouse scrolls and
 * mouse dragging.
 * <br/>
 * The content node is out forward in the z-index, so it can react to mouse
 * events first. The node should consume any event not meant to propagate to
 * this pane.
 *
 * @author brunomnsilva
 */
public class ContentZoomPane extends BorderPane {

    private final DoubleProperty scaleFactorProperty = new ReadOnlyDoubleWrapper(1);
    private final Node content;

    public static final double MIN_SCALE = 1;
    public static final double MAX_SCALE = 5;
    public static final double SCROLL_DELTA = 0.25;

    public ContentZoomPane(Node content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        content.toFront();

        // Apply same background color of graph to mask the "panning"
        try {
            File f = new File("smartgraph.css");
            String css = f.toURI().toURL().toExternalForm();
            getStylesheets().add(css);
            this.getStyleClass().add("graph");
        } catch (MalformedURLException e) {
            // do nothing
        }

        setCenter(this.content = content);
        enablePanAndZoom();
        enableResizeListener();
    }

    public DoubleProperty scaleFactorProperty() {
        return scaleFactorProperty;
    }

    private void setContentPivot(double x, double y) {
        content.setTranslateX(content.getTranslateX() - x);
        content.setTranslateY(content.getTranslateY() - y);
    }

    private static double boundValue(double value, double min, double max) {

        if (Double.compare(value, min) < 0) {
            return min;
        }

        if (Double.compare(value, max) > 0) {
            return max;
        }

        return value;
    }

    private void enableResizeListener() {
        this.widthProperty().addListener((observableValue, oldValue, newValue) -> clipArea());
        this.heightProperty().addListener((observableValue, oldValue, newValue) -> clipArea());
    }

    private void enablePanAndZoom() {

        setOnScroll((ScrollEvent event) -> {

            double direction = event.getDeltaY() >= 0 ? 1 : -1;

            double currentScale = scaleFactorProperty.getValue();
            double computedScale = currentScale + direction * SCROLL_DELTA;

            computedScale = boundValue(computedScale, MIN_SCALE, MAX_SCALE);

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

        setOnMouseReleased((MouseEvent event) -> {
            getScene().setCursor(Cursor.DEFAULT);
        });

        setOnMouseDragged((MouseEvent event) -> {
            if (event.isSecondaryButtonDown()) {
                
                content.setTranslateX(sceneDragContext.translateAnchorX + event.getX() - sceneDragContext.mouseAnchorX);
                content.setTranslateY(sceneDragContext.translateAnchorY + event.getY() - sceneDragContext.mouseAnchorY);

                clipArea();
            }
        });

    }

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
