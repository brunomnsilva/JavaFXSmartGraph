/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2023-2024  brunomnsilva@gmail.com
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

package com.brunomnsilva.smartgraph.example;

import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Example program that depicts some world cities and their distances.
 * @author brunomnsilva
 */
public class ExampleMain extends Application {

    @Override
    public void start(Stage ignored) {
        
        Graph<City, Distance> distances = new GraphEdgeList<>();
        
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
        
        /* Only Java 15 allows for multi-line strings. */
        String customProps = "edge.label = true" + "\n" + "edge.arrow = true";
                
        SmartGraphProperties properties = new SmartGraphProperties(customProps);
        
        SmartGraphPanel<City, Distance> graphView = new SmartGraphPanel<>(distances, properties, new SmartCircularSortedPlacementStrategy());
        
        Scene scene = new Scene(new SmartGraphDemoContainer(graphView), 1024, 768);

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFX SmartGraph City Distances");
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setScene(scene);
        stage.show();
        
        graphView.init();
        
        //graphView.setAutomaticLayout(true);
        
        /* You can manually place vertices at any time. However, these are
        absolute coordinates inside the container panel.
        Careful choice of a panel's background image can allow you to overlay
        the vertices over a, e.g., world map.
        */
        graphView.setVertexPosition(beijing, 100, 100);
        graphView.setVertexPosition(helsinky, 924, 100);
        graphView.setVertexPosition(london, 200, 668);
        graphView.setVertexPosition(prague, 824, 668);
        graphView.setVertexPosition(tokyo, 512, 200);
        graphView.setVertexPosition(newYork, 512, 400);
        
        graphView.getStylableLabel(tokyo).setStyleInline("-fx-stroke: green; -fx-fill: green;");

        graphView.setVertexDoubleClickAction((SmartGraphVertex<City> graphVertex) -> {
            graphVertex.addStyleClass("myVertex");
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