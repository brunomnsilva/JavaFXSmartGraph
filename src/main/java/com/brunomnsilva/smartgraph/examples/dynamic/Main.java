/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2019-2025  brunomnsilva@gmail.com
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
package com.brunomnsilva.smartgraph.examples.dynamic;

import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that provides an example of using the library.
 *
 * @author brunomnsilva
 */
public class Main extends Application {

    private volatile boolean running;

    @Override
    public void start(Stage ignored) {

        Graph<String, String> g = build_sample_graph();
        
        SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();
        ForceDirectedLayoutStrategy<String> automaticPlacementStrategy = new ForceDirectedSpringGravityLayoutStrategy<>();

        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(g, initialPlacement, automaticPlacementStrategy);
        graphView.setAutomaticLayout(true);

        Scene scene = new Scene(new SmartGraphDemoContainer(graphView), 1024, 768);

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFX SmartGraph Visualization");
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setScene(scene);
        stage.show();

        // Programmatically define the radius of a vertex. In this case the vertex size will increase with its connectivity
        graphView.setVertexRadiusProvider(vertexElement -> 10 + getIncidentEdgeCount(g, vertexElement) * 1.5);

        /* Double click will remove a vertex */
        graphView.setVertexDoubleClickAction((SmartGraphVertex<String> graphVertex) -> {
            g.removeVertex(graphVertex.getUnderlyingVertex());
            graphView.update();
        });

        /* Double click will remove an edge */
        graphView.setEdgeDoubleClickAction(graphEdge -> {
            Edge<String, String> underlyingEdge = graphEdge.getUnderlyingEdge();
            g.removeEdge(underlyingEdge);
            graphView.update();
        });

        /*
        IMPORTANT: Must call init() after scene is displayed, so we can have width and height values
        to initially place the vertices according to the placement strategy.
        */
        graphView.init();

        continuously_test_adding_elements(g, graphView);

        stage.setOnCloseRequest(event -> {
            running = false;
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private Graph<String, String> build_sample_graph() {

        Graph<String, String> g = new GraphEdgeList<>();

        g.insertVertex("A");
        g.insertVertex("B");
        g.insertVertex("C");
        g.insertVertex("D");


        g.insertEdge("A", "B", "AB");
        g.insertEdge("A", "C", "AC");
        g.insertEdge("A", "D", "AD");
        g.insertEdge("B", "C", "BC");
        g.insertEdge("C", "D", "CD");

        return g;
    }

    private static final Random random = new Random(/* seed to reproduce*/);

    private void continuously_test_adding_elements(Graph<String, String> g, SmartGraphPanel<String, String> graphView) {
        //update graph
        running = true;
        final long ITERATION_WAIT = 3000; //milliseconds

        Runnable r;
        r = () -> {
            int count = 0;
            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            while (running) {
                try {
                    Thread.sleep(ITERATION_WAIT);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //generate new vertex with 2/3 probability, else connect two existing
                String id = String.format("%02d", ++count);
                if (random.nextInt(3) < 2) {
                    //add a new vertex connected to a random existing vertex
                    Vertex<String> existing = get_random_vertex(g);
                    Vertex<String> vertexId = g.insertVertex(("V" + id));
                    g.insertEdge(existing, vertexId, ("E" + id));
                    
                    //this variant must be called to ensure the view has reflected the
                    //underlying graph before styling a node immediately after.
                    graphView.updateAndWait();

                } else {
                    Vertex<String> existing1 = get_random_vertex(g);
                    Vertex<String> existing2 = get_random_vertex(g);
                    g.insertEdge(existing1, existing2, ("E" + id));
                    
                    graphView.update();
                }
            }
        };

        new Thread(r).start();
    }

    private static Vertex<String> get_random_vertex(Graph<String, String> g) {

        int size = g.numVertices();
        int rand = random.nextInt(size);
        Vertex<String> existing = null;
        int i = 0;
        for (Vertex<String> v : g.vertices()) {
            existing = v;
            if (i++ == rand) {
                break;
            }
        }
        return existing;
    }


    private static int getIncidentEdgeCount(Graph<String, String> model, String vertexElement) {
        for (Vertex<String> vertex : model.vertices()) {
            if(vertex.element().equalsIgnoreCase(vertexElement)) {
                return model.incidentEdges(vertex).size();
            }
        }
        return 0;
    }
}
