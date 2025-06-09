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
package com.brunomnsilva.smartgraph.examples.flowers;

import com.brunomnsilva.fluentfxcss.FluentFxCss;
import com.brunomnsilva.fluentfxcss.definitions.*;
import com.brunomnsilva.fluentfxcss.enums.PseudoClassValue;
import com.brunomnsilva.fluentfxcss.enums.UnitValue;
import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.effect.BlurType;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class that provides an example of using the library.
 *
 * @author brunomnsilva
 */
public class Main extends Application {

    @Override
    public void start(Stage ignored) {

        Graph<String, String> g = build_flower_graph();
        System.out.println(g);

        URI stylesheetURI = createStylesheet();

        SmartGraphProperties properties = new SmartGraphProperties();
        SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();
        ForceDirectedLayoutStrategy<String> automaticPlacementStrategy = new ForceDirectedSpringGravityLayoutStrategy<>();

        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(g,
                properties,
                initialPlacement,
                stylesheetURI,
                automaticPlacementStrategy);

        graphView.setAutomaticLayout(true); // Set automatic layout from the start

        Scene scene = new Scene(new SmartGraphDemoContainer(graphView), 1024, 768);

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFX SmartGraph Visualization");
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setScene(scene);
        stage.show();

        /*
        IMPORTANT: Must call init() after scene is displayed, so we can have width and height values
        to initially place the vertices according to the placement strategy.
        */
        graphView.init();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private Graph<String, String> build_flower_graph() {

        Graph<String, String> g = new GraphEdgeList<>();

        g.insertVertex("A");
        g.insertVertex("B");
        g.insertVertex("C");
        g.insertVertex("D");
        g.insertVertex("E");
        g.insertVertex("F");
        g.insertVertex("G");

        g.insertEdge("A", "B", "1");
        g.insertEdge("A", "C", "2");
        g.insertEdge("A", "D", "3");
        g.insertEdge("A", "E", "4");
        g.insertEdge("A", "F", "5");
        g.insertEdge("A", "G", "6");

        g.insertVertex("H");
        g.insertVertex("I");
        g.insertVertex("J");
        g.insertVertex("K");
        g.insertVertex("L");
        g.insertVertex("M");
        g.insertVertex("N");

        g.insertEdge("H", "I", "7");
        g.insertEdge("H", "J", "8");
        g.insertEdge("H", "K", "9");
        g.insertEdge("H", "L", "10");
        g.insertEdge("H", "M", "11");
        g.insertEdge("H", "N", "12");

        g.insertEdge("A", "H", "0");

        g.insertVertex("ISOLATED");
        
        return g;
    }

    private static URI createStylesheet() {

        /* .graph */
        PaneStyleDefinition graph = FluentFxCss.paneStyle()
                .backgroundColor(Color.BLACK)
                .build();

        /* .vertex and .vertex:hover */
        ShapeStyleDefinition vertex = FluentFxCss.shapeStyle()
                .fill(Color.web("#3498db"))
                .stroke(Color.web("#2980b9"))
                .strokeWidth(2)
                .dropShadow(BlurType.GAUSSIAN, Color.web("#00ffff"), 15, 0.8, 0, 0)
                .opacity(0.5)
                .build();

        ShapeStyleDefinition vertexHover = FluentFxCss.shapeStyle()
                .strokeWidth(4)
                .build();

        /* .vertex-label */
        TextStyleDefinition vertexLabelText = FluentFxCss.textStyle()
                .fontWeight(FontWeight.BOLD)
                .fontSize(8, UnitValue.PT)
                .fontFamily("sans-serif")
                .build();

        RegionStyleDefinition vertexLabelBackground = FluentFxCss.regionStyle()
                .padding(UnitValue.PX, 5)
                .dropShadow(BlurType.GAUSSIAN, Color.web("#00ffff"), 10, 0.6, 0, 0)
                .build();

        StyleDefinition vertexLabel = vertexLabelText.mergeWith(vertexLabelBackground);

        /* .edge and .edge:hover */
        ShapeStyleDefinition edge = FluentFxCss.shapeStyle()
                .stroke(Color.WHITE)
                .strokeWidth(2)
                .dropShadow(BlurType.GAUSSIAN, Color.web("#00ffff"), 10, 0.6, 0, 0)
                .fill(Color.TRANSPARENT)
                .strokeLineCap(StrokeLineCap.ROUND)
                .opacity(0.8)
                .build();

        ShapeStyleDefinition edgeHover = FluentFxCss.shapeStyle()
                .strokeWidth(3)
                .build();

        TextStyleDefinition edgeLabelText = FluentFxCss.textStyle()
                .fontWeight(FontWeight.NORMAL)
                .fontSize(5, UnitValue.PT)
                .fontFamily("sans-serif")
                .build();

        RegionStyleDefinition edgeLabelBackground = FluentFxCss.regionStyle()
                .dropShadow(BlurType.GAUSSIAN, Color.web("#00ffff"), 10, 0.6, 0, 0)
                .build();

        StyleDefinition edgeLabel = edgeLabelText.mergeWith(edgeLabelBackground);

        TextStyleDefinition edgeLabelTextHover = FluentFxCss.textStyle()
                .fontWeight(FontWeight.BOLD)
                .fontSize(5, UnitValue.PT)
                .fontFamily("sans-serif")
                .build();

        RegionStyleDefinition edgeLabelBackgroundHover = FluentFxCss.regionStyle()
                .backgroundRadius(5)
                .borderRadius(5)
                .build();

        StyleDefinition edgeLabelHover = edgeLabelTextHover.mergeWith(edgeLabelBackgroundHover);


        /* Compose CSS */
        String graphClass = graph.toCssClass("graph");
        String vertexClass = vertex.toCssClass("vertex");
        String vertexHoverClass = vertexHover.toCssPseudoClass("vertex", PseudoClassValue.HOVER);

        String vertexLabelClass = vertexLabel.toCssClass("vertex-label");

        String edgeClass = edge.toCssClass("edge");
        String edgeHoverClass = edgeHover.toCssPseudoClass("edge", PseudoClassValue.HOVER);

        String edgeLabelClass = edgeLabel.toCssClass("edge-label");
        String edgeLabelHoverClass = edgeLabelHover.toCssPseudoClass("edge-label", PseudoClassValue.HOVER);

        String css = String.join("\n",
                graphClass,
                vertexClass,
                vertexHoverClass,
                vertexLabelClass,
                edgeClass,
                edgeHoverClass,
                edgeLabelClass,
                edgeLabelHoverClass
        );

        try {
            // Create a temporary file (OS will handle cleanup eventually)
            Path tempCssFile = Files.createTempFile("dynamic-style", ".css");
            Files.write(tempCssFile, css.getBytes(StandardCharsets.UTF_8));

            // Convert to URI
            return tempCssFile.toUri();
        } catch(Exception e) {
            System.err.println("Cannot create temporary file.");
        }

        return null;
    }

}
