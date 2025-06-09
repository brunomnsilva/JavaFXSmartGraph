/**
 * The SmartGraph module provides graph data structures and visualizations for JavaFX.
 */
module com.brunomnsilva.smartgraph {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires java.logging;

    requires com.brunomnsilva.fluentfxcss;

    exports com.brunomnsilva.smartgraph.containers;
    exports com.brunomnsilva.smartgraph.graph;
    exports com.brunomnsilva.smartgraph.graphview;
    exports com.brunomnsilva.smartgraph.examples.digraph;
    exports com.brunomnsilva.smartgraph.examples.flowers;
    exports com.brunomnsilva.smartgraph.examples.cities;
    exports com.brunomnsilva.smartgraph.examples.dynamic;
}