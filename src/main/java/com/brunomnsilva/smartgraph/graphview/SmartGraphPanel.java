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
package com.brunomnsilva.smartgraph.graphview;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.brunomnsilva.smartgraph.graphview.UtilitiesJavaFX.pick;

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
     * CONSTANTS
     */

    /** Padding (in pixels) around a new vertex spawn position. */
    public static final int VERTEX_SPAWN_PADDING = 50;
    /** Multiplier used for introducing randomness when spawning a vertex. */
    public static final int VERTEX_SPAWN_RANDOM_MULTIPLER = 10;

    /*
     * CONFIGURATION PROPERTIES
     */

    /** Configuration properties for customizing the graph behavior and appearance. */
    private final SmartGraphProperties graphProperties;
    /** Default CSS file used to style the graph. */
    private static final String DEFAULT_CSS_FILE = "smartgraph.css";

    /*
     * INTERNAL DATA STRUCTURE
     */

    /** The internal graph data structure representing vertices and edges. */
    private final Graph<V, E> theGraph;
    /** Strategy used to determine the initial placement of graph elements. */
    private final SmartPlacementStrategy placementStrategy;
    /** Mapping between logical vertices and their corresponding visual nodes. */
    private final Map<Vertex<V>, SmartGraphVertexNode<V>> vertexNodes;
    /** Mapping between logical edges and their corresponding visual nodes. */
    private final Map<Edge<E, V>, SmartGraphEdgeNode<E, V>> edgeNodes;
    /** Mapping between edges and the pair of vertices they connect. */
    private final Map<Edge<E, V>, Tuple<Vertex<V>>> connections;
    /** Flag indicating whether the graph visualization has been initialized. */
    private boolean initialized = false;
    /** Flag indicating whether edges should be rendered with arrows. */
    private final boolean edgesWithArrows;

    /*
     * INTERACTION WITH VERTICES AND EDGES
     */

    /** Function to be executed when a vertex is clicked. */
    private Consumer<SmartGraphVertex<V>> vertexClickConsumer;
    /** Function to be executed when an edge is clicked. */
    private Consumer<SmartGraphEdge<E, V>> edgeClickConsumer;

    /*
     * OPTIONAL PROVIDERS FOR LABELS, RADII AND SHAPE TYPES OF NODES.
     * THESE HAVE PRIORITY OVER ANY MODEL ANNOTATIONS (E.G., SmartLabelSource)
     */

    /** Provides custom labels for vertices, overriding annotation-based sources. */
    private SmartLabelProvider<V> vertexLabelProvider;
    /** Provides custom labels for edges, overriding annotation-based sources. */
    private SmartLabelProvider<E> edgeLabelProvider;
    /** Provides custom radii for vertices, overriding annotation-based sources. */
    private SmartRadiusProvider<V> vertexRadiusProvider;
    /** Provides custom shape types for vertices, overriding annotation-based sources. */
    private SmartShapeTypeProvider<V> vertexShapeTypeProvider;

    /*
     * AUTOMATIC LAYOUT RELATED ATTRIBUTES
     */

    /** Property to toggle the automatic layout of nodes. */
    public final BooleanProperty automaticLayoutProperty;
    /** Timer used to drive the automatic layout updates. */
    private final AnimationTimer timer;
    /** Strategy used for automatic layout of graph nodes. */
    private ForceDirectedLayoutStrategy<V> automaticLayoutStrategy;
    /** Number of iterations per frame for the automatic layout algorithm. */
    private static final int AUTOMATIC_LAYOUT_ITERATIONS = 20;

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom parameters.
     * <br/>
     * This is the only FXML-friendly constructor (there can only be one). If you need to instantiate the default
     * parameters (besides <code>graph</code>), they are the following:
     * <ul>
     *     <li>properties - <code>new SmartGraphProperties()</code></li>
     *     <li>placementStrategy - <code>new SmartCircularSortedPlacementStrategy()</code></li>
     *     <li>cssFileURI - <code>new File("smartgraph.css").toURI()</code></li>
     *     <li>automaticLayoutStrategy - <code>new ForceDirectedSpringGravityLayoutStrategy()</code></li>
     * </ul>
     *
     * @param theGraph underlying graph
     * @param properties custom properties
     * @param placementStrategy placement strategy
     * @param cssFile alternative css file, instead of default 'smartgraph.css'
     * @param layoutStrategy  the automatic layout strategy to use
     * @throws IllegalArgumentException if any of the arguments is <code>null</code>
     */
    public SmartGraphPanel(@NamedArg("graph") Graph<V, E> theGraph,
                           @NamedArg("properties") SmartGraphProperties properties,
                           @NamedArg("placementStrategy") SmartPlacementStrategy placementStrategy,
                           @NamedArg("cssFileURI") URI cssFile,
                           @NamedArg("automaticLayoutStrategy") ForceDirectedLayoutStrategy<V> layoutStrategy) {

        Args.requireNotNull(theGraph, "theGraph");
        Args.requireNotNull(properties, "properties");
        Args.requireNotNull(placementStrategy, "placementStrategy");
        Args.requireNotNull(cssFile, "cssFile");
        Args.requireNotNull(layoutStrategy, "layoutStrategy");

        this.theGraph = theGraph;
        this.graphProperties = properties;
        this.placementStrategy = placementStrategy;

        this.edgesWithArrows = this.graphProperties.getUseEdgeArrow();

        this.automaticLayoutStrategy = layoutStrategy;

        this.vertexNodes = new HashMap<>();
        this.edgeNodes = new HashMap<>();

        // We will explore the insertion order of edges for decreasing the multiplicity of edges between the same
        // pair of vertices, when others are removed
        this.connections = new LinkedHashMap<>();

        // consumers initially are not set. This initialization is not necessary, but we make it explicit
        // for the sake of readability
        this.vertexClickConsumer = null;
        this.edgeClickConsumer = null;

        //set stylesheet and class
        loadAndApplyStylesheet(cssFile);

        initNodes();

        enableDoubleClickListener();

        //automatic layout initializations
        timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                runAutomaticLayout();
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

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties, default circular
     * placement of vertices, default automatic spring gravity layout strategy
     * and styling from smartgraph.css.
     * @see Graph
     * @see SmartGraphProperties
     * @see SmartCircularSortedPlacementStrategy
     * @see ForceDirectedSpringGravityLayoutStrategy
     *
     * @param theGraph underlying graph
     * @throws IllegalArgumentException if <code>theGraph</code> is <code>null</code>
     */
    public SmartGraphPanel(Graph<V, E> theGraph) {
        this(theGraph,
            new SmartGraphProperties(),
            new SmartCircularSortedPlacementStrategy(),
            new File(DEFAULT_CSS_FILE).toURI(),
            new ForceDirectedSpringGravityLayoutStrategy<>()
        );
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties, default circular
     * placement of vertices and styling from smartgraph.css.
     *
     * @param theGraph underlying graph
     * @param layoutStrategy the automatic layout strategy
     * @throws IllegalArgumentException if any of the arguments is <code>null</code>
     */
    public SmartGraphPanel(Graph<V, E> theGraph, ForceDirectedLayoutStrategy<V> layoutStrategy) {
        this(theGraph,
            new SmartGraphProperties(),
            new SmartCircularSortedPlacementStrategy(),
            new File(DEFAULT_CSS_FILE).toURI(),
            layoutStrategy
        );
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties, default automatic spring gravity layout strategy
     * and styling from smartgraph.css.
     *
     * @param theGraph underlying graph
     * @param properties custom properties
     * @throws IllegalArgumentException if any of the arguments is <code>null</code>
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties) {
        this(theGraph,
            properties,
            new SmartCircularSortedPlacementStrategy(),
            new File(DEFAULT_CSS_FILE).toURI(),
            new ForceDirectedSpringGravityLayoutStrategy<>()
        );
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties and styling from smartgraph.css.
     *
     * @param theGraph underlying graph
     * @param placementStrategy placement strategy
     * @param layoutStrategy the automatic layout strategy
     * @throws IllegalArgumentException if any of the arguments is <code>null</code>
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy,
                           ForceDirectedLayoutStrategy<V> layoutStrategy) {
        this(theGraph,
            new SmartGraphProperties(),
            placementStrategy,
            new File(DEFAULT_CSS_FILE).toURI(),
            layoutStrategy
        );
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom placement of
     * vertices, default properties, default automatic spring gravity layout strategy
     * and styling from smartgraph.css.
     *
     * @param theGraph underlying graph
     * @param placementStrategy placement strategy, null for default
     * @throws IllegalArgumentException if any of the arguments is <code>null</code>
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy) {
        this(theGraph,
            new SmartGraphProperties(),
            placementStrategy,
            new File(DEFAULT_CSS_FILE).toURI(),
            new ForceDirectedSpringGravityLayoutStrategy<>()
        );
    }

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties and custom placement of
     * vertices, default automatic spring gravity layout strategy
     * and styling from smartgraph.css.
     *
     * @param theGraph underlying graph
     * @param properties custom properties, null for default
     * @param placementStrategy placement strategy, null for default
     * @throws IllegalArgumentException if any of the arguments is <code>null</code>
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy) {

        this(theGraph,
            properties,
            placementStrategy,
            new File(DEFAULT_CSS_FILE).toURI(),
            new ForceDirectedSpringGravityLayoutStrategy<>()
        );
    }
    
    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom properties, custom placement of
     * vertices and default automatic spring gravity layout strategy.
     *
     * @param theGraph underlying graph
     * @param properties custom properties, null for default
     * @param placementStrategy placement strategy, null for default
     * @param cssFile alternative css file, instead of default 'smartgraph.css'
     * @throws IllegalArgumentException if any of the arguments is <code>null</code>
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy, URI cssFile) {

        this(theGraph,
            properties,
            placementStrategy,
            cssFile,
            new ForceDirectedSpringGravityLayoutStrategy<>()
        );
    }

    /**
     * Executes a single animation step of the automatic layout algorithm.
     * <br/>
     * This method performs a fixed number of layout iterations where:
     * <ul>
     *     <li>All force vectors are reset.</li>
     *     <li>Attractive and repulsive forces are computed between nodes.</li>
     *     <li>These forces are accumulated and updated.</li>
     *     <li>Final positions are then updated accordingly.</li>
     * </ul>
     * This method is synchronized to ensure thread safety during layout updates.
     */
    private synchronized void runAutomaticLayout() {
        for (int i = 0; i < AUTOMATIC_LAYOUT_ITERATIONS; i++) {
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
     * <br/>
     * Furthermore, required updates should be performed through the {@link #update()
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
                    this);
        } else {
            //apply circular placement, I think it's a better initial state for automatic layout
            new SmartCircularSortedPlacementStrategy().place(this.widthProperty().doubleValue(),
                    this.heightProperty().doubleValue(),
                    this);

            //start automatic layout
            timer.start();
        }

        this.initialized = true;
    }

    /**
     * Returns the property used to toggle the automatic layout of vertices.
     * @return  automatic layout property
     */
    public BooleanProperty automaticLayoutProperty() {
        return this.automaticLayoutProperty;
    }
    
    /**
     * Toggle the automatic layout of vertices.
     * @param value true if enabling; false, otherwise
     */
    public void setAutomaticLayout(boolean value) {
        automaticLayoutProperty.set(value);
    }

    /**
     * Changes the current automatic layout strategy.
     * @param strategy the new strategy to use
     */
    public void setAutomaticLayoutStrategy(ForceDirectedLayoutStrategy<V> strategy) {
        Args.requireNotNull(strategy, "strategy");

        this.automaticLayoutStrategy = strategy;
    }

    /**
     * Returns the reference of underlying model depicted by this panel.
     * <br/>
     * The concrete type of the returned instance may be a {@link Graph} or {@link Digraph}.
     * @return the underlying model
     */
    public Graph<V, E> getModel() {
        return theGraph;
    }

    /**
     * Returns a collection of the smart vertices that represent the underlying model vertices.
     * @return a collection of the smart vertices
     */
    public final Collection<SmartGraphVertex<V>> getSmartVertices() {
        return new ArrayList<>(this.vertexNodes.values());
    }

    /**
     * Returns a collection of the smart edges that represent the underlying model edges.
     * @return a collection of the smart edges
     */
    public final Collection<SmartGraphEdge<E, V>> getSmartEdges() {
        return new ArrayList<>(this.edgeNodes.values());
    }

    /**
     * Forces a refresh of the visualization based on current state of the
     * underlying graph, immediately returning to the caller.
     * <br/>
     * This method invokes the refresh in the graphical
     * thread through Platform.runLater(), so its not guaranteed that the visualization is in sync
     * immediately after this method finishes. That is, this method
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
        Platform.runLater(() -> updateViewModel());
    }
    
    /**
     * Forces a refresh of the visualization based on current state of the
     * underlying graph and waits for completion of the update.
     * <br/>
     * Use this variant only when necessary, e.g., need to style an element
     * immediately after adding it to the underlying graph. Otherwise, use
     * {@link #update() } instead for performance’s sake.
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
        
        final FutureTask<Boolean> update = new FutureTask<>(() -> {
            updateViewModel();
            return true;
        });
        
        //
        if(!Platform.isFxApplicationThread()) {            
            //this will be called from a non-javafx thread, so this must be guaranteed to run of the graphics thread
            Platform.runLater(update);
        
            //wait for completion, only outside javafx thread; otherwise -> deadlock
            try {            
                update.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            updateViewModel();
        }
        
    }

    /**
     * Updates the visual representation of the graph to match the underlying data model.
     * <p>
     * This includes removing obsolete nodes, inserting new ones, and updating existing nodes
     * to reflect any changes. Synchronized to ensure thread safety during view updates.
     */
    private synchronized void updateViewModel() {
        removeNodes();
        insertNodes();
        updateNodes();
    }

    /**
     * Initializes the graphical nodes for all vertices and edges in the graph.
     * <br/>
     * For each vertex in the graph, creates a corresponding visual node and registers it.
     * Then, iterates over all edges to create and connect their visual representations, avoiding duplicates.
     * Finally, adds all vertex nodes to the view, ensuring they are placed above edge representations.
     */
    private void initNodes() {

        /* Create vertex graphical representations. */
        for (Vertex<V> vertex : listOfVertices()) {

            SmartGraphVertexNode<V> vertexAnchor = createVertex(vertex, 0, 0);

            vertexNodes.put(vertex, vertexAnchor);
        }

        /* Create graphical representations of edges between existing vertices. */
        List<Edge<E, V>> edgesToPlace = listOfEdges();

        for (Vertex<V> vertex : vertexNodes.keySet()) {

            Iterable<Edge<E, V>> incidentEdges = theGraph.incidentEdges(vertex);

            for (Edge<E, V> edge : incidentEdges) {

                // If already plotted, ignore edge.
                if (!edgesToPlace.contains(edge)) {
                    continue;
                }

                Vertex<V> oppositeVertex = theGraph.opposite(vertex, edge);

                SmartGraphVertexNode<V> graphVertexIn = vertexNodes.get(vertex);
                SmartGraphVertexNode<V> graphVertexOppositeOut = vertexNodes.get(oppositeVertex);

                graphVertexIn.addAdjacentVertex(graphVertexOppositeOut);
                graphVertexOppositeOut.addAdjacentVertex(graphVertexIn);

                SmartGraphEdgeNode<E,V> graphEdge = createEdge(edge, graphVertexIn, graphVertexOppositeOut);

                // Track already placed edges
                connections.put(edge, new Tuple<>(vertex, oppositeVertex));
                addEdge(graphEdge, edge);

                edgesToPlace.remove(edge);
            }

        }

        /*
         * Vertices are added after to maintain a z-order of (edges -> vertices)
         */
        for (Vertex<V> vertex : vertexNodes.keySet()) {
            SmartGraphVertexNode<V> v = vertexNodes.get(vertex);

            addVertex(v);
        }
    }

    /**
     * Creates a visual representation of the given vertex at the specified (x, y) coordinates.
     *
     * @param v the vertex to create a visual node for
     * @param x the x-coordinate for the vertex placement
     * @param y the y-coordinate for the vertex placement
     * @return the newly created {@code SmartGraphVertexNode} representing the vertex
     */
    private SmartGraphVertexNode<V> createVertex(Vertex<V> v, double x, double y) {
        // Read shape type from annotation or use default (circle)
        String shapeType = getVertexShapeTypeFor(v.element());

        // Read shape radius from annotation or use default
        double shapeRadius = getVertexShapeRadiusFor(v.element());

        return new SmartGraphVertexNode<>(this, v, x, y, shapeRadius, shapeType, graphProperties.getVertexAllowUserMove());
    }

    /**
     * Creates a visual representation of the given edge between two specified vertex nodes, assigning a multiplicity index
     * to manage parallel edges. The index determines the curvature of the edge: straight for index 0 and curved for higher values.
     * <br/>
     * The multiplicity index is incremented based on the current maximum index between the given vertices, ensuring proper spacing
     * between multiple edges. This index is later recycled when edges are removed.
     *
     * @param edge the edge to be represented
     * @param graphVertexInbound the inbound (source) vertex node
     * @param graphVertexOutbound the outbound (destination) vertex node
     * @return a newly created {@code SmartGraphEdgeNode} representing the edge
     */
    private SmartGraphEdgeNode<E,V> createEdge(Edge<E, V> edge, SmartGraphVertexNode<V> graphVertexInbound, SmartGraphVertexNode<V> graphVertexOutbound) {
        int maxIndex = getMaxMultiplicityIndexBetween(graphVertexInbound, graphVertexOutbound);
        return new SmartGraphEdgeNode<>(edge, graphVertexInbound, graphVertexOutbound, ++maxIndex);
    }

    /**
     * Adds a vertex node to the graph pane, along with its optional tooltip and label, based on the current graph properties.
     * <br/>
     * If tooltips are enabled, a tooltip is created with the vertex label text and installed on the vertex node.
     * If labels are enabled, a {@code SmartLabel} is created, styled, and attached to the vertex node.
     *
     * @param v the {@code SmartGraphVertexNode} to be added to the graph pane
     */
    private void addVertex(SmartGraphVertexNode<V> v) {
        this.getChildren().add(v);

        String labelText = getVertexLabelFor(v.getUnderlyingVertex().element());
        
        if (graphProperties.getUseVertexTooltip()) {            
            Tooltip t = new Tooltip(labelText);
            Tooltip.install(v, t);
        }

        if (graphProperties.getUseVertexLabel()) {
            SmartLabel label = new SmartLabel(labelText);

            label.addStyleClass("vertex-label");
            this.getChildren().add(label);
            v.attachLabel(label);
        }
    }

    /**
     * Adds an edge node to the graph pane, along with its optional tooltip, label, and directional arrow if applicable.
     * <p>
     * The edge node is inserted at the back of the children list to maintain visual z-ordering (edge, arrow, label).
     * If tooltips are enabled, a tooltip is created and installed on the edge node.
     * If labels are enabled, a {@code SmartLabel} is created, styled, and attached to the edge node.
     * If the graph is directed and edge arrows are enabled, a {@code SmartArrow} is created, attached, and placed behind the label.
     *
     * @param e the {@code SmartGraphEdgeNode} to be added to the graph pane
     * @param edge the model {@code Edge} associated with the graphical edge node
     */
    private void addEdge(SmartGraphEdgeNode<E,V> e, Edge<E, V> edge) {
        // Keep the following z-order (edge, arrow, label)

        this.getChildren().add(0, e);
        edgeNodes.put(edge, e);

        String labelText = getEdgeLabelFor(edge.element());
        
        if (graphProperties.getUseEdgeTooltip()) {
            Tooltip t = new Tooltip(labelText);
            Tooltip.install(e, t);
        }

        if (graphProperties.getUseEdgeLabel()) {
            SmartLabel label = new SmartLabel(labelText);

            label.addStyleClass("edge-label");
            this.getChildren().add(1, label);
            e.attachLabel(label);
        }

        // Arrows to the back
        if (this.edgesWithArrows && theGraph instanceof Digraph) {
            SmartArrow arrow = new SmartArrow(this.graphProperties.getEdgeArrowSize());
            e.attachArrow(arrow);
            this.getChildren().add(1, arrow);
        }
    }

    /**
     * Inserts new vertices and edges into the graph visualization.
     * <br/>
     * For each unplotted vertex, a new graphical vertex node is spawned, tracked, and added to the vertex mapping.
     * For each unplotted edge, it connects the corresponding vertex nodes if both exist, creates a graphical edge node,
     * tracks the connection, and adds the edge to the visualization.
     * <br/>
     * Finally, all newly spawned vertices are added to the pane.
     * If vertices or edges are missing from the mapping during updates (possibly due to rapid changes), the method skips
     * adding those edges to avoid inconsistency and waits for subsequent updates.
     */
    private void insertNodes() {
        Collection<Vertex<V>> unplottedVertices = unplottedVertices();

        List<SmartGraphVertexNode<V>> newVertices = null;

        if (!unplottedVertices.isEmpty()) {

            newVertices = new LinkedList<>();

            for (Vertex<V> vertex : unplottedVertices) {

                SmartGraphVertexNode<V> newVertex = spawnVertex(vertex);

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
                Vertex<V> u = vertices[0]; //outbound if digraph, by javadoc requirement
                Vertex<V> v = vertices[1]; //inbound if digraph, by javadoc requirement

                SmartGraphVertexNode<V> graphVertexOut = vertexNodes.get(u);
                SmartGraphVertexNode<V> graphVertexIn = vertexNodes.get(v);

                /* 
                Updates may be coming too fast, and we can get out of sync.
                Skip and wait for another update call, since they will surely
                be coming at this pace.
                */
                if(graphVertexIn == null || graphVertexOut == null) {
                    continue;
                }
                
                graphVertexOut.addAdjacentVertex(graphVertexIn);
                graphVertexIn.addAdjacentVertex(graphVertexOut);

                SmartGraphEdgeNode<E,V> graphEdge = createEdge(edge, graphVertexIn, graphVertexOut);

                 /* Track edges */
                connections.put(edge, new Tuple<>(u, v));
                addEdge(graphEdge, edge);

            }
        }

        if (newVertices != null) {
            for (SmartGraphVertexNode<V> v : newVertices) {
                addVertex(v);
            }
        }

    }

    /**
     * Find an optimal position for the spawning of a new vertex.
     * @param vertex the underlying vertex
     * @return an instance of SmartGraphVertexNode
     */
    private SmartGraphVertexNode<V> spawnVertex(Vertex<V> vertex) {
        Bounds graphBounds = getDisplayedVerticesBoundingBox();
        //double fallbackX = graphBounds.getMinX() + graphBounds.getWidth() / 2.0;
        //double fallBackY = graphBounds.getMinY() + graphBounds.getHeight() / 2.0;

        // top-left corner of the bounding box
        double fallbackX = graphBounds.getMinX();
        double fallBackY = graphBounds.getMinY();

        double x =  fallbackX, y = fallBackY;
        Collection<Edge<E, V>> incidentEdges = theGraph.incidentEdges(vertex);

        /* If not (yet?) connected, it may be an isolated vertex or the user is calling
         * update() before connecting it with an edge; impossible to predict.
         * Put the vertex outside the bounds of the current graph, trying to minimize
         * consequences of the automatic (spring) layout system.
         * TODO: Should produce some documentation warning the user to avoid this usage.
         */

        int neighorCount = 0;
        if(!incidentEdges.isEmpty()) {
            /*
             * Compute centroid of neighbor positions. This will be more effective if the new vertex has
             * several neighbors. In a more frequent situation (e.g. one neighbor), this will place the
             * new vertex over the single neighbor. We'll improve this position afterwards.
             */
            double sumX = 0, sumY = 0;

            for (Edge<E, V> edge : incidentEdges) {
                Vertex<V> vNeighbor = theGraph.opposite(vertex, edge);
                if(vertex == vNeighbor) continue; // self-loop

                SmartGraphVertexNode<V> neighbor = vertexNodes.get(vNeighbor);

                if(neighbor != null) {
                    sumX += neighbor.getPositionCenterX();
                    sumY += neighbor.getPositionCenterY();
                    neighorCount++;
                }
            }

            /*
             * Updates may be coming too fast, and we can get out of sync.
             * The opposite vertex exists in the (di)graph, but we have not yet
             * created it for the panel. Therefore, its position is unknown,
             * so place the vertex outside the bounds of the current graph.
             * This will yield neighborCount == 0, hence we maintain the fallback positioning
             */

            if(neighorCount > 0) {
                x = sumX / neighorCount;
                y = sumY / neighorCount;
            }
        }

        // 2. Check if initial placement proposal is free
        if(isPositionFreeForVertexSpawn(vertex, x, y)) {
            return createVertex(vertex, x, y);
        }

        // 3. If not, spiral search outward around centroid
        double spawnRadius = getVertexShapeRadiusFor(vertex.element());
        int angularSteps = 16;
        double stepRadius = 2 * spawnRadius + VERTEX_SPAWN_PADDING;

        for (int ring = 1; ring <= 10; ring++) {
            double r = ring * stepRadius;
            for (int i = 0; i < angularSteps; i++) {
                double angle = 2 * Math.PI * i / angularSteps;
                double newX = x + r * Math.cos(angle);
                double newY = y + r * Math.sin(angle);

                // Quit if we overflow for some reason
                if(newX < 0 || newY < 0 || !Double.isFinite(newX) || !Double.isFinite(newY)) {
                    break;
                }

                if (isPositionFreeForVertexSpawn(vertex, newX, newY)) {
                    return createVertex(vertex, newX, newY);
                }
            }
        }

        // 4. We're desperate: random scatter
        int tries = 20;
        while (tries > 0) {
            double newX = x + (Math.random() - 0.5) * VERTEX_SPAWN_RANDOM_MULTIPLER * stepRadius;
            double newY = y + (Math.random() - 0.5) * VERTEX_SPAWN_RANDOM_MULTIPLER * stepRadius;

            // Quit if we overflow for some reason
            if(newX < 0 || newY < 0 || !Double.isFinite(newX) || !Double.isFinite(newY)) {
                break;
            }

            if (isPositionFreeForVertexSpawn(vertex, newX, newY)) {
                return createVertex(vertex, newX, newY);
            }
            tries--;
        }

        // Force initial proposal, could not find any better placement. The graph is packed!
        // Offset the position by some amount
        return createVertex(vertex, x + (Math.random() - 0.5) * spawnRadius, y + (Math.random() - 0.5) * spawnRadius);
    }

    /**
     * Check if a position is not occupied by any vertex and if it's inside the layout bounds.
     *
     * @param vertex the element that the spawn vertex will contain. Used internally to derive the future vertex radius.
     * @param x x-coordinate to check
     * @param y y-coordinate to check
     * @return true if available; false, otherwise
     */
    private boolean isPositionFreeForVertexSpawn(Vertex<V> vertex, double x, double y) {

        double spawnRadius = getVertexShapeRadiusFor(vertex.element());
        Bounds panelBounds = this.getLayoutBounds();

        for (SmartGraphVertexNode<V> v : vertexNodes.values()) {
            double dx = v.getPositionCenterX() - x;
            double dy = v.getPositionCenterY() - y;
            double minDistance = spawnRadius + v.getRadius() + VERTEX_SPAWN_PADDING;

            if (Math.hypot(dx, dy) < minDistance || !panelBounds.contains(x, y)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes vertices and edges that are no longer present in the underlying graph model.
     * <br/>
     * For each removed edge, it removes the graphical edge node from the pane and internal mappings,
     * updates the adjacency of its connected vertices if no other edges remain between them,
     * and updates edge multiplicities accordingly.
     * <br/>
     * For each removed vertex, it removes the graphical vertex node from the pane and internal mappings.
     */
    private void removeNodes() {

        Collection<Edge<E, V>> removedEdges = removedEdges();
        for (Edge<E, V> e : removedEdges) {
            SmartGraphEdgeNode<E,V> edgeToRemove = edgeNodes.get(e);
            edgeNodes.remove(e);
            removeEdge(edgeToRemove);   //remove from panel

            //when edges are removed, the adjacency between vertices changes
            //the adjacency is kept in parallel in an internal data structure
            Tuple<Vertex<V>> vertexTuple = connections.get(e);

            SmartGraphVertexNode<V> v0 = vertexNodes.get(vertexTuple.first);
            SmartGraphVertexNode<V> v1 = vertexNodes.get(vertexTuple.second);

            if( getTotalEdgesBetweenInModel(vertexTuple.first, vertexTuple.second) == 0 ) {
                v0.removeAdjacentVertex(v1);
                v1.removeAdjacentVertex(v0);
            }

            connections.remove(e);

            // Update the multiplicity of the edges between the removed edge connecting vertices
            updateEdgeMultiplicityBetween(v0, v1);
        }

        Collection<Vertex<V>> removedVertices = removedVertices();
        for (Vertex<V> removedVertex : removedVertices) {
            SmartGraphVertexNode<V> removed = vertexNodes.remove(removedVertex);
            removeVertex(removed);
        }
                
    }

    /**
     * Removes the specified edge graphical node and its associated arrow and label from the pane.
     *
     * @param e the SmartGraphEdgeNode to be removed from the view
     */
    private void removeEdge(SmartGraphEdgeNode<E,V> e) {
        getChildren().remove(e);

        SmartArrow attachedArrow = e.getAttachedArrow();
        if (attachedArrow != null) {
            getChildren().remove(attachedArrow);
        }

        Pane attachedLabel = e.getAttachedLabel();
        if (attachedLabel != null) {
            getChildren().remove(attachedLabel);
        }
    }

    /**
     * Updates the multiplicity indices of parallel edges between two vertex nodes to ensure proper visual separation.
     * The oldest edge gets a multiplicity index of 0 (straight line), and subsequent edges are assigned
     * indices that maintain parity and minimize overlap by alternating sides of the line axis.
     *
     * @param v the first vertex node
     * @param w the second vertex node
     */
    private void updateEdgeMultiplicityBetween(SmartGraphVertexNode<V> v, SmartGraphVertexNode<V> w) {
        if(v == null || w == null) return;

        // 'getEdgesBetween' returns a collection of edges, ordered by their "age".
        List<SmartGraphEdgeNode<E, V>> parallelEdges = getEdgesBetween(v, w);

        int numEdges = parallelEdges.size();
        if(numEdges > 0) {

            // Oldest edge will always have multiplicity 0 (straight line)
            parallelEdges.get(0).setMultiplicityIndex(0);

            // The remaining edges will have their multiplicities as close to 0 has possible,
            // while maintaining their parity (same side of the line axis)
            int current = 1;
            for (int i = 1; i < numEdges; i++) {
                int parity = parallelEdges.get(i).getMultiplicityIndex() % 2;

                while (current % 2 != parity) {
                    current++;
                }

                parallelEdges.get(i).setMultiplicityIndex(current);
                current++; // Move to the next possible value
            }
        }

    }

    /**
     * Removes the specified vertex node and its attached label from the pane.
     *
     * @param v the vertex node to be removed
     */
    private void removeVertex(SmartGraphVertexNode<V> v) {
        getChildren().remove(v);

        Pane attachedLabel = v.getAttachedLabel();
        if (attachedLabel != null) {
            getChildren().remove(attachedLabel);
        }
    }

    /**
     * Updates graphical properties of all existing vertex and edge nodes.
     * For each vertex, updates the attached label text, radius, and shape type.
     * For each edge, updates the attached label text.
     */
    private void updateNodes() {
        theGraph.vertices().forEach((v) -> {
            SmartGraphVertexNode<V> vertexNode = vertexNodes.get(v);
            if (vertexNode != null) {
                SmartLabel label = vertexNode.getAttachedLabel();
                if(label != null) {
                    String text = getVertexLabelFor(v.element());
                    label.setText( text );
                }

                double radius = getVertexShapeRadiusFor(v.element());
                vertexNode.setRadius(radius);

                String shapeType = getVertexShapeTypeFor(v.element());
                vertexNode.setShapeType(shapeType);
            }

        });
        
        theGraph.edges().forEach((e) -> {
            SmartGraphEdgeNode<E,V> edgeNode = edgeNodes.get(e);
            if (edgeNode != null) {
                SmartLabel label = edgeNode.getAttachedLabel();
                if (label != null) {
                    String text = getEdgeLabelFor(e.element());
                    label.setText( text );
                }
            }
        });
    }

    /**
     * Returns the label string to display for the given vertex element.
     * <br/>
     * Priority order:
     * <ol>
     *   <li>If the vertex element is null, returns the string "&lt;NULL&gt;".</li>
     *   <li>If a vertexLabelProvider is set, delegates to it.</li>
     *   <li>Otherwise, reflects on the vertex element's methods to find one annotated
     *       with {@link SmartLabelSource}, invokes it, and returns its string value.</li>
     *   <li>If reflection fails or no annotated method is found, returns
     *       {@code vertexElement.toString()}.</li>
     * </ol>
     *
     * @param vertexElement the vertex element for which to get the label
     * @return the label string for the vertex element
     */
    protected final String getVertexLabelFor(V vertexElement) {

        if(vertexElement == null) return "<NULL>";

        if(vertexLabelProvider != null) {
            return vertexLabelProvider.valueFor(vertexElement);
        }
        
        try {
            Class<?> clazz = vertexElement.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SmartLabelSource.class)) {
                    method.setAccessible(true);
                    Object value = method.invoke(vertexElement);
                    return value.toString();
                }
            }
        } catch (SecurityException | IllegalAccessException  | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return vertexElement.toString();
    }

    /**
     * Returns the label string to display for the given edge element.
     * <br/>
     * Priority order:
     * <ol>
     *   <li>If the edge element is null, returns the string "&lt;NULL&gt;".</li>
     *   <li>If an edgeLabelProvider is set, delegates to it.</li>
     *   <li>Otherwise, reflects on the edge element's methods to find one annotated
     *       with {@link SmartLabelSource}, invokes it, and returns its string value.</li>
     *   <li>If reflection fails or no annotated method is found, returns
     *       {@code edgeElement.toString()}.</li>
     * </ol>
     *
     * @param edgeElement the edge element for which to get the label
     * @return the label string for the edge element
     */
    protected final String getEdgeLabelFor(E edgeElement) {

        if(edgeElement == null) return "<NULL>";

        if(edgeLabelProvider != null) {
            return edgeLabelProvider.valueFor(edgeElement);
        }
        
        try {
            Class<?> clazz = edgeElement.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SmartLabelSource.class)) {
                    method.setAccessible(true);
                    Object value = method.invoke(edgeElement);
                    return value.toString();
                }
            }
        } catch (SecurityException | IllegalAccessException  | IllegalArgumentException |InvocationTargetException ex) {
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return edgeElement.toString();
    }

    /**
     * Returns the shape type string to use for the given vertex element.
     * <br/>
     * Priority order:
     * <ol>
     *   <li>If the vertex element is null, returns the default shape from configuration.</li>
     *   <li>If a vertexShapeTypeProvider is set, delegates to it.</li>
     *   <li>Otherwise, reflects on the vertex element's methods to find one annotated
     *       with {@link SmartShapeTypeSource}, invokes it, and returns its string value.</li>
     *   <li>If reflection fails or no annotated method is found, returns the default shape from configuration.</li>
     * </ol>
     *
     * @param vertexElement the vertex element for which to get the shape type
     * @return the shape type string for the vertex element
     */
    protected final String getVertexShapeTypeFor(V vertexElement) {

        if(vertexElement == null) return graphProperties.getVertexShape();

        if(vertexShapeTypeProvider != null) {
            return vertexShapeTypeProvider.valueFor(vertexElement);
        }

        try {
            Class<?> clazz = vertexElement.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SmartShapeTypeSource.class)) {
                    method.setAccessible(true);
                    Object value = method.invoke(vertexElement);
                    return value.toString();
                }
            }
        } catch (SecurityException | IllegalAccessException  | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return graphProperties.getVertexShape();
    }

    /**
     * Returns the radius size for the graphical representation of the given vertex element.
     * <br/>
     * Priority order:
     * <ol>
     *   <li>If the vertex element is null, returns the default radius from configuration.</li>
     *   <li>If a vertexRadiusProvider is set, delegates to it.</li>
     *   <li>Otherwise, reflects on the vertex element's methods to find one annotated
     *       with {@link SmartRadiusSource}, invokes it, parses its return value as a double, and returns it.</li>
     *   <li>If reflection fails or no annotated method is found, returns the default radius from configuration.</li>
     * </ol>
     *
     * @param vertexElement the vertex element for which to get the radius
     * @return the radius size to use for the vertex's graphical shape
     */
    protected final double getVertexShapeRadiusFor(V vertexElement) {

        if(vertexElement == null) return graphProperties.getVertexRadius();

        if(vertexRadiusProvider != null) {
            return vertexRadiusProvider.valueFor(vertexElement);
        }

        try {
            Class<?> clazz = vertexElement.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SmartRadiusSource.class)) {
                    method.setAccessible(true);
                    Object value = method.invoke(vertexElement);
                    return Double.parseDouble(value.toString());
                }
            }
        } catch (SecurityException | IllegalAccessException  | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return graphProperties.getVertexRadius();
    }

    /**
     * Calculates and returns the bounding box that tightly contains all displayed vertex nodes.
     * <br/>
     * The bounding box is computed based on the minimum and maximum center coordinates
     * of all vertex nodes currently present.
     * If no vertices are present, the bounding box defaults to cover the entire panel area.
     *
     * @return a {@link Bounds} object representing the rectangular bounding area covering all displayed vertices,
     *         or the entire panel bounds if no vertices exist.
     */
    private Bounds getDisplayedVerticesBoundingBox() {

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE,
                maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        
        if(vertexNodes.size() == 0) return new BoundingBox(0, 0, getWidth(), getHeight());
        
        for (SmartGraphVertexNode<V> v : vertexNodes.values()) {
            minX = Math.min(minX, v.getCenterX());
            minY = Math.min(minY, v.getCenterY());
            maxX = Math.max(maxX, v.getCenterX());
            maxY = Math.max(maxY, v.getCenterY());
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Computes the forces to be applied on each vertex node based on the current automatic layout strategy.
     * The layout strategy determines vertex positions by simulating forces such as attraction and repulsion.
     */
    private void computeForces() {
        // Delegate to current layout strategy
        automaticLayoutStrategy.computeForces(vertexNodes.values(), getWidth(), getHeight());
    }

    /**
     * Updates the delta (change in position) values for all vertex nodes after forces have been computed.
     * This prepares vertex nodes for movement in the layout.
     */
    private void updateForces() {
        vertexNodes.values().forEach((v) -> v.updateDelta());
    }

    /**
     * Applies the computed forces to move all vertex nodes accordingly in the layout.
     */
    private void applyForces() {
        vertexNodes.values().forEach((v) -> v.moveFromForces());
    }

    /**
     * Resets all force values on the vertex nodes to prepare for the next computation cycle.
     */
    private void resetForces() {
        vertexNodes.values().forEach((v) -> v.resetForces());
    }

    /**
     * Counts the total number of edges between two given vertices in the graph model,
     * considering edges in both directions if the graph is undirected.
     *
     * @param v the first vertex
     * @param u the second vertex
     * @return the count of edges connecting the two vertices
     */
    private int getTotalEdgesBetweenInModel(Vertex<V> v, Vertex<V> u) {
        int count = 0;
        for (Edge<E, V> edge : theGraph.edges()) {
            if (edge.vertices()[0] == v && edge.vertices()[1] == u
                    || edge.vertices()[0] == u && edge.vertices()[1] == v) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the edges between 'v' and 'u', by "age", starting with the "oldest".
     * This is achieved because we use a LinkedHashMap for the 'connections' collection.
     * The order of 'u' and 'v' is irrelevant.
     *
     * @param v first vertex
     * @param u second vertex
     * @return an ordered (by age) list of edges that exist between 'u' and 'v'
     */
    private List<SmartGraphEdgeNode<E, V>> getEdgesBetween(SmartGraphVertexNode<V> v, SmartGraphVertexNode<V> u) {
        Vertex<V> V = v.getUnderlyingVertex();
        Vertex<V> U = u.getUnderlyingVertex();

        List<SmartGraphEdgeNode<E, V>> parallelEdges = new ArrayList<>();

        for (Map.Entry<Edge<E, V>, Tuple<Vertex<V>>> edgeTupleEntry : this.connections.entrySet()) {
            Edge<E, V> edge = edgeTupleEntry.getKey();
            Tuple<Vertex<V>> tuple = edgeTupleEntry.getValue();

            if ((tuple.first == V && tuple.second == U) || (tuple.first == U && tuple.second == V)) {
                parallelEdges.add( edgeNodes.get(edge) );
            }
        }

        return parallelEdges;
    }

    /**
     * Finds the maximum multiplicity index for current edges between two vertices 'v' and 'u'.
     * The order of 'v' and 'u' is irrelevant.
     * @param v first vertex
     * @param u second vertex
     * @return the maximum multiplicity index for current edges between two vertices 'v' and 'u'
     */
    private int getMaxMultiplicityIndexBetween(SmartGraphVertexNode<V> v, SmartGraphVertexNode<V> u) {
        int max = -1;
        for (SmartGraphEdgeNode<E, V> edge : edgeNodes.values()) {
            SmartGraphVertexNode<V> in = (SmartGraphVertexNode<V>)edge.getInbound();
            SmartGraphVertexNode<V> out = (SmartGraphVertexNode<V>)edge.getOutbound();

            if(in == v && out == u || in == u && out == v) {
                int cur = edge.getMultiplicityIndex();
                max = Math.max(cur, max);
            }
        }

        return max;
    }

    /**
     * Returns a list of all edges in the graph.
     * @return a LinkedList containing all edges
     */
    private List<Edge<E, V>> listOfEdges() {
        return new LinkedList<>(theGraph.edges());
    }

    /**
     * Returns a list of all vertices in the graph.
     * @return a LinkedList containing all vertices
     */private List<Vertex<V>> listOfVertices() {
        return new LinkedList<>(theGraph.vertices());
    }

    /**
     * Computes the vertex collection of the underlying graph that are not
     * currently being displayed.
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
     * Computes the collection for vertices that are currently being displayed but do
     * no longer exist in the underlying graph.
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
     * Computes the collection for edges that are currently being displayed but do
     * no longer exist in the underlying graph.
     *
     * @return collection of edges
     */
    private Collection<Edge<E, V>> removedEdges() {
        List<Edge<E, V>> removed = new LinkedList<>();

        Collection<Edge<E, V>> graphEdges = theGraph.edges();
        Collection<SmartGraphEdgeNode<E,V>> plotted = edgeNodes.values();

        for (SmartGraphEdgeNode<E,V> e : plotted) {
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
     * <br/>
     * The positioning should be inside the boundaries of the panel, but
     * no restrictions are enforced by this method, so be aware. 
     * 
     * @param v underlying vertex
     * @param x x-coordinate on panel
     * @param y y-coordinate on panel
     */
    public void setVertexPosition(Vertex<V> v, double x, double y) {
        SmartGraphVertexNode<V> node = vertexNodes.get(v);
        if(node != null) {
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
        if(node != null) {
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
        if(node != null) {
            return node.getPositionCenterY();
        }
        return Double.NaN;
    }

    /**
     * Checks whether two vertex nodes are adjacent (i.e., directly connected by an edge).
     *
     * @param v the first vertex node
     * @param u the second vertex node
     * @return true if the vertices are adjacent; false otherwise
     */
    private boolean areAdjacent(SmartGraphVertexNode<V> v, SmartGraphVertexNode<V> u) {
        return v.isAdjacentTo(u);
    }

    /**
     * Sets the action that should be performed when a vertex is double-clicked.
     *
     * @param action action to be performed
     */
    public void setVertexDoubleClickAction(Consumer<SmartGraphVertex<V>> action) {
        this.vertexClickConsumer = action;
    }

    /**
     * Sets the action that should be performed when an edge is double-clicked.
     *
     * @param action action to be performed
     */
    public void setEdgeDoubleClickAction(Consumer<SmartGraphEdge<E, V>> action) {
        this.edgeClickConsumer = action;
    }

    /**
     * Sets the vertex label provider for this SmartGraphPanel.
     * <br/>
     * The label provider has priority over any other method of obtaining the same values, such as annotations.
     * <br/>
     * To remove the provider, call this method with a <code>null</code> argument.
     *
     * @param labelProvider the label provider to set
     */
    public void setVertexLabelProvider(SmartLabelProvider<V> labelProvider) {
        this.vertexLabelProvider = labelProvider;
    }

    /**
     * Sets the edge label provider for this SmartGraphPanel.
     * <br/>
     * The label provider has priority over any other method of obtaining the same values, such as annotations.
     * <br/>
     * To remove the provider, call this method with a <code>null</code> argument.
     *
     * @param labelProvider the label provider to set
     */
    public void setEdgeLabelProvider(SmartLabelProvider<E> labelProvider) {
        this.edgeLabelProvider = labelProvider;
    }

    /**
     * Sets the radius provider for this SmartGraphPanel.
     * <br/>
     * The radius provider has priority over any other method of obtaining the same values, such as annotations.
     * <br/>
     * To remove the provider, call this method with a <code>null</code> argument.
     *
     * @param vertexRadiusProvider the radius provider to set
     */
    public void setVertexRadiusProvider(SmartRadiusProvider<V> vertexRadiusProvider) {
        this.vertexRadiusProvider = vertexRadiusProvider;
    }

    /**
     * Sets the shape type provider for this SmartGraphPanel.
     * <br/>
     * The shape type provider has priority over any other method of obtaining the same values, such as annotations.
     * <br/>
     * To remove the provider, call this method with a <code>null</code> argument.
     *
     * @param vertexShapeTypeProvider the shape type provider to set
     */
    public void setVertexShapeTypeProvider(SmartShapeTypeProvider<V> vertexShapeTypeProvider) {
        this.vertexShapeTypeProvider = vertexShapeTypeProvider;
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
     * Returns the associated stylable element with a graph vertex.
     *
     * @param v underlying vertex
     * @return stylable element (label)
     */
    public SmartStylableNode getStylableLabel(Vertex<V> v) {
        SmartGraphVertexNode<V> vertex = vertexNodes.get(v);
        
        return vertex != null ? vertex.getStylableLabel() : null;
    }
    
    /**
     * Returns the associated stylable element with a graph edge.
     *
     * @param e underlying graph edge
     * @return stylable element (label)
     */
    public SmartStylableNode getStylableLabel(Edge<E,V> e) {
        SmartGraphEdgeNode<E,V> edge = edgeNodes.get(e);
        
        return edge != null ? edge.getStylableLabel() : null;
    }
   

    /**
     * Loads the stylesheet and applies the .graph class to this panel.
     */
    private void loadAndApplyStylesheet(URI cssFile) {
        try {
            String css = cssFile.toURL().toExternalForm();
            getStylesheets().add(css);
            this.getStyleClass().add("graph");
        } catch (MalformedURLException ex) {
            String msg = String.format("Error loading stylesheet from URI = %s", cssFile);
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, msg, ex);
        }
    }

    /**
     * Enables the double click action on this pane.
     * <br/>
     * This method identifies the node that was clicked and, if any, calls the
     * appropriate consumer, i.e., vertex or edge consumers.
     */
    @SuppressWarnings("unchecked")
    private void enableDoubleClickListener() {
        setOnMouseClicked((MouseEvent mouseEvent) -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {

                    Node node = pick(SmartGraphPanel.this, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    if (node == null) {
                        return;
                    }

                    if (node instanceof SmartGraphVertex) {
                        SmartGraphVertex<V> v = (SmartGraphVertex<V>) node;
                        if(vertexClickConsumer != null) { // Only if the consumer is set
                            vertexClickConsumer.accept(v);
                        }
                    } else if (node instanceof SmartGraphEdge) {
                        SmartGraphEdge<E,V> e = (SmartGraphEdge<E,V>) node;
                        if(edgeClickConsumer != null) { // Only if the consumer is set
                            edgeClickConsumer.accept(e);
                        }
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
    private static class Tuple<T> {

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
            return Objects.equals(this.second, other.second);
        }
    }

}
