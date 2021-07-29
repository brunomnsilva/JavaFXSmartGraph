/* 
 * The MIT License
 *
 * Copyright 2019 
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

import javafx.beans.property.BooleanProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

/**
 *
 * @author pantape.k@gmail.com
 */
public class BigSmartGraphPane extends SmartGraphView {

    private final SmartGraphView graphView;
    private double iniWidth, iniHeight;
    private boolean isInitialized;


    /**
     * Constructs a visualization of the graph from existing SmartGraphPanel
     * @param graphView graph view
     */
    public BigSmartGraphPane(SmartGraphView graphView) {
        this.iniWidth = 0;
        this.iniHeight = 0;
        this.isInitialized = false;

        this.graphView = graphView;

        this.widthProperty().addListener((obs, ov, nv) -> {
            this.adjustView();
        });
        this.heightProperty().addListener((obs, ov, nv) -> {
            this.adjustView();
        });

    }

    private void adjustView() {
        double w = this.getWidth();
        double h = this.getHeight();

        if (w > 0 && h > 0) {

            if (!this.isInitialized) {
                // set size of the graph panel, 2 time of its parent
                this.iniWidth = w * 2;
                this.iniHeight = h * 2;

                // get graph panel size
                this.graphView.setPrefSize(this.iniWidth, this.iniHeight);
                Scene scene = new Scene(this.graphView, this.iniWidth, this.iniHeight);
                this.graphView.applyCss();
                this.graphView.layout();

                // add to pane
                this.getChildren().add(this.graphView);

                // initialize graph panel
                this.graphView.init();
                
                this.isInitialized = true;
            }

            // translate to center
            this.graphView.setTranslateX(0.5 * (w - this.iniWidth));
            this.graphView.setTranslateY(0.5 * (h - this.iniHeight));
        }
    }

    @Override
    public BooleanProperty automaticLayoutProperty() {
        return this.graphView.automaticLayoutProperty();
    }

    @Override
    public void init() {
        this.graphView.init();
    }

    @Override
    public boolean isInitialized() {
        return this.graphView.isInitialized();
    }

    public Pane getGraphView() {
        return this.graphView;
    }
}
