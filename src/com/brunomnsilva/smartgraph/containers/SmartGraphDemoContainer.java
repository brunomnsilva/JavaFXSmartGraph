/*
 * The MIT License
 *
 * Copyright 2019 Bruno Silva.
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

import com.brunomnsilva.smartgraph.graphview.SmartForceDirectedGraphView;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPane;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.Pane;

/**
 *
 * @author Bruno Silva
 */
public class SmartGraphDemoContainer extends BorderPane {

    public SmartGraphDemoContainer(SmartGraphPanel graphView) {
        this(new SmartGraphPane() {
            @Override
            public SmartGraphPanel getGraphView() {
                return graphView;
            }
        });
    }

    public SmartGraphDemoContainer(SmartForceDirectedGraphView graphView) {
        this.setup(graphView, graphView.automaticLayoutProperty());
    }

    public SmartGraphDemoContainer(SmartGraphPane pane) {

        this.setup(pane, pane.getGraphView().automaticLayoutProperty());
    }

    private void setup(Pane pane, BooleanProperty autoLayoutProperty) {

        this.setCenter(new ContentZoomPane(pane));

        //create bottom pane with controls
        HBox bottom = new HBox(10);

        CheckBox automatic = new CheckBox("Automatic layout");
        automatic.selectedProperty().bindBidirectional(autoLayoutProperty);

        bottom.getChildren().add(automatic);

        setBottom(bottom);
    }
}
