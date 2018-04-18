package org.mate;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoHierarchyCheck;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.ui.EnvironmentManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset.LATEST;
import static com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset.getInfoChecksForPreset;

/**
 * Created by marceloeler on 26/07/17.
 */
@RunWith(AndroidJUnit4.class)
public class CheckScreenUsingAccessibilityFramework {

    @Test
    public void useAppContext() throws Exception {

        int i=0;
        while (i<100) {
            Thread.sleep(2000);
            MATE.log("execute");
            UseTestFramework use = new UseTestFramework();
            use.runTests();
            i++;
        }


    }
}
