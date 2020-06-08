package org.mate.accessibility.check.wcag.bbc;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class SpacingAccessibilityCheck implements IWCAGCheck {

    private int distance;

    private int matrix[][] = null;
    private List<Widget> widgets;

    private void loadMatrix(IScreenState state){
        //MATE.log("LLLOOOOADDD MATRIX");
        int maxw = MATE.device.getDisplayWidth();
        int maxh = MATE.device.getDisplayHeight();
        matrix = new int[maxw][maxh];

        for (int i=0; i<maxw; i++)
            for (int j=0; j<maxh; j++)
                matrix[i][j]=-1;

        widgets = new ArrayList<Widget>();
        int index = -1;
        for (Widget w: state.getWidgets()){
            if (w.isImportantForAccessibility()&&w.isActionable()) {
                if (w.isCheckable() || w.isClickable() || w.isLongClickable() || w.isSonOfLongClickable() || w.isEditable() || w.isSpinnerType()) {
                    index++;
                    //MATE.log("->"+w.getId()+" index: " + index);

                    widgets.add(w);
                    for (int i = w.getX1(); i <= w.getX2() && i < maxw; i++) {
                        for (int j = w.getY1(); j <= w.getY2() && j < maxh; j++)
                            matrix[i][j] = index;
                    }
                }
            }
        }
    }

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {
        try {

            if (!widget.isImportantForAccessibility())
                return null;

            int maxw = MATE.device.getDisplayWidth();
            int maxh = MATE.device.getDisplayHeight();

            if (matrix == null)
                loadMatrix(state);

            int index = widgets.indexOf(widget);

            //MATE.log(" CHECKING SPACE FOR "+ widget.getId() +"  index: " + index);
            List<Widget> conflicts = new ArrayList<Widget>();
            int conflictIndex = -1;
            if (index >= 0) {
                int x1 = widget.getX1();
                int x2 = widget.getX2();
                int y1 = widget.getY1();
                int y2 = widget.getY2();

                if (x1 > 0) {
                    int count = 0;
                    for (int j = y1; j < y2; j++) {
                        //MATE.log("x1-1: " + String.valueOf(matrix[x1-1][j]));
                        if (matrix[x1 - 1][j] != -1) {
                            count++;
                            conflictIndex = matrix[x1 - 1][j];
                        }
                    }
                    if (count > (y2 - y1) / 2) {
                        Widget wconf = widgets.get(conflictIndex);
                        conflicts.add(wconf);
                    }

                }

                if (x2 < maxw) {
                    // MATE.log("y1: " + y1 + " y2: " + y2);
                    int count = 0;
                    for (int j = y1; j < y2; j++) {
                        //MATE.log("j: " + j);
                        //MATE.log("x2+1: " + String.valueOf(matrix[x2+1][j]));
                        if (matrix[x2 + 1][j] != -1) {
                            count++;
                            conflictIndex = matrix[x2 + 1][j];
                        }
                    }
                    if (count > (y2 - y1) / 2) {
                        Widget wconf = widgets.get(conflictIndex);
                        conflicts.add(wconf);
                    }
                }

                if (y1 > 0) {
                    int count = 0;
                    for (int j = x1; j < x2; j++) {

                        if (matrix[j][y1 - 1] != -1) {
                            count++;
                            conflictIndex = matrix[j][y1 - 1];
                        }
                    }
                    if (count > (x2 - x1) / 2) {
                        Widget wconf = widgets.get(conflictIndex);
                        conflicts.add(wconf);
                    }

                }

                if (y2 < maxh) {
                    int count = 0;
                    for (int j = x1; j < x2; j++) {
                        if (matrix[j][y2 + 1] != -1) {
                            count++;
                            conflictIndex = matrix[j][y2 + 1];
                        }
                    }
                    if (count > (x2 - x1) / 2) {
                        Widget wconf = widgets.get(conflictIndex);
                        conflicts.add(wconf);
                    }
                }
            }

            if (conflicts.size() > 0) {
                String extraInfo = "";
                for (Widget wconf : conflicts) {
                    extraInfo += wconf.getId() + " ";
                }
                return new AccessibilityViolation(AccessibilityViolationType.SPACING, widget, state, extraInfo);
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
