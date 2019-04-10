package org.mate.state.executables;

import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.MATE;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.ui.ActionType;
import org.mate.ui.Widget;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

/**
 * Created by marceloeler on 21/06/17.
 */

public class ActionsScreenState extends AbstractScreenState {

    private Vector<Action> actions;
    private static Hashtable<String,Hashtable<String,Vector<Integer>>> idSizes = new Hashtable<String,Hashtable<String,Vector<Integer>>>();
    private Vector<Float> pheromone;
    private HashMap<Action,Float> actionsWithPheromone;
    private HashMap<Action,Float> actionWithFitness;
    private String id;
    private AppScreen appScreen;
    private AccessibilityNodeInfo rootNodeInfo;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public ActionsScreenState(AppScreen appScreen){
        super(appScreen.getPackageName(),appScreen.getActivityName());
        this.widgets = appScreen.getWidgets();
        this.rootNodeInfo = appScreen.getRootNodeInfo();
        actions=null;
        this.appScreen = appScreen;

    }

    private int getMaxAmountOfID(Hashtable<String,Vector<Integer>> sameIDWidgets, String wid){
        Vector<Integer> amounts = sameIDWidgets.get(wid);
        if (amounts==null) {
            amounts = new Vector<Integer> ();
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

    public Vector<Action> getActions(){
        if (actions!=null)
            return actions;

        Hashtable<String,Vector<Integer>> sameIDWidgets = idSizes.get(activityName);
        if (sameIDWidgets==null){
            sameIDWidgets = new Hashtable<String,Vector<Integer>>();
            idSizes.put(activityName,sameIDWidgets);
        }

        Vector<Action> executables = new Vector<Action>();
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
                Action event;
                if (!widget.isEditable()){
                    event = new Action(widget, ActionType.CLICK);
                    executables.add(event);
                    widget.setClickable(true);
                }

                if (widget.isEditable() || widget.getClazz().contains("Edit")){
                    event = new Action(widget,ActionType.TYPE_TEXT);
                    executables.add(0,event);
                    editables++;
                }


                if (widget.isLongClickable()&&!widget.isEditable()){
                    event = new Action(widget, ActionType.LONG_CLICK);
                    executables.add(event);
                    widget.setLongClickable(true);
                }
                else
                {
                    if ((widget.isSonOfLongClickable()) && (!widget.isEditable()&& !widget.getClazz().contains("TextView"))){
                        event = new Action(widget, ActionType.LONG_CLICK);
                        executables.add(event);
                        widget.setLongClickable(true);
                    }
                }


                if (widget.isScrollable()){

                    if (!widget.getClazz().contains("Spinner")&&!widget.isSonOf("Spinner")){
                        event = new Action(widget,ActionType.SWIPE_LEFT);
                        executables.add(event);

                        event = new Action(widget,ActionType.SWIPE_RIGHT);
                        executables.add(event);

                        event = new Action(widget,ActionType.SWIPE_UP);
                        executables.add(event);

                        event = new Action(widget,ActionType.SWIPE_DOWN);
                        executables.add(event);

                        widget.setScrollable(true);
                    }
                }
            }

            if (editables>0 && !enterAdded){
                executables.add(editables,new Action(ActionType.ENTER));
                enterAdded=true;
            }
        }

        //update number of ids
        for (String id: idAmount.keySet()){
            Vector<Integer> amounts = sameIDWidgets.get(id);
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
            executables.add(new Action(ActionType.BACK));

        if (appScreen.isHastoScrollDown()||appScreen.isHasToScrollUp()){
            executables.add(new Action(ActionType.SWIPE_DOWN));
            executables.add(new Action(ActionType.SWIPE_UP));
        }

        if (appScreen.isHasToScrollLeft()||appScreen.isHasToScrollRight()){
            executables.add(new Action(ActionType.SWIPE_LEFT));
            executables.add(new Action(ActionType.SWIPE_RIGHT));
        }

        if (activityName.contains("GoogleOAuthActivity"))
            executables = new Vector<Action>();
        executables.add(new Action(ActionType.BACK));
        executables.add(new Action(ActionType.MENU));

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

        Vector<Action> actionsThis = this.getActions();
        Vector<Action> actionsOther = screenState.getActions();

        Vector<String> setActThis = new Vector<String>();
        Vector<String> setActOther = new Vector<String>();
        for (Action act: actionsThis) {
            if (act.getWidget()!=null) {
                if (act.getWidget().getClazz().contains("Button"))
                    setActThis.add(act.getWidget().getId() + "-" + act.getActionType()+"-"+act.getWidget().getText());
                else
                    setActThis.add(act.getWidget().getId() + "-" + act.getActionType());
            }

        }
        for (Action act: actionsOther) {
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
    public HashMap<Action, Float> getActionsWithPheromone() {
        return actionsWithPheromone;
    }

    //yan
    @Override
    public AccessibilityNodeInfo getRootAccessibilityNodeInfo() {
        return rootNodeInfo;
    }

}
