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

import com.brunomnsilva.smartgraph.graph.Graph;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartForceDirectedGraphView;
import com.brunomnsilva.smartgraph.graphview.BigSmartGraphPane;
import com.brunomnsilva.smartgraph.graphview.SmartRandomNearCenterPlacementStrategy;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

/**
 *
 * @author brunomnsilva
 */
public class Test3 extends Application {

    private volatile boolean running;

    @Override
    public void start(Stage ignored) {

        Graph<Object, String> g = build_sample_digraph();
        System.out.println(g);
        
        SmartPlacementStrategy strategy = new SmartRandomNearCenterPlacementStrategy();
        BigSmartGraphPane graphView = new BigSmartGraphPane(new SmartForceDirectedGraphView(g, strategy));

        /*
        Basic usage:            
        Use SmartGraphDemoContainer if you want zoom capabilities and automatic layout toggling
        */
        //Scene scene = new Scene(graphView, 1024, 768);
        Scene scene = new Scene(new SmartGraphDemoContainer(graphView), 1024, 768);


        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFX SmartGraph Visualization");
        stage.setScene(scene);
        stage.show();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private Graph<Object, String> build_sample_digraph() {

        Digraph<Object, String> g = new DigraphEdgeList<>();
        
        Label a = new Label("Ants are waling down the street.");
        Label b = new Label("Birds are singing");
        Label c = new Label("Cats are watching the singing birds");
        Label d = new Label("Dogs ard sleeping");
        Circle e = new Circle(0, 0, 50);
        e.setStyle("-fx-stroke-color: blue; -fx-fill: rgba(0, 0, 255, 0.25);");

        g.insertVertex(a);
        g.insertVertex(b);
        g.insertVertex(c);
        g.insertVertex(d);
        g.insertVertex(e);
        g.insertVertex("F");

        g.insertEdge(a, b, "AB");
        g.insertEdge(b, a, "BA");
        g.insertEdge(a, c, "AC");
        g.insertEdge(a, d, "AD");
        g.insertEdge(a, e, "AE");
        g.insertEdge(b, c, "BC");
        g.insertEdge(c, d, "CD1");
        g.insertEdge(c, d, "CD2");
        g.insertEdge(c, d, "CD3");
        g.insertEdge(d, "F", "DF");

        //yep, its a loop!
        g.insertEdge(a, a, "AA1");
        g.insertEdge(a, a, "AA2");
        g.insertEdge(a, a, "AA3");

        return g;
    }   
}
