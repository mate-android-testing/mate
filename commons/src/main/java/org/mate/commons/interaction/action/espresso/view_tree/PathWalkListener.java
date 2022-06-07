package org.mate.commons.interaction.action.espresso.view_tree;

public interface PathWalkListener {
    PathWalkListener EMPTY_LISTENER = new PathWalkListener() {
        @Override
        public void onMoveToParent(EspressoViewTreeNode parentNode) {
            // do nothing
        }

        @Override
        public void onMoveToChild(EspressoViewTreeNode childNode) {
            // do nothing
        }

        @Override
        public void onPathCompleted(EspressoViewTreeNode lastNode) {
            // do nothing
        }
    };

    void onMoveToParent(EspressoViewTreeNode parentNode);

    void onMoveToChild(EspressoViewTreeNode childNode);

    void onPathCompleted(EspressoViewTreeNode lastNode);
}
