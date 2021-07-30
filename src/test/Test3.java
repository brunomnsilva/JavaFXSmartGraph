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
import com.brunomnsilva.smartgraph.graphview.SmartGraphPaneBig;
import com.brunomnsilva.smartgraph.graphview.SmartRandomNearCenterPlacementStrategy;
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
        SmartGraphPaneBig graphView = new SmartGraphPaneBig(new SmartForceDirectedGraphView(g, strategy));

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
        
        Label a = new Label("Ants");
        Label b = new Label("Birds");
        Label c = new Label("Cats");
        Label d = new Label("Dogs");
        Circle e = new Circle(0, 0, 10);
        e.setStyle("-fx-stroke-color: blue; -fx-stroke-with: 3; -fx-fill: rgba(255, 0, 0, 0.25);");

        g.insertVertex(a);
        g.insertVertex(b);
        g.insertVertex(c);
        g.insertVertex(d);
        g.insertVertex(e);
        g.insertVertex("Food");

        g.insertEdge(a, e, "go to");
        g.insertEdge(b, d, "sing");
        g.insertEdge(a, d, "bite");
        g.insertEdge(c, b, "watch1");
        g.insertEdge(c, b, "watch2");
        g.insertEdge(c, d, "fight");
        g.insertEdge(d, "Food", "eat");

        //yep, its a loop!
        g.insertEdge(c, c, "scratch");
        g.insertEdge(d, d, "jump");
        g.insertEdge(d, d, "wag tail");

        return g;
    }   
}
