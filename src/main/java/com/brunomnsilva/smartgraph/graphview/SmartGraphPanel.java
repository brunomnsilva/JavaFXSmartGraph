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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

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
    CONFIGURATION PROPERTIES
     */
    private final SmartGraphProperties graphProperties;

    /*
    INTERNAL DATA STRUCTURE
     */
    private final Graph<V, E> theGraph;
    private final SmartPlacementStrategy placementStrategy;
    private final Map<Vertex<V>, SmartGraphVertexNode<V>> vertexNodes;
    private final Map<Edge<E, V>, SmartGraphEdgeBase<E, V>> edgeNodes;
    private final Map<Edge<E,V>, Tuple<Vertex<V>>> connections;
    private final Map<Tuple<SmartGraphVertexNode<V>>, Integer> placedEdges = new HashMap<>();
    private boolean initialized = false;
    private final boolean edgesWithArrows;
    
    /*
    INTERACTION WITH VERTICES AND EDGES
     */
    private Consumer<SmartGraphVertex<V>> vertexClickConsumer = null;
    private Consumer<SmartGraphEdge<E, V>> edgeClickConsumer = null;

    /*
    OPTIONAL PROVIDERS FOR LABELS, RADII AND SHAPE TYPES OF NODES.
    THESE HAVE PRIORITY OVER ANY MODEL ANNOTATIONS (E.G., SmartLabelSource)
     */
    private SmartLabelProvider<V> vertexLabelProvider;
    private SmartLabelProvider<E> edgeLabelProvider;
    private SmartRadiusProvider<V> vertexRadiusProvider;
    private SmartShapeTypeProvider<V> vertexShapeTypeProvider;

    /*
    AUTOMATIC LAYOUT RELATED ATTRIBUTES
     */

    /**
     * Property to toggle the automatic layout of nodes.
     */
    public final BooleanProperty automaticLayoutProperty;
    private final AnimationTimer timer;
    private ForceDirectedLayoutStrategy<V> automaticLayoutStrategy;
    //This value was obtained experimentally
    private static final int AUTOMATIC_LAYOUT_ITERATIONS = 20;

    /**
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using default properties, default circular
     * placement of vertices, default automatic spring gravity layout strategy
     * and styling from smartgraph.css.
     *
     * @param theGraph underlying graph
     *
     * @see Graph
     */
    public SmartGraphPanel(Graph<V, E> theGraph) {
        this(theGraph,
            new SmartGraphProperties(),
            new SmartCircularSortedPlacementStrategy(),
            null,
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
     * @see Graph
     */
    public SmartGraphPanel(Graph<V, E> theGraph, ForceDirectedLayoutStrategy<V> layoutStrategy) {
        this(theGraph,
                new SmartGraphProperties(),
                new SmartCircularSortedPlacementStrategy(),
                null,
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
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties) {
        this(theGraph,
            properties,
            new SmartCircularSortedPlacementStrategy(),
            null,
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
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy,
                           ForceDirectedLayoutStrategy<V> layoutStrategy) {
        this(theGraph,
            new SmartGraphProperties(),
            placementStrategy,
            null,
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
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy) {
        this(theGraph,
                new SmartGraphProperties(),
                placementStrategy,
                null,
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
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy) {

        this(theGraph,
            properties,
            placementStrategy,
            null,
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
     * Constructs a visualization of the graph referenced by
     * <code>theGraph</code>, using custom parameters.
     *
     * @param theGraph underlying graph
     * @param properties custom properties, null for default
     * @param placementStrategy placement strategy, null for default
     * @param cssFile alternative css file, instead of default 'smartgraph.css'
     * @param layoutStrategy  the automatic layout strategy to use
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties,
                           SmartPlacementStrategy placementStrategy, URI cssFile,
                           ForceDirectedLayoutStrategy<V> layoutStrategy) {

        Args.requireNotNull(theGraph, "theGraph");
        Args.requireNotNull(properties, "properties");
        Args.requireNotNull(placementStrategy, "placementStrategy");

        this.theGraph = theGraph;
        this.graphProperties = properties;
        this.placementStrategy = placementStrategy;

        this.edgesWithArrows = this.graphProperties.getUseEdgeArrow();

        this.automaticLayoutStrategy = layoutStrategy;

        this.vertexNodes = new HashMap<>();
        this.edgeNodes = new HashMap<>();
        this.connections = new HashMap<>();

        //set stylesheet and class
        loadStylesheet(cssFile);

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
            //apply random placement
            new SmartRandomPlacementStrategy().place(this.widthProperty().doubleValue(),
                    this.heightProperty().doubleValue(),
                    this);

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
     * Changes the current automatic layout strategy.
     * @param strategy the new strategy to use
     */
    public void setAutomaticLayoutStrategy(ForceDirectedLayoutStrategy<V> strategy) {
        Args.requireNotNull(strategy, "strategy");

        this.automaticLayoutStrategy = strategy;
    }

    /**
     * Returns the reference of underlying model depicted by this panel.
     * <br/> The concrete type of the returned instance may be a {@link Graph} or {@link Digraph}.
     *
     * @return the underlying model
     */
    public Graph<V, E> getModel() {
        return theGraph;
    }

    /**
     * Returns a collection of the smart vertices that represent the underlying model vertices.
     * @return a collection of the smart vertices
     */
    protected final Collection<SmartGraphVertex<V>> getSmartVertices() {
        return new ArrayList<>(this.vertexNodes.values());
    }

    /**
     * Returns a collection of the smart edges that represent the underlying model edges.
     * @return a collection of the smart edges
     */
    protected final Collection<SmartGraphEdge<E, V>> getSmartEdges() {
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

    private synchronized void updateViewModel() {
        removeNodes();
        insertNodes();
        updateNodes();
    }

    /*
    INTERACTION WITH VERTICES AND EDGES
     */
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

    /* PROVIDERS */

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

    /*
        NODES CREATION/UPDATES
         */
    private void initNodes() {

        /* create vertex graphical representations */
        for (Vertex<V> vertex : listOfVertices()) {

            SmartGraphVertexNode<V> vertexAnchor = createVertex(vertex, 0, 0);

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

                SmartGraphEdgeBase<E,V> graphEdge = createEdge(edge, graphVertexIn, graphVertexOppositeOut);

                /* Track Edges already placed */
                connections.put(edge, new Tuple<>(vertex, oppositeVertex));
                addEdge(graphEdge, edge);

                if (this.edgesWithArrows && theGraph instanceof Digraph) {
                    SmartArrow arrow = new SmartArrow(this.graphProperties.getEdgeArrowSize());
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

    private SmartGraphVertexNode<V> createVertex(Vertex<V> v, double x, double y) {
        // Read shape type from annotation or use default (circle)
        String shapeType = getVertexShapeTypeFor(v.element());

        // Read shape radius from annotation or use default
        double shapeRadius = getVertexShapeRadiusFor(v.element());

        return new SmartGraphVertexNode<>(v, x, y, shapeRadius, shapeType, graphProperties.getVertexAllowUserMove());
    }

    private SmartGraphEdgeBase<E,V> createEdge(Edge<E, V> edge, SmartGraphVertexNode<V> graphVertexInbound, SmartGraphVertexNode<V> graphVertexOutbound) {
        /*
        Even if edges are later removed, the corresponding index remains the same. Otherwise, we would have to
        regenerate the appropriate edges.
         */
        int edgeIndex = 0;
        Integer counter = placedEdges.get(new Tuple<>(graphVertexInbound, graphVertexOutbound));
        if (counter != null) {
            edgeIndex = counter;
        }

        SmartGraphEdgeBase<E,V> graphEdge;

        if (getTotalEdgesBetween(graphVertexInbound.getUnderlyingVertex(), graphVertexOutbound.getUnderlyingVertex()) > 1
                || graphVertexInbound == graphVertexOutbound) {
            graphEdge = new SmartGraphEdgeCurve<>(edge, graphVertexInbound, graphVertexOutbound, edgeIndex);
        } else {
            graphEdge = new SmartGraphEdgeLine<>(edge, graphVertexInbound, graphVertexOutbound);
        }

        placedEdges.put(new Tuple<>(graphVertexInbound, graphVertexOutbound), ++edgeIndex);

        return graphEdge;
    }

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

    private void addEdge(SmartGraphEdgeBase<E,V> e, Edge<E, V> edge) {
        //edges to the back
        this.getChildren().add(0, (Node) e);
        edgeNodes.put(edge, e);

        String labelText = getEdgeLabelFor(edge.element());
        
        if (graphProperties.getUseEdgeTooltip()) {
            Tooltip t = new Tooltip(labelText);
            Tooltip.install((Node) e, t);
        }

        if (graphProperties.getUseEdgeLabel()) {
            SmartLabel label = new SmartLabel(labelText);

            label.addStyleClass("edge-label");
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
                    
                    if(existing == null) {
                        /* 
                        Updates may be coming too fast, and we can get out of sync.
                        The opposite vertex exists in the (di)graph, but we have not yet
                        created it for the panel. Therefore, its position is unknown,
                        so place the vertex representation in the middle.
                        */                        
                        x = mx;
                        y = my;
                    } else {
                        Point2D p = UtilitiesPoint2D.rotate(existing.getPosition().add(50.0, 50.0),
                                existing.getPosition(), Math.random() * 360);

                        x = clamp(p.getX(), 0, getWidth());
                        y = clamp(p.getY(), 0, getHeight());
                    }
                }

                SmartGraphVertexNode<V> newVertex = createVertex(vertex, x, y);

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

                SmartGraphEdgeBase<E,V> graphEdge = createEdge(edge, graphVertexIn, graphVertexOut);

                if (this.edgesWithArrows && theGraph instanceof Digraph) {
                    SmartArrow arrow = new SmartArrow(this.graphProperties.getEdgeArrowSize());
                    graphEdge.attachArrow(arrow);
                    this.getChildren().add(arrow);
                }

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

    private void removeNodes() {
         //remove edges (graphical elements) that were removed from the underlying graph
        Collection<Edge<E, V>> removedEdges = removedEdges();
        for (Edge<E, V> e : removedEdges) {
            SmartGraphEdgeBase<E,V> edgeToRemove = edgeNodes.get(e);
            edgeNodes.remove(e);
            removeEdge(edgeToRemove);   //remove from panel

            //when edges are removed, the adjacency between vertices changes
            //the adjacency is kept in parallel in an internal data structure
            Tuple<Vertex<V>> vertexTuple = connections.get(e);

            if( getTotalEdgesBetween(vertexTuple.first, vertexTuple.second) == 0 ) {
                SmartGraphVertexNode<V> v0 = vertexNodes.get(vertexTuple.first);
                SmartGraphVertexNode<V> v1 = vertexNodes.get(vertexTuple.second);

                v0.removeAdjacentVertex(v1);
                v1.removeAdjacentVertex(v0);
            }

            connections.remove(e);
        }

        //remove vertices (graphical elements) that were removed from the underlying graph
        Collection<Vertex<V>> removedVertices = removedVertices();
        for (Vertex<V> removedVertex : removedVertices) {
            SmartGraphVertexNode<V> removed = vertexNodes.remove(removedVertex);
            removeVertex(removed);
        }
                
    }

    private void removeEdge(SmartGraphEdgeBase<E,V> e) {
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

    private void removeVertex(SmartGraphVertexNode<V> v) {
        getChildren().remove(v);

        Text attachedLabel = v.getAttachedLabel();
        if (attachedLabel != null) {
            getChildren().remove(attachedLabel);
        }
    }

    /**
     * Updates node's labels
     */
    private void updateNodes() {
        theGraph.vertices().forEach((v) -> {
            SmartGraphVertexNode<V> vertexNode = vertexNodes.get(v);
            if (vertexNode != null) {
                SmartLabel label = vertexNode.getAttachedLabel();
                if(label != null) {
                    String text = getVertexLabelFor(v.element());
                    label.setText_( text );
                }

                double radius = getVertexShapeRadiusFor(v.element());
                vertexNode.setRadius(radius);

                String shapeType = getVertexShapeTypeFor(v.element());
                vertexNode.setShapeType(shapeType);
            }

        });
        
        theGraph.edges().forEach((e) -> {
            SmartGraphEdgeBase<E,V> edgeNode = edgeNodes.get(e);
            if (edgeNode != null) {
                SmartLabel label = edgeNode.getAttachedLabel();
                if (label != null) {
                    String text = getEdgeLabelFor(e.element());
                    label.setText_( text );
                }
            }
        });
    }
    
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
     * Computes the bounding box from all displayed vertices.
     *
     * @return bounding box
     */
    private Bounds getPlotBounds() {
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

    private void computeForces() {
        // Delegate to current layout strategy
        automaticLayoutStrategy.computeForces(vertexNodes.values(), getWidth(), getHeight());
    }

    private boolean areAdjacent(SmartGraphVertexNode<V> v, SmartGraphVertexNode<V> u) {
        return v.isAdjacentTo(u);
    }

    private void updateForces() {
        vertexNodes.values().forEach((v) -> v.updateDelta());
    }

    private void applyForces() {
        vertexNodes.values().forEach((v) -> v.moveFromForces());
    }

    private void resetForces() {
        vertexNodes.values().forEach((v) -> v.resetForces());
    }

    private int getTotalEdgesBetween(Vertex<V> v, Vertex<V> u) {
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
        return new LinkedList<>(theGraph.edges());
    }

    private List<Vertex<V>> listOfVertices() {
        return new LinkedList<>(theGraph.vertices());
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
        Collection<SmartGraphEdgeBase<E,V>> plotted = edgeNodes.values();

        for (SmartGraphEdgeBase<E,V> e : plotted) {
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
        SmartGraphEdgeBase<E,V> edge = edgeNodes.get(e);
        
        return edge != null ? edge.getStylableLabel() : null;
    }
   

    /**
     * Loads the stylesheet and applies the .graph class to this panel.
     */
    private void loadStylesheet(URI cssFile) {
        try {
            String css;
            if( cssFile != null ) {
                css = cssFile.toURL().toExternalForm();
            } else {
                File f = new File("smartgraph.css");
                css = f.toURI().toURL().toExternalForm();
            }

            getStylesheets().add(css);
            this.getStyleClass().add("graph");
        } catch (MalformedURLException ex) {
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
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
                    //no need to continue otherwise
                    if (vertexClickConsumer == null && edgeClickConsumer == null) {
                        return;
                    }

                    Node node = pick(SmartGraphPanel.this, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    if (node == null) {
                        return;
                    }

                    if (node instanceof SmartGraphVertex) {
                        SmartGraphVertex<V> v = (SmartGraphVertex<V>) node;
                        vertexClickConsumer.accept(v);
                    } else if (node instanceof SmartGraphEdge) {
                        SmartGraphEdge<E,V> e = (SmartGraphEdge<E,V>) node;
                        edgeClickConsumer.accept(e);
                    }
                }
            }
        });
    }

    private static double clamp(double value, double min, double max) {

        if (Double.compare(value, min) < 0) {
            return min;
        }

        if (Double.compare(value, max) > 0) {
            return max;
        }

        return value;
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
