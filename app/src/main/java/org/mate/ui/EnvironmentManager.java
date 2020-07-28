package org.mate.ui;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.mate.MATE;
import org.mate.message.Message;
import org.mate.message.serialization.Parser;
import org.mate.message.serialization.Serializer;
import org.mate.utils.TimeoutRun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EnvironmentManager {
    public static final String ACTIVITY_UNKNOWN = "unknown";
    private static final String DEFAULT_SERVER_IP = "10.0.2.2";
    private static final int DEFAULT_PORT = 12345;
    //private static final String DEFAULT_SERVER_IP = "192.168.1.26";
    private static final String METADATA_PREFIX = "__meta__";
    private static final String MESSAGE_PROTOCOL_VERSION = "1.2";
    private static final String MESSAGE_PROTOCOL_VERSION_KEY = "version";

    private String emulator = null;
    private final Socket server;
    private final Parser messageParser;
    private boolean active;

    public EnvironmentManager() throws IOException {
        this(DEFAULT_PORT);
    }

    public EnvironmentManager(int port) throws IOException {
        active = true;
        server = new Socket(DEFAULT_SERVER_IP, port);
        messageParser = new Parser(server.getInputStream());
    }

    public boolean isActive(){
        return active;
    }

    public void close() throws IOException {
        sendMessage(new Message("/close"));
        active = false;
        server.close();
    }

    public String getEmulator() {
        return emulator;
    }

    /**
     * Send a {@link org.mate.message.Message} to the server and return the response of the server
     * @param message {@link org.mate.message.Message} that will be send to the server
     * @return Response {@link org.mate.message.Message} of the server
     */
    public Message sendMessage(Message message) {
        if (!active) {
            throw new IllegalStateException("EnvironmentManager is no longer active and can not be used for communication!");
        }
        addMetadata(message);

        TimeoutRun.maskInterrupt();
        try {
            server.getOutputStream().write(Serializer.serialize(message));
            server.getOutputStream().flush();
        } catch (IOException e) {
            TimeoutRun.unmaskInterrupt();
            active = false;
            MATE.log("socket error sending");
            throw new IllegalStateException(e);
        } finally {
            TimeoutRun.unmaskInterrupt();
        }

        TimeoutRun.maskInterrupt();
        Message response = messageParser.nextMessage();
        TimeoutRun.unmaskInterrupt();

        verifyMetadata(response);
        if (response.getSubject().equals("/error")) {
            MATE.log("Received error message from mate-server: "
                    + response.getParameter("info"));
            return null;
        }
        stripMetadata(response);
        return response;
    }

    public String tunnelLegacyCmdX(String cmd){
        String response = "";
        try {

            Socket server = new Socket("10.0.2.2", 12345);
            PrintStream output = new PrintStream(server.getOutputStream());
            output.println(cmd);


            String serverResponse="";
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
            MATE.log("socket error sending");
            e.printStackTrace();
        }
        return response;
    }

    public String tunnelLegacyCmd(String cmd) {
        Message message = new Message.MessageBuilder("/legacy")
                .withParameter("cmd", cmd)
                .build();
        //MATE.log("CMD: " + message.getParameter("cmd"));
        Message response = sendMessage(message);
        //MATE.log(response.getParameter("response"));
        if (!response.getSubject().equals("/legacy")) {
            StringBuilder sb = new StringBuilder("Received unexpected message with subject: <");
            sb.append(response.getSubject());
            for (Map.Entry<String, String> parameterEntry : response.getParameters().entrySet()) {
                sb.append(">\n\tand parameter with key <")
                        .append(parameterEntry.getKey())
                        .append("> and value <")
                        .append(parameterEntry.getValue());
            }
            sb.append(">");
            throw new IllegalStateException(sb.toString());
        }
        return response.getParameter("response");
    }

    private void addMetadata(Message message) {
        message.addParameter(
                METADATA_PREFIX + MESSAGE_PROTOCOL_VERSION_KEY, MESSAGE_PROTOCOL_VERSION);
    }

    private void stripMetadata(Message message) {
        List<String> metadataKeys = new ArrayList<>();
        Map<String, String> parameters = message.getParameters();
        for (String parameterKey: parameters.keySet()) {
            if (parameterKey.startsWith(METADATA_PREFIX)) {
                metadataKeys.add(parameterKey);
            }
        }
        for (String metadataKey : metadataKeys) {
            parameters.remove(metadataKey);
        }
    }

    private void verifyMetadata(Message message) {
        String protocolVersion = message.getParameter(
                METADATA_PREFIX + MESSAGE_PROTOCOL_VERSION_KEY);
        if (!protocolVersion.equals(MESSAGE_PROTOCOL_VERSION)) {
            MATE.log(
                    "WARNING: Message protocol version used by MATE ("
                            + MESSAGE_PROTOCOL_VERSION
                            + ") does not match with the version used by MATE-Server ("
                            + protocolVersion
                            + ")");
        }
    }

    public void releaseEmulator() {
        String cmd = "releaseEmulator:" + emulator;
        tunnelLegacyCmd(cmd);
    }

    public void setEmulator(String emulatorID){
        this.emulator = emulatorID;
    }

    public String detectEmulator(String packageName) {
        String cmd = "getEmulator:" + packageName;
        String response = tunnelLegacyCmd(cmd);
        if (response != null && !response.isEmpty()) {
            emulator = response;
        }
        if (emulator != null)
            emulator = emulator.replace(" ", "");
        return emulator;
    }

    public long getTimeout() {
        String cmd = "timeout";
        return Long.valueOf(tunnelLegacyCmd(cmd));
    }

    public void copyCoverageData(Object source, Object target, List<? extends Object> entities) {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (Object entity : entities) {
            sb.append(prefix);
            prefix = ",";
            sb.append(entity.toString());
        }
        String cmd = "copyCoverageData:" + emulator + ":" + source.toString() + ":" + target.toString()
                + ":" + sb.toString();
        tunnelLegacyCmd(cmd);
    }

    public void storeCoverageData(Object o, Object o2) {
        String cmd = "storeCoverageData:" + emulator + ":" + o.toString();
        if (o2 != null) {
            cmd += ":" + o2.toString();
        }
        tunnelLegacyCmd(cmd);
    }


    public String getCurrentActivityName() {
        String currentActivity = "current_activity";
        if (emulator == null || emulator.isEmpty()) {
            return ACTIVITY_UNKNOWN;
        }

        String cmd = "getActivity:" + emulator;
        return tunnelLegacyCmd(cmd);
    }

    public List<String> getActivityNames() {
        List<String> activities = new ArrayList<>();

        String cmd = "getActivities:" + emulator;
        return Arrays.asList(tunnelLegacyCmd(cmd).split("\n"));
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
     * otherwise {@code false}.
     */
    public boolean grantRuntimePermissions(String packageName) {
        String cmd = "grantPermissions:" + packageName + ":" + emulator;

        return Boolean.parseBoolean(tunnelLegacyCmd(cmd));
    }

    /**
     * Initializes the CFG; the path to the APK file is given
     * as command line argument (key: apk). If no argument was specified,
     * {@code false} is returned.
     *
     * @return Returns whether the CFG can be initialised.
     */
    public boolean initCFG() {

        Bundle arguments = InstrumentationRegistry.getArguments();
        String apkPath = arguments.getString("apk");
        String packageName = arguments.getString("packageName");
        //MATE.log("Path to APK file: " + apkPath);

        boolean isInit = false;

        if (apkPath != null && packageName != null) {
            String cmd = "initCFG:" + packageName + ":" + apkPath;

            isInit = Boolean.parseBoolean(tunnelLegacyCmd(cmd));
        }
        return isInit;
    }

    /**
     * Returns the list of branches of the AUT.
     *
     * @return Returns the set of branches.
     */
    public List<String> getBranches() {
        String cmd = "getBranches";
        return Arrays.asList(tunnelLegacyCmd(cmd).split("\n"));
    }

    public List<String> getSourceLines() {
        List<String> lines = new ArrayList<>();

        String cmd = "getSourceLines:" + emulator;
        return Arrays.asList(tunnelLegacyCmd(cmd).split("\n"));
    }

    public double getCombinedCoverage() {
        String cmd = "getCombinedCoverage:" + emulator;

        return Double.valueOf(tunnelLegacyCmd(cmd));
    }

    public double getCombinedCoverage(List<? extends Object> os) {
        StringBuilder sb = new StringBuilder();
        sb.append("getCombinedCoverage:" + emulator + ":");
        for (Object o : os) {
            sb.append(o);
            sb.append("+");
        }
        sb.setLength(sb.length() - 1);
        String cmd = sb.toString();

        return Double.valueOf(tunnelLegacyCmd(cmd));
    }

    public double getCoverage(Object o) {
        String cmd = "getCoverage:" + emulator + ":" + o.toString();

        return Double.valueOf(tunnelLegacyCmd(cmd));
    }

    /**
     * Stores the branch coverage information of the given test case.
     *
     * @param chromosome The given test case.
     */
    public void storeBranchCoverage(Object chromosome) {
        storeBranchCoverage("storeBranchCoverage:" + chromosome.toString());
    }

    /**
     * Stores the total branch coverage information.
     */
    public void storeBranchCoverage() {
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
    private void storeBranchCoverage(String cmd) {
        cmd += ":" + emulator;
        tunnelLegacyCmd(cmd);
    }

    /**
     * Returns the branch coverage.
     *
     * @param cmd The command string specifying the branch coverage
     *            either for a given test case or the (global) branch coverage.
     * @return Returns the branch coverage.
     */
    private double getBranchCoverage(String cmd) {
        return Double.valueOf(tunnelLegacyCmd(cmd));
    }

    /**
     * Returns the (global) branch coverage.
     *
     * @return Returns the (global) branch coverage.
     */
    public double getBranchCoverage() {
        String cmd = "getBranchCoverage";
        return getBranchCoverage(cmd);
    }

    /**
     * Returns the branch coverage of a given test case.
     *
     * @param chromosome The given test case.
     * @return Returns the branch coverage for the specified test case.
     */
    public double getBranchCoverage(Object chromosome) {
        String cmd = "getBranchCoverage" + ":" + chromosome.toString();
        return getBranchCoverage(cmd);
    }

    /**
     * Returns the branch distance for a given test case.
     *
     * @param chromosome The given test case.
     * @return Returns the branch distance for the given test case.
     */
    public double getBranchDistance(Object chromosome) {
        String cmd = "getBranchDistance:" + emulator + ":" + chromosome.toString();
        return Double.valueOf(tunnelLegacyCmd(cmd));
    }

    /**
     * Computes the branch distance fitness vector for a given test case (chromosome).
     * In particular, the given test case is evaluated against each branch.
     *
     * @param chromosome The given test case.
     * @return Returns the branch distance vector for a given test case.
     */
    public List<Double> getBranchDistanceVector(Object chromosome) {
        String cmd = "getBranchDistanceVector:" + emulator + ":" + chromosome.toString();
        List<Double> branchDistanceVectors = new ArrayList<>();
        for (String branchDistanceVectorStr : tunnelLegacyCmd(cmd).split("\n")) {
            branchDistanceVectors.add(Double.valueOf(branchDistanceVectorStr));
        }
        return branchDistanceVectors;
    }

    public List<Double> getLineCoveredPercentage(Object o, List<String> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("getLineCoveredPercentage:" + emulator + ":" + o.toString() + ":");
        for (String line : lines) {
            sb.append(line);
            sb.append("*");
        }
        if (!lines.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }

        String cmd = sb.toString();
        List<Double> coveredPercentages = new ArrayList<>();
        for (String coveredPercentageStr : tunnelLegacyCmd(cmd).split("\n")) {
            coveredPercentages.add(Double.valueOf(coveredPercentageStr));
        }

        return coveredPercentages;
    }

    public String getLastCrashStackTrace() {
        return sendMessage(new Message.MessageBuilder("/crash/stacktrace")
                .withParameter("deviceId", emulator)
                .build()).getParameter("stacktrace");
    }

    public Map<String, String> getProperties() {
        return sendMessage(new Message("/properties")).getParameters();
    }

    public void screenShot(String packageName, String nodeId) {
        packageName = packageName.replace("_","-");
        String cmd = "screenshot:" + emulator + ":" + emulator + "_" + packageName + "_" + nodeId + ".png";
        tunnelLegacyCmd(cmd);
    }

    public void screenShotForFlickerDetection(String packageName,String nodeId){
        packageName = packageName.replace("_","-");
        String cmd = "flickerScreenshot:"+emulator+":"+emulator+"_"+packageName+"_"+nodeId+".png";
        tunnelLegacyCmd(cmd);
    }

    public void clearAppData() {
        String cmd = "clearApp:" + emulator;
        tunnelLegacyCmd(cmd);
    }

    public double matchesSurroundingColor(String packageName, String stateId, Widget widget){
        packageName = packageName.replace("_","-");
        String cmd = "surroundingColor:";
        cmd += emulator + "_" + packageName + ":";
        cmd += stateId + ":";
        cmd += widget.getX1() + "," + widget.getY1() + "," + widget.getX2() + "," + widget.getY2();

        Message message = new Message("/accessibility");
        message.addParameter("cmd",cmd);

        //will break commands in several parameters-values in the future.
        //For now I'm sending the whole command with parameters in one single string
        String response = sendMessage(message).getParameter("response");
        return Double.valueOf(response);
    }

    public double getContrastRatio(String packageName, String stateId, Widget widget) {
        packageName = packageName.replace("_","-");
        int maxw = MATE.device.getDisplayWidth();
        int maxh = MATE.device.getDisplayHeight();
        String cmd = "contrastratio:";
        cmd += emulator + "_" + packageName + ":";
        cmd += stateId + ":";
        int x1 = widget.getX1();
        int x2 = widget.getX2();
        int y1 = widget.getY1();
        int y2 = widget.getY2();
        int borderExpanded = 0;
        if (x1 - borderExpanded >= 0)
            x1 -= borderExpanded;
        if (x2 + borderExpanded <= maxw)
            x2 += borderExpanded;
        if (y1 - borderExpanded >= 0)
            y1 -= borderExpanded;
        if (y2 + borderExpanded <= maxh)
            y2 += borderExpanded;
        cmd += x1 + "," + y1 + "," + x2 + "," + y2;

        return Double.valueOf(tunnelLegacyCmd(cmd));
    }

    public String getLuminances(String packageName, String stateId, Widget widget){
        packageName = packageName.replace("_","-");
        int maxw = MATE.device.getDisplayWidth();
        int maxh = MATE.device.getDisplayHeight();
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

        //MATE.log(cmd);
        //MATE.log(widget.getClazz()+ " - " + widget.getId() + " - " + widget.getText() + " - vis:" + widget.isVisibleToUser() + " - foc: " +widget.isFocusable());
        return tunnelLegacyCmd(cmd);
    }

    public long getRandomLength() {
        String cmd = "randomlength";
        return Long.valueOf(tunnelLegacyCmd(cmd));
    }




    public void sendFlawToServer(String msg) {
        String cmd = "reportFlaw:" + emulator + ":" + msg;
        tunnelLegacyCmd(cmd);
    }

    public void generalReport(String header, String values) {
        String cmd = "reportFlaw"+"@"+emulator+"@"+header+"@"+values;
        tunnelLegacyCmd(cmd);
    }

    public void measureReport(String header, String values) {
        String cmd = "reportFlaw"+"%"+emulator+"%"+header+"%"+values;
        tunnelLegacyCmd(cmd);
    }
}
