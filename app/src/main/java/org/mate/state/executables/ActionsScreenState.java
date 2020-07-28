package org.mate.state.executables;

import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.MATE;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.ui.ActionType;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by marceloeler on 21/06/17.
 */

public class ActionsScreenState extends AbstractScreenState {

    private List<WidgetAction> actions;
    private static Hashtable<String,Hashtable<String,List<Integer>>> idSizes = new Hashtable<>();
    private List<Float> pheromone;
    private Map<Action,Float> actionsWithPheromone;
    private Map<Action,Float> actionWithFitness;
    private String id;
    private AppScreen appScreen;
    private AccessibilityNodeInfo rootNodeInfo;
    private String sessionID;
    private String typeOfNewState;

    @Override
    public String getId() {

        return id+"_"+sessionID;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public ActionsScreenState(AppScreen appScreen){
        super(appScreen.getPackageName(),appScreen.getActivityName());
        this.widgets = appScreen.getWidgets();
        this.screenTitle = appScreen.getTitle();
        this.rootNodeInfo = appScreen.getRootNodeInfo();
        actions=null;
        this.id="";
        this.appScreen = appScreen;
        sessionID = MATE.sessionID;
        typeOfNewState="";
    }


    public String getSessionId(){
        return sessionID;
    }

    private int getMaxAmountOfID(Hashtable<String, List<Integer>> sameIDWidgets, String wid){
         List<Integer> amounts = sameIDWidgets.get(wid);
        if (amounts==null) {
            amounts = new ArrayList<>();
            sameIDWidgets.put(wid,amounts);
            return 0;
        }

        if (amounts.size()==0)
            return 0;

        int max = amounts.get(0);
        int i=1;
        while (i<amounts.size()){
            if (max<amounts.get(i))
                max = amounts.get(i);
            i++;
        }
        return max;
    }

    public List<WidgetAction> getActions(){
        if (actions!=null)
            return actions;

        Hashtable<String,List<Integer>> sameIDWidgets = idSizes.get(activityName);
        if (sameIDWidgets==null){
            sameIDWidgets = new Hashtable<>();
            idSizes.put(activityName,sameIDWidgets);
        }

        List<WidgetAction> executables = new ArrayList<>();
        int editables = 0;
        boolean enterAdded = false;
        Hashtable<String,Integer> idAmount = new Hashtable<String,Integer>();
        boolean selected;
        for (Widget widget: widgets){
            selected = false;

            if (widget.getClazz().contains("Button"))
                selected=true;

            if (widget.isClickable() || widget.isLongClickable() || widget.isScrollable() || widget.isEditable() ){
                selected=true;
            }

            if (widget.directSonOf("ListView")||widget.directSonOf("GridView")) {
                if (widget.getParent().isClickable())
                    selected = true;

            }

//            if (widget.getClazz().contains("ImageView"))
//                selected=true;

            if (widget.getClazz().equals("android.view.View")&&(!widget.getContentDesc().equals("")||!widget.getText().equals(""))){
                selected=true;
            }

            if (widget.getClazz().contains("Spinner")){
                selected=true;
            }

            //for skype test
            if (widget.getClazz().contains("ViewGroup")){
                if (!widget.getContentDesc().equals(""))
                    selected=true;
            }

            Integer amount = idAmount.get(widget.getId());
            if (amount==null){
                idAmount.put(widget.getId(),1);
                amount = idAmount.get(widget.getId());
            }
            else{
                idAmount.put(widget.getId(),++amount);
                amount = idAmount.get(widget.getId());
            }

            if (amount>getMaxAmountOfID(sameIDWidgets,widget.getId()) && sameIDWidgets.get(widget.getId()).size()==2){
                selected=false;
            }

            if (selected){
                if (widget.getClazz().equals("android.widget.GridView"))
                    selected=false;
            }

            if (selected){
                if (widget.getClazz().equals("android.view.View")){
                    if (!widget.isClickable()&&!widget.isLongClickable()&&!widget.isScrollable()&&widget.getText().equals("")&&widget.getContentDesc().equals("")){
                        selected=false;
                    }
                }
            }

            if (selected){
                if (widget.getClazz().contains("ListView"))
                    selected=false;
            }

            if (selected){
                if (widget.getClazz().contains("ScrollView"))
                    selected=false;
            }

            if (selected){
                if (widget.isSonOf("android.webkit.WebView"))
                    selected=false;
            }

            if (!widget.isEnabled())
                selected=false;

            if (selected){
                WidgetAction event;
                if (!widget.isEditable()){
                    event = new WidgetAction(widget, ActionType.CLICK);
                    executables.add(event);
                    widget.setClickable(true);
                }

                if (widget.isEditable() || widget.getClazz().contains("Edit")){
                    event = new WidgetAction(widget,ActionType.TYPE_TEXT);
                    executables.add(0,event);
                    editables++;
                }


                if (widget.isLongClickable()&&!widget.isEditable()){
                    event = new WidgetAction(widget, ActionType.LONG_CLICK);
                    executables.add(event);
                    widget.setLongClickable(true);
                }
                else
                {
                    if ((widget.isSonOfLongClickable()) && (!widget.isEditable()&& !widget.getClazz().contains("TextView"))){
                        event = new WidgetAction(widget, ActionType.LONG_CLICK);
                        executables.add(event);
                        widget.setLongClickable(true);
                    }
                }


                if (widget.isScrollable()){

                    if (!widget.getClazz().contains("Spinner")&&!widget.isSonOf("Spinner")){
                        event = new WidgetAction(widget,ActionType.SWIPE_LEFT);
                        executables.add(event);

                        event = new WidgetAction(widget,ActionType.SWIPE_RIGHT);
                        executables.add(event);

                        event = new WidgetAction(widget,ActionType.SWIPE_UP);
                        executables.add(event);

                        event = new WidgetAction(widget,ActionType.SWIPE_DOWN);
                        executables.add(event);

                        widget.setScrollable(true);
                    }
                }
            }

            if (editables>0 && !enterAdded){
                executables.add(editables,new WidgetAction(ActionType.ENTER));
                enterAdded=true;
            }
        }

        //update number of ids
        for (String id: idAmount.keySet()){
            List<Integer> amounts = sameIDWidgets.get(id);
            if (amounts.size()<2){
                boolean sameAmount=false;
                for (int i=0; i<amounts.size(); i++)
                    if (amounts.get(i)==idAmount.get(id))
                        sameAmount=true;
                if (!sameAmount)
                    amounts.add(idAmount.get(id));
            }
        }

        if (executables.size()==0)
            executables.add(new WidgetAction(ActionType.BACK));

        if (appScreen.isHastoScrollDown()||appScreen.isHasToScrollUp()){
            executables.add(new WidgetAction(ActionType.SWIPE_DOWN));
            executables.add(new WidgetAction(ActionType.SWIPE_UP));
        }

        if (appScreen.isHasToScrollLeft()||appScreen.isHasToScrollRight()){
            executables.add(new WidgetAction(ActionType.SWIPE_LEFT));
            executables.add(new WidgetAction(ActionType.SWIPE_RIGHT));
        }

        if (activityName.contains("GoogleOAuthActivity"))
            executables = new ArrayList<>();
        executables.add(new WidgetAction(ActionType.BACK));
        executables.add(new WidgetAction(ActionType.MENU));

        actions = executables;
        return executables;
    }

     //Todo: add hashcode method

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionsScreenState that = (ActionsScreenState) o;

        if (!this.activityName.equals(that.getActivityName())){
            //MATE.log_acc("Strobe 1: State "+this.id+"different from State: "+object.getId());
            return false;
        }


        if (!this.packageName.equals(that.getPackageName())){
            //MATE.log_acc("Strobe 2: State "+this.id+"different from State: "+object.getId());
            return false;
        }


        ActionsScreenState screenState = (ActionsScreenState) that;

        List<WidgetAction> actionsThis = this.getActions();
        List<WidgetAction> actionsOther = screenState.getActions();

        List<String> setActThis = new ArrayList<>();
        List<String> setActOther = new ArrayList<>();
        for (WidgetAction act: actionsThis) {
            if (act.getWidget()!=null) {
                if (act.getWidget().getClazz().contains("Button"))
                    setActThis.add(act.getWidget().getId() + "-" + act.getActionType()+"-"+act.getWidget().getText());
                else
                    setActThis.add(act.getWidget().getId() + "-" + act.getActionType());
            }

        }
        for (WidgetAction act: actionsOther) {
            if (act.getWidget()!=null) {
                if (act.getWidget().getClazz().contains("Button"))
                    setActOther.add(act.getWidget().getId() + "-" + act.getActionType()+"-"+act.getWidget().getText());
                else
                    setActOther.add(act.getWidget().getId() + "-" + act.getActionType());
            }
        }

        if (setActThis.size()==setActOther.size()){

            for (String strActThis: setActThis){
                if (!setActOther.contains(strActThis)) {
                    //MATE.log_acc("Strobe 3: State "+this.id+"different from State: "+object.getId());
                    return false;
                }
            }

            Hashtable<String, Widget> editablesThis = this.getEditableWidgets();
            Hashtable<String, Widget> editablesOther = screenState.getEditableWidgets();

            for (Widget wdgThis: editablesThis.values()){

                Widget wdgOther = editablesOther.get(wdgThis.getId());
                if (wdgOther==null) {
                    //MATE.log_acc("Strobe 4 State "+this.id+"different from State: "+object.getId());
                    return false;
                }


                if (wdgOther.isEmpty() != wdgThis.isEmpty()) {
                    //MATE.log_acc("Strobe 5: State "+this.id+"different from State: "+object.getId());
                    return false;
                }
            }


            //as for the checkables it considers two GUIs equals if they have the same objects checked
            Hashtable<String,Widget> checkablesThis = this.getCheckableWidgets();
            Hashtable<String,Widget> checkablesOther = screenState.getCheckableWidgets();
            for (Widget wdgThis: checkablesThis.values()){
                Widget wdgOther = checkablesOther.get(wdgThis.getId());
                if (wdgOther==null)
                    return false;
                if (wdgOther.isChecked()!=wdgThis.isChecked())
                    return false;
            }


            return true;
        }
        return false;
    }

    public String getType(){
        return "ActionsScreenState";
    }


    //yan
    @Override
    public void updatePheromone(Action triggeredAction) {
        //find pheromone in hashMap and update
    }

    //yan
    @Override
    public Map<Action, Float> getActionsWithPheromone() {
        return actionsWithPheromone;
    }

    //yan
    @Override
    public AccessibilityNodeInfo getRootAccessibilityNodeInfo() {
        return rootNodeInfo;
    }

    @Override
    public boolean differentColor(IScreenState visitedState) {
        if (visitedState==null) {
            //MATE.log("visited state = null");
            return true;
        }
        ActionsScreenState that = (ActionsScreenState) visitedState;

        List<Widget> thisWidgets = this.getWidgets();
        List<Widget> otherWidgets = visitedState.getWidgets();

        boolean found = false;
        for (Widget wThis: thisWidgets){
            //search by id
            for (Widget wOther: otherWidgets){
                if (wThis.getId().equals(wOther.getId()) &&
                        wThis.getText().equals(wOther.getText())){

                    found = true;

                    if (!wOther.getColor().equals(wThis.getColor()) &&
                            !wOther.isFocused() &&
                            wThis.isFocused()==wOther.isFocused() &&
                            wOther.getHint().equals(wThis.getHint()) &&
                            wOther.getContentDesc().equals(wThis.getContentDesc())){
                        return true;
                    }
                }
            }

            if (!found){
                //search by text
                for (Widget wOther: otherWidgets){

                    if (!wThis.getText().equals("") && wThis.getText().equals(wOther.getText())){
                        found = true;

                        if (!wOther.getColor().equals(wThis.getColor()) &&
                                !wOther.isFocused() &&
                                wThis.isFocused()==wOther.isFocused() &&
                                wOther.getHint().equals(wThis.getHint()) &&
                                wOther.getContentDesc().equals(wThis.getContentDesc()))
                            return true;
                    }
                }
            }
        }
        return false;

    }


    public String getTypeOfNewState() {
        return typeOfNewState;
    }

    public void setTypeOfNewState(String typeOfNewState) {
        this.typeOfNewState = typeOfNewState;
    }
}
