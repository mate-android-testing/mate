package org.mate.exploration.random;

import android.os.RemoteException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject2;

import org.mate.MATE;
import org.mate.datagen.DataGenerator;
import org.mate.interaction.DeviceMgr;

import java.util.Date;
import java.util.Random;

import static org.mate.MATE.device;

/**
 * Created by marceloeler on 23/06/17.
 */

public class CompleteRandom {

    private DeviceMgr deviceMgr;

    public CompleteRandom(DeviceMgr deviceMgr){
        this.deviceMgr = deviceMgr;
    }

    public void startCompleteRandomExploration( long runningTime){

        long currentTime = new Date().getTime();
        Random random = new Random();
        while (currentTime - runningTime <= MATE.TIME_OUT){

            int x1 = random.nextInt(device.getDisplayWidth());
            int x2 = random.nextInt(device.getDisplayWidth());
            int y1 = random.nextInt(device.getDisplayHeight());
            int y2 = random.nextInt(device.getDisplayHeight());
            int steps = 1;
            int action = random.nextInt(20);
            MATE.log("action: " + action);
            try {
                switch (action) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        device.click(x1, y1);
                        break;

                    case 7:
                        device.clearLastTraversedText();

                        break;

                    case 8:
                        device.drag(x1, y1, x2, y2, steps);
                        break;

                    case 9:
                        device.pressBack();
                        break;

                    case 10:
                        device.openQuickSettings();
                        break;

                    case 11:
                        device.setOrientationRight();
                        break;

                    case 12:
                        device.swipe(x1, y1, x2, y2, steps);
                        break;

                    case 13:
                    case 14:
                        DataGenerator dataGen = new DataGenerator();
                        String text = dataGen.getRandomString(random.nextInt(10));
                        device.click(x1, y1);
                        UiObject2 obj = device.findObject(By.focused(true));
                        if (obj!=null && obj.isEnabled()&&obj.getClassName().contains("Edit"))
                            obj.setText(text);
                        break;

                    case 15:
                        device.pressDelete();

                        break;

                    case 16:
                        device.pressEnter();
                        break;

                    case 17:
                        device.pressMenu();
                        break;

                    case 18:
                        device.setOrientationLeft();
                        break;

                    case 19:
                        device.setOrientationNatural();
                        break;



                }
            }
            catch(RuntimeException re){
                re.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            currentTime = new Date().getTime();
        }
    }



}
