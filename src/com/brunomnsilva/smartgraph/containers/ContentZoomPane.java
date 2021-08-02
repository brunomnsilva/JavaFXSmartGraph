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
package com.brunomnsilva.smartgraph.containers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * This class provides zooming and panning for a JavaFX node.
 *
 * It shows the zoom level with a slider control and reacts to mouse scrolls and
 * mouse dragging.
 *
 * The content node is out forward in the z-index so it can react to mouse
 * events first. The node should consume any event not meant to propagate to
 * this pane.
 *
 * @author brunomnsilva
 */
public class ContentZoomPane extends BorderPane {

    /*
    PAN AND ZOOM
     */
    private final DoubleProperty scaleFactorProperty = new ReadOnlyDoubleWrapper(1);
    private final Node content;

    private static final double MIN_SCALE = 0.25;
    private static final double MAX_SCALE = 5.0;
    private static final double SCROLL_DELTA = 0.25;

    private double viewX = 0;
    private double viewY = 0;

    public ContentZoomPane(Node content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        this.content = content;
        this.content.toFront();

        this.setCenter(this.content);
        this.setRight(createSlider());

        this.enablePanAndZoom();

        this.widthProperty().addListener((obs, ov, nv) -> {
            this.viewX = (double) nv / 2;
        });

        this.heightProperty().addListener((obs, ov, nv) -> {
            this.viewY = (double) nv / 2;
        });
    }

    private Node createSlider() {

        Slider slider = new Slider(MIN_SCALE, MAX_SCALE, MIN_SCALE);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(SCROLL_DELTA);
        slider.setMinorTickCount(1);
        slider.setBlockIncrement(0.125f);
        slider.valueProperty().bindBidirectional(this.scaleFactorProperty());

        Text zoom = new Text("Zoom");
        Text zoomValue = new Text();
        zoomValue.textProperty().bind(
                Bindings.format("x %.2f", slider.valueProperty())
        );

        VBox paneSlider = new VBox(slider, zoom, zoomValue);
        paneSlider.setPadding(new Insets(10, 10, 10, 10));
        paneSlider.setSpacing(2);

        return paneSlider;
    }

    public void setContentPivot(double x, double y) {
        content.setTranslateX(content.getTranslateX() - x);
        content.setTranslateY(content.getTranslateY() - y);
    }

    public static double boundValue(double value, double min, double max) {

        if (Double.compare(value, min) < 0) {
            return min;
        }

        if (Double.compare(value, max) > 0) {
            return max;
        }

        return value;
    }

    private void enablePanAndZoom() {

        this.scaleFactorProperty.addListener((obs, ov, nv) -> {
            double newScale = (double) nv;
            double currentScale = (double) ov;

            this.content.setScaleX(newScale);
            this.content.setScaleY(newScale);

            Bounds bounds = this.content.localToScene(this.content.getBoundsInLocal());
            double f = (newScale / currentScale) - 1;
            double dx = (this.viewX - (bounds.getWidth() / 2 + bounds.getMinX()));
            double dy = (this.viewY - (bounds.getHeight() / 2 + bounds.getMinY()));

            this.setContentPivot(f * dx, f * dy);

        });

        this.setOnScroll((ScrollEvent event) -> {

            double direction = event.getDeltaY() >= 0 ? 1 : -1;

            double currentScale = scaleFactorProperty.getValue();
            double computedScale = currentScale + direction * SCROLL_DELTA;
            if (computedScale >= MIN_SCALE) {
                this.viewX = event.getX();
                this.viewY = event.getY();

                this.scaleFactorProperty.set(computedScale);
            }

            //do not propagate
            event.consume();

        });

        final DragContext sceneDragContext = new DragContext();

        this.setOnMousePressed((MouseEvent event) -> {

            if (event.isPrimaryButtonDown()) {
                this.getScene().setCursor(Cursor.MOVE);

                sceneDragContext.mouseAnchorX = event.getX();
                sceneDragContext.mouseAnchorY = event.getY();

                sceneDragContext.translateAnchorX = content.getTranslateX();
                sceneDragContext.translateAnchorY = content.getTranslateY();
            }

        });

        this.setOnMouseReleased((MouseEvent event) -> {
            this.getScene().setCursor(Cursor.DEFAULT);
        });

        setOnMouseDragged((MouseEvent event) -> {
            if (event.isPrimaryButtonDown()) {
                this.content.setTranslateX(sceneDragContext.translateAnchorX + event.getX() - sceneDragContext.mouseAnchorX);
                this.content.setTranslateY(sceneDragContext.translateAnchorY + event.getY() - sceneDragContext.mouseAnchorY);
            }
        });

    }

    public DoubleProperty scaleFactorProperty() {
        return scaleFactorProperty;
    }

    class DragContext {

        double mouseAnchorX;
        double mouseAnchorY;

        double translateAnchorX;
        double translateAnchorY;

    }

}
