package org.mate.accessibility;

import org.mate.MATE;
import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/06/17.
 */

public class ContentDescAccessibilityCheck implements IWidgetAccessibilityCheck{

    @Override
    public boolean check(Widget widget) {

        boolean contentDesc=true;
        if (widget.getClazz().contains("Image")&&widget.isExecutable()){
            if (widget.getText().equals("")&&widget.getContentDesc().equals("")){
                MATE.log("1 - " + widget.getId()+ " - cd false");
                contentDesc=false;
            }
        }
        else{
            if (widget.isEditable()){
                if (widget.getText().equals("")){
                    MATE.log("2 - " + widget.getId()+ " - cd false");
                    contentDesc=false;
                }

                if (widget.getContentDesc().equals("")) {
                    MATE.log("3 - " + widget.getId()+ " - cd false");
                    contentDesc=false;
                }
            }
            else{
                if (widget.getClazz().contains("Layout")&&widget.isExecutable()){
                    String childText = "";
                    for (Widget wg: widget.getNextChildWithText())
                        childText+=wg.getText()+" ";

                    String childContentDesc = "";
                    for (Widget wg: widget.getNextChildWithDescContentText())
                        childContentDesc+=wg.getText()+" ";

                    if (childText.equals("")&&childContentDesc.equals("")) {
                        MATE.log("4 - " + widget.getId()+ " - cd false");
                        contentDesc = false;
                    }
                }

                if (widget.getClazz().contains("TextView") && widget.isClickable() && widget.getText().equals("") && widget.getContentDesc().equals("")){
                    MATE.log("5 - " + widget.getId()+ " - cd false");
                   contentDesc=false;
                }

            }
        }
        return contentDesc;
    }
}
