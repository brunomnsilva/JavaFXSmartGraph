/**
 * The SmartGraph module provides graph data structures and visualizations for JavaFX.
 */
module com.brunomnsilva.smartgraph {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires java.logging;

    exports com.brunomnsilva.smartgraph;
    exports com.brunomnsilva.smartgraph.containers;
    exports com.brunomnsilva.smartgraph.graph;
    exports com.brunomnsilva.smartgraph.graphview;
}