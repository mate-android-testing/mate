package org.mate.ui;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.mate.MATE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by marceloeler on 12/05/17.
 */

public class EnvironmentManager {

    public static String SERVER_IP = "10.0.2.2";
    public static int port = 12345;
    //public static String SERVER_IP = "192.168.1.26";

    public static String emulator=null;
    public static final String ACTIVITY_UNKNOWN = "unknown";

    public static void releaseEmulator(){
        String cmd = "releaseEmulator:"+emulator;
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            String release="";
            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    release = serverResponse;
                    break;
                }
            }

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();

        }
    }

    public static String detectEmulator(String packageName){

        String cmd = "getEmulator:"+packageName;
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            String emulatorStr="";
            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    emulatorStr = serverResponse;
                    MATE.log(emulatorStr + " " + emulatorStr.length());
                    MATE.log("server response: " + emulatorStr);
                    break;
                }
            }

            if (!emulatorStr.equals(""))
                emulator=emulatorStr;

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();

        }
        if (emulator!=null)
            emulator=emulator.replace(" ","");

        return emulator;

    }

    public static long getTimeout(){
        long timeout=0;

        String cmd = "timeout";
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            String timeoutStr="";
            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    timeoutStr = serverResponse;
                    break;
                }
            }

            timeout = Long.valueOf(timeoutStr);

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
            timeout=0;
        }
        return timeout;
    }

    public static void copyCoverageData(Object source, Object target, List<? extends Object> entities){
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (Object entity : entities) {
            sb.append(prefix);
            prefix = ",";
            sb.append(entity.toString());
        }
        String cmd = "copyCoverageData:"+emulator+":"+source.toString()+":"+target.toString()
                +":"+sb.toString();
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    break;
                }
            }

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }
    }

    public static void storeCoverageData(Object o, Object o2){
        String cmd = "storeCoverageData:"+emulator+":"+o.toString();
        if (o2 != null) {
            cmd += ":" + o2.toString();
        }
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    break;
                }
            }

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }
    }


    public static String getCurrentActivityName(){
        String currentActivity = "current_activity";
        if (emulator == null || emulator.isEmpty()) {
            return ACTIVITY_UNKNOWN;
        }

        //String cmd = "adb shell dumpsys activity activities | grep mFocusedActivity | cut -d \" \" -f 6 | cut -d / -f 2";
        String cmd = "getActivity:"+emulator;
        //String cmd = "adb -s " + emulator+" shell dumpsys activity activities | grep mFocusedActivity | cut -d \" \" -f 6";
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    currentActivity = serverResponse;
                    break;
                }
            }

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }

        return currentActivity;
    }

    public static List<String> getActivityNames() {
        List<String> activities = new ArrayList<>();

        //String cmd = "adb shell dumpsys activity activities | grep mFocusedActivity | cut -d \" \" -f 6 | cut -d / -f 2";
        String cmd = "getActivities:"+emulator;
        //String cmd = "adb -s " + emulator+" shell dumpsys activity activities | grep mFocusedActivity | cut -d \" \" -f 6";
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                activities.add(line);
            }

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }

        return activities;
    }

    /**
     * API level 23 and higher requires that permissions are also granted
     * at runtime. This can be done at install time with the flag -g,
     * i.e. adb install -g apk, or via adb shell pm grant packageName permission.
     * However, the intermediate reset/restart of the app causes the
     * loss of those runtime permissions.
     *
     * @param packageName The app that requires the permissions.
     * @return Returns {@code true} if the granting permissions succeeded,
     *          otherwise {@code false}.
     */
    public static boolean grantRuntimePermissions(String packageName) {

        String cmd = "grantPermissions:" + packageName + ":" + emulator;

        boolean granted = false;

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    granted = Boolean.parseBoolean(serverResponse);
                    break;
                }
            }

            server.close();
            output.close();
            in.close();
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }
        return granted;
    }

    /**
     * Initializes the CFG; the path to the APK file is given
     * as command line argument (key: apk). If no argument was specified,
     * {@code false} is returned.
     *
     * @return Returns whether the CFG can be initialised.
     */
    public static boolean initCFG() {

        Bundle arguments = InstrumentationRegistry.getArguments();
        String apkPath = arguments.getString("apk");
        String packageName = arguments.getString("packageName");
        MATE.log("Path to APK file: " + apkPath);

        boolean isInit = false;

        if (apkPath != null && packageName != null) {
            String cmd = "initCFG:" + packageName + ":" + apkPath;

            try {
                Socket server = new Socket(SERVER_IP, port);
                PrintStream output = new PrintStream(server.getOutputStream());
                output.println(cmd);

                String serverResponse="";
                BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
                while(true) {
                    if ((serverResponse = in.readLine()) != null) {
                        isInit = Boolean.parseBoolean(serverResponse);
                        break;
                    }
                }

                server.close();
                output.close();
                in.close();
            } catch (IOException e) {
                MATE.log("socket error sending");
                e.printStackTrace();
            }
        }
        return isInit;
    }

    /**
     * Returns the list of branches of the AUT.
     *
     * @return Returns the set of branches.
     */
    public static List<String> getBranches() {

        List<String> branches = new LinkedList<>();

        String cmd = "getBranches";
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                branches.add(line);
            }

            server.close();
            output.close();
            in.close();
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
            throw new IllegalStateException("Couldn't retrieve branches!");
        }
        return branches;
    }

    public static List<String> getSourceLines() {
        List<String> lines = new ArrayList<>();

        String cmd = "getSourceLines:"+emulator;
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                lines.add(line);
            }

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }

        return lines;
    }

    public static double getCombinedCoverage(){
        String cmd = "getCombinedCoverage:"+emulator;

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            String coverageString;

            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    coverageString = serverResponse;
                    break;
                }
            }

            server.close();
            output.close();
            in.close();

            return Double.valueOf(coverageString);
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }

        throw new IllegalStateException("Coverage could not be retrieved");
    }

    public static double getCombinedCoverage(List<? extends Object> os){
        StringBuilder sb = new StringBuilder();
        sb.append("getCombinedCoverage:"+emulator + ":");
        for (Object o : os) {
            sb.append(o);
            sb.append("+");
        }
        sb.setLength(sb.length() - 1);
        String cmd = sb.toString();

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            String coverageString;

            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    coverageString = serverResponse;
                    break;
                }
            }

            server.close();
            output.close();
            in.close();

            return Double.valueOf(coverageString);
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }

        throw new IllegalStateException("Coverage could not be retrieved");
    }

    public static double getCoverage(Object o){
        String cmd = "getCoverage:"+emulator+":"+o.toString();

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            String coverageString;

            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    coverageString = serverResponse;
                    break;
                }
            }

            server.close();
            output.close();
            in.close();

            return Double.valueOf(coverageString);
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }

        throw new IllegalStateException("Coverage could not be retrieved");
    }

    /**
     * Stores the branch coverage information of the given test case.
     *
     * @param chromosome The given test case.
     */
    public static void storeBranchCoverage(Object chromosome) {
        storeBranchCoverage("storeBranchCoverage:" + chromosome.toString());
    }

    /**
     * Stores the total branch coverage information.
     */
    public static void storeBranchCoverage() {
        storeBranchCoverage("storeBranchCoverage");
    }

    /**
     * Stores the obtained branch coverage information. Depending on the given
     * command string, either the total branch coverage is stored or only the
     * branch coverage of a single test case.
     *
     * @param cmd The command string specifying which branch coverage
     *            should be stored.
     */
    private static void storeBranchCoverage(String cmd) {

        cmd += ":" + emulator;

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            server.close();
            output.close();
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
            throw new IllegalStateException("Couldn't store branch coverage information!");
        }
    }

    /**
     * Returns the branch coverage.
     *
     * @param cmd The command string specifying the branch coverage
     *            either for a given test case or the (global) branch coverage.
     * @return Returns the branch coverage.
     */
    private static double getBranchCoverage(String cmd) {

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            String coverageString;

            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    coverageString = serverResponse;
                    break;
                }
            }

            server.close();
            output.close();
            in.close();

            return Double.valueOf(coverageString);
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
            throw new IllegalStateException("BranchCoverage could not be retrieved!");
        }
    }

    /**
     * Returns the (global) branch coverage.
     *
     * @return Returns the (global) branch coverage.
     */
    public static double getBranchCoverage() {
        String cmd = "getBranchCoverage";
        return getBranchCoverage(cmd);
    }

    /**
     * Returns the branch coverage of a given test case.
     *
     * @param chromosome The given test case.
     * @return Returns the branch coverage for the specified test case.
     */
    public static double getBranchCoverage(Object chromosome) {
        String cmd = "getBranchCoverage" + ":" + chromosome.toString();
        return getBranchCoverage(cmd);
    }

    /**
     * Returns the branch distance for a given test case.
     *
     * @param chromosome The given test case.
     * @return Returns the branch distance for the given test case.
     */
    public static double getBranchDistance(Object chromosome) {

        String cmd = "getBranchDistance:"+emulator+":"+chromosome.toString();
        double branchDistance = 0;

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                try {
                    branchDistance = Double.valueOf(line);
                    break;
                } catch (NumberFormatException e) {
                    MATE.log_acc("Branch Distance Response: " + line);
                    e.printStackTrace();
                }
            }

            server.close();
            output.close();
            in.close();

            return branchDistance;
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
            throw new IllegalStateException("Couldn't retrieve branch distance!");
        }
    }

    /**
     * Computes the branch distance fitness vector for a given test case (chromosome).
     * In particular, the given test case is evaluated against each branch.
     *
     * @param chromosome The given test case.
     * @return Returns the branch distance vector for a given test case.
     */
    public static List<Double> getBranchDistanceVector(Object chromosome) {

        String cmd = "getBranchDistanceVector:"+emulator+":"+chromosome.toString();
        List<Double> branchDistanceVector = new ArrayList<>();

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                try {
                    branchDistanceVector.add(Double.valueOf(line));
                } catch (NumberFormatException e) {
                    MATE.log_acc("Branch Distance Response: " + line);
                    e.printStackTrace();
                }
            }

            server.close();
            output.close();
            in.close();

            return branchDistanceVector;
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
            throw new IllegalStateException("Couldn't retrieve branch distance vector!");
        }
    }

    public static List<Double> getLineCoveredPercentage(Object o, List<String> lines){
        StringBuilder sb = new StringBuilder();
        sb.append("getLineCoveredPercentage:"+emulator+":"+o.toString()+":");
        for (String line : lines) {
            sb.append(line);
            sb.append("*");
        }
        if (!lines.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }

        String cmd = sb.toString();
        List<Double> coveredPercentage = new ArrayList<>();

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                coveredPercentage.add(Double.valueOf(line));
            }

            server.close();
            output.close();
            in.close();

            return coveredPercentage;
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }

        throw new IllegalStateException("Coverage could not be retrieved");
    }

    public static void screenShot(String packageName,String nodeId){

        String cmd = "screenshot:"+emulator+":"+emulator+"_"+packageName+"_"+nodeId+".png";

        sendCommandToServer(cmd);
    }

    public static void clearAppData() {
        String cmd = "clearApp:" + emulator;

        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);

            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
            }

            server.close();
            output.close();
            in.close();
        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
        }
    }

    public static double getContrastRatio(String packageName, String stateId, Widget widget){
        int maxw = MATE.device.getDisplayWidth();
        int maxh = MATE.device.getDisplayHeight();
        double contrastRatio = 21;
        try {
            Socket cliente = new Socket(SERVER_IP, port);
            PrintStream saida = new PrintStream(cliente.getOutputStream());

            String cmd = "contrastratio:";
            cmd+=emulator+"_"+packageName+":";
            cmd+=stateId+":";
            int x1=widget.getX1();
            int x2=widget.getX2();
            int y1=widget.getY1();
            int y2=widget.getY2();
            int borderExpanded=1;
            if (x1-borderExpanded>=0)
                x1-=borderExpanded;
            if (x2+borderExpanded<=maxw)
                x2+=borderExpanded;
            if (y1-borderExpanded>=0)
                y1-=borderExpanded;
            if (y2+borderExpanded<=maxh)
                y2+=borderExpanded;
            cmd+=x1+","+y1+","+x2+","+y2;

            saida.println(cmd);
            MATE.log(cmd);
            MATE.log(widget.getClazz()+ " - " + widget.getId() + " - " + widget.getText() + " - vis:" + widget.isVisibleToUser() + " - foc: " +widget.isFocusable());

            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            long ta = new Date().getTime();
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    contrastRatio = Double.valueOf(serverResponse);
                    break;
                }
                long tb = new Date().getTime();
                if (tb-ta>5000) {
                    MATE.logsum("timeout - contrast");
                    break;
                }
            }
            cliente.close();
            saida.close();
            MATE.log(serverResponse);
        } catch (IOException e) {
            MATE.log_acc("socket error: contrast");
            e.printStackTrace();
        }

        return contrastRatio;
    }


    public static String getLuminances(String packageName, String stateId, Widget widget){
        int maxw = MATE.device.getDisplayWidth();
        int maxh = MATE.device.getDisplayHeight();
        String luminances = "0,0";
        try {
            Socket cliente = new Socket(SERVER_IP, port);
            PrintStream saida = new PrintStream(cliente.getOutputStream());

            String cmd = "luminance:";
            cmd+=emulator+"_"+packageName+":";
            cmd+=stateId+":";
            int x1=widget.getX1();
            int x2=widget.getX2();
            int y1=widget.getY1();
            int y2=widget.getY2();
            int borderExpanded=1;
            if (x1-borderExpanded>=0)
                x1-=borderExpanded;
            if (x2+borderExpanded<=maxw)
                x2+=borderExpanded;
            if (y1-borderExpanded>=0)
                y1-=borderExpanded;
            if (y2+borderExpanded<=maxh)
                y2+=borderExpanded;
            cmd+=x1+","+y1+","+x2+","+y2;

            saida.println(cmd);
            MATE.log(cmd);
            MATE.log(widget.getClazz()+ " - " + widget.getId() + " - " + widget.getText() + " - vis:" + widget.isVisibleToUser() + " - foc: " +widget.isFocusable());

            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            long ta = new Date().getTime();
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    luminances = serverResponse;
                    break;
                }
                long tb = new Date().getTime();
                if (tb-ta>5000) {
                    MATE.logsum("timeout - luminance");
                    break;
                }
            }
            cliente.close();
            saida.close();
            MATE.log(serverResponse);
        } catch (IOException e) {
            MATE.log_acc("socket error: luminance");
            e.printStackTrace();
        }

        return luminances;
    }

    public static void deleteAllScreenShots(String packageName) {
        MATE.log("DELETE SCREENSHOTS");
        try {
            Socket cliente = new Socket(SERVER_IP, port);
            PrintStream saida = new PrintStream(cliente.getOutputStream());
            saida.println("rm "+emulator+"_"+packageName+"*.png");

            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    break;
                }
            }
            cliente.close();
            saida.close();
        } catch (IOException e) {
            MATE.log_acc("socket error: delete png");
            e.printStackTrace();
        }
    }

    public static long getRandomLength(){
        long length = 0;

        String cmd = "randomlength";
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            String randomLengthStr="";
            String serverResponse="";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    randomLengthStr = serverResponse;
                    break;
                }
            }

            length = Long.valueOf(randomLengthStr);

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log("socket error sending");
            e.printStackTrace();
            length=0;
        }
        return length;
    }

    public static void markScreenshot(final Widget widget, final String packageName,
                                      final String nodeId, final String flawDescription,
                                      final String extraInfo) {

        final String imageName = emulator +  "_" + packageName + "_" +
                String.valueOf(Math.abs(System.currentTimeMillis())) +".png";

        String cmd = "screenshot:"+emulator+":"+imageName;

        //for now I'm keeping this command commented so it can speed up the process
        //sendCommandToServer(cmd);

        cmd = "mark-image:" + imageName + ":x-" + widget.getX1() + ":y-"
                + widget.getY1() + ":width-" + (widget.getX2() - widget.getX1())
                + ":heigth-" + (widget.getY2() - widget.getY1()) + ":" + flawDescription + ":" + extraInfo;

        //for now I'm keeping this command commented so it can speed up the process
        //sendCommandToServer(cmd);
    }

    public static void sendCommandToServer(String cmd) {
        String response;
        MATE.log(cmd);
        try {
            Socket server = new Socket(SERVER_IP, port);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            String serverResponse = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            while(true) {
                if ((serverResponse = in.readLine()) != null) {
                    response = serverResponse;
                    MATE.log("screenshot response: " + response);
                    break;
                }
            }

            server.close();
            output.close();
            in.close();

        } catch (IOException e) {
            MATE.log_acc("socket error sending: screenshot");
            e.printStackTrace();
        }
    }

    public static void sendFlawToServer(String msg) {
        String cmd = "reportFlaw:"+emulator+":"+msg;
        MATE.log("reportFlaw: " + cmd);
        sendCommandToServer(cmd);
    }
}
