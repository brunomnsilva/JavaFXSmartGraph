## Changelog

- (2.5.0) Interaction events:
    - New event-based click API for vertices, edges and the canvas:
      `setVertexClickHandler`, `setEdgeClickHandler` and `setCanvasClickHandler`.
    - `SmartGraphClickEvent` exposes the clicked element, mouse button, click count, modifiers
      and the click position in panel coordinates.
    - Clicks on vertex labels, edge labels and arrows are routed to their owning graph element.
    - Hover effects now propagate in both directions between graph elements and their labels/arrows.
    - Panels initialize automatically once attached to a scene with a non-zero size; `init()` is
      optional and idempotent.
    - Performance improvements in automatic layout and updates: unordered allocation-free force
      calculations, coalesced updates, cached annotation lookups and set-based removal detection.
    - Deprecated `setVertexDoubleClickAction` and `setEdgeDoubleClickAction`; use
      `setVertexClickHandler` / `setEdgeClickHandler` and `SmartGraphClickEvent#isDoubleClick()`.
      These deprecated methods are scheduled for removal in 3.0.0.

- (2.4.0) Notable changes:
    - Fix a latent scroll bug in `ContentZoomScrollPane` under certain scroll events sequences.
    - ⚠️ JavaFX is now an `optional` dependency (provided by the application): the consuming application must declare the JavaFX modules it uses, so smartgraph no longer imposes a JavaFX version;
      - Minimum runtime is now documented as JDK 17 (JavaFX 21 requires it); the library is compiled against JavaFX 21 (LTS);
      - Build aligned with the actual requirements: `maven.compiler.release` instead of `source`/`target`, centralized `javafx.version` property, and an enforcer rule for JDK 17+.

- (2.3.0) Notable changes:
    
    - 👍 Edges only span the distance between vertex boundaries (instead of centers). This yields a nicer visualization when vertices are made transparent.

- (2.2.0) Notable changes:

    - ⚠️ Bump minimum JDK to 11 and define library as a Java Module;
    - 👍 Robust algorithm for vertex spawning;
    - Propagation of hover events to labels and arrows (can be styled);
    - 🎉 Improve edge readability (issue #43) by changing the label underlying representation to a StackPane. Labels now accept CSS styles for Pane (background) and Text (the label itself).
    - ⚠️ Fixed a regression that affected the movement of vertices.
    - Other small fixes and improvements.
  
- (2.1.0) Notable changes:
  
    - Improved edge and label rendering;
        - Fix arrow "z-order" placement in some situations.
        - Fix arrow misalignment on self-loops (issue \#40).
        - Fix parallel edge spacing (issue \#40).      
        - Parallel edge spacing, including self-loops, is kept as compact as possible when inserting/removing, without loosing visual reference.
    
    - Fix visibility of methods necessary to create custom placement strategies.
  
    - Improved `ContentZoomScrollPane` with clipping of any overflow.

      - Bring vertex and label to front, while dragging.
    
- (2.0.0) 🎉 Minor fixes and stable version release.

- (2.0.0-rc2) Several minor improvements, including:

    - Example on how to use a background image for a vertex, see issue \#34.

    - Styles applied to edges are propagated to their respective arrows, see issue \#31.

- (2.0.0-rc1) Shapes, sizes, providers, annotations and minor improvements:

    - Different shapes can be used to represent vertices, namely circles, stars and regular polygons (from triangles to dodecagons);
        - The default shape can be specified with the `vertex.shape` property in `smartgraph.properties`
        - Can be set/changed at runtime through a `SmartShapeTypeProvider` or `SmartShapeTypeSource` annotation.

    - The radius of the shape (enclosing circle) used to represent a vertex can be set/changed at runtime through a `SmartRadiusProvider` or `SmartRadiusSource` annotation.

    - Updated shapes and radii are only reflected in the visualization after calling `SmartGraphPanel.update()` or `SmartGraphPanel.updateAndWait()`.

    - Improvements:
        - When dragging nodes, they will be kept within the panel's bounds.
        - The look of curved edges has been improved.

- (1.1.0) Automatic layout is now performed through an instantiated *strategy*. There are two available (but the pattern allows for the user to devise others):

    - `ForceDirectedSpringSystemLayoutStrategy`: this is the original implementation for the automatic placement, through a spring system;
    - `ForceDirectedSpringGravityLayoutStrategy`: (**new**) this is a variant of the spring system implementation, but with a gravity pull towards the center of the panel. This is now the default strategy and has the advantage of not repelling isolated vertices and/or bipartite graphs to the edges of the panel.

- (1.0.0) Package now available through [Maven Central](https://central.sonatype.com/namespace/com.brunomnsilva). The library seems stable, after dozens of college projects of my students have used it. Hence, the version was bumped to 1.0.0.

- (0.9.4) You can now annotate a method with `@SmartLabelSource` within a model class to provide the displayed label for a vertex/edge; see the example at `com.brunomnsilva.smartgraph.examples.cities`. If no annotation is present, then the `toString()` method is used to obtain the label's text.

- (0.9.4) You can manually alter a vertex position on the panel at anytime, through `SmartGraphPanel.setVertexPosition(Vertex<V> v)`; see the example at `com.brunomnsilva.smartgraph.examples.cities`.

- (0.9.4) You can override specific default properties by using a *String* parameter to the `SmartGraphProperties` constructor; see the example at `com.brunomnsilva.smartgraph.examples.cities`. This is useful if you want to display visually different graphs within the same application.

- (0.9.4) You can now style labels and arrows individually.