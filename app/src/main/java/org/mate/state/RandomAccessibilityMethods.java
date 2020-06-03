package org.mate.state;


import org.mate.MATE;
import org.mate.accessibility.check.bbc.AccessibilityViolationChecker;
import org.mate.exceptions.AUTCrashException;
import org.mate.interaction.DeviceMgr;
import org.mate.message.ResultsRandomExecution;
import org.mate.model.IGUIModel;
import org.mate.model.graph.EventEdge;
import org.mate.model.graph.ScreenNode;
import org.mate.ui.ActionType;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.mate.MATE.device;

public class RandomAccessibilityMethods {
    protected String packageName;
    protected IGUIModel guiModel;
    protected static String currentActivityName;
    protected DeviceMgr deviceMgr;
    protected Map<String, ScreenNode> historic;
    protected ScreenNode screenHistoric;
    protected boolean runAccChecks = true;
    protected ResultsRandomExecution factory;
    protected String log;
    protected  int qtdeScreen;

    public long waitForProgressBar(IScreenState state) {
        long ini = new Date().getTime();
        long end = new Date().getTime();
        boolean hadProgressBar = false;
        boolean hasProgressBar = true;
        while (hasProgressBar && (end-ini)<22000) {
            hasProgressBar=false;
            for (Widget widget : state.getWidgets()) {
                if (widget.getClazz().contains("ProgressBar") && widget.isEnabled() && widget.getContentDesc().contains("Loading")) {
                    MATE.log("WAITING PROGRESS BAR TO FINISH");
                    hasProgressBar = true;
                    hadProgressBar=true;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    state = ScreenStateFactory.getScreenState(state.getType());
                }
            }
            end = new Date().getTime();
        }
        if (!hadProgressBar)
            return 0;
        return end-ini;
    }

    public static String handleAuth(DeviceMgr dmgr, String currentPackage) {

        if (currentPackage.contains("com.android.packageinstaller")) {
            long timeA = new Date().getTime();


            boolean goOn = true;
            while (goOn) {

                IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
                List<WidgetAction> actions = screenState.getActions();
                for (WidgetAction action : actions) {
                    if (action.getWidget().getId().contains("allow")) {
                        try {
                            dmgr.executeAction(action);
                        } catch (AUTCrashException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                currentPackage = device.getCurrentPackageName();
                MATE.log("new package name: " + currentPackage);
                long timeB = new Date().getTime();
                if (timeB - timeA > 30000)
                    goOn = false;
                if (!currentPackage.contains("com.android.packageinstaller"))
                    goOn = false;
            }
            return currentPackage;
        } else
            return currentPackage;
    }

    public String handleAuth(String currentPackage) {

        if (currentPackage.contains("com.android.packageinstaller")) {
            long timeA = new Date().getTime();


            boolean goOn = true;
            while (goOn) {

                IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
                List<WidgetAction> actions = screenState.getActions();
                for (WidgetAction action : actions) {
                    if (action.getWidget().getId().contains("allow")) {
                        try {
                            this.deviceMgr.executeAction(action);
                        } catch (AUTCrashException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                currentPackage = device.getCurrentPackageName();
                MATE.log("new package name: " + currentPackage);
                long timeB = new Date().getTime();
                if (timeB - timeA > 30000)
                    goOn = false;
                if (!currentPackage.contains("com.android.packageinstaller"))
                    goOn = false;
            }
            return currentPackage;
        } else
            return currentPackage;
    }

    public void deviceMgrHandleCrashDialog(){
        this.deviceMgr.handleCrashDialog();
    }

    protected void setWeightToActions(IScreenState selectedScreenState){
        List<WidgetAction> executableActions = selectedScreenState.getActions();
        ActionType typeOfAction;
        for(int i = 0; i < executableActions.size(); i++){
            typeOfAction = executableActions.get(i).getActionType();
            executableActions.get(i).setWeight(returnWeight(typeOfAction));
        }
    }

    public static int runAccessibilityChecks(IScreenState state) {
        //this.setLog("Executando verificação de acessibilidade...");
        MATE.log("start ACCESSIBILITY CHECKS: " );
        return AccessibilityViolationChecker.runAccessibilityChecks(state).size();
    }

    private int returnWeight(ActionType typeOfAction){

        switch (typeOfAction){

            case CLICK:
                return 11;
            case LONG_CLICK:
                return 10;

            case TYPE_TEXT:
                return 8;

            case TYPE_SPECIFIC_TEXT:
                return 9;

            case CLEAR_WIDGET:
                return 4;

            case SWIPE_DOWN:
                return 7;

            case SWIPE_UP:
                return 6;

            case SWIPE_LEFT:
                return 5;

            case SWIPE_RIGHT:
                return 4;

            case BACK:
                return 3;

            case MENU:
                return 2;

            case ENTER:
                return 1;
            default:
                return 0;
        }
    }

    protected String getListOfActions(List<WidgetAction> executableActions){
        String screen = "";
        String actionsExecuted = "";
        for (WidgetAction ea : executableActions) {
            if (ea.isExecuted()) {
                if (ea.getAdjScreen() != null) {
                    screen = ea.getAdjScreen().getId();
                } else {
                    screen = "n/a";
                }
                actionsExecuted = actionsExecuted + ea.getActionType().toString() + "(x" + ea.getQtdeOfExec() + ")(" + screen + "), ";
            }
        }
        return actionsExecuted;
    }

    public void executeAction(EventEdge eventAction) {
        WidgetAction action = eventAction.getWidgetAction();
        try {
            this.deviceMgr.executeAction(action);
            action.setExecuted(true);//set as executed
            action.increaseWeight(1);//add 1 points by execute the action
            action.plusQtdeOfExec();//plus 1 execution
            this.setLog("-> Ação " + action.getActionType() + " executada...");
            MATE.log("-> Ação " + action.getActionType() + " executada...");
            eventAction.setEvent(action);
        } catch (AUTCrashException e) {
            e.printStackTrace();
            this.deviceMgr.handleCrashDialog();
        }
    }
    public boolean updateModel(EventEdge eventAction, ScreenNode screenNodeState, IScreenState state) {
        WidgetAction action = (WidgetAction) eventAction.getEvent();
        boolean newState = this.guiModel.updateModel(action, state);
        MATE.log("Verificando se foi gerado um novo estado da tela");
        if (newState) {//if a new state, increase the weight
            this.setQtdeScreen();
            eventAction.increaseWeight(10);
            eventAction.setNewStateGenerated(true);
            eventAction.setTarget(new ScreenNode(state));//set the next screen accessed because the action executed
            this.setLog("A tela " + screenNodeState.getScreenState().getActivityName() + " foi redirecionada para "
                    + state.getActivityName());
            MATE.log("A tela " + screenNodeState.getScreenState().getActivityName() + " foi redirecionada para "
                    + state.getActivityName());
        }
        return newState;
    }
    public boolean updateModel(WidgetAction action, IScreenState selectedScreenState, IScreenState state) {
        this.setLog("Verificando se foi gerado um novo estado da tela");
        MATE.log("Verificando se foi gerado um novo estado da tela");
        if (this.guiModel.updateModel(action, state)) {//if a new state, increase the weight
            this.setLog("A tela " + selectedScreenState.getActivityName() + " foi redirecionada para "
                    + state.getActivityName());
            MATE.log("A tela " + selectedScreenState.getActivityName() + " foi redirecionada para "
                    + state.getActivityName());
            this.setQtdeScreen();
            action.increaseWeight(10);
            action.setNewStateGenerated(true);
            action.setAdjScreen(state); //set the next screen accessed because the action executed
            return true;
        } else {
            this.setLog("A tela permaneceu na mesma");
            MATE.log("A tela permaneceu na mesma");
            //action.decreaseWeight(1);
            action.decreaseFitness(1);
            return false;
        }
    }

    protected int selectRandomAction(int executionActionSize){
        Random rand = new Random();
        return rand.nextInt(executionActionSize);
    }

    public void deviceMgrRestartApp(){
        MATE.log("package name: " + this.packageName);
        this.deviceMgr.restartApp();
    }

    protected int getQtdeScreen() {
        return qtdeScreen;
    }

    public void setQtdeScreen() {
        this.qtdeScreen++;
    }

    public Map<String, ScreenNode> getHistoric() {
        return historic;
    }

    public ScreenNode getHistoric(String id){return this.historic.get(id);}

    public void setHistoric(Map<String, ScreenNode> historic) {
        this.historic = historic;
    }

    public void setHistoric(String id, ScreenNode screenNode) {this.historic.put(id, screenNode);}

    protected List<EventEdge> getEventEdgesFromHistoric(String id){
        ScreenNode screen = this.getHistoric(id);
        if(screen != null){
            return screen.getEventEdges();
        }
        return null;
    }

    public ScreenNode getScreenHistoric() {
        return screenHistoric;
    }

    public void setScreenHistoric(ScreenNode screenHistoric) {
        this.screenHistoric = screenHistoric;
    }

    public String getLog() {return log;}

    public void setLog(String log) {this.log = this.log+log+"\n";}

    public IGUIModel getGuiModel() {
        return guiModel;
    }

}
