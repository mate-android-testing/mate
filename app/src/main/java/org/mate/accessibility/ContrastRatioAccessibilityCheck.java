package org.mate.accessibility;

import android.graphics.Bitmap;

import org.mate.MATE;
import org.mate.exploration.random.UniformRandomForAccessibility;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by marceloeler on 26/06/17.
 */

public class ContrastRatioAccessibilityCheck implements IWidgetAccessibilityCheck{


    private String packageName;
    private String stateId;
    public double contratio;
    private String activity;
    int maxh;
    int maxw;
    boolean screenShot;

    public ContrastRatioAccessibilityCheck(String packageName, String activity, String stateId, int maxw, int maxh){
        contratio=21;
        this.packageName = packageName;
        this.stateId = stateId;
        this.maxw = maxw;
        this.maxh = maxh;
        this.activity=activity;
        screenShot=false;
    }

    @Override
    public boolean check(Widget widget) {

        String activityName = UniformRandomForAccessibility.currentActivityName;
        String widgetIdentifier = activityName + widget.getId()+"-"+widget.getText().isEmpty()+"-"+widget.getClazz()+ ":CONTRAST";
        //if (!MATE.checkedWidgets.contains(widgetIdentifier)){
          //  MATE.checkedWidgets.add(widgetIdentifier);

            //if (!screenShot) {
              //  EnvironmentManager.screenShot(packageName, stateId);
                //screenShot=true;
            //}


            if (!widget.needsContrastChecked())
                return true;

            contratio=21;
            double contrastRatio = EnvironmentManager.getContrastRatio(packageName,stateId,widget,maxw,maxh);
            //MATE.log("check contrast for "+widget.getId()+ " " + widget.getText()+ " = " + contrastRatio);
            contratio=contrastRatio;
            UniformRandomForAccessibility.totalNumberOfChecks++;
            //MATE.log("..contrast: " + contrastRatio);
            if (contrastRatio>=AccessibilitySettings.MIN_CONTRAST_RATIO)
                return true;
            return false;
       // }
        //else
          //  return true;
    }




    /*
    private Bitmap image;

    public ContrastRatioAccessibilityCheck(Bitmap image){
        this.image = image;
    }

    public boolean check(Widget widget){

        if (widget.isEditable()&&widget.getText().equals(""))
            return true;

        boolean contrastIssue=false;

        double contrastRatio = 21;
        //System.out.println("Widget: " + widget.getClazz()+ " - "+ widget.getId() + " "+ widget.getText() + " " + widget.getNextChildsText() + " "+widget.getBounds());
        if (image!=null && !widget.getBounds().equals("[0,0][0,0]"))
            contrastRatio=ColorUtils.calculateContrastRatioForAreaOtsu(image,widget.getX1(),widget.getY1(),widget.getX2(),widget.getY2());


        //int size = getSize();
        int size = 2;
        double minContrast=0;
        float dpiRatio = (float) AccessibilitySettings.DENSITY / 160;
        if (AccessibilitySettings.DENSITY!=0){
            int density=AccessibilitySettings.DENSITY;
            int width = widget.getX2()-widget.getX1();
            int height = widget.getY1()-widget.getY2();
            int targetHeight = (int) (Math.abs(height) / dpiRatio);
            int targetWidth = (int) (Math.abs(width) / dpiRatio);
            if (targetHeight <AccessibilitySettings.MIN_HEIGHT || targetWidth < AccessibilitySettings.MIN_WIDTH)
                size=1;
        }


        if (size==1)
            minContrast = AccessibilitySettings.minConstrastSmall;
        else
        if (size==2)
            minContrast = AccessibilitySettings.minContrastMed;
        else
            minContrast = AccessibilitySettings.minContrastLarge;

        if (contrastRatio<minContrast) {
            //System.out.println("issue  - size:  " + size);
            contrastIssue = true;
        }

        if (widget.getClazz().contains("Layout")){

            Vector<Widget> textChildren = widget.getNextChildWithText();
            for (Widget w: textChildren){
                ContrastRatioAccessibilityCheck checker = new ContrastRatioAccessibilityCheck(image);
                if (checker.check(w)==false){
                    contrastIssue=true;
                }
            }
        }
        MATE.log("...CONTRAST: " +contrastRatio);
        return !contrastIssue;
    }
    */
}
