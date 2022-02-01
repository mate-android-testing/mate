package org.mate.representation.commands;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import org.mate.commons.IMATEServiceInterface;
import org.mate.representation.util.MATERepLog;

import java.util.ArrayList;

/**
 * Handles commands requested by the MATE Service as messages, to process them in the Main thread.
 */
public class MessageHandler extends Handler {
    public static final int GET_AVAILABLE_ACTIONS = 1;

    private IMATEServiceInterface mateService;

    public void setMateService(IMATEServiceInterface mateService) {
        this.mateService = mateService;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (mateService == null) {
            MATERepLog.info("MATE Service is null when handling message");
            return;
        }

        switch (msg.what) {
            case GET_AVAILABLE_ACTIONS:
                ArrayList<String> actions = new ArrayList<>();

                try {
                    mateService.reportAvailableActions(actions);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
