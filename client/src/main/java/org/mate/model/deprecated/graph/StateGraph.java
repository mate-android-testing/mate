package org.mate.model.deprecated.graph;

import org.mate.commons.interaction.action.Action;
import org.mate.commons.utils.MATELog;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.WidgetAction;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by marceloe on 08/12/16.
 */
@Deprecated
public class StateGraph {

    private ScreenNode rootNode;
    private Hashtable<String,ScreenNode> screenNodes;
    private Hashtable<String,EventEdge> eventEdges;
    private Hashtable<String,List<List<Action>>> savedPaths;
    private long pathTimeout;
    public List<String> allStrPaths = null;

    public StateGraph(){
        savedPaths=new Hashtable<String, List<List<Action>>>();
        screenNodes = new Hashtable<String,ScreenNode>();
        eventEdges = new Hashtable<String,EventEdge>();
        rootNode = null;
        pathTimeout=60*1000;
    }

    public void setRootNode(ScreenNode rootNode) {
        this.rootNode = rootNode;
    }

    public Hashtable<String, ScreenNode> getScreenNodes() {
        return screenNodes;
    }

    public Hashtable<String, EventEdge> getEventEdges() {
        return eventEdges;
    }

    public void addScreenNode(ScreenNode screenNode){
        screenNodes.put(screenNode.getId(),screenNode);
        if (this.rootNode == null){
            setRootNode(screenNode);
        }
    }

    public void addEventEdge(EventEdge eventEdge){
        eventEdges.put(eventEdge.getId(),eventEdge);
        eventEdge.getSource().addEdge(eventEdge);
    }

    public void addEventEdge(ScreenNode source, ScreenNode target, WidgetAction event){
        EventEdge eventEdge = new EventEdge(source,target,event);
        addEventEdge(eventEdge);
    }

    public void addScreenNode(String packageName, String activityName, String stateId, IScreenState screenState) {
        ScreenNode screenNode = new ScreenNode(stateId, screenState);
        this.addScreenNode(screenNode);
    }

    public ScreenNode getRootNode(){
        return rootNode;
    }

    public EventEdge getEdgeByAction(Action a) {
        for (EventEdge eventEdge : eventEdges.values()) {
            if (eventEdge.getEvent().equals(a)) {
                return eventEdge;
            }
        }
        return null;
    }

    public EventEdge getEdge(String node1, String node2){
        ScreenNode source = screenNodes.get(node1);
        if (source==null)
            return null;
        for (EventEdge edge: source.getEventEdges()){
            if (edge.getTarget().getId().equals(node2))
                return edge;
        }
        return null;
    }

    public EventEdge getEdge(ScreenNode targetScreenNode, ScreenNode currentScreenNode) {
        if (screenNodes.get(targetScreenNode.getId())==null || screenNodes.get(currentScreenNode.getId())==null)
            return null;
        for (EventEdge edge: targetScreenNode.getEventEdges()){
            if (edge.getTarget().getId().equals(currentScreenNode.getId()))
                return edge;
        }
        return null;
    }

    //poor algorithm - quick implementation
    public List<List<Action>> pathFromTo(String sourceStr, String targetStr){
        ScreenNode source = getScreenNodes().get(sourceStr);
        ScreenNode target = getScreenNodes().get(targetStr);

        String pathID =source.getId()+"-"+target.getId();

        List<List<Action>> multPaths  = new ArrayList<>();
        long ts1 = new Date().getTime();
        List<Action> path = new ArrayList<>();
        allStrPaths = new ArrayList<>();
        String newPath = "";
        searchGraph(new ArrayList<ScreenNode>(), source,newPath,ts1);

        List<String> selectedPaths = new ArrayList<>();
        String selected = "";
        int distance=32000;
        if (allStrPaths==null){
            allStrPaths = new ArrayList<>();
        }
        else{
            //busca no grafo existente - se nao tiver atualiza e procura
            for (String stPath: allStrPaths){
                int indexSource = stPath.indexOf(source.getId());
                int indexTarget = stPath.indexOf(target.getId());

                if (indexSource>=0 && indexTarget>=0){
                    if (indexSource < indexTarget) {
                        if (indexTarget-indexSource < distance){
                            selected = stPath;
                            selectedPaths.add(selected);
                            distance = indexTarget - indexSource;
                        }
                    }
                }
            }
        }

        if (selectedPaths.size()!=0){
            for (String selectedPath: selectedPaths){
                path = new ArrayList<>();
                String[] pieces = selectedPath.split("-");
                boolean targetNodeReached = false;
                for (int i=0; i<pieces.length-1 && !targetNodeReached; i++){
                    EventEdge edge = this.getEdge(pieces[i],pieces[i+1]);
                    if (edge!=null){
                        path.add(edge.getEvent());
                    }
                    if (edge.getTarget().getId().equals(target.getId()))
                        targetNodeReached=true;
                }
                if (targetNodeReached) {
                    multPaths.add(path);

                    List<List<Action>> svpths = savedPaths.get(pathID);
                    if (svpths==null)
                        svpths=new ArrayList<>();
                    svpths.add(path);
                    savedPaths.put(pathID,svpths);
                }
            }
        }


        if (multPaths.size()>0){

            for (int i=0; i<multPaths.size(); i++){
                int max = multPaths.get(i).size();
                int pos = i;
                for (int j=i; j<multPaths.size(); j++){
                   if (multPaths.get(j).size()>max){
                       pos = j;
                       max = multPaths.get(j).size();
                   }
                }
                List<Action> pth = multPaths.remove(pos);
                multPaths.add(i,pth);
            }
        }

        if (multPaths.size()>1){
            MATELog.log("paths: "+multPaths.size());
            for (List<Action> actions : multPaths)
                MATELog.log("... "+actions.size() + " actions");
        }


        return multPaths;
    }



    private void searchGraph(List<ScreenNode> visitedNodes, ScreenNode node, String path, long t1){
        long t2 = new Date().getTime();
        if ( (t2-t1)>pathTimeout) {
            return;
        }
        visitedNodes.add(node);
        if (!path.equals(""))
            path+="-";
        path+=node.getId();
        List<ScreenNode> neightbors = node.getNeighbors();
        if (neightbors.isEmpty()) {
            if (!allStrPaths.contains(path))
                allStrPaths.add(path);
        }
        else {

            boolean atLeastOne = false;
            for (ScreenNode nd: neightbors){
                if ( (t2-t1)>pathTimeout) {
                    return;
                }
                if (!visitedNodes.contains(nd)){
                    atLeastOne = true;
                    searchGraph(visitedNodes,nd,path,t1);
                }
            }
            if (!atLeastOne) {
                if (!allStrPaths.contains(path))
                    allStrPaths.add(path);
            }
        }

        visitedNodes.remove(node);
    }

}
