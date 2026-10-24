/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2026  brunomnsilva@gmail.com
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

import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Describes a mouse click on a graph vertex, graph edge or on the empty canvas.
 * <p>
 * A click event is delivered to the handlers registered through
 * {@link SmartGraphPanel#setVertexClickHandler(java.util.function.Consumer)},
 * {@link SmartGraphPanel#setEdgeClickHandler(java.util.function.Consumer)} and
 * {@link SmartGraphPanel#setCanvasClickHandler(java.util.function.Consumer)}.
 * <p>
 * Handlers receive the raw JavaFX click events. A double-click therefore produces a
 * click-count-1 event followed by a click-count-2 event; the library does not merge them. Use
 * {@link #isDoubleClick()} to ignore the first event when appropriate.
 * <p>
 * A click on a vertex label is reported as a click on the owning vertex. Likewise, a click on an
 * edge label or arrow is reported as a click on the owning edge.
 * <p>
 * Instances of this class are immutable and are created by {@link SmartGraphPanel}.
 *
 * @param <V> type of the vertex elements
 * @param <E> type of the edge elements
 *
 * @see SmartGraphPanel#setVertexClickHandler(java.util.function.Consumer)
 * @see SmartGraphPanel#setEdgeClickHandler(java.util.function.Consumer)
 * @see SmartGraphPanel#setCanvasClickHandler(java.util.function.Consumer)
 *
 * @author brunomnsilva
 */
public final class SmartGraphClickEvent<V, E> {

    private final MouseEvent mouseEvent;
    private final SmartGraphVertex<V> vertex;
    private final SmartGraphEdge<E, V> edge;
    private final Point2D point;

    /**
     * Creates a new click event.
     *
     * @param mouseEvent the original JavaFX mouse event
     * @param vertex     the clicked vertex, or {@code null}
     * @param edge       the clicked edge, or {@code null}
     * @param point      the click position in panel coordinates
     */
    SmartGraphClickEvent(MouseEvent mouseEvent, SmartGraphVertex<V> vertex,
                         SmartGraphEdge<E, V> edge, Point2D point) {
        this.mouseEvent = mouseEvent;
        this.vertex = vertex;
        this.edge = edge;
        this.point = point;
    }

    /**
     * Returns the original JavaFX mouse event.
     *
     * @return the original mouse event
     */
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    /**
     * Returns the clicked vertex.
     *
     * @return the clicked vertex, or {@code null} if this event targets an edge or the canvas
     */
    public SmartGraphVertex<V> getVertex() {
        return vertex;
    }

    /**
     * Returns the clicked edge.
     *
     * @return the clicked edge, or {@code null} if this event targets a vertex or the canvas
     */
    public SmartGraphEdge<E, V> getEdge() {
        return edge;
    }

    /**
     * Returns the click position in the panel's local coordinate space.
     * <p>
     * The coordinates are independent of the panel's current zoom and scroll position, and are
     * expressed in the same coordinate system used by
     * {@link SmartGraphPanel#getVertexPositionX(com.brunomnsilva.smartgraph.graph.Vertex)} and
     * {@link SmartGraphPanel#getVertexPositionY(com.brunomnsilva.smartgraph.graph.Vertex)}.
     *
     * @return the click position in panel coordinates
     */
    public Point2D getPoint() {
        return point;
    }

    /**
     * Checks whether this event targets a vertex.
     *
     * @return true if a vertex was clicked
     */
    public boolean isVertex() {
        return vertex != null;
    }

    /**
     * Checks whether this event targets an edge.
     *
     * @return true if an edge was clicked
     */
    public boolean isEdge() {
        return edge != null;
    }

    /**
     * Checks whether this event targets the empty canvas.
     *
     * @return true if neither a vertex nor an edge was clicked
     */
    public boolean isCanvas() {
        return vertex == null && edge == null;
    }

    /**
     * Returns the number of clicks reported by the original event.
     *
     * @return the click count
     */
    public int getClickCount() {
        return mouseEvent.getClickCount();
    }

    /**
     * Returns the mouse button that generated this event.
     *
     * @return the mouse button
     */
    public MouseButton getButton() {
        return mouseEvent.getButton();
    }

    /**
     * Checks whether this event was generated by the primary mouse button.
     *
     * @return true for a primary-button click
     */
    public boolean isPrimaryButton() {
        return mouseEvent.getButton() == MouseButton.PRIMARY;
    }

    /**
     * Checks whether this event was generated by the secondary mouse button.
     *
     * @return true for a secondary-button click
     */
    public boolean isSecondaryButton() {
        return mouseEvent.getButton() == MouseButton.SECONDARY;
    }

    /**
     * Checks whether this event was generated by the middle mouse button.
     *
     * @return true for a middle-button click
     */
    public boolean isMiddleButton() {
        return mouseEvent.getButton() == MouseButton.MIDDLE;
    }

    /**
     * Checks whether this is a single-click, regardless of the mouse button.
     * <p>
     * A double-click is reported by JavaFX as two events: the first has a click count of 1 and
     * the second has a click count of 2. Use {@link #getButton()} or the button-specific helpers
     * to distinguish the button.
     *
     * @return true for a click with a click count of 1
     */
    public boolean isSingleClick() {
        return getClickCount() == 1;
    }

    /**
     * Checks whether this is a double-click, regardless of the mouse button.
     * <p>
     * A triple-click's third event is not considered a double-click. A right-button double-click
     * is therefore both {@link #isDoubleClick()} and {@link #isRightClick()}.
     *
     * @return true for a click with a click count of 2
     */
    public boolean isDoubleClick() {
        return getClickCount() == 2;
    }

    /**
     * Checks whether this is a click with the secondary mouse button.
     *
     * @return true for a secondary-button click, regardless of its click count
     */
    public boolean isRightClick() {
        return isSecondaryButton();
    }

    /**
     * Checks whether the mouse has remained in the same position since it was pressed.
     * <p>
     * Clicks generated after a drag or pan are filtered out by {@link SmartGraphPanel}; this
     * method exposes the original JavaFX information for completeness.
     *
     * @return true if the mouse did not move between press and release
     */
    public boolean isStillSincePress() {
        return mouseEvent.isStillSincePress();
    }

    @Override
    public String toString() {
        String target;
        if (isCanvas()) {
            target = "canvas";
        } else if (isVertex()) {
            target = "vertex=" + String.valueOf(vertex.getUnderlyingVertex().element());
        } else {
            target = "edge=" + String.valueOf(edge.getUnderlyingEdge().element());
        }

        return "SmartGraphClickEvent{target=" + target
                + ", button=" + getButton()
                + ", clickCount=" + getClickCount()
                + ", point=" + point + "}";
    }
}
