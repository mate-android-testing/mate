package org.mate.ui;

import org.mate.MATE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by marceloeler on 12/05/17.
 */

public class EnvironmentManager {

    public static String SERVER_IP = "10.0.2.2";
    public static int port = 12345;
    //public static String SERVER_IP = "192.168.1.26";

    public static String emulator=null;

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

    public static String getCurrentActivityName(){
        String currentActivity = "current_activity";

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

    public static double getCoverage(){
        String cmd = "getCoverage:"+emulator;

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

    public static void screenShot(String packageName,String nodeId){

        String cmd = "screenshot:"+emulator+":"+emulator+"_"+packageName+"_"+nodeId+".png";

        sendCommandToServer(cmd);
    }

    public static void clearAppData(String packageName) {
        try {
            Socket cliente = new Socket(SERVER_IP, port);
            PrintStream saida = new PrintStream(cliente.getOutputStream());
            saida.println("adb -s "+emulator+" shell pm clear "+packageName);

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
            MATE.log_acc("socket error: clear");
            e.printStackTrace();
        }
    }

    public static double getContrastRatio(String packageName, String stateId, Widget widget, int maxw, int maxh){
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
            int borderExpanded=5;
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
        } catch (IOException e) {
            MATE.log_acc("socket error: contrast");
            e.printStackTrace();
        }

        return contrastRatio;
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
}
