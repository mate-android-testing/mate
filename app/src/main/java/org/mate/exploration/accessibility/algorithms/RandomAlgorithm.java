package org.mate.exploration.accessibility.algorithms;

import org.mate.interaction.DeviceMgr;
import org.mate.message.ResultsRandomExecution;
import org.mate.model.IGUIModel;
import org.mate.model.graph.EventEdge;
import org.mate.model.graph.ScreenNode;
import org.mate.model.graph.StateGraph;
import org.mate.state.IScreenState;

import java.util.Map;

public interface RandomAlgorithm {
    public void setHistoric(ScreenNode selectedScreenNodeState, EventEdge eventAction);
    public Map<String, ScreenNode> getHistoric();

    public String getLog();
    public void setLog(String log);
    public void clearLog();
    public ResultsRandomExecution getFactory();
    public boolean isRunAccChecks();
    public boolean updateModel(EventEdge eventAction, ScreenNode screenNodeState, IScreenState state);
    public void executeAction(EventEdge eventAction);
    //public int runAccessibilityChecks(IScreenState state);
    //public WidgetAction getAExecutableActions(IScreenState selectedScreenState);
    public EventEdge getAExecutableActions(ScreenNode selectedScreenNodeState);
    void deviceMgrRestartApp();
    long waitForProgressBar(IScreenState state);
    String handleAuth(String currentPackage);
    void deviceMgrHandleCrashDialog();
    public DeviceMgr getDeviceMgr();
    public String getPackageName();
    void setQtdeScreen();
    void setScreenHistoric(ScreenNode screenHistoric);
    void setScreenHistoric(ScreenNode screenHistoric, EventEdge eventAction);
    ScreenNode getScreenHistoric();
    public IGUIModel getGuiModel();
    void setStateGraph(StateGraph stateGraph);
    StateGraph getStateGraph();
}
