package org.mate.interaction;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiSelector;
import android.text.InputType;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.datagen.DataGenerator;
import org.mate.exceptions.AUTCrashException;
import org.mate.model.IGUIModel;
import org.mate.ui.Action;
import org.mate.ui.ActionType;
import org.mate.ui.PrimitiveAction;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.List;

/**
 * Created by marceloeler on 08/03/17.
 */

public class DeviceMgr implements IApp {

    private UiDevice device;
    private String packageName;

    public UiDevice getDevice(){
        return device;
    }

    public DeviceMgr(UiDevice device, String packageName){
        this.device = device;
        this.packageName = packageName;
        MATE.log("Device manager device: " + this.device);
    }

    public void executeAction(Action action) throws AUTCrashException{
        if (action instanceof WidgetAction) {
            executeAction((WidgetAction) action);
        } else if (action instanceof PrimitiveAction) {
            executeAction((PrimitiveAction) action);
        }
        else {
            throw new UnsupportedOperationException("Actions class " + action.getClass().getSimpleName() + " not yet supported");
        }
        sleep(100);
    }

    public void executeAction(PrimitiveAction action) throws AUTCrashException{
        MATE.log(" ____ execute primitive action at " + action.getX() + ", " + action.getY());


        switch (action.getActionType()) {
            case CLICK:
                device.click(action.getX(), action.getY());
                break;

            case LONG_CLICK:
                device.swipe(action.getX(), action.getY(), action.getX(), action.getY(),120);
                break;

            case SWIPE_DOWN:
                device.swipe(action.getX(), action.getY(), action.getX(), action.getY() - 300, 15);
                break;

            case SWIPE_UP:
                device.swipe(action.getX(), action.getY(), action.getX(), action.getY() + 300, 15);
                break;

            case SWIPE_LEFT:
                device.swipe(action.getX(), action.getY(), action.getX() + 300, action.getY(), 15);
                break;

            case SWIPE_RIGHT:
                device.swipe(action.getX(), action.getY(), action.getX() - 300, action.getY(), 15);
                break;

            case BACK:
                device.pressBack();;
                break;

            case MENU:
                device.pressMenu();
                break;


            default:
                throw new IllegalArgumentException("Action type " + action.getActionType() + " not implemented for primitive actions.");
        }

        checkForCrash();
    }

    public void executeAction(WidgetAction action) throws AUTCrashException{
        //MATE.log(" ____ execute " + action.getActionType() + " on " + action.getWidget().getId() + "  : " + action.getWidget().getText() + "  hint: " + action.getWidget().getHint());
        Widget selectedWidget = action.getWidget();
        ActionType typeOfAction = action.getActionType();

        switch (typeOfAction){

            case CLICK:
                handleClick(selectedWidget);
                break;

            case LONG_CLICK:
                handleLongPress(selectedWidget);
                break;

            case TYPE_TEXT:
                handleEdit(action);
                break;

            case TYPE_SPECIFIC_TEXT:
                handleEdit(action);

            case CLEAR_WIDGET:
                handleClear(selectedWidget);
                break;

            case SWIPE_DOWN:
                handleSwipe(selectedWidget, 0);
                break;

            case SWIPE_UP:
                handleSwipe(selectedWidget, 1);
                break;

            case SWIPE_LEFT:
                handleSwipe(selectedWidget, 2);
                break;

            case SWIPE_RIGHT:
                handleSwipe(selectedWidget, 3);
                break;

            case BACK:
                device.pressBack();;
                break;

            case MENU:
                device.pressMenu();
                break;

            case ENTER:
                device.pressEnter();
                break;
        }

        //if there is a progress bar associated to that action
        sleep(action.getTimeToWait());

        checkForCrash();
    }

    private void checkForCrash() throws AUTCrashException {
        //handle app crashes
        UiObject window = new UiObject(new UiSelector().packageName("android")
                .textContains("has stopped"));
        UiObject window2 = new UiObject(new UiSelector().packageName("android")
                .textContains("keeps stopping"));
        if (window.exists() || window2.exists()) {
            MATE.log("CRASH");
            throw new AUTCrashException("App crashed");
        }
    }

    public void handleClick(Widget widget){
        device.click(widget.getX(),widget.getY());
    }

    public void handleClear(Widget widget){
        UiObject2 obj = findObject(widget);
        if (obj!=null)
            obj.setText("");
    }

    public void handleSwipe(Widget widget, int direction){

        int pixelsmove=300;
        int X = 0;
        int Y = 0;
        int steps = 15;

        if (!widget.getClazz().equals("")){
            UiObject2 obj = findObject(widget);
            if (obj!=null){
                X = obj.getVisibleBounds().centerX();
                Y = obj.getVisibleBounds().centerY();
            }
            else {
                X = widget.getX();
                Y = widget.getY();
            }
        }
        else{
            X = device.getDisplayWidth()/2;
            Y = device.getDisplayHeight()/2;
            if (direction==0 || direction==1)
                pixelsmove=Y;
            else
                pixelsmove=X;
        }

        //50 pixels has been arbitrarily selected - create a properties file in the future
        switch (direction){
            case 0: device.swipe(X, Y, X, Y-pixelsmove,steps);
                break;

            case 1: device.swipe(X, Y, X, Y+pixelsmove,steps);
                break;
            case 2: device.swipe(X, Y, X+pixelsmove, Y,steps);
                break;
            case 3: device.swipe(X, Y, X-pixelsmove, Y,steps);
                break;
        }
    }

    public void handleLongPress(Widget widget) {
        UiObject2 obj = findObject(widget);
        int X = widget.getX();
        int Y = widget.getY();
        if (obj!=null){
            X = obj.getVisibleBounds().centerX();
            Y = obj.getVisibleBounds().centerY();
        }
        device.swipe(X, Y, X, Y,120);
    }

    public void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private UiObject2 findObject(Widget widget){
        List<UiObject2> objs = device.findObjects(By.res(widget.getId()));
        if (objs!=null){
            if (objs.size()==1)
                return objs.get(0);
            else {
                for (UiObject2 uiObject2: objs){
                    if (uiObject2.getText()!=null && uiObject2.getText().equals(widget.getText()))
                        return uiObject2;
                }
            }
        }

        //if no obj has been found by id, and then text if there is more than one object with the same id
        objs = device.findObjects(By.text(widget.getText()));
        if (objs!=null){
            if (objs.size()==1)
                return objs.get(0);
            else {
                for (UiObject2 uiObject2: objs){
                    if (uiObject2.getContentDescription()!=null && uiObject2.getContentDescription().equals(widget.getContentDesc()) ||
                            (uiObject2.getVisibleBounds()!=null && uiObject2.getVisibleBounds().centerX()==widget.getX() && uiObject2.getVisibleBounds().centerY()==widget.getY()))
                        return uiObject2;
                }
            }
        }
        return null;
    }

    public void handleEdit(WidgetAction action){

        Widget widget = action.getWidget();
        String textData = "";

        if (action.getExtraInfo().equals(""))
           textData = generateTextData(action);
        else
            textData = action.getExtraInfo();

        MATE.log("TEXT DATA: " + textData);

        if (widget.getResourceID().equals("")){
            if (!widget.getText().equals("")) {
                UiObject2 obj = device.findObject(By.text(widget.getText()));
                if (obj != null) {
                    obj.setText(textData);
                }
            }
            else{
                device.click(widget.getX(),widget.getY());
                UiObject2 obj = device.findObject(By.focused(true));
                if (obj!=null){
                    obj.setText(textData);
                }
            }
        }
        else{
            List<UiObject2> objs = device.findObjects(By.res(widget.getId()));
            if (objs!=null && objs.size()>0){
                int i=0;
                int size = objs.size();
                boolean objfound=false;
                while (i<size && !objfound){
                    UiObject2 obj = objs.get(i);
                    if (obj!=null) {
                        String objText = "";
                        if (obj.getText()!=null)
                            objText = obj.getText();
                        if (objText.equals(widget.getText())) {
                            obj.setText(textData);
                            objfound=true;

                        }
                    }
                    i++;
                }
                if (!objfound)
                    MATE.log("  ********* obj "+widget.getId()+ "  not found");
            }
            else{
                MATE.log("  ********* obj "+widget.getId()+ "  not found");
            }
        }

        action.setExtraInfo(textData);

    }

    private String generateTextData(WidgetAction action) {
        Widget widget = action.getWidget();

        String widgetText = widget.getText();
        if (widgetText.equals(""))
            widgetText = widget.getHint();

        String textData = "";
        String inputType ="";
        int maxLengthInt=widget.getMaxLength();
        if (action.getExtraInfo().equals("")){

            if (maxLengthInt<0)
                maxLengthInt=15;
            if (maxLengthInt>15)
                maxLengthInt=15;

            if (widget.getInputType() == (InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER))
                inputType="number";
            if (widget.getInputType()==InputType.TYPE_CLASS_PHONE)
                inputType="phone";
            if (widget.getInputType()==InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                inputType="email";
            if (widget.getInputType()==InputType.TYPE_TEXT_VARIATION_URI)
                inputType="uri";


            widgetText = widgetText.replace(".","");
            widgetText = widgetText.replace(",","");
            if (inputType.equals("") && !widgetText.equals("") && android.text.TextUtils.isDigitsOnly(widgetText)) {
                inputType = "number";
            }

            if (inputType.equals("")){
                String desc = widget.getContentDesc();
                if (desc!=null){
                    if (desc.contains("email")||desc.contains("e-mail")||desc.contains("E-mail")||desc.contains("Email"))
                        inputType="email";
                }
            }
            if (inputType.equals(""))
                inputType="text";


            textData = getRandomData(inputType,maxLengthInt);
        }
        else{
            textData = action.getExtraInfo();
        }
        return textData;
    }

    private String getRandomData(String inputType, int maxLengthInt) {
        //need to also generate random invalid string, number, email, uri, ...
        String textData = "";
        DataGenerator dataGen = new DataGenerator();
        if (inputType!=null){

            if (inputType.contains("phone") || inputType.contains("number") || inputType.contains("Phone") || inputType.contains("Number")) {
                textData = dataGen.getRandomValidNumber(maxLengthInt);
                // textData = dataGen.getRandomValidNumber();
            }
            else
            if (inputType.contains("Email") || inputType.contains("email")) {
                textData = dataGen.getRandomValidEmail(maxLengthInt);
            }
            else
            if (inputType.contains("uri") || inputType.contains("URI")) {
                textData = dataGen.getRandomUri(maxLengthInt);
            }
            else {
                textData = dataGen.getRandomValidString(maxLengthInt);
            }
        }
        else
            textData = dataGen.getRandomValidString(maxLengthInt);
        return textData;
    }

    public void reinstallApp(){
        MATE.log("Reinstall app");
        Registry.getEnvironmentManager().clearAppData();
        //sleep(1000);
    }

    public void restartApp() {
            MATE.log("Restarting app");
            // Launch the app
            Context context = InstrumentationRegistry.getContext();
            final Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(packageName);
            // Clear out any previous instances
            try{
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            catch(Exception e){
                e.printStackTrace();
                MATE.log("EXCEPTION CLEARING ACTIVITY FLAG");
            }
            context.startActivity(intent);
           // sleep(1000);

    }

    public void handleCrashDialog() {
        device.pressHome();
    }



    public boolean goToState(IGUIModel guiModel, String targetScreenStateId){
        return new GUIWalker(guiModel, packageName,this).goToState(targetScreenStateId);
    }

}
