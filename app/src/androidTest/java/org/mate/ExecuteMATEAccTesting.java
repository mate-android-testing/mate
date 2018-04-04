package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.model.IGUIModel;
import org.mate.ui.AccessibilityChecker;

/**
 * Created by marceloeler on 11/07/17.
 */

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAccTesting {


    @Test
    public void useAppContext() throws Exception {
        MATE mate = new MATE();
        mate.testApp("DepthFirst");
        String packageName = mate.getPackageName();
        IGUIModel guiModel = mate.getGuiModel();
        AccessibilityChecker.checkAccessibility(packageName,guiModel);
    }
}
