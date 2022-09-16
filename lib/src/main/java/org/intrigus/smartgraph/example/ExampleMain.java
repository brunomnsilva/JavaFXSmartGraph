/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.intrigus.smartgraph.example;

import org.intrigus.smartgraph.containers.SmartGraphDemoContainer;
import org.intrigus.smartgraph.graph.Graph;
import org.intrigus.smartgraph.graph.GraphEdgeList;
import org.intrigus.smartgraph.graph.Vertex;
import org.intrigus.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import org.intrigus.smartgraph.graphview.SmartGraphPanel;
import org.intrigus.smartgraph.graphview.SmartGraphProperties;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author brunomnsilva
 */
public class ExampleMain extends Application {

    @Override
    public void start(Stage ignored) {
        
        Graph<City, Distance> distances = new GraphEdgeList<>();
        
        Vertex<City> prague = distances.insertVertex(new City("Prague", 0));
        Vertex<City> tokyo = distances.insertVertex(new City("Tokyo", 0));
        Vertex<City> beijing = distances.insertVertex(new City("Beijing", 0));
        Vertex<City> newYork = distances.insertVertex(new City("New York", 0));
        Vertex<City> london = distances.insertVertex(new City("London", 0));
        Vertex<City> helsinky = distances.insertVertex(new City("Helsinky", 0));
        
        distances.insertEdge(tokyo, newYork, new Distance(10838));
        distances.insertEdge(beijing, newYork, new Distance(11550));
        distances.insertEdge(beijing, tokyo, new Distance(1303));
        distances.insertEdge(london, newYork, new Distance(5567));
        distances.insertEdge(london, prague, new Distance(1264));
        distances.insertEdge(helsinky, tokyo, new Distance(7815));
        distances.insertEdge(prague, helsinky, new Distance(1845));
        distances.insertEdge(beijing, london, new Distance(8132));
        
        /* Only Java 15 allows for multi-line strings */
        String customProps = "edge.label = true" + "\n" + "edge.arrow = false";
                
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
        
        /* You can mannualy place vertices at any time. However, these are
        absolute coordinates inside the container panel. */
        graphView.setVertexPosition(beijing, 100, 100);
        graphView.setVertexPosition(helsinky, 924, 100);
        graphView.setVertexPosition(london, 200, 668);
        graphView.setVertexPosition(prague, 824, 668);
        graphView.setVertexPosition(tokyo, 512, 300);
        graphView.setVertexPosition(newYork, 512, 400);
        
        graphView.getStylableLabel(tokyo).setStyle("-fx-stroke: red; -fx-fill: red;");
        
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}