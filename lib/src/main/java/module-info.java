module com.brunomnsilva.smartgraph {
	exports com.brunomnsilva.smartgraph.containers;
	exports com.brunomnsilva.smartgraph.example;
	exports com.brunomnsilva.smartgraph;
	exports com.brunomnsilva.smartgraph.graph;
	exports com.brunomnsilva.smartgraph.graphview;

	requires java.logging;
	requires javafx.base;
	requires javafx.controls;
	requires transitive javafx.graphics;
}