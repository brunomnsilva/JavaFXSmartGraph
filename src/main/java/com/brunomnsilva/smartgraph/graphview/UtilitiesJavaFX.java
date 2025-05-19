/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2023-2024  brunomnsilva@gmail.com
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

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.List;

/**
 * Utility methods for JavaFX.
 * 
 * @author brunomnsilva
 */
public class UtilitiesJavaFX {
    /**
     * Determines the closest node that resides in the x,y scene position, if any.
     * <br>
     * Obtained from <a href="http://fxexperience.com/2016/01/node-picking-in-javafx/">here</a>
     * 
     * @param node      parent node
     * @param sceneX    x-coordinate of picking point
     * @param sceneY    y-coordinate of picking point
     * 
     * @return          topmost node containing (sceneX, sceneY) point
     */
    public static Node pick(Node node, double sceneX, double sceneY) {
        Point2D p = node.sceneToLocal(sceneX, sceneY, true /* rootScene */);

        // check if the given node has the point inside it, or else we drop out
        if (!node.contains(p)) {
            return null;
        }

        // at this point we know that _at least_ the given node is a valid
        // answer to the given point, so we will return that if we don't find
        // a better child option
        if (node instanceof Parent && !(node instanceof SmartGraphVertexNode)) {
            // we iterate through all children in reverse order, and stop when we find a match.
            // We do this as we know the elements at the end of the list have a higher
            // z-order, and are therefore the better match, compared to children that
            // might also intersect (but that would be underneath the element).
            Node bestMatchingChild = null;
            List<Node> children = ((Parent) node).getChildrenUnmodifiable();
            for (int i = children.size() - 1; i >= 0; i--) {
                Node child = children.get(i);
                p = child.sceneToLocal(sceneX, sceneY, true /* rootScene */);
                if (child.isVisible() && !child.isMouseTransparent() && child.contains(p)) {
                    bestMatchingChild = child;
                    break;
                }
            }

            if (bestMatchingChild != null) {
                return pick(bestMatchingChild, sceneX, sceneY);
            }
        }

        return node;
    }


    /**
     * Combines the bounds of the specified JavaFX nodes into a single bounding box.
     * This method calculates the minimum and maximum coordinates of all the nodes
     * and returns a bounding box that encompasses all of them.
     *
     * @param nodes the JavaFX nodes whose bounds need to be combined
     * @return the combined bounding box containing the bounds of all specified nodes,
     *         or {@code null} if the input array is empty or {@code null}
     */
    public static Bounds combineBounds(Node... nodes) {
        if (nodes == null || nodes.length == 0) {
            return null;
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        // Iterate through all nodes to find the combined bounds
        for (Node node : nodes) {
            Bounds nodeBounds = node.getLayoutBounds();
            minX = Math.min(minX, nodeBounds.getMinX());
            minY = Math.min(minY, nodeBounds.getMinY());
            maxX = Math.max(maxX, nodeBounds.getMaxX());
            maxY = Math.max(maxY, nodeBounds.getMaxY());
        }

        // Create a new Bounds object using the calculated minimum and maximum values
        return new BoundingBox(minX, minY, 0, maxX - minX, maxY - minY, 0);
    }

    /**
     * Computes the union of multiple {@link Bounds} objects, returning the smallest
     * {@link BoundingBox} that fully contains all given bounds.
     *
     * <p>If the input array is {@code null} or empty, this method returns {@code null}.</p>
     *
     * @param bounds an array of {@link Bounds} objects to be merged
     * @return a {@link BoundingBox} that represents the union of all input bounds,
     *         or {@code null} if the input is {@code null} or empty
     */
    public static Bounds union(Bounds... bounds) {
        if (bounds == null || bounds.length == 0) {
            return null;
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        // Iterate through all nodes to find the combined bounds
        for (Bounds box : bounds) {
            minX = Math.min(minX, box.getMinX());
            minY = Math.min(minY, box.getMinY());
            maxX = Math.max(maxX, box.getMaxX());
            maxY = Math.max(maxY, box.getMaxY());
        }

        // Create a new Bounds object using the calculated minimum and maximum values
        return new BoundingBox(minX, minY, 0, maxX - minX, maxY - minY, 0);
    }

    public static void triggerMouseEntered(Node node) {
        if(node == null) return;

        node.fireEvent(new MouseEvent(MouseEvent.MOUSE_ENTERED, 0, 0, 0, 0,
                MouseButton.NONE, 0, true, true, true, true,
                true, true, true, true, true,
                true, null));
    }

    public static void triggerMouseExited(Node node) {
        if(node == null) return;

        node.fireEvent(new MouseEvent(MouseEvent.MOUSE_EXITED, 0, 0, 0, 0,
                MouseButton.NONE, 0, true, true, true, true,
                true, true, true, true, true,
                true, null));
    }

}
