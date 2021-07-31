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

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.Pane;
import javafx.geometry.Point2D;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import static com.brunomnsilva.smartgraph.graphview.UtilitiesPoint2D.attractiveForce;
import static com.brunomnsilva.smartgraph.graphview.UtilitiesPoint2D.repellingForce;
import java.net.URI;

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
public class SmartForceDirectedGraphView<V, E> extends SmartGraphView {

    /*
    AUTOMATIC LAYOUT RELATED ATTRIBUTES
     */
    private AnimationTimer timer;
    private double repulsionForce;
    private final double repulsionScale;
    private double attractionForce;
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
    public SmartForceDirectedGraphView(Graph<V, E> theGraph) {
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
    public SmartForceDirectedGraphView(Graph<V, E> theGraph, SmartGraphProperties properties) {
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
    public SmartForceDirectedGraphView(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy) {
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
    public SmartForceDirectedGraphView(Graph<V, E> theGraph, SmartGraphProperties properties,
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
    public SmartForceDirectedGraphView(Graph<V, E> theGraph, SmartGraphProperties properties,
            SmartPlacementStrategy placementStrategy, URI cssFile) {

        super(theGraph, properties, placementStrategy, cssFile);

        this.repulsionForce = this.graphProperties.getRepulsionForce();
        this.repulsionScale = this.graphProperties.getRepulsionScale();
        this.attractionForce = this.graphProperties.getAttractionForce();
        this.attractionScale = this.graphProperties.getAttractionScale();

        //automatic layout initializations        
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                runLayoutIteration();
            }
        };

        this.automaticLayoutProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.setupForces();
                this.timer.start();
            } else {
                this.timer.stop();
            }
        });
    }

    private void setupForces() {
        // make repulsion force and attraction force proportional to average vertex radius
        double radius = 0;
        for (Object v : this.vertexNodes.values()) {
            double nodeRadius = ((SmartGraphVertexNode)v).getRadius();
            radius = radius < nodeRadius ? nodeRadius : radius;
        }
        //radius /= this.vertexNodes.size();
        double rForce = 75 * Math.pow((int) radius, 2) * this.repulsionScale;
        this.repulsionForce = rForce > this.repulsionForce ? rForce : this.repulsionForce;
        double aForce = radius * 2 / 3;
        this.attractionForce = aForce > this.attractionForce ? aForce : this.attractionForce;

        System.out.println("Biggest radius: " + radius);
        System.out.println("Repulsion force: " + this.repulsionForce);
        System.out.println("Attraction force: " + this.attractionForce);
    }

    private synchronized void runLayoutIteration() {
        for (int i = 0; i < 25; i++) {
            this.resetForces();
            this.computeForces();
            this.updateForces();
        }
        this.applyForces();
    }

    @Override
    protected void onInitialize() {
        //apply random placement
        new SmartRandomNearCenterPlacementStrategy().place(this.widthProperty().doubleValue(),
                this.heightProperty().doubleValue(),
                this.theGraph,
                this.vertexNodes.values());

        //start automatic layout
        this.setupForces();
        this.timer.start();
    }

    @Override
    protected SmartGraphVertexNode getSmartGraphVertexNode(Vertex vertex) {
        return new SmartGraphForceDirectedVertexNode(vertex, this.graphProperties.getVertexAllowUserMove());
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

    /*
    * AUTOMATIC LAYOUT 
     */
    private void computeForces() {
        for (Object item1 : vertexNodes.values()) {
            SmartGraphForceDirectedVertexNode v = (SmartGraphForceDirectedVertexNode)item1;
            for (Object item2 : vertexNodes.values()) {
                SmartGraphForceDirectedVertexNode other = (SmartGraphForceDirectedVertexNode)item2;
                if (v == other) {
                    continue; //NOP
                }

                //double k = Math.sqrt(getWidth() * getHeight() / graphVertexMap.size());
                Point2D repellingForce = repellingForce(
                        v.getUpdatedPosition(),
                        other.getUpdatedPosition(), this.repulsionForce);

                double deltaForceX = 0, deltaForceY = 0;

                //compute attractive and reppeling forces
                //opt to use internal areAdjacent check, because a vertex can be removed from
                //the underlying graph before we have the chance to remove it from our
                //internal data structure
                if (areAdjacent(v, other)) {

                    Point2D attractiveForce = attractiveForce(
                            v.getUpdatedPosition(),
                            other.getUpdatedPosition(),
                            vertexNodes.size(),
                            this.attractionForce,
                            this.attractionScale);

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
            ((SmartGraphForceDirectedVertexNode)v).updateDelta();
        });
    }

    private void applyForces() {
        vertexNodes.values().forEach((v) -> {
            ((SmartGraphForceDirectedVertexNode)v).moveFromForces();
        });
    }

    private void resetForces() {
        vertexNodes.values().forEach((v) -> {
            ((SmartGraphForceDirectedVertexNode)v).resetForces();
        });
    }
}
