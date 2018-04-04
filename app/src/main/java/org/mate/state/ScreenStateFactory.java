package org.mate.state;

import org.mate.state.executables.ActionsScreenState;
import org.mate.state.executables.AppScreen;

/**
 * Created by marceloeler on 21/06/17.
 */

public class ScreenStateFactory {
    public static IScreenState getScreenState(String stateType){
        if (stateType==null)
            return null;
        if (stateType.equals("ActionsScreenState")) {
            ActionsScreenState state =  new ActionsScreenState(new AppScreen());
            //if there is only the back button
            if (state.getActions().size()==0){
                try{
                    Thread.sleep(5000);
                }
                catch(Exception e){

                }
            }
            else
                return state;

            state =  new ActionsScreenState(new AppScreen());
            //if there is only the back button
            if (state.getActions().size()==0){
                try{
                    Thread.sleep(5000);
                }
                catch(Exception e){

                }
            }
            else
                return state;

            state =  new ActionsScreenState(new AppScreen());
            return state;
        }
        return null;
    }
}
