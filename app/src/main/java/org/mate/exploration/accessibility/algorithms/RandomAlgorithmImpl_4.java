package org.mate.exploration.accessibility.algorithms;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.interaction.DeviceMgr;
import org.mate.message.ResultsRandomExecution;
import org.mate.model.IGUIModel;
import org.mate.model.graph.EventEdge;
import org.mate.model.graph.ScreenNode;
import org.mate.state.RandomAccessibilityMethods;
import org.mate.ui.ActionType;
import org.mate.ui.WidgetAction;

import java.util.List;
import java.util.TreeMap;

/* Esta adaptação eseleciona os eventos com base em pesos que podem mudar conforme o tempo passa (decrementa peso depois de ser executado)
 * @author Diogo O Santos
 *
 */
public class RandomAlgorithmImpl_4 extends RandomAccessibilityMethods implements RandomAlgorithm {

    public RandomAlgorithmImpl_4(DeviceMgr deviceMgr,
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
    }

    /**
     * TODO - Improve the implementation to search the screenNode from a tree/grafh
     * @param selectedScreenNodeState
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public EventEdge getAExecutableActions(ScreenNode selectedScreenNodeState){
        EventEdge eventAction = null;
        ScreenNode screen = null;
        if(selectedScreenNodeState.getId() != null) {
            screen = this.getHistoric(selectedScreenNodeState.getId());
        } else{
            selectedScreenNodeState.setId("(n/a) - " + selectedScreenNodeState.getScreenState().getActivityName());
        }
        if(screen != null){
           //selectedScreenNodeState
            List<EventEdge> executableActions = screen.getEventEdges();
            //eventAction = Collections.max(executableActions, Comparator.comparing(s -> s.getWeight() ));
            double maxValue = -1;
            int indexOfMaxValue = -1;
            for(int i = 0; i < executableActions.size(); i++) {
                if(executableActions.get(i).getWidgetAction().getWeight() > maxValue) {
                    if (!executableActions.get(i).getWidgetAction().isExecuted()) {// already executed?
                        indexOfMaxValue = i;
                        maxValue = executableActions.get(i).getWidgetAction().getWeight();
                    }
                }
            }
            if (maxValue >= 0) {
                eventAction = (EventEdge) executableActions.get(indexOfMaxValue);
            } else{
                eventAction = new EventEdge(selectedScreenNodeState, new WidgetAction(ActionType.BACK));
            }

        } else{
            eventAction = (EventEdge) selectedScreenNodeState.getEventEdges()
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

    public void setScreenHistoric(ScreenNode selectedScreenNodeState, EventEdge eventAction){
        int index = 0;
        int index2 = 0;
        if(selectedScreenNodeState != null && eventAction != null){
            index = selectedScreenNodeState.getEventEdges().indexOf(eventAction);
            if(!this.listScreenHistoric.contains(selectedScreenNodeState)){
                this.listScreenHistoric.add(selectedScreenNodeState);
            }else{
                index2 = this.listScreenHistoric.indexOf(selectedScreenNodeState);
                selectedScreenNodeState.getEventEdges().get(index).setEvent(eventAction.getWidgetAction());
                this.listScreenHistoric.get(index2).setEventEdges(selectedScreenNodeState.getEventEdges());
            }
        }
    }
}
