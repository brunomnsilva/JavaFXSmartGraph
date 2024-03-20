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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * A simple container that provides zoom and toggling of automatic layout of a SmartGraphPanel instance. It can
 * also force update of the SmartGraphPanel instance for testing purposes.
 * <br/>
 * It shows the current zoom level with a slider control.
 *
 * @author brunomnsilva
 */
public class SmartGraphDemoContainer extends BorderPane {

    private final ContentZoomPane contentZoomPane;

    /**
     * Creates a new instance of SmartGraphDemoContainer pane.
     * @param graphView the SmartGraphPanel instance to show and control
     */
    public SmartGraphDemoContainer(SmartGraphPanel<?,?> graphView) {
        if(graphView == null) throw new IllegalArgumentException("View cannot be null.");

        setCenter(this.contentZoomPane = new ContentZoomPane(graphView));

        Background background = new Background(new BackgroundFill(Color.WHITE, null, null));

        setRight(createSidebar(this.contentZoomPane, background));
        setBottom(createBottomBar(graphView, background));
    }

    /**
     * Create bottom pane with automatic layout toggle, force update button and help.
     * @param view the SmartGraphPanel instance to control
     * @param bg the background to apply
     * @return the bottom pane
     */
    private Node createBottomBar(SmartGraphPanel<?,?> view, Background bg) {
        HBox bar = new HBox(20);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(10));
        bar.setBackground(bg);

        /* Create toggle to control automatic layout */
        CheckBox automatic = new CheckBox("Automatic layout");
        automatic.selectedProperty().bindBidirectional(view.automaticLayoutProperty());

        /* Create button to force SmartGraphPanel update() */
        Button btUpdate = new Button("Force update");
        btUpdate.setOnAction(actionEvent -> view.update());

        /* Create help */
        Text helpLabel = new Text("?");
        helpLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Create a Tooltip with the help message
        Tooltip tooltip = new Tooltip("Mouse wheel to zoom; left-click to drag and interact; right-click for panning.");

        // Attach the Tooltip to the Label
        Tooltip.install(helpLabel, tooltip);

        /* Add components */
        bar.getChildren().addAll(automatic,
                new Separator(Orientation.VERTICAL),
                btUpdate,
                new Separator(Orientation.VERTICAL),
                helpLabel);

        return bar;
    }

    /**
     * Creates a sidebar with slider control pane to control the zoom level
     * @param zoomPane the ContentZoomPane instance to control
     * @param bg the background to apply
     * @return the side pane
     */
    private Node createSidebar(ContentZoomPane zoomPane, Background bg) {
        VBox paneSlider = new VBox(10);
        paneSlider.setAlignment(Pos.CENTER);
        paneSlider.setPadding(new Insets(10));
        paneSlider.setSpacing(10);
        paneSlider.setBackground(bg);

        /* Create slider to control zoom level */
        Slider slider = new Slider(zoomPane.getMinScaleFactor(),
                contentZoomPane.getMaxScaleFactor(), zoomPane.getMinScaleFactor());

        slider.setOrientation(Orientation.VERTICAL);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(zoomPane.getDeltaScaleFactor());
        slider.setMinorTickCount(1);
        slider.setBlockIncrement(0.125f);
        slider.setSnapToTicks(true);

        slider.valueProperty().bind(zoomPane.scaleFactorProperty());

        /* Add components */
        paneSlider.getChildren().addAll(slider, new Text("Zoom"));

        return paneSlider;
    }
    
}
