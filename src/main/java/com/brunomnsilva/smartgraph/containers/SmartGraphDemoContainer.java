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

import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * A simple container that provides zoom and toggling of automatic layout of a SmartGraphPanel.
 * <br/>
 * It shows the current zoom level with a slider control.
 * @author brunomnsilva
 */
public class SmartGraphDemoContainer extends BorderPane {

    private final ContentZoomPane contentZoomPane;

    public SmartGraphDemoContainer(SmartGraphPanel<?,?> graphView) {
        if(graphView == null) throw new IllegalArgumentException("View cannot be null.");

        setCenter(this.contentZoomPane = new ContentZoomPane(graphView));

        // Create right side scale slider
        setRight(createSlider());

        // Create bottom pane with automatic layout toggle
        HBox bottom = new HBox(10);
        CheckBox automatic = new CheckBox("Automatic layout");
        automatic.selectedProperty().bindBidirectional(graphView.automaticLayoutProperty());
        bottom.getChildren().add(automatic);
        setBottom(bottom);
    }

    private Node createSlider() {

        Slider slider = new Slider(ContentZoomPane.MIN_SCALE,
                ContentZoomPane.MAX_SCALE, ContentZoomPane.MIN_SCALE);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(ContentZoomPane.SCROLL_DELTA);
        slider.setMinorTickCount(1);
        slider.setBlockIncrement(0.125f);
        slider.setSnapToTicks(true);

        Text label = new Text("Zoom");

        VBox paneSlider = new VBox(slider, label);

        paneSlider.setPadding(new Insets(10, 10, 10, 10));
        paneSlider.setSpacing(10);

        slider.valueProperty().bind(this.contentZoomPane.scaleFactorProperty());

        return paneSlider;
    }
    
}
