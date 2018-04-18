package org.mate.accessibility;

import android.support.test.InstrumentationRegistry;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoHierarchyCheck;

import org.mate.MATE;
import org.mate.state.IScreenState;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset.LATEST;
import static com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset.getInfoChecksForPreset;

/**
 * Created by marceloeler on 26/07/17.
 */

public class AccessibilityInfoChecker {

    public void runAccessibilityTests(IScreenState state){
        Set<AccessibilityInfoHierarchyCheck> checks = getInfoChecksForPreset(LATEST);
        List<AccessibilityInfoCheckResult> results = new LinkedList<AccessibilityInfoCheckResult>();
        for (AccessibilityInfoHierarchyCheck check : checks) {
            try{
                results.addAll(check.runCheckOnInfoHierarchy(state.getRootAccessibilityNodeInfo(),InstrumentationRegistry.getContext()));
            }
            catch(Exception e){
                MATE.log("exception while running check: " + check.toString());
            }
        }
        List<AccessibilityInfoCheckResult> errors = AccessibilityCheckResultUtils.getResultsForType(
                results, AccessibilityCheckResult.AccessibilityCheckResultType.ERROR);
    }

}
