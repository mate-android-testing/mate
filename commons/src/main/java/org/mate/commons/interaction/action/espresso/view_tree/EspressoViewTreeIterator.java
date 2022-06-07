package org.mate.commons.interaction.action.espresso.view_tree;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class to iterate the EspressoViewTree using a bread-first traverse that starts at the target
 * view.
 */
public class EspressoViewTreeIterator implements Iterable<EspressoViewTreeNodeWithPath> {

    // Starting view
    private EspressoViewTreeNode targetNode;

    // BFS frontier (views unvisited)
    private Queue<EspressoViewTreeNodeWithPath> frontier;

    // Views already visited
    // TODO: check that this HashSet works correctly with the EspressoView class
    private HashSet<EspressoViewTreeNode> visited = new HashSet<>();

    public EspressoViewTreeIterator(EspressoViewTreeNode targetNode) {
        this.targetNode = targetNode;
        this.frontier = new LinkedList<>();

        if (targetNode != null) {
            this.frontier.add(EspressoViewTreeNodeWithPath.buildWithEmptyPath(targetNode));
        }
    }

    public EspressoViewTreeIterator() {
        // empty iterator
        this(null);
    }

    @NonNull
    @Override
    public Iterator<EspressoViewTreeNodeWithPath> iterator() {
        return new Iterator<EspressoViewTreeNodeWithPath>() {
            @Override
            public boolean hasNext() {
                return frontier.size() > 0;
            }

            @Override
            public EspressoViewTreeNodeWithPath next() {
                // pop an item from the frontier
                EspressoViewTreeNodeWithPath item = frontier.remove();

                // mark it as visited
                visited.add(item.getNode());

                // add parent and children of the item to the frontier, but only if they were
                // not visited before.
                for (EspressoViewTreeNodeWithPath other : item.getNeighbors()) {
                    if (!visited.contains(other.getNode())) {
                        frontier.add(other);
                    }
                }

                return item;
            }
        };
    }

}
