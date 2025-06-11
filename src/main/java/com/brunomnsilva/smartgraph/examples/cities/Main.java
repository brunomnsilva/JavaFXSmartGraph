/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2023-2025  brunomnsilva@gmail.com
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

package com.brunomnsilva.smartgraph.examples.cities;

import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Example program that depicts some world cities and their distances.
 * @author brunomnsilva
 */
public class Main extends Application {

    @Override
    public void start(Stage ignored) {
        
        Graph<City, Distance> distances = new GraphEdgeList<>();

        /* Vertex radius will be derived from the city's population */
        Vertex<City> prague = distances.insertVertex(new City("Prague", 1.3f));
        Vertex<City> tokyo = distances.insertVertex(new City("Tokyo", 37.5f));
        Vertex<City> beijing = distances.insertVertex(new City("Beijing", 21.5f));
        Vertex<City> newYork = distances.insertVertex(new City("New York", 19.5f));
        Vertex<City> london = distances.insertVertex(new City("London", 14.4f));
        Vertex<City> helsinky = distances.insertVertex(new City("Helsinky", 0.6f));
        
        distances.insertEdge(tokyo, newYork, new Distance(10838));
        distances.insertEdge(beijing, newYork, new Distance(11550));
        distances.insertEdge(beijing, tokyo, new Distance(1303));
        distances.insertEdge(london, newYork, new Distance(5567));
        distances.insertEdge(london, prague, new Distance(1264));
        distances.insertEdge(helsinky, tokyo, new Distance(7815));
        distances.insertEdge(prague, helsinky, new Distance(1845));
        distances.insertEdge(beijing, london, new Distance(8132));
        
        SmartGraphPanel<City, Distance> graphView = new SmartGraphPanel<>(distances, new SmartCircularSortedPlacementStrategy());

        Scene scene = new Scene(new SmartGraphDemoContainer(graphView), 1024, 768);

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFX SmartGraph City Distances");
        stage.setMinWidth(800);
        stage.setMinHeight(400);
        stage.setScene(scene);
        stage.show();
        
        graphView.init();

        graphView.setVertexPosition(beijing, 520, 284);      // Center-ish
        graphView.setVertexPosition(tokyo,   820, 250);      // Far right-top
        graphView.setVertexPosition(newYork, 150, 600);      // Far left-bottom
        graphView.setVertexPosition(london,  250, 200);      // Left-top
        graphView.setVertexPosition(prague,  450, 600);      // Center-bottom
        graphView.setVertexPosition(helsinky,650, 120);      // Right-top

        /*
        * This illustrates setting an image to the background of a node.
        * By default, the css class "vertex" is applied to all vertices.
        * Note that all inline styles have
        * priority over any properties set in css classes, even if they are applied cumulatively through
        * .addStyleClass(class). However, inline styles can be overwritten by using .setStyleInline(css);
        * also, when you use .setStyleClass(class), all previous styles will be discarded, including inline.
        */
        graphView.getStylableVertex(tokyo).setStyleInline("-fx-fill: url(\"file:squares.jpg\");");
        //graphVertex.setStyleInline("-fx-fill: red;"); //this will overwrite the property later on

        graphView.setVertexDoubleClickAction(graphVertex -> {
            //toggle different styling
            if( !graphVertex.removeStyleClass("myVertex") ) {
                graphVertex.addStyleClass("myVertex");
            }
            // Use instead (to set it permanently): graphVertex.setStyleClass("myVertex");
        });

    }

    /**
     * Main program.
     * @param args program arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}