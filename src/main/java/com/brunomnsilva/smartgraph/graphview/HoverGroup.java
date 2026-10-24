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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Coordinates the hover state of a graph element and its attachments, such as labels and arrows.
 * <p>
 * Hovering any member makes every member hovered, and leaving all members makes every member
 * un-hovered. Physical hover changes are tracked separately from the synthetic events used to
 * propagate hover, which prevents feedback loops and avoids clearing the hover state while the
 * pointer is still over another member.
 */
final class HoverGroup {

    private final Set<Node> members = new LinkedHashSet<>();
    private final Set<Node> physicallyHovered = new LinkedHashSet<>();
    private final Map<Node, ChangeListener<Boolean>> hoverListeners = new LinkedHashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> enterHandlers = new LinkedHashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> exitHandlers = new LinkedHashMap<>();

    private boolean active;
    private boolean propagating;
    private boolean deactivationScheduled;

    /**
     * Creates a hover group containing the given nodes.
     *
     * @param members the initial members
     */
    HoverGroup(Node... members) {
        for (Node member : members) {
            add(member);
        }
    }

    /**
     * Adds a node to this hover group.
     *
     * @param member the node to add
     */
    void add(Node member) {
        if (member == null || members.contains(member)) {
            return;
        }

        members.add(member);

        ChangeListener<Boolean> hoverListener = (observable, oldValue, newValue) ->
                onHoverChanged(member, newValue);
        hoverListeners.put(member, hoverListener);
        member.hoverProperty().addListener(hoverListener);

        // A member can already be hovered because of a synthetic hover propagated by the group.
        // In that case a real mouse-enter does not change hoverProperty, so it must be observed
        // through the mouse events as well.
        EventHandler<MouseEvent> enterHandler = event -> onPhysicalEnter(member);
        EventHandler<MouseEvent> exitHandler = event -> {
            if (event.getTarget() == member) {
                onPhysicalExit(member);
            }
        };
        enterHandlers.put(member, enterHandler);
        exitHandlers.put(member, exitHandler);
        member.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, enterHandler);
        member.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, exitHandler);

        if (member.isHover()) {
            physicallyHovered.add(member);
        }

        if (active) {
            ensureAllHovered();
        } else if (!physicallyHovered.isEmpty()) {
            setActive(true);
        }
    }

    /**
     * Removes a node from this hover group.
     *
     * @param member the node to remove
     */
    void remove(Node member) {
        if (!members.remove(member)) {
            return;
        }

        ChangeListener<Boolean> hoverListener = hoverListeners.remove(member);
        if (hoverListener != null) {
            member.hoverProperty().removeListener(hoverListener);
        }

        EventHandler<MouseEvent> enterHandler = enterHandlers.remove(member);
        if (enterHandler != null) {
            member.removeEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, enterHandler);
        }

        EventHandler<MouseEvent> exitHandler = exitHandlers.remove(member);
        if (exitHandler != null) {
            member.removeEventHandler(MouseEvent.MOUSE_EXITED_TARGET, exitHandler);
        }

        physicallyHovered.remove(member);

        if (active) {
            boolean previous = propagating;
            propagating = true;
            try {
                UtilitiesJavaFX.triggerMouseExited(member);
            } finally {
                propagating = previous;
            }

            if (!physicallyHovered.isEmpty()) {
                ensureAllHovered();
            } else {
                scheduleDeactivation();
            }
        }
    }

    private void onPhysicalEnter(Node member) {
        if (propagating) {
            return;
        }

        physicallyHovered.add(member);
        if (active) {
            ensureAllHovered();
        } else {
            setActive(true);
        }
    }

    private void onPhysicalExit(Node member) {
        if (propagating) {
            return;
        }

        if (!physicallyHovered.remove(member)) {
            return;
        }

        if (!physicallyHovered.isEmpty()) {
            ensureAllHovered();
        } else {
            scheduleDeactivation();
        }
    }

    private void onHoverChanged(Node member, boolean hovered) {
        if (propagating) {
            return;
        }

        if (hovered) {
            physicallyHovered.add(member);
        } else {
            physicallyHovered.remove(member);
        }

        if (!physicallyHovered.isEmpty()) {
            if (active) {
                ensureAllHovered();
            } else {
                setActive(true);
            }
        } else {
            scheduleDeactivation();
        }
    }

    private void setActive(boolean value) {
        if (active == value) {
            return;
        }

        active = value;
        propagate(value);
    }

    private void ensureAllHovered() {
        boolean previous = propagating;
        propagating = true;
        try {
            for (Node member : members) {
                if (!member.isHover()) {
                    UtilitiesJavaFX.triggerMouseEntered(member);
                }
            }
        } finally {
            propagating = previous;
        }
    }

    private void propagate(boolean hovered) {
        boolean previous = propagating;
        propagating = true;
        try {
            for (Node member : members) {
                if (hovered) {
                    UtilitiesJavaFX.triggerMouseEntered(member);
                } else {
                    UtilitiesJavaFX.triggerMouseExited(member);
                }
            }
        } finally {
            propagating = previous;
        }
    }

    private void scheduleDeactivation() {
        if (!active || deactivationScheduled) {
            return;
        }

        deactivationScheduled = true;
        Platform.runLater(() -> {
            deactivationScheduled = false;
            if (active && physicallyHovered.isEmpty()) {
                setActive(false);
            }
        });
    }
}
