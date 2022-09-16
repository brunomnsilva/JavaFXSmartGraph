module org.intrigus.smartgraph {
	exports org.intrigus.smartgraph.containers;
	exports org.intrigus.smartgraph.example;
	exports org.intrigus.smartgraph;
	exports org.intrigus.smartgraph.graph;
	exports org.intrigus.smartgraph.graphview;

	requires java.logging;
	requires javafx.base;
	requires javafx.controls;
	requires transitive javafx.graphics;
}