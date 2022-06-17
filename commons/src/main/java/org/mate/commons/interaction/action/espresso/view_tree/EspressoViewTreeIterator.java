package org.mate.commons.interaction.action.espresso.view_tree;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class to iterate an EspressoViewTree using a bread-first traverse that starts at a certain view.
 */
public class EspressoViewTreeIterator implements Iterable<PathWithNodes> {

    /**
     * BFS frontier: unvisited nodes.
     */
    private Queue<PathWithNodes> frontier;

    /**
     * Nodes already visited.
     * TODO (Ivan): check that this HashSet works correctly with the EspressoViewTreeNode class
     */
    private HashSet<EspressoViewTreeNode> visited = new HashSet<>();

    public EspressoViewTreeIterator(EspressoViewTreeNode startingNode) {
        this.frontier = new LinkedList<>();

        if (startingNode != null) {
            this.frontier.add(PathWithNodes.buildWithEmptyPath(startingNode));
        }
    }

    public EspressoViewTreeIterator() {
        // empty iterator
        this(null);
    }

    @NonNull
    @Override
    public Iterator<PathWithNodes> iterator() {
        return new Iterator<PathWithNodes>() {
            @Override
            public boolean hasNext() {
                return frontier.size() > 0;
            }

            @Override
            public PathWithNodes next() {
                // pop an item from the frontier
                PathWithNodes item = frontier.remove();

                // mark it as visited
                visited.add(item.getEndingNode());

                // add parent and children of the item to the frontier, but only if they were not
                // visited before.
                for (PathWithNodes other : item.getEndingNodeNeighbors()) {
                    if (!visited.contains(other.getEndingNode())) {
                        frontier.add(other);
                    }
                }

                return item;
            }
        };
    }

}
