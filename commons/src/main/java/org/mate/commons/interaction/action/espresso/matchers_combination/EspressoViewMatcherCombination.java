package org.mate.commons.interaction.action.espresso.matchers_combination;


import static org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType.WITH_CLASS_NAME;
import static org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType.WITH_CONTENT_DESCRIPTION;
import static org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType.WITH_ID;
import static org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType.WITH_RESOURCE_NAME;
import static org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType.WITH_TEXT;

import org.mate.commons.interaction.action.espresso.EspressoView;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeIterator;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeNode;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeNodeWithPath;
import org.mate.commons.interaction.action.espresso.view_tree.PathInTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class EspressoViewMatcherCombination {

    /**
     * The types of Base matchers to use during construction of a Matcher combination (in order
     * of importance).
     */
    static EspressoViewMatcherType[] BASE_MATCHER_TYPES_FOR_COMBINATION = {
            WITH_RESOURCE_NAME,
            WITH_ID,
            WITH_TEXT,
            WITH_CONTENT_DESCRIPTION,
            WITH_CLASS_NAME,
    };

    private final EspressoViewTreeNode targetNode;
    private final EspressoViewTree viewTree;

    private List<MatcherForPath> matchers = new ArrayList<>();

    // TODO: explain
    private List<EspressoViewTreeNode> matchingNodes;

    // TODO: explain
    private final HashMap<EspressoView, String> hashCache = new HashMap<>();

    public EspressoViewMatcherCombination(EspressoViewTreeNode targetNode,
                                          EspressoViewTree viewTree) {
        this.targetNode = targetNode;
        this.viewTree = viewTree;
        this.matchingNodes = viewTree.getAllNodes();
    }

    private EspressoViewMatcherCombination(EspressoViewTreeNode targetNode,
                                           EspressoViewTree viewTree,
                                           HashSet<MatcherForPath> initialMatchers) {
        this(targetNode, viewTree);
        for (MatcherForPath matcher : initialMatchers) {
            this.addMatcher(matcher);
        }
    }

    public EspressoViewTreeNode getTargetNode() {
        return targetNode;
    }

    public EspressoViewTree getViewTree() {
        return viewTree;
    }

    /**
     * Method that generates an Espresso View Matcher that unequivocally targets the provided
     * targetView, in the context of the other views also available in the screen.
     * If it is unable to find a unique View Matcher, it returns null.
     *
     * The algorithm proceeds as follows:
     * - First, we collect all the relative paths from the target view to any other view in the
     * screen.
     * - Then, for each relative path, we add all possible matcher types until we find a unique
     * View Matcher. Each Matcher Type defines a type of constraint that can be added to the
     * final View Matcher (e.g., search for a view with a certain resource id).
     *
     *
     *
     * @param targetNode
     * @param viewTree
     * @return
     */
    public static EspressoViewMatcherCombination buildUniqueCombination(
            EspressoViewTreeNode targetNode,
            EspressoViewTree viewTree) {

        long startTime = System.nanoTime();

        EspressoViewMatcherCombination matcherCombination =
                new EspressoViewMatcherCombination(targetNode, viewTree);

        boolean uniqueMatcherFound = false;

        // traverse all nodes in the tree starting from the target node
        EspressoViewTreeIterator treeIteratorForTargetNode = viewTree.getTreeIteratorForTargetNode(targetNode);
        for (EspressoViewTreeNodeWithPath espressoViewTreeNodeWithPath : treeIteratorForTargetNode) {
            EspressoViewTreeNode nodeAfterPath = espressoViewTreeNodeWithPath.getNode();
            PathInTree pathFromTarget = espressoViewTreeNodeWithPath.getPathFromTarget();

            for (EspressoViewMatcherType type : BASE_MATCHER_TYPES_FOR_COMBINATION) {
                if (type.isValidForEspressoViewTreeNode(nodeAfterPath)) {
                    matcherCombination.addMatcher(new MatcherForPath(pathFromTarget, type));
                    if (matcherCombination.isUnique()) {
                        uniqueMatcherFound = true;
                        break;
                    }
                }
            }

            if (uniqueMatcherFound) {
                break;
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.

        // String debugMsg = String.format(
        //         "Matcher combination generation took %d ms (for %d other views). ",
        //         duration,
        //         viewTree.getAllNodes().size());
        // debugMsg += String.format("Result has %d matchers ", matcherCombination.getMatchers().size());
        // if (matcherCombination.isUnique()) {
        //     debugMsg += "and is unique.";
        // } else {
        //     debugMsg += "and is not unique.";
        // }
        // MATELog.log_debug(debugMsg);

        if (matcherCombination.isUnique()) {
            return matcherCombination;
        }

        return null;
    }

    public List<MatcherForPath> getMatchers() {
        return matchers;
    }

    /**
     * This method uses Delta Debugging to reduce the matcher combination to its minimal parts.
     * In other words, produce a _unique_ matcher that stops being unique after removing any part.
     *
     * @return
     */
    public EspressoViewMatcherCombination getMinimalCombination() {
        if (matchers.size() == 1) {
            return this;
        }

        List<MatcherForPath> newMatchers = new ArrayList<>(matchers);

        // N will be the partition size for the Delta Debugging. Initial value is 2.
        int n = 2;

        while (newMatchers.size() >= n) {
            // divide matchers into deltas and their complements
            List<List<MatcherForPath>> deltas = new ArrayList<>();
            List<List<MatcherForPath>> complements = new ArrayList<>();
            int partitionSize = (int) Math.floor((float) newMatchers.size() / (float) n);

            for (int start = 0; start < newMatchers.size(); start += partitionSize) {
                int end = Math.min(start + partitionSize, newMatchers.size());
                List<MatcherForPath> delta = new ArrayList<>(newMatchers.subList(start, end));
                deltas.add(delta);

                List<MatcherForPath> complement = new ArrayList<>(newMatchers.subList(0, start));
                complement.addAll(newMatchers.subList(end, newMatchers.size()));
                complements.add(complement);
            }

            // remove duplicate entry from complements list that is in deltas list
            complements.removeAll(deltas);

            // test matchers in deltas
            int uniqueSublistIndex = -1;
            for (int i = 0; i < deltas.size(); i++) {
                EspressoViewMatcherCombination m = new EspressoViewMatcherCombination(targetNode,
                        viewTree,
                        new HashSet<>(deltas.get(i)));
                if (m.isUnique()) {
                    uniqueSublistIndex = i;
                    break;
                }
            }

            if (uniqueSublistIndex != -1) {
                // reduce to the failing delta in next iteration
                n = 2;
                newMatchers = deltas.get(uniqueSublistIndex);
                continue;
            }

            // test matchers in complements
            int uniqueComplementIndex = -1;
            for (int i = 0; i < complements.size(); i++) {
                EspressoViewMatcherCombination m = new EspressoViewMatcherCombination(targetNode,
                        viewTree,
                        new HashSet<>(complements.get(i)));
                if (m.isUnique()) {
                    uniqueComplementIndex = i;
                    break;
                }
            }

            if (uniqueComplementIndex != -1) {
                // reduce to the failing complement in next iteration
                newMatchers = complements.get(uniqueComplementIndex);
                n = n - 1;
            } else {
                // increase granularity, search in a finer space
                n = 2 * n;
                if (n > newMatchers.size()) {
                    break;
                }
            }
        }


        return new EspressoViewMatcherCombination(targetNode, viewTree, new HashSet<>(newMatchers));
    }


    private void addMatcher(MatcherForPath matcher) {
        matchers.add(matcher);
        updateMatchingNodes(matcher);
    }

    private void updateMatchingNodes(MatcherForPath matcher) {
        // update the hash of each matching node
        for (EspressoViewTreeNode node : matchingNodes) {
            EspressoView espressoView = node.getEspressoView();

            String previousHash = "";
            if (hashCache.containsKey(espressoView)) {
                previousHash = hashCache.get(espressoView);
            }

            StringBuilder hash = new StringBuilder(previousHash);
            updateHash(matcher, node, hash);

            hashCache.put(espressoView, hash.toString());
        }

        // filter out nodes that no longer match the hash of the target view
        List<EspressoViewTreeNode> newMatchingNodes = new ArrayList<>();
        String targetViewHash = hashCache.get(targetNode.getEspressoView());
        for (EspressoViewTreeNode node : matchingNodes) {
            String hash = hashCache.get(node.getEspressoView());
            if (hash.equals(targetViewHash)) {
                newMatchingNodes.add(node);
            }
        }

        matchingNodes = newMatchingNodes;
    }

    private void updateHash(MatcherForPath matcher, EspressoViewTreeNode node, StringBuilder hash) {
        EspressoViewTreeNode nodeAfterPath = matcher.getPath().walkPathFromNode(node);
        if (nodeAfterPath == null) {
            hash.append("-");
            return;
        }

        if (!matcher.getType().isValidForEspressoViewTreeNode(nodeAfterPath)) {
            hash.append("-");
            return;
        }

        EspressoView espressoViewAfterPath = nodeAfterPath.getEspressoView();

        switch (matcher.getType()) {
            case WITH_ID:
                hash.append(espressoViewAfterPath.getId());
                break;
            case WITH_CLASS_NAME:
                hash.append(espressoViewAfterPath.getClassName());
                break;
            case WITH_RESOURCE_NAME:
                hash.append(espressoViewAfterPath.getResourceName());
                break;
            case WITH_TEXT:
                hash.append(espressoViewAfterPath.getText());
                break;
            case WITH_CONTENT_DESCRIPTION:
                hash.append(espressoViewAfterPath.getContentDescription());
                break;
            default:
                throw new IllegalStateException(String.format("Hash update not implemented for " +
                        "matcher type: %s", matcher.getType()));
        }
    }

    private boolean isUnique() {
        return matchingNodes.size() == 1;
    }
}
