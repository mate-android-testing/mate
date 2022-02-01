package org.mate.accessibility.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.mate.commons.utils.MATELog;

import java.util.Hashtable;

/**
 * Created by marceloeler on 16/02/17.
 */
public class ColorUtils {


    public static double getRelativeLuminance(int colorNumber){


        double RsRGB = Math.abs((double)Color.red(colorNumber)/255);
        double GsRGB = Math.abs((double)Color.green(colorNumber)/255);
        double BsRGB = Math.abs((double)Color.blue(colorNumber)/255);


        double R, G, B;

        if (RsRGB <= 0.03928)
            R = RsRGB/12.92;
        else
            R = Math.pow(((RsRGB+0.055)/1.055),2.4);

        if (GsRGB <= 0.03928)
            G = GsRGB/12.92;
        else G = Math.pow(((GsRGB+0.055)/1.055), 2.4);

        if (BsRGB <= 0.03928)
            B = BsRGB/12.92;
        else B = Math.pow ( ((BsRGB+0.055)/1.055), 2.4);


        double L = 0.2126 * R + 0.7152 * G + 0.0722 * B;
        return L;
    }

    public static double gray(int color){
        return 0.2989 * Color.red(color) + 0.5870 * Color.green(color) + 0.1140 * Color.blue(color);

    }

    //public static double calculateContrastRatioForAreaOtsu(BufferedImage image,int x1, int y1, int x2, int y2){
    public static double calculateContrastRatioForAreaOtsu(Bitmap image, int x1, int y1, int x2, int y2){
        if (x1> image.getWidth()|| y1>image.getHeight()) {
            return 21;
        }
        if (x2-x1<=0&&y2-y1<=0) {
            return 21;
        }
        if (x1<0||x2<0||y1<0||y1<0) {
            return 21;
        }

        byte[] grayScaleValues = new byte[(y2-y1)*(x2-x1)];
        int index = 0;
        String colors="\n";
        for (int x=x1; x<x2; x++) {
            for (int y = y1; y < y2; y++) {
                colors+=image.getPixel(x,y)+" ";
                grayScaleValues[index] = (byte)gray(image.getPixel(x,y));
                index++;
            }
        }

        OtsuThresholder otsu = new OtsuThresholder();
        int o = otsu.doThreshold(grayScaleValues,null);

        Hashtable<Integer,Integer> freqHigh = new Hashtable<Integer, Integer>();
        Hashtable<Integer,Integer> freqLow= new Hashtable<Integer, Integer>();

        try {
            int i=0;
            double genMean=0;
            int cont=0;
            int xcolor = x1;
            int ycolor = y1;
            for (int x=x1; x<x2; x++){
                xcolor = x1+x;
                for (int y=y1; y<y2; y++){
                    ycolor = y1+y;
                    int grayValue = (int) gray(image.getPixel(x,y));
                    int rgbColor = image.getPixel(x,y);
                    if (grayValue < o){
                        if (freqLow.get(rgbColor)==null){
                            freqLow.put(rgbColor,1);
                        }
                        else{
                            int freq = freqLow.get(rgbColor);
                            freq+=1;
                            freqLow.put(rgbColor,freq);
                        }
                    }
                    else {
                        if (freqHigh.get(rgbColor) == null) {
                            freqHigh.put(rgbColor, 1);
                        } else {
                            int freq = freqHigh.get(rgbColor);
                            freq += 1;
                            freqHigh.put(rgbColor, freq);
                        }
                    }
                }
            }
        }
        catch(Exception ex){

            ex.printStackTrace();
        }

        int maxHigh = 0;
        int colorHigh = 0;

        for (Integer key: freqHigh.keySet()){
            int freq = freqHigh.get(key);
            if (freq > maxHigh) {
                maxHigh=freq;
                colorHigh=key;
            }
        }

        MATELog.log("MAX HIGH: " + maxHigh);
        MATELog.log("color HIGH: " + colorHigh);

        int maxLow = 0;
        int colorLow=0;
        for (Integer key: freqLow.keySet()){
            int freq = freqLow.get(key);
            if (freq>maxLow){
                maxLow = freqLow.get(key);
                colorLow = key;
            }
        }

        MATELog.log("MAX HIGH: " + maxLow);
        MATELog.log("color HIGH: " + colorLow);

        double lowLum = ColorUtils.getRelativeLuminance(colorLow);
        double highLum = ColorUtils.getRelativeLuminance(colorHigh);

        MATELog.log("low lum: " + lowLum);
        MATELog.log("high lum: " + highLum);

        return ColorUtils.getContrastRatio(highLum, lowLum);
    }

    public static double getContrastRatio(double l1, double l2){
        return (l1+0.05)/(l2+0.05);
    }
/*
    relative luminance
    the relative brightness of any point in a colorspace, normalized to 0 for darkest black and 1 for lightest white
    Note 1: For the sRGB colorspace, the relative luminance of a color is defined as L = 0.2126 * R + 0.7152 * G + 0.0722 * B where R, G and B are defined as:

            if RsRGB <= 0.03928 then R = RsRGB/12.92 else R = ((RsRGB+0.055)/1.055) ^ 2.4
            if GsRGB <= 0.03928 then G = GsRGB/12.92 else G = ((GsRGB+0.055)/1.055) ^ 2.4
            if BsRGB <= 0.03928 then B = BsRGB/12.92 else B = ((BsRGB+0.055)/1.055) ^ 2.4
    and RsRGB, GsRGB, and BsRGB are defined as:

    RsRGB = R8bit/255
    GsRGB = G8bit/255
    BsRGB = B8bit/255



    other: http://springmeier.org/www/contrastcalculator/index.php
    function adjustValue($val) {
// Parameter $val:
// Hexadecimal value of colour component (00-FF)

	$val = hexdec($val)/255;
	if ($val <= 0.03928) {
		$val = $val / 12.92;
	} else {
		$val = pow((($val + 0.055) / 1.055), 2.4);
	}
	return $val;
}

->  Luminance = (0.2126 × red) + (0.7152 × green) + (0.0722 × blue)


ratio

(L1 + 0.05) / (L2 + 0.05)
where L1 is the relative luminance of the lighter of the colors, and L2 is the relative luminance of the darker of the colors. Contrast ratios can range from 1 to 21 (commonly written 1:1 to 21:1). [w3.org] 21:1
*/

}
