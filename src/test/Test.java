/* 
 * The MIT License
 *
 * Copyright 2019 brunomnsilva@gmail.com.
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
package test;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartStylableNode;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.stage.Window;

/**
 *
 * @author brunomnsilva
 */
public class Test extends Application {

    private volatile boolean running;

    @Override
    public void start(Stage ignored) {

        Graph<String, String> g = build_sample_digraph();
        System.out.println(g);
        
        SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();
        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(g, strategy);

        /*
        Basic usage:            
        Use SmartGraphDemoContainer if you want zoom capabilities and automatic layout toggling
        */
        //Scene scene = new Scene(graphView, 1024, 768);
        Scene scene = new Scene(new SmartGraphDemoContainer(graphView), 1024, 768);


        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFX SmartGraph Visualization");
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setScene(scene);
        stage.show();

        /*
        IMPORTANT: Must call init() after scene is displayed so we can have width and height values
        to initially place the vertices according to the placement strategy
        */
        //graphView.init();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private Graph<String, String> build_sample_digraph() {

        Digraph<String, String> g = new DigraphEdgeList<>();

        g.insertVertex("A");
        g.insertVertex("B");
        g.insertVertex("C");
        g.insertVertex("D");
        g.insertVertex("E");
        g.insertVertex("F");
        g.insertVertex("G1");
        g.insertVertex("G2");
        g.insertVertex("G3");
        g.insertVertex("G4");
        g.insertVertex("G5");
        g.insertVertex("G6");
        g.insertVertex("G7");
        g.insertVertex("G8");
        g.insertVertex("G9");
        g.insertVertex("G10");

        g.insertEdge("A", "B", "AB");
        g.insertEdge("B", "A", "AB2");
        g.insertEdge("A", "C", "AC");
        g.insertEdge("A", "D", "AD");
        g.insertEdge("B", "C", "BC");
        g.insertEdge("C", "D", "CD");
        g.insertEdge("D", "E", "DE");
        g.insertEdge("F", "D", "DF1");
        g.insertEdge("F", "D", "DF2");
        g.insertEdge("F", "D", "DF3");
        g.insertEdge("D", "F", "FD1");
        g.insertEdge("D", "F", "FD2");
        g.insertEdge("D", "F", "FD3");
        g.insertEdge("G1", "F", "GF1");
        g.insertEdge("G2", "F", "GF2");
        g.insertEdge("G3", "F", "GF3");
        g.insertEdge("G4", "F", "GF4");
        g.insertEdge("G5", "F", "GF5");
        g.insertEdge("G6", "F", "GF6");
        g.insertEdge("G7", "F", "GF7");
        g.insertEdge("G8", "F", "GF8");
        g.insertEdge("G9", "F", "GF9");
        g.insertEdge("G10", "F", "GF10");

        //yep, its a loop!
        g.insertEdge("A", "A", "AA1");
        g.insertEdge("A", "A", "AA2");
        g.insertEdge("A", "A", "AA3");
        g.insertEdge("E", "E", "EE1");
        g.insertEdge("E", "E", "EE2");
        g.insertEdge("E", "E", "EE3");
        g.insertEdge("E", "E", "EE4");
        g.insertEdge("E", "E", "EE5");
        g.insertEdge("E", "E", "EE6");
        g.insertEdge("E", "E", "EE7");
        g.insertEdge("E", "E", "EE8");
        g.insertEdge("E", "E", "EE9");
        g.insertEdge("E", "E", "EE10");
        g.insertEdge("F", "F", "FF");

        return g;
    }   
}
