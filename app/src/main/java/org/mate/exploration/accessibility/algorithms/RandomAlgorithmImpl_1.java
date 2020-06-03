package org.mate.exploration.accessibility.algorithms;

import org.mate.interaction.DeviceMgr;
import org.mate.message.ResultsRandomExecution;
import org.mate.model.IGUIModel;
import org.mate.model.graph.EventEdge;
import org.mate.model.graph.ScreenNode;
import org.mate.state.RandomAccessibilityMethods;

import java.util.List;
import java.util.TreeMap;

/* Esta adaptação executa as ações de cada tela aleatoriamente podendo executar mais de uma vez.
 * Pode também não executar uma ação da tela.
 * @author Diogo O Santos
 *
 */
public class RandomAlgorithmImpl_1 extends RandomAccessibilityMethods implements RandomAlgorithm {

    public RandomAlgorithmImpl_1(DeviceMgr deviceMgr,
                                 String packageName, IGUIModel guiModel, boolean runAccChecks){
        this.factory = new ResultsRandomExecution();
        this.historic = new TreeMap<>();
        log = "";
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        this.guiModel = guiModel;
        currentActivityName="";
        this.runAccChecks = runAccChecks;
        this.qtdeScreen = 0;
        this.screenHistoric = null;
    }

    /**
     * TODO - Improve the implementation to search the screenNode from a tree/grafh
     * @param selectedScreenNodeState
     * @return
     */
    public EventEdge getAExecutableActions(ScreenNode selectedScreenNodeState){
        EventEdge eventAction = null;
        ScreenNode screen = null;
        if(selectedScreenNodeState.getId() != null) {
            screen = this.getHistoric(selectedScreenNodeState.getId());
        } else{
            selectedScreenNodeState.setId("(n/a) - " + selectedScreenNodeState.getScreenState().getActivityName());
        }
        if(screen != null){
            eventAction = screen.getEventEdges().get(selectRandomAction(screen.getEventEdges().size()));
            //selectedScreenNodeState
        } else{
            eventAction = selectedScreenNodeState.getEventEdges()
                    .get(selectRandomAction(selectedScreenNodeState.getEventEdges().size()));
        }

        return eventAction;
    }

    public void setHistoric(ScreenNode selectedScreenNodeState, EventEdge eventAction) {
        int index = 0;

        if(!(selectedScreenNodeState == null || eventAction == null)) {
            List<EventEdge> executableEventsActions = this.getEventEdgesFromHistoric(selectedScreenNodeState.getId());
            if (executableEventsActions != null) {
                index = executableEventsActions.indexOf(eventAction);
            } else {
                executableEventsActions = selectedScreenNodeState.getEventEdges();
                index = selectedScreenNodeState.getEventEdges().indexOf(eventAction);
            }
            if (index != -1) {
                executableEventsActions.set(index, eventAction);
            } else {
                executableEventsActions.add(eventAction);
            }
            selectedScreenNodeState.setEventEdges(executableEventsActions);
            this.setHistoric(selectedScreenNodeState.getId(), selectedScreenNodeState);

        }
    }

    //public void setHistoric(String id, ScreenNode screenNode) {this.historic.put(id, screenNode);}

    //public ScreenNode getHistoric(String id){return this.historic.get(id);}

    public ResultsRandomExecution getFactory() {return factory;}

    public boolean isRunAccChecks() {return runAccChecks;}

    public void clearLog() {this.log = "";}

    public DeviceMgr getDeviceMgr() {return deviceMgr;}

    public String getPackageName() {return packageName;}


}
