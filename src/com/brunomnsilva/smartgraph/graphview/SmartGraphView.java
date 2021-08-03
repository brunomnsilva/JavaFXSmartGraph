/* 
 * The MIT License
 *
 * Copyright 2021
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
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.scene.shape.Shape;

/**
 * JavaFX {@link Pane} that is capable of plotting a {@link Graph} or
 * {@link Digraph}.
 * <br>
 * Be sure to call {@link #init() } after the Stage is displayed.
 * <br>
 * Whenever changes to the underlying graph are made, you should call
 * {@link #update()} to force the rendering of any new elements and, also, the
 * removal of others, if applicable.
 * <br>
 * Vertices can be dragged by the user, if configured to do so. Consequently,
 * any connected edges will also adjust automatically to the new vertex
 * positioning.
 *
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 *
 * @author brunomnsilva
 */
public class SmartGraphView<V, E> extends SmartGraphPane {

    /* 
    CONFIGURATION PROPERTIES
     */
    protected final SmartGraphProperties graphProperties;

    /*
    INTERNAL DATA STRUCTURE
     */
    protected final Graph<V, E> theGraph;
    protected final SmartPlacementStrategy placementStrategy;
    protected final Map<Vertex<V>, SmartGraphVertexNode<V>> vertexNodes;
    protected final Map<Edge<E, V>, SmartGraphEdgeBase> edgeNodes;
    private final Map<Tuple<SmartGraphVertexNode>, Integer> placedEdges = new HashMap<>();
    protected boolean initialized = false;
    protected final boolean edgesWithArrows;
    /*
    INTERACTION WITH VERTICES AND EDGES
     */
    private Consumer<SmartGraphVertex<V>> vertexClickConsumer = null;
    private Consumer<SmartGraphEdge<E, V>> edgeClickConsumer = null;

    /*
    AUTOMATIC LAYOUT RELATED ATTRIBUTES
     */
    protected final BooleanProperty automaticLayoutProperty;

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties and default random
     * placement of vertices.
     *
     * @param theGraph underlying graph
     *
     * @see Graph
     */
    public SmartGraphView(Graph<V, E> theGraph) {
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
    public SmartGraphView(Graph<V, E> theGraph, SmartGraphProperties properties) {
        this(theGraph, properties, null);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties and custom placement of
     * vertices.
     *
     * @param theGraph underlying graph
     * @param placementStrategy placement strategy, null for default
     */
    public SmartGraphView(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy) {
        this(theGraph, null, placementStrategy);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties and custom placement of
     * vertices.
     *
     * @param theGraph underlying graph
     * @param properties custom properties, null for default
     * @param placementStrategy placement strategy, null for default
     */
    public SmartGraphView(Graph<V, E> theGraph, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy) {

        this(theGraph, properties, placementStrategy, null);
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties and custom placement of
     * vertices.
     *
     * @param theGraph underlying graph
     * @param properties custom properties, null for default
     * @param placementStrategy placement strategy, null for default
     * @param cssFile alternative css file, instead of default 'smartgraph.css'
     */
    public SmartGraphView(Graph<V, E> theGraph, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy, URI cssFile) {

        if (theGraph == null) {
            throw new IllegalArgumentException("The graph cannot be null.");
        }

        this.theGraph = theGraph;
        this.graphProperties = properties != null ? properties : new SmartGraphProperties();
        this.placementStrategy = placementStrategy != null ? placementStrategy : new SmartRandomPlacementStrategy();

        this.edgesWithArrows = this.graphProperties.getUseEdgeArrow();

        this.vertexNodes = new HashMap<>();
        this.edgeNodes = new HashMap<>();

        //set stylesheet and class
        this.loadStylesheet(cssFile);

        this.initNodes();

        this.enableMouseEventListener();

        this.automaticLayoutProperty = new SimpleBooleanProperty(false);
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
    @Override
    public final void init() throws IllegalStateException {
        if (this.getScene() == null) {
            throw new IllegalStateException("You must call this method after the instance was added to a scene.");
        } else if (this.getWidth() == 0 || this.getHeight() == 0) {
            throw new IllegalStateException("The layout for this panel has zero width and/or height");
        } else if (this.initialized) {
            throw new IllegalStateException("Already initialized. Use update() method instead.");
        }

        this.onInitialize();

        this.initialized = true;
    }

    /**
     * This method will be called inside the init() method. It is a place to
     * initialize the view. All child class must implement this method.
     */
    protected void onInitialize() {
        if (this.placementStrategy != null) {
            // call strategy to place the vertices in their initial locations 
            this.placementStrategy.place(this.widthProperty().doubleValue(),
                    this.heightProperty().doubleValue(),
                    this.theGraph,
                    this.vertexNodes.values());
        } else {
            //apply random placement
            new SmartRandomPlacementStrategy().place(this.widthProperty().doubleValue(),
                    this.heightProperty().doubleValue(),
                    this.theGraph,
                    this.vertexNodes.values());
        }
    }

    /**
     * Creates SmartGraphVertexNode, child class must implement this method to
     * create the desired SmartGraphVertexNode.
     *
     * @param vertex SmartGraphVertex
     * @return SmartGraphVertexNode
     */
    protected SmartGraphVertexNode getSmartGraphVertexNode(Vertex vertex) {
        return new SmartGraphVertexNodeBasic(vertex, this.graphProperties.getVertexAllowUserMove());
    }

    /**
     * Gets whether the graph panel is initialized.
     *
     * @return true if the graph panel is initialized, false otherwise.
     */
    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    /**
     * Returns the property used to toggle the automatic layout of vertices.
     *
     * @return automatic layout property
     */
    @Override
    public BooleanProperty automaticLayoutProperty() {
        return this.automaticLayoutProperty;
    }

    /**
     * Toggle the automatic layout of vertices.
     *
     * @param value true if enabling; false, otherwise
     */
    public void setAutomaticLayout(boolean value) {
        this.automaticLayoutProperty.set(value);
    }

    /**
     * Forces a refresh of the visualization based on current state of the
     * underlying graph, immediately returning to the caller.
     *
     * This method invokes the refresh in the graphical thread through
     * Platform.runLater(), so its not guaranteed that the visualization is in
     * sync immediately after this method finishes. That is, this method
     * immediately returns to the caller without waiting for the update to the
     * visualization.
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
            this.updateNodes();
        });

    }

    /**
     * This method will be called in the update() method, providing a place
     * where child class can do something in the update process.
     */
    protected void onUpdate() {

    }

    /**
     * Forces a refresh of the visualization based on current state of the
     * underlying graph and waits for completion of the update.
     *
     * Use this variant only when necessary, e.g., need to style an element
     * immediately after adding it to the underlying graph. Otherwise, use
     * {@link #update() } instead for performance sake.
     * <p>
     * New vertices will be added close to adjacent ones or randomly for
     * isolated vertices.
     */
    public void updateAndWait() {
        if (this.getScene() == null) {
            throw new IllegalStateException("You must call this method after the instance was added to a scene.");
        }

        if (!this.initialized) {
            throw new IllegalStateException("You must call init() method before any updates.");
        }

        final FutureTask update = new FutureTask(() -> {
            this.updateNodes();
            return true;
        });

        //
        if (!Platform.isFxApplicationThread()) {
            //this will be called from a non-javafx thread, so this must be guaranteed to run of the graphics thread
            Platform.runLater(update);

            //wait for completion, only outside javafx thread; otherwise -> deadlock
            try {
                update.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                Logger.getLogger(SmartGraphView.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.updateNodes();
        }
    }

    private synchronized void updateNodes() {
        this.removeNodes();
        this.insertNodes();
        this.updateLabels();
        this.onUpdate();
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
        for (Vertex<V> vertex : this.listOfVertices()) {
            SmartGraphVertexNode<V> vertexAnchor = this.getSmartGraphVertexNode(vertex);
            this.vertexNodes.put(vertex, vertexAnchor);
            vertexAnchor.getNode().setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                    this.toggleAdjacentVertexVisible(vertexAnchor);
                }
            });
        }

        /* create edges graphical representations between existing vertices */
        //this is used to guarantee that no duplicate edges are ever inserted
        List<Edge<E, V>> edgesToPlace = this.listOfEdges();

        for (Vertex<V> vertex : this.vertexNodes.keySet()) {

            Iterable<Edge<E, V>> incidentEdges = theGraph.incidentEdges(vertex);

            for (Edge<E, V> edge : incidentEdges) {

                //if already plotted, ignore edge.
                if (!edgesToPlace.contains(edge)) {
                    continue;
                }

                Vertex<V> oppositeVertex = this.theGraph.opposite(vertex, edge);

                SmartGraphVertexNode<V> graphVertexIn = vertexNodes.get(vertex);
                SmartGraphVertexNode<V> graphVertexOppositeOut = vertexNodes.get(oppositeVertex);

                graphVertexIn.addAdjacentVertex(graphVertexOppositeOut);
                graphVertexOppositeOut.addAdjacentVertex(graphVertexIn);

                SmartGraphEdgeBase graphEdge = this.createEdge(edge, graphVertexIn, graphVertexOppositeOut);

                graphVertexIn.addEdge(graphEdge);
                graphVertexOppositeOut.addEdge(graphEdge);

                /* Track Edges already placed */
                this.addEdge(graphEdge, edge);

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
            this.addVertex(v);
        }
    }

    private SmartGraphEdgeBase createEdge(Edge<E, V> edge, SmartGraphVertexNode<V> graphVertexInbound, SmartGraphVertexNode<V> graphVertexOutbound) {
        /*
        Even if edges are later removed, the corresponding index remains the same. Otherwise, we would have to
        regenerate the appropriate edges.
         */
        int edgeIndex = 0;
        Integer counter = this.placedEdges.get(new Tuple(graphVertexInbound, graphVertexOutbound));
        if (counter != null) {
            edgeIndex = counter;
        }
        // make inbound->outbound edge and outbound->inbound edge are in the same index group
        counter = this.placedEdges.get(new Tuple(graphVertexOutbound, graphVertexInbound));
        if (counter != null && counter > edgeIndex) {
            edgeIndex = counter;
        }

        SmartGraphEdgeBase graphEdge;
        // can set edge to curve edge since all edges have index.
        // for index = 0 the curve will appear as straight line.
        graphEdge = new SmartGraphEdgeCurve(edge, graphVertexInbound, graphVertexOutbound, edgeIndex);

        this.placedEdges.put(new Tuple(graphVertexInbound, graphVertexOutbound), ++edgeIndex);

        return graphEdge;
    }

    private void addVertex(SmartGraphVertexNode<V> v) {
        Node node = v.getNode();
        if (!(v.getUnderlyingVertex().element() instanceof Node)) {
            String labelText = (v.getUnderlyingVertex().element() != null)
                    ? v.getUnderlyingVertex().element().toString()
                    : "<NULL>";

            if (this.graphProperties.getUseVertexTooltip()) {
                Tooltip t = new Tooltip(labelText);
                Tooltip.install(node, t);
            }

            if (this.graphProperties.getUseVertexLabel()) {
                SmartLabel label = new SmartLabel(labelText);

                label.addStyleClass("vertex-label");
                this.getChildren().add(label);
                v.attachLabel(label);
            }
        }
        this.getChildren().add(v.getNode());
    }

    private void addEdge(SmartGraphEdgeBase e, Edge<E, V> edge) {
        //edges to the back
        this.getChildren().add(0, (Node) e);
        this.edgeNodes.put(edge, e);

        String labelText = (edge.element() != null)
                ? edge.element().toString()
                : "<NULL>";

        if (this.graphProperties.getUseEdgeTooltip()) {
            Tooltip t = new Tooltip(labelText);
            Tooltip.install((Node) e, t);
        }

        if (this.graphProperties.getUseEdgeLabel()) {
            SmartLabel label = new SmartLabel(labelText);

            label.addStyleClass("edge-label");
            this.getChildren().add(label);
            e.attachLabel(label);
        }
    }

    private void insertNodes() {
        Collection<Vertex<V>> unplottedVertices = this.unplottedVertices();

        List<SmartGraphVertexNode<V>> newVertices = null;

        Bounds bounds = this.getPlotBounds();
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

                    if (existing == null) {
                        /* 
                        Updates may be coming too fast and we can getNode out of sync.
                        The opposite vertex exists in the (di)graph, but we have not yet
                        created it for the panel. Therefore, its position is unknown,
                        so place the vertex representation in the middle.
                         */
                        x = mx;
                        y = my;
                    } else {
                        /* TODO: fix -- the placing point can be set out of bounds*/
                        Point2D position = new Point2D(existing.layoutXProperty().get(), existing.layoutYProperty().get());
                        Point2D p = UtilitiesPoint2D.rotate(position.add(50.0, 50.0),
                                position, Math.random() * 360);

                        x = p.getX();
                        y = p.getY();
                    }
                }

                SmartGraphVertexNode newVertex = this.getSmartGraphVertexNode(vertex);

                //track new nodes
                newVertices.add(newVertex);
                //add to global mapping
                this.vertexNodes.put(vertex, newVertex);
            }

        }

        Collection<Edge<E, V>> unplottedEdges = this.unplottedEdges();
        if (!unplottedEdges.isEmpty()) {
            for (Edge<E, V> edge : unplottedEdges) {

                Vertex<V>[] vertices = edge.vertices();
                Vertex<V> u = vertices[0]; //oubound if digraph, by javadoc requirement
                Vertex<V> v = vertices[1]; //inbound if digraph, by javadoc requirement

                SmartGraphVertexNode<V> graphVertexOut = this.vertexNodes.get(u);
                SmartGraphVertexNode<V> graphVertexIn = this.vertexNodes.get(v);

                /* 
                Updates may be coming too fast and we can getNode out of sync.
                Skip and wait for another update call, since they will surely
                be coming at this pace.
                 */
                if (graphVertexIn == null || graphVertexOut == null) {
                    continue;
                }

                graphVertexOut.addAdjacentVertex(graphVertexIn);
                graphVertexIn.addAdjacentVertex(graphVertexOut);

                SmartGraphEdgeBase graphEdge = createEdge(edge, graphVertexIn, graphVertexOut);

                if (this.edgesWithArrows) {
                    SmartArrow arrow = new SmartArrow();
                    graphEdge.attachArrow(arrow);
                    this.getChildren().add(arrow);
                }

                this.addEdge(graphEdge, edge);

            }
        }

        if (newVertices != null) {
            for (SmartGraphVertexNode<V> v : newVertices) {
                this.addVertex(v);
            }
        }

    }

    private void removeNodes() {
        Collection<Vertex<V>> removedVertices = this.removedVertices();
        Collection<SmartGraphEdgeBase> values = new LinkedList<>(this.edgeNodes.values());

        Set<SmartGraphVertexNode<V>> verticesToRemove = new HashSet<>();
        Set<SmartGraphEdgeBase> edgesToRemove = new HashSet<>();

        //filter vertices to remove and their adjacent edges
        for (Vertex<V> v : removedVertices) {

            for (SmartGraphEdgeBase edge : values) {
                Vertex[] vertices = edge.getUnderlyingEdge().vertices();

                if (vertices[0] == v || vertices[1] == v) {
                    edgesToRemove.add(edge);
                }
            }

            SmartGraphVertexNode<V> get = this.vertexNodes.get(v);
            verticesToRemove.add(get);
        }

        //permanently remove edges
        for (SmartGraphEdgeBase e : edgesToRemove) {
            this.edgeNodes.remove(e.getUnderlyingEdge());
            this.removeEdge(e);
            this.vertexNodes.get(e.getUnderlyingEdge().vertices()[0]).removeEdge(e);
            this.vertexNodes.get(e.getUnderlyingEdge().vertices()[1]).removeEdge(e);
        }

        //permanently remove vertices
        for (SmartGraphVertexNode<V> v : verticesToRemove) {
            this.vertexNodes.remove(v.getUnderlyingVertex());
            removeVertice(v);
        }

        //permanently remove remaining edges that were removed from the underlying graph
        Collection<Edge<E, V>> removedEdges = removedEdges();
        for (Edge<E, V> e : removedEdges) {
            this.removeEdge(this.edgeNodes.remove(e));
        }

        //remove adjacencies from remaining vertices
        for (SmartGraphVertexNode<V> v : this.vertexNodes.values()) {
            v.getAdjacentVertices().removeAll(verticesToRemove);
        }
    }

    private void removeEdge(SmartGraphEdgeBase e) {
        this.getChildren().remove((Node) e);

        SmartArrow attachedArrow = e.getAttachedArrow();
        if (attachedArrow != null) {
            this.getChildren().remove(attachedArrow);
        }

        Text attachedLabel = e.getAttachedLabel();
        if (attachedLabel != null) {
            this.getChildren().remove(attachedLabel);
        }
    }

    private void removeVertice(SmartGraphVertexNode v) {
        this.getChildren().remove(v.getNode());

        Text attachedLabel = v.getAttachedLabel();
        if (attachedLabel != null) {
            this.getChildren().remove(attachedLabel);
        }
    }

    /**
     * Updates node's labels
     */
    private void updateLabels() {
        this.theGraph.vertices().forEach((v) -> {
            SmartGraphVertexNode<V> vertexNode = this.vertexNodes.get(v);
            if (vertexNode != null) {
                SmartLabel label = vertexNode.getAttachedLabel();
                if (label != null) {
                    label.setText(v.element() != null ? v.element().toString() : "<NULL>");
                }
            }
        });

        this.theGraph.edges().forEach((e) -> {
            SmartGraphEdgeBase edgeNode = this.edgeNodes.get(e);
            if (edgeNode != null) {
                SmartLabel label = edgeNode.getAttachedLabel();
                if (label != null) {
                    label.setText(e.element() != null ? e.element().toString() : "<NULL>");
                }
            }
        });
    }

    /**
     * Computes the bounding box from all displayed vertices.
     *
     * @return bounding box
     */
    private Bounds getPlotBounds() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE,
                maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        if (this.vertexNodes.isEmpty()) {
            return new BoundingBox(0, 0, this.getWidth(), this.getHeight());
        }

        for (SmartGraphVertexNode<V> v : vertexNodes.values()) {
            minX = Math.min(minX, v.getPositionCenterX());
            minY = Math.min(minY, v.getPositionCenterY());
            maxX = Math.max(maxX, v.getPositionCenterX());
            maxY = Math.max(maxY, v.getPositionCenterY());
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    private List<Edge<E, V>> listOfEdges() {
        List<Edge<E, V>> list = new LinkedList<>();
        for (Edge<E, V> edge : this.theGraph.edges()) {
            list.add(edge);
        }
        return list;
    }

    private List<Vertex<V>> listOfVertices() {
        List<Vertex<V>> list = new LinkedList<>();
        for (Vertex<V> vertex : this.theGraph.vertices()) {
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

        for (Vertex<V> v : this.theGraph.vertices()) {
            if (!vertexNodes.containsKey(v)) {
                unplotted.add(v);
            }
        }

        return unplotted;
    }

    /**
     * Computes the collection for vertices that are currently being displayed
     * but do not longer exist in the underlying graph.
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
     * Computes the collection for edges that are currently being displayed but
     * do not longer exist in the underlying graph.
     *
     * @return collection of edges
     */
    private Collection<Edge<E, V>> removedEdges() {
        List<Edge<E, V>> removed = new LinkedList<>();

        Collection<Edge<E, V>> graphEdges = theGraph.edges();
        Collection<SmartGraphEdgeBase> plotted = edgeNodes.values();

        for (SmartGraphEdgeBase e : plotted) {
            if (!graphEdges.contains(e.getUnderlyingEdge())) {
                removed.add(e.getUnderlyingEdge());
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
     * Sets a vertex position (its center) manually.
     *
     * The positioning should be inside the boundaries of the panel, but no
     * restrictions are enforced by this method, so be aware.
     *
     * @param v underlying vertex
     * @param x x-coordinate on panel
     * @param y y-coordinate on panel
     */
    public void setVertexPosition(Vertex<V> v, double x, double y) {
        SmartGraphVertexNode<V> node = vertexNodes.get(v);
        if (node != null) {
            node.setPosition(x, y);
        }
    }

    /**
     * Return the current x-coordinate (relative to the panel) of a vertex.
     *
     * @param v underlying vertex
     * @return the x-coordinate or NaN if the vertex does not exist
     */
    public double getVertexPositionX(Vertex<V> v) {
        SmartGraphVertexNode<V> node = vertexNodes.get(v);
        if (node != null) {
            return node.getPositionCenterX();
        }
        return Double.NaN;
    }

    /**
     * Return the current y-coordinate (relative to the panel) of a vertex.
     *
     * @param v underlying vertex
     * @return the y-coordinate or NaN if the vertex does not exist
     */
    public double getVertexPositionY(Vertex<V> v) {
        SmartGraphVertexNode<V> node = vertexNodes.get(v);
        if (node != null) {
            return node.getPositionCenterY();
        }
        return Double.NaN;
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
     * Hides adjacent vertices that has no other adjacent.
     *
     * @param v target {@link  SmartGraphVertexNode}
     */
    public void hideAdjacentVertices(SmartGraphVertexNode<V> v) {
        this.setAdjacentVertexVisible(v, false);
    }

    /**
     * Shows adjacent vertices .
     *
     * @param v target {@link  SmartGraphVertexNode}
     */
    public void showAdjacentVertices(SmartGraphVertexNode<V> v) {
        this.setAdjacentVertexVisible(v, true);
    }

    /**
     * Hides adjacent vertices that has no other adjacent.
     *
     * @param v target {@link  SmartGraphVertexNode}
     */
    public void toggleAdjacentVertexVisible(SmartGraphVertexNode<V> v) {
        this.setAdjacentVertexVisible(v, null);
    }

    private void setAdjacentVertexVisible(SmartGraphVertexNode<V> targetNode, Boolean visible) {
        // get vertices from edge collection
        List<SmartGraphVertexNode> processedNode = new ArrayList<>();
        this.edgeNodes.values().forEach((edge) -> {
            Vertex[] vertices = edge.getUnderlyingEdge().vertices();
            // select only those connected to targetNode
            if (vertices[0] != vertices[1]
                    && (vertices[0] == targetNode.getUnderlyingVertex() || vertices[1] == targetNode.getUnderlyingVertex())) {
                Vertex adjV = vertices[0];
                if (adjV == targetNode.getUnderlyingVertex()) {
                    adjV = vertices[1];
                }
                SmartGraphVertexNode adjNode = this.vertexNodes.get(adjV);
                // process only new adjacent node
                // the already processed nodes are store in processedNode list
                if (!processedNode.contains(adjNode)) {
                    processedNode.add(adjNode);

                    // prepare visible value
                    // if not provided ('visible' = null) toggle back from previus status
                    boolean value;
                    if (visible != null) {
                        value = visible;
                    } else {
                        value = !adjNode.visibleProperty().get();
                    }
                    // process hiding
                    if (adjNode.getAdjacentVertices().size() == 1) {
                        // has only one adjacent i.e. the targetNode so it can be hidden.
                        this.doSetAdjacentVertexVisible(adjNode, edge, value);
                    } else {
                        // has many adjacents
                        // check if all adjacents are itself or targetNode 
                        boolean toSelf = true;
                        for (Object obj : adjNode.getAdjacentVertices()) {
                            SmartGraphVertexNode adjOfAdjNode = (SmartGraphVertexNode) obj;
                            if (adjOfAdjNode != targetNode && adjOfAdjNode.visibleProperty().get() & adjOfAdjNode != adjNode) {
                                toSelf = false;
                                break;
                            }
                        }
                        // if all adjacents are itself or targetNode
                        // hide node and its edges
                        if (toSelf) {
                            this.edgeNodes.values().forEach(selfEdge -> {
                                Vertex[] selfVertices = selfEdge.getUnderlyingEdge().vertices();
                                if ((selfVertices[0] == adjNode.getUnderlyingVertex() && selfVertices[1] == targetNode.getUnderlyingVertex())
                                        || (selfVertices[0] == targetNode.getUnderlyingVertex() && selfVertices[1] == adjNode.getUnderlyingVertex())
                                        || (selfVertices[0] == adjNode.getUnderlyingVertex() && selfVertices[1] == adjNode.getUnderlyingVertex())) {
                                    this.doSetAdjacentVertexVisible(adjNode, selfEdge, value);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void doSetAdjacentVertexVisible(SmartGraphVertexNode v, SmartGraphEdgeBase e, boolean value) {
        v.visibleProperty().set(value);
        if (v.getAttachedLabel() != null) {
            v.getAttachedLabel().setVisible(value);
        }
        ((Shape) e).setVisible(value);
        if (e.getAttachedArrow() != null) {
            e.getAttachedArrow().setVisible(value);
        }
        if (e.getAttachedLabel() != null) {
            e.getAttachedLabel().setVisible(value);
        }
    }

    /**
     * Loads the stylesheet and applies the .graph class to this panel.
     */
    private void loadStylesheet(URI cssFile) {
        try {
            String css;
            if (cssFile != null) {
                css = cssFile.toURL().toExternalForm();
            } else {
                File f = new File("smartgraph.css");
                css = f.toURI().toURL().toExternalForm();
            }

            getStylesheets().add(css);
            this.getStyleClass().add("graph");
        } catch (MalformedURLException ex) {
            Logger.getLogger(SmartGraphView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Enables the mouse action on this pane.
     *
     * This method identifies the node that was clicked and, if any, calls the
     * appropriate consumer, i.e., vertex or edge consumers.
     */
    private void enableMouseEventListener() {
        this.setOnMouseClicked((MouseEvent mouseEvent) -> {
            Node node = pick(SmartGraphView.this, mouseEvent.getSceneX(), mouseEvent.getSceneY());
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                // detect double click
                if (mouseEvent.getClickCount() == 2) {
                    //no need to continue otherwise
                    if (vertexClickConsumer == null && edgeClickConsumer == null) {
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
