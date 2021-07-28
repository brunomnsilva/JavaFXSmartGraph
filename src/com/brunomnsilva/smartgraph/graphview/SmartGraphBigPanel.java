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

import com.brunomnsilva.smartgraph.graph.Graph;
import java.net.URI;
import javafx.scene.Scene;

/**
 *
 * @author pantape.k@gmail.com
 */
public class SmartGraphBigPanel<V, E> extends SmartGraphPane {

    private SmartGraphPanel graphView;
    private double iniWidth, iniHeight;

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties and default random
     * placement of vertices.
     *
     * @param theGraph underlying graph
     *
     * @see Graph
     */
    public SmartGraphBigPanel(Graph<V, E> theGraph) {
        this(theGraph, new SmartGraphProperties(), null);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties and default random
     * placement of vertices.
     *
     * @param theGraph underlying graph
     * @param properties custom properties
     */
    public SmartGraphBigPanel(Graph<V, E> theGraph, SmartGraphProperties properties) {
        this(theGraph, properties, null);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties and custom placement of
     * vertices.
     *
     * @param theGraph underlying graph
     * @param placementStrategy placement strategy, null for default
     */
    public SmartGraphBigPanel(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy) {
        this(theGraph, null, placementStrategy);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties and custom placement of
     * vertices.
     *
     * @param theGraph underlying graph
     * @param properties custom properties, null for default
     * @param placementStrategy placement strategy, null for default
     */
    public SmartGraphBigPanel(Graph<V, E> theGraph, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy) {

        this(theGraph, properties, placementStrategy, null);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties and custom placement of
     * vertices.
     *
     * @param theGraph underlying graph
     * @param properties custom properties, null for default
     * @param placementStrategy placement strategy, null for default
     * @param cssFile alternative css file, instead of default 'smartgraph.css'
     */
    public SmartGraphBigPanel(Graph<V, E> theGraph, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy, URI cssFile) {

        this(new SmartGraphPanel(theGraph, properties, placementStrategy, cssFile));

    }

    /**
     * Constructs a visualization of the graph from existing SmartGraphPanel
     * @param graphPanel SmartGraphPanel
     */
    public SmartGraphBigPanel(SmartGraphPanel graphPanel) {
        this.iniWidth = 0;
        this.iniHeight = 0;

        this.graphView = graphPanel;

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

            if (!this.graphView.isInitialized()) {
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

            }

            // translate to center
            this.graphView.setTranslateX(0.5 * (w - this.iniWidth));
            this.graphView.setTranslateY(0.5 * (h - this.iniHeight));
        }
    }

    @Override
    public SmartGraphPanel getGraphView() {
        return this.graphView;
    }
}
