package org.mate;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoHierarchyCheck;

import org.mate.accessibility.MultipleContentDescCheck;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Widget;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset.LATEST;
import static com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset.getInfoChecksForPreset;

/**
 * Created by marceloeler on 26/07/17.
 */

public class UseTestFramework {

    public void runTests(){
        Instrumentation instrumentation =  getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        String packageName = device.getCurrentPackageName();
        AccessibilityNodeInfo ninfo= InstrumentationRegistry.getInstrumentation().getUiAutomation().getRootInActiveWindow();
        Set<AccessibilityInfoHierarchyCheck> checks = getInfoChecksForPreset(LATEST);
        List<AccessibilityInfoCheckResult> results = new LinkedList<AccessibilityInfoCheckResult>();
        for (AccessibilityInfoHierarchyCheck check : checks) {
            results.addAll(check.runCheckOnInfoHierarchy(ninfo,InstrumentationRegistry.getContext()));
        }
        List<AccessibilityInfoCheckResult> errors = AccessibilityCheckResultUtils.getResultsForType(
                results, AccessibilityCheckResult.AccessibilityCheckResultType.ERROR);
        for (AccessibilityInfoCheckResult erro: errors){
            MATE.log(erro.getMessage()+ ": " + erro.getInfo().getViewIdResourceName());
        }

        IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");
        MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(state);

        for (Widget widget: state.getWidgets()) {

            boolean multDescOK = multDescChecker.check(widget);
            if (!multDescOK)
                MATE.log("  "+widget.getId()+ " DUP");
        }
    }
}
