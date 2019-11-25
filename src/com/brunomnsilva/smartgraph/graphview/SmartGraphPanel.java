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
package com.brunomnsilva.smartgraph.graphview;


import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graph.Edge;
import static com.brunomnsilva.smartgraph.graphview.UtilitiesJavaFX.pick;
import static com.brunomnsilva.smartgraph.graphview.UtilitiesPoint2D.attractiveForce;
import static com.brunomnsilva.smartgraph.graphview.UtilitiesPoint2D.repellingForce;

/**
 * JavaFX {@link Pane} that is capable of plotting a {@link Graph} or {@link Digraph}.
 * <br>
 * Be sure to call {@link #init() } after the Stage is displayed.
 * <br>
 * Whenever changes to the underlying graph are made, you should call
 * {@link #update()} to force the rendering of any new elements and, also, the
 * removal of others, if applicable.
 * <br>
 * Vertices can be dragged by the user, if configured to do so. Consequently, 
 * any connected edges will also adjust automatically to the new vertex positioning.
 *
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 *
 * @author brunomnsilva
 */
public class SmartGraphPanel<V, E> extends Pane {

    /* 
    CONFIGURATION PROPERTIES
     */
    private final SmartGraphProperties graphProperties;

    /*
    INTERNAL DATA STRUCTURE
     */
    private final Graph<V, E> theGraph;
    private final SmartPlacementStrategy placementStrategy;
    private final Map<Vertex<V>, SmartGraphVertexNode<V>> vertexNodes;
    private final Map<Edge<E, V>, SmartGraphEdgeBase> edgeNodes;
    private final Map<Tuple<SmartGraphVertexNode>, Integer> placedEdges = new HashMap<>();
    private boolean initialized = false;
    private final boolean edgesWithArrows;
    /*
    INTERACTION WITH VERTICES AND EDGES
     */
    private Consumer<SmartGraphVertex<V>> vertexClickConsumer = null;
    private Consumer<SmartGraphEdge<E, V>> edgeClickConsumer = null;

    /*
    AUTOMATIC LAYOUT RELATED ATTRIBUTES
     */
    public final BooleanProperty automaticLayoutProperty;
    private AnimationTimer timer;
    private final double repulsionForce;
    private final double attractionForce;
    private final double attractionScale;

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties and default random
     * placement of vertices.
     *
     * @param theGraph underlying graph
     *
     * @see Graph
     */
    public SmartGraphPanel(Graph<V, E> theGraph) {
        this(theGraph, new SmartGraphProperties(), null);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties and default random
     * placement of vertices.
     *
     * @param theGraph underlying graph
     * @param properties custom properties
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties) {
        this(theGraph, properties, null);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties and custom placement of
     * vertices.
     *
     * @param theGraph underlying graph
     * @param placementStrategy placement strategy
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy) {
        this(theGraph, null, placementStrategy);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties and custom placement of
     * vertices.
     *
     * @param theGraph underlying graph
     * @param properties custom properties
     * @param placementStrategy placement strategy
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy) {

        if (theGraph == null) {
            throw new IllegalArgumentException("The graph cannot be null.");
        }
        this.theGraph = theGraph;
        this.graphProperties = properties != null ? properties : new SmartGraphProperties();
        this.placementStrategy = placementStrategy != null ? placementStrategy : new SmartRandomPlacementStrategy();

        this.edgesWithArrows = this.graphProperties.getUseEdgeArrow();

        this.repulsionForce = this.graphProperties.getRepulsionForce();
        this.attractionForce = this.graphProperties.getAttractionForce();
        this.attractionScale = this.graphProperties.getAttractionScale();

        vertexNodes = new HashMap<>();
        edgeNodes = new HashMap<>(); 

        //set stylesheet and class
        loadStylesheet();

        initNodes();

        enableDoubleClickListener();

        //automatic layout initializations        
        timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                runLayoutIteration();
            }
        };
        
        this.automaticLayoutProperty = new SimpleBooleanProperty(false);
        this.automaticLayoutProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.start();
            } else {
                timer.stop();
            }
        });
                
    }

    private synchronized void runLayoutIteration() {
        for (int i = 0; i < 20; i++) {
            resetForces();
            computeForces();
            updateForces();
        }
        applyForces();
    }

    /**
     * Runs the initial current vertex placement strategy.
     * <p>
     * This method should only be called once during the lifetime of the object
     * and only after the underlying {@link Scene} is displayed.
     *
     * Further required updates should be performed through the {@link #update()
     * } method.
     *
     * @throws IllegalStateException The exception is thrown if: (1) the Scene
     * is not yet displayed; (2) It has zero width and/or height, and; (3) If
     * this method was already called.
     */
    public void init() throws IllegalStateException {
        if (this.getScene() == null) {
            throw new IllegalStateException("You must call this method after the instance was added to a scene.");
        } else if (this.getWidth() == 0 || this.getHeight() == 0) {
            throw new IllegalStateException("The layout for this panel has zero width and/or height");
        } else if (this.initialized) {
            throw new IllegalStateException("Already initialized. Use update() method instead.");
        }

        if (placementStrategy != null) {
            // call strategy to place the vertices in their initial locations 
            placementStrategy.place(this.widthProperty().doubleValue(),
                    this.heightProperty().doubleValue(),
                    this.theGraph,
                    this.vertexNodes.values());
        } else {
            //apply random placement
            new SmartRandomPlacementStrategy().place(this.widthProperty().doubleValue(),
                    this.heightProperty().doubleValue(),
                    this.theGraph,
                    this.vertexNodes.values());

            //start automatic layout
            timer.start();
        }

        this.initialized = true;
    }

    /**
     * Returns the property used to toggle the automatic layout of vertices.
     * 
     * @return  automatic layout property
     */
    public BooleanProperty automaticLayoutProperty() {
        return this.automaticLayoutProperty;
    }
    
    /**
     * Toggle the automatic layout of vertices.
     * 
     * @param value     true if enabling; false, otherwise
     */
    public void setAutomaticLayout(boolean value) {
        automaticLayoutProperty.set(value);
    }

    /**
     * Forces a refresh of the visualization based on current state of the
     * underlying graph.
     * <p>
     * New vertices will be added close to adjacent ones or randomly for
     * isolated vertices.
     */
    public void update() {
        if (this.getScene() == null) {
            throw new IllegalStateException("You must call this method after the instance was added to a scene.");
        }

        if (!this.initialized) {
            throw new IllegalStateException("You must call init() method before any updates.");
        }

        //this will be called from a non-javafx thread, so this must be guaranteed to run of the graphics thread
        Platform.runLater(() -> {
            updateNodes();
        });

    }

    private synchronized void updateNodes() {
        removeNodes();
        insertNodes();
    }

    /*
    INTERACTION WITH VERTICES AND EDGES
     */
    /**
     * Sets the action that should be performed when a vertex is double clicked.
     *
     * @param action action to be performed
     */
    public void setVertexDoubleClickAction(Consumer<SmartGraphVertex<V>> action) {
        this.vertexClickConsumer = action;
    }

    /**
     * Sets the action that should be performed when an edge is double clicked.
     *
     * @param action action to be performed
     */
    public void setEdgeDoubleClickAction(Consumer<SmartGraphEdge<E, V>> action) {
        this.edgeClickConsumer = action;
    }

    /*
    NODES CREATION/UPDATES
     */
    private void initNodes() {

        /* create vertex graphical representations */
        for (Vertex<V> vertex : listOfVertices()) {
            SmartGraphVertexNode<V> vertexAnchor = new SmartGraphVertexNode(vertex, 0, 0,
                    graphProperties.getVertexRadius(), graphProperties.getVertexAllowUserMove());

            vertexNodes.put(vertex, vertexAnchor);
        }

        /* create edges graphical representations between existing vertices */
        //this is used to guarantee that no duplicate edges are ever inserted
        List<Edge<E, V>> edgesToPlace = listOfEdges();

        for (Vertex<V> vertex : vertexNodes.keySet()) {

            Iterable<Edge<E, V>> incidentEdges = theGraph.incidentEdges(vertex);

            for (Edge<E, V> edge : incidentEdges) {

                //if already plotted, ignore edge.
                if (!edgesToPlace.contains(edge)) {
                    continue;
                }

                Vertex<V> oppositeVertex = theGraph.opposite(vertex, edge);

                SmartGraphVertexNode<V> graphVertexIn = vertexNodes.get(vertex);
                SmartGraphVertexNode<V> graphVertexOppositeOut = vertexNodes.get(oppositeVertex);

                graphVertexIn.addAdjacentVertex(graphVertexOppositeOut);
                graphVertexOppositeOut.addAdjacentVertex(graphVertexIn);

                SmartGraphEdgeBase graphEdge = createEdge(edge, graphVertexIn, graphVertexOppositeOut);

                /* Track Edges already placed */
                addEdge(graphEdge, edge);

                if (this.edgesWithArrows) {
                    SmartArrow arrow = new SmartArrow();
                    graphEdge.attachArrow(arrow);
                    this.getChildren().add(arrow);
                }

                edgesToPlace.remove(edge);
            }

        }

        /* place vertices above lines */
        for (Vertex<V> vertex : vertexNodes.keySet()) {
            SmartGraphVertexNode<V> v = vertexNodes.get(vertex);

            addVertex(v);
        }
    }

    private SmartGraphEdgeBase createEdge(Edge<E, V> edge, SmartGraphVertexNode<V> graphVertexInbound, SmartGraphVertexNode<V> graphVertexOutbound) {
        /*
        Even if edges are later removed, the corresponding index remains the same. Otherwise, we would have to
        regenerate the appropriate edges.
         */
        int edgeIndex = 0;
        Integer counter = placedEdges.get(new Tuple(graphVertexInbound, graphVertexOutbound));
        if (counter != null) {
            edgeIndex = counter;
        }

        SmartGraphEdgeBase graphEdge;

        if (getTotalEdgesBetween(graphVertexInbound.getUnderlyingVertex(), graphVertexOutbound.getUnderlyingVertex()) > 1
                || graphVertexInbound == graphVertexOutbound) {
            graphEdge = new SmartGraphEdgeCurve(edge, graphVertexInbound, graphVertexOutbound, edgeIndex);
        } else {
            graphEdge = new SmartGraphEdgeLine<>(edge, graphVertexInbound, graphVertexOutbound);
        }

        placedEdges.put(new Tuple(graphVertexInbound, graphVertexOutbound), ++edgeIndex);

        return graphEdge;
    }

    private void addVertex(SmartGraphVertexNode<V> v) {
        this.getChildren().add(v);

        String labelText = (v.getUnderlyingVertex().element() != null) ? 
                    v.getUnderlyingVertex().element().toString() :
                    "<NULL>";
        
        if (graphProperties.getUseVertexTooltip()) {            
            Tooltip t = new Tooltip(labelText);
            Tooltip.install(v, t);
        }

        if (graphProperties.getUseVertexLabel()) {
            SmartLabel label = new SmartLabel(labelText);

            label.setStyle("vertex-label");
            this.getChildren().add(label);
            v.attachLabel(label);
        }
    }

    private void addEdge(SmartGraphEdgeBase e, Edge<E, V> edge) {
        //edges to the back
        this.getChildren().add(0, (Node) e);
        edgeNodes.put(edge, e);

        String labelText = (edge.element() != null) ? 
                    edge.element().toString() :
                    "<NULL>";
        
        if (graphProperties.getUseEdgeTooltip()) {
            Tooltip t = new Tooltip(labelText);
            Tooltip.install((Node) e, t);
        }

        if (graphProperties.getUseEdgeLabel()) {
            SmartLabel label = new SmartLabel(labelText);

            label.setStyle("edge-label");
            this.getChildren().add(label);
            e.attachLabel(label);
        }
    }

    private void insertNodes() {
        Collection<Vertex<V>> unplottedVertices = unplottedVertices();

        List<SmartGraphVertexNode<V>> newVertices = null;

        Bounds bounds = getPlotBounds();
        double mx = bounds.getMinX() + bounds.getWidth() / 2.0;
        double my = bounds.getMinY() + bounds.getHeight() / 2.0;

        if (!unplottedVertices.isEmpty()) {

            newVertices = new LinkedList<>();

            for (Vertex<V> vertex : unplottedVertices) {
                //create node
                //Place new nodes in the vicinity of existing adjacent ones;
                //Place them in the middle of the plot, otherwise.
                double x, y;
                Collection<Edge<E, V>> incidentEdges = theGraph.incidentEdges(vertex);
                if (incidentEdges.isEmpty()) {
                    /* not (yet) connected, put in the middle of the plot */
                    x = mx;
                    y = my;
                } else {
                    Edge<E, V> firstEdge = incidentEdges.iterator().next();
                    Vertex<V> opposite = theGraph.opposite(vertex, firstEdge);
                    SmartGraphVertexNode<V> existing = vertexNodes.get(opposite);

                    /* TODO: fix -- the placing point can be set out of bounds*/
                    Point2D p = UtilitiesPoint2D.rotate(existing.getPosition().add(50.0, 50.0),
                            existing.getPosition(), Math.random() * 360);

                    x = p.getX();
                    y = p.getY();

                }

                SmartGraphVertexNode newVertex = new SmartGraphVertexNode<>(vertex,
                        x, y, graphProperties.getVertexRadius(), graphProperties.getVertexAllowUserMove());

                //track new nodes
                newVertices.add(newVertex);
                //add to global mapping
                vertexNodes.put(vertex, newVertex);
            }

        }

        Collection<Edge<E, V>> unplottedEdges = unplottedEdges();
        if (!unplottedEdges.isEmpty()) {
            for (Edge<E, V> edge : unplottedEdges) {

                Vertex<V>[] vertices = edge.vertices();
                Vertex<V> u = vertices[0]; //oubound if digraph, by javadoc requirement
                Vertex<V> v = vertices[1]; //inbound if digraph, by javadoc requirement

                SmartGraphVertexNode<V> graphVertexOut = vertexNodes.get(u);
                SmartGraphVertexNode<V> graphVertexIn = vertexNodes.get(v);

                graphVertexOut.addAdjacentVertex(graphVertexIn);
                graphVertexIn.addAdjacentVertex(graphVertexOut);

                SmartGraphEdgeBase graphEdge = createEdge(edge, graphVertexIn, graphVertexOut);

                if (this.edgesWithArrows) {
                    SmartArrow arrow = new SmartArrow();
                    graphEdge.attachArrow(arrow);
                    this.getChildren().add(arrow);
                }

                addEdge(graphEdge, edge);

            }
        }

        if (newVertices != null) {
            for (SmartGraphVertexNode<V> v : newVertices) {
                addVertex(v);
            }
        }

    }

    private void removeNodes() {
        Collection<Vertex<V>> removedVertices = removedVertices();
        Collection<SmartGraphEdgeBase> values = new LinkedList<>(edgeNodes.values());

        Set<SmartGraphVertexNode<V>> verticesToRemove = new HashSet<>();
        Set<SmartGraphEdgeBase> edgesToRemove = new HashSet<>();

        for (Vertex<V> v : removedVertices) {

            for (SmartGraphEdgeBase edge : values) {
                Vertex[] vertices = edge.getUnderlyingEdge().vertices();

                if (vertices[0] == v || vertices[1] == v) {
                    edgesToRemove.add(edge);
                }
            }

            SmartGraphVertexNode<V> get = vertexNodes.get(v);
            verticesToRemove.add(get);
        }

        //permanently remove edges
        for (SmartGraphEdgeBase e : edgesToRemove) {
            edgeNodes.remove(e.getUnderlyingEdge());
            removeEdge(e);
        }

        //permanently remove vertices
        for (SmartGraphVertexNode<V> v : verticesToRemove) {
            vertexNodes.remove(v.getUnderlyingVertex());
            removeVertice(v);
        }

        //remove adjacencies from remaining vertices
        for (SmartGraphVertexNode<V> v : vertexNodes.values()) {
            v.removeAdjacentVertices(verticesToRemove);
        }
    }

    private void removeEdge(SmartGraphEdgeBase e) {
        getChildren().remove((Node) e);

        SmartArrow attachedArrow = e.getAttachedArrow();
        if (attachedArrow != null) {
            getChildren().remove(attachedArrow);
        }

        Text attachedLabel = e.getAttachedLabel();
        if (attachedLabel != null) {
            getChildren().remove(attachedLabel);
        }
    }

    private void removeVertice(SmartGraphVertexNode v) {
        getChildren().remove(v);

        Text attachedLabel = v.getAttachedLabel();
        if (attachedLabel != null) {
            getChildren().remove(attachedLabel);
        }
    }

    /**
     * Computes the bounding box from all displayed vertices.
     *
     * @return bounding box
     */
    private Bounds getPlotBounds() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE,
                maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (SmartGraphVertexNode<V> v : vertexNodes.values()) {
            minX = Math.min(minX, v.getCenterX());
            minY = Math.min(minY, v.getCenterY());
            maxX = Math.max(maxX, v.getCenterX());
            maxY = Math.max(maxY, v.getCenterY());
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }


    /*
    * AUTOMATIC LAYOUT 
     */
    private void computeForces() {
        for (SmartGraphVertexNode<V> v : vertexNodes.values()) {
            for (SmartGraphVertexNode<V> other : vertexNodes.values()) {
                if (v == other) {
                    continue; //NOP
                }

                //double k = Math.sqrt(getWidth() * getHeight() / graphVertexMap.size());
                Point2D repellingForce = repellingForce(v.getUpdatedPosition(), other.getUpdatedPosition(), this.repulsionForce);

                double deltaForceX = 0, deltaForceY = 0;

                //compute attractive and reppeling forces
                //opt to use internal areAdjacent check, because a vertex can be removed from
                //the underlying graph before we have the chance to remove it from our
                //internal data structure
                if (areAdjacent(v, other)) {

                    Point2D attractiveForce = attractiveForce(v.getUpdatedPosition(), other.getUpdatedPosition(),
                            vertexNodes.size(), this.attractionForce, this.attractionScale);

                    deltaForceX = attractiveForce.getX() + repellingForce.getX();
                    deltaForceY = attractiveForce.getY() + repellingForce.getY();
                } else {
                    deltaForceX = repellingForce.getX();
                    deltaForceY = repellingForce.getY();
                }

                v.addForceVector(deltaForceX, deltaForceY);
            }
        }
    }

    private boolean areAdjacent(SmartGraphVertexNode<V> v, SmartGraphVertexNode<V> u) {
        return v.isAdjacentTo(u);
    }

    private void updateForces() {
        vertexNodes.values().forEach((v) -> {
            v.updateDelta();
        });
    }

    private void applyForces() {
        vertexNodes.values().forEach((v) -> {
            v.moveFromForces();
        });
    }

    private void resetForces() {
        vertexNodes.values().forEach((v) -> {
            v.resetForces();
        });
    }

    private int getTotalEdgesBetween(Vertex<V> v, Vertex<V> u) {
        //TODO: It may be necessary to adjust this method if you use another Graph
        //variant, e.g., Digraph (directed graph)
        int count = 0;
        for (Edge<E, V> edge : theGraph.edges()) {
            if (edge.vertices()[0] == v && edge.vertices()[1] == u
                    || edge.vertices()[0] == u && edge.vertices()[1] == v) {
                count++;
            }
        }
        return count;
    }

    private List<Edge<E, V>> listOfEdges() {
        List<Edge<E, V>> list = new LinkedList<>();
        for (Edge<E, V> edge : theGraph.edges()) {
            list.add(edge);
        }
        return list;
    }

    private List<Vertex<V>> listOfVertices() {
        List<Vertex<V>> list = new LinkedList<>();
        for (Vertex<V> vertex : theGraph.vertices()) {
            list.add(vertex);
        }
        return list;
    }

    /**
     * Computes the vertex collection of the underlying graph that are not
     * currently being displayed.
     *
     * @return collection of vertices
     */
    private Collection<Vertex<V>> unplottedVertices() {
        List<Vertex<V>> unplotted = new LinkedList<>();

        for (Vertex<V> v : theGraph.vertices()) {
            if (!vertexNodes.containsKey(v)) {
                unplotted.add(v);
            }
        }

        return unplotted;
    }

    /**
     * Computes the vertex collection that are currently being displayed but do
     * not longer exist in the underlying graph.
     *
     * @return collection of vertices
     */
    private Collection<Vertex<V>> removedVertices() {
        List<Vertex<V>> removed = new LinkedList<>();

        Collection<Vertex<V>> graphVertices = theGraph.vertices();
        Collection<SmartGraphVertexNode<V>> plotted = vertexNodes.values();

        for (SmartGraphVertexNode<V> v : plotted) {
            if (!graphVertices.contains(v.getUnderlyingVertex())) {
                removed.add(v.getUnderlyingVertex());
            }
        }

        return removed;
    }

    /**
     * Computes the edge collection of the underlying graph that are not
     * currently being displayed.
     *
     * @return collection of edges
     */
    private Collection<Edge<E, V>> unplottedEdges() {
        List<Edge<E, V>> unplotted = new LinkedList<>();

        for (Edge<E, V> e : theGraph.edges()) {
            if (!edgeNodes.containsKey(e)) {
                unplotted.add(e);
            }
        }

        return unplotted;
    }

    /**
     * Returns the associated stylable element with a graph vertex.
     *
     * @param v underlying vertex
     * @return stylable element
     */
    public SmartStylableNode getStylableVertex(Vertex<V> v) {
        return vertexNodes.get(v);
    }

    /**
     * Returns the associated stylable element with a graph vertex.
     *
     * @param vertexElement underlying vertex's element
     * @return stylable element
     */
    public SmartStylableNode getStylableVertex(V vertexElement) {
        for (Vertex<V> v : vertexNodes.keySet()) {
            if (v.element().equals(vertexElement)) {
                return vertexNodes.get(v);
            }
        }
        return null;
    }

    /**
     * Returns the associated stylable element with a graph edge.
     *
     * @param edge underlying graph edge
     * @return stylable element
     */
    public SmartStylableNode getStylableEdge(Edge<E, V> edge) {
        return edgeNodes.get(edge);
    }

    /**
     * Returns the associated stylable element with a graph edge.
     *
     * @param edgeElement underlying graph edge's element
     * @return stylable element
     */
    public SmartStylableNode getStylableEdge(E edgeElement) {
        for (Edge<E, V> e : edgeNodes.keySet()) {
            if (e.element().equals(edgeElement)) {
                return edgeNodes.get(e);
            }
        }
        return null;
    }

    /**
     * Loads the default stylesheet and applies the .graph class to this panel.
     */
    private void loadStylesheet() {
        try {
            File f = new File("smartgraph.css");

            String css = f.toURI().toURL().toExternalForm();
            getStylesheets().add(css);
            this.getStyleClass().add("graph");
        } catch (MalformedURLException ex) {
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Enables the double click action on this pane.
     *
     * This method identifies the node that was clicked and, if any, calls the
     * appropriate consumer, i.e., vertex or edge consumers.
     */
    private void enableDoubleClickListener() {
        setOnMouseClicked((MouseEvent mouseEvent) -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    //no need to continue otherwise
                    if (vertexClickConsumer == null && edgeClickConsumer == null) {
                        return;
                    }

                    Node node = pick(SmartGraphPanel.this, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    if (node == null) {
                        return;
                    }

                    if (node instanceof SmartGraphVertex) {
                        SmartGraphVertex v = (SmartGraphVertex) node;
                        vertexClickConsumer.accept(v);
                    } else if (node instanceof SmartGraphEdge) {
                        SmartGraphEdge e = (SmartGraphEdge) node;
                        edgeClickConsumer.accept(e);
                    }

                }
            }
        });
    }

    /**
     * Represents a tuple in Java.
     *
     * @param <T> the type of the tuple
     */
    private class Tuple<T> {

        private final T first;
        private final T second;

        public Tuple(T first, T second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.first);
            hash = 29 * hash + Objects.hashCode(this.second);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Tuple<?> other = (Tuple<?>) obj;
            if (!Objects.equals(this.first, other.first)) {
                return false;
            }
            if (!Objects.equals(this.second, other.second)) {
                return false;
            }
            return true;
        }
    }

}
