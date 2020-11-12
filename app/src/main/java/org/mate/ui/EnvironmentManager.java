package org.mate.ui;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.graph.GraphType;
import org.mate.message.Message;
import org.mate.message.serialization.Parser;
import org.mate.message.serialization.Serializer;
import org.mate.utils.Coverage;
import org.mate.utils.Objective;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EnvironmentManager {
    public static final String ACTIVITY_UNKNOWN = "unknown";
    private static final String DEFAULT_SERVER_IP = "10.0.2.2";
    private static final int DEFAULT_PORT = 12345;
    //private static final String DEFAULT_SERVER_IP = "192.168.1.26";
    private static final String METADATA_PREFIX = "__meta__";
    private static final String MESSAGE_PROTOCOL_VERSION = "1.8";
    private static final String MESSAGE_PROTOCOL_VERSION_KEY = "version";

    private String emulator = null;
    private final Socket server;
    private final Parser messageParser;
    private boolean active;

    /**
     * Tracks for which test case the pulling of traces files have been already performed.
     * This is necessary that BranchDistance and BranchCoverage don't try to fetch for the same
     * test case the traces file multiple times. Otherwise, the last fetch trial overwrites
     * the traces file for the given test case with an empty file.
     */
    private Set<String> coveredTestCases = new HashSet<>();

    public EnvironmentManager() throws IOException {
        this(DEFAULT_PORT);
    }

    public EnvironmentManager(int port) throws IOException {
        active = true;
        server = new Socket(DEFAULT_SERVER_IP, port);
        messageParser = new Parser(server.getInputStream());
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
     *
     * @param message {@link org.mate.message.Message} that will be send to the server
     * @return Response {@link org.mate.message.Message} of the server
     */
    public synchronized Message sendMessage(Message message) {
        if (!active) {
            throw new IllegalStateException("EnvironmentManager is no longer active and can not be used for communication!");
        }
        addMetadata(message);

        try {
            server.getOutputStream().write(Serializer.serialize(message));
            server.getOutputStream().flush();
        } catch (IOException e) {
            MATE.log("socket error sending");
            throw new IllegalStateException(e);
        }

        Message response = messageParser.nextMessage();

        verifyMetadata(response);
        if (response.getSubject().equals("/error")) {
            MATE.log("Received error message from mate-server: "
                    + response.getParameter("info"));
            return null;
        }
        stripMetadata(response);
        return response;
    }

    public String tunnelLegacyCmd(String cmd) {
        Message message = new Message.MessageBuilder("/legacy")
                .withParameter("cmd", cmd)
                .build();
        Message response = sendMessage(message);
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
        for (String parameterKey : parameters.keySet()) {
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
        long timeout = 0;
        String cmd = "timeout";
        return Long.valueOf(tunnelLegacyCmd(cmd));
    }

    // TODO: use new coverage endpoint
    @Deprecated
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
     * Returns the list of objectives based on objective property.
     *
     * @param objective The specified objective property.
     * @return Returns a list of objectives, e.g. the list of branches.
     */
    public List<String> getObjectives(Objective objective) {
        if (objective == null) {
            throw new IllegalStateException("Objective property not defined!");
        }

        MATE.log_acc("Getting objectives...!");

        if (objective == Objective.LINES) {
            return getSourceLines();
        } else if (objective == Objective.BRANCHES) {
            return getBranches();
        }

        throw new UnsupportedOperationException("Objective not yet supported!");
    }

    /**
     * Fetches a serialized test case from the internal storage of the emulator.
     * Also removes the serialized test case afterwards.
     *
     * @param testcaseDir The test case directory.
     * @param testCase    The name of the test case file.
     */
    public void fetchTestCase(String testcaseDir, String testCase) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/utility/fetch_test_case")
                .withParameter("deviceId", emulator)
                .withParameter("testcaseDir", testcaseDir)
                .withParameter("testcase", testCase);
        Message response = sendMessage(messageBuilder.build());
        MATE.log("Fetching TestCase from emulator succeeded: " + response.getParameter("response"));
    }

    /**
     * Simulates a system event by broadcasting the notification of the occurrence of
     * a system event to a certain receiver.
     *
     * @param packageName The package name of the AUT.
     * @param receiver    The receiver listening for the system event.
     * @param action      The system event.
     * @param dynamic     Whether the receiver is a dynamic receiver or not.
     * @return Returns whether broadcasting the system event succeeded or not.
     */
    public boolean executeSystemEvent(String packageName, String receiver, String action, boolean dynamic) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fuzzer/execute_system_event")
                .withParameter("deviceId", emulator)
                .withParameter("packageName", packageName)
                .withParameter("receiver", receiver)
                .withParameter("action", action)
                .withParameter("dynamic", String.valueOf(dynamic));
        Message response = sendMessage(messageBuilder.build());
        return Boolean.parseBoolean(response.getParameter("response"));
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

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fuzzer/grant_runtime_permissions")
                .withParameter("deviceId", emulator)
                .withParameter("packageName", packageName);
        Message response = sendMessage(messageBuilder.build());
        return Boolean.parseBoolean(response.getParameter("response"));
    }

    /**
     * Pushes dummy files for various data types, e.g. video, onto
     * the external storage (sd card). This method should be only used in combination
     * with the intent fuzzing functionality.
     *
     * @return Returns whether the push operation succeeded or not.
     */
    public boolean pushDummyFiles() {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fuzzer/push_dummy_files")
                .withParameter("deviceId", emulator);
        Message response = sendMessage(messageBuilder.build());
        return Boolean.parseBoolean(response.getParameter("response"));
    }

    /**
     * Initialises a graph.
     */
    public void initGraph() {

        GraphType graphType = Properties.GRAPH_TYPE();

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/init")
                .withParameter("deviceId", emulator)
                .withParameter("packageName", MATE.packageName)
                .withParameter("graph_type", graphType.name())
                .withParameter("apk", Properties.APK())
                .withParameter("target", Properties.TARGET());

        if (graphType == GraphType.INTRA_CFG) {
            messageBuilder.withParameter("method", Properties.METHOD_NAME());
            messageBuilder.withParameter("basic_blocks", String.valueOf(Properties.BASIC_BLOCKS()));
        }

        if (graphType == GraphType.INTER_CFG) {
            messageBuilder.withParameter("basic_blocks", String.valueOf(Properties.BASIC_BLOCKS()));
            messageBuilder.withParameter("exclude_art_classes", String.valueOf(Properties.EXCLUDE_ART_CLASSES()));
        }

        sendMessage(messageBuilder.build());
    }

    /**
     * Requests the drawing of the graph.
     *
     * @param raw Whether drawing the raw graph or target and visited vertices should be marked.
     */
    public void drawGraph(boolean raw) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/draw")
                .withParameter("raw", String.valueOf(raw));
        sendMessage(messageBuilder.build());
    }

    /**
     * Requests the list of branches of the AUT. Each branch typically
     * represents a testing target into the context of MIO/MOSA.
     * A branch's representation coincides with the trace that is produced
     * by the branchDistance instrumentation tool.
     *
     * @return Returns the list of branches.
     */
    public List<String> getBranches() {

        GraphType graphType = Properties.GRAPH_TYPE();

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/get_branches")
                .withParameter("graph_type", graphType.name());

        Message response = sendMessage(messageBuilder.build());

        return Arrays.asList(response.getParameter("branches").split("\\+"));
    }

    /**
     * Stores the branch distance data for the given chromosome.
     *
     * @param chromosomeId Identifies either a test case or a test suite.
     * @param entityId     Identifies the test case if chromosomeId specifies a test suite,
     *                     otherwise {@code null}.
     */
    public void storeBranchDistanceData(String chromosomeId, String entityId) {

        MATE.log("Storing branch distance data...");

        String testcase = entityId == null ? chromosomeId : entityId;
        if (coveredTestCases.contains(testcase)) {
            // don't fetch again traces file from emulator
            return;
        }
        coveredTestCases.add(testcase);

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/store")
                .withParameter("deviceId", emulator)
                // required for sending a broadcast to the AUT (target component), may use app name of graph from init request
                .withParameter("packageName", MATE.packageName)
                .withParameter("chromosome", chromosomeId);
        if (entityId != null) {
            messageBuilder.withParameter("entity", entityId);
        }

        // TODO: may log failure contained in response
        sendMessage(messageBuilder.build());
    }

    /**
     * Retrieves the branch distance for the given chromosome. Note that
     * {@link #storeBranchDistanceData(String, String)} has to be called previously.
     *
     * @param chromosomeId Identifies either a test case or a test suite.
     * @return Returns the branch distance for the given chromosome.
     */
    public double getBranchDistance(String chromosomeId) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/get_branch_distance")
                .withParameter("deviceId", emulator)
                .withParameter("chromosomes", chromosomeId);

        Message response = sendMessage(messageBuilder.build());
        return Double.parseDouble(response.getParameter("branch_distance"));
    }

    /**
     * Retrieves the branch distance vector for the given chromosome. A branch distance
     * vector consists of n entries, where n refers to the number of branches.
     * The nth entry in the vector refers to the fitness value of the nth branch.
     *
     * @param chromosome The given chromosome.
     * @param <T>        Specifies whether the chromosome refers to a test case or a test suite.
     * @return Returns the branch distance vector for the given chromosome.
     */
    public <T> List<Double> getBranchDistanceVector(IChromosome<T> chromosome) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/get_branch_distance_vector")
                .withParameter("deviceId", emulator)
                // required for sending a broadcast to the AUT (target component), may use app name of graph from init request
                .withParameter("packageName", MATE.packageName)
                .withParameter("chromosomes", chromosome.getValue().toString());

        Message response = sendMessage(messageBuilder.build());
        List<Double> branchDistanceVector = new ArrayList<>();
        String[] branchDistances = response.getParameter("branch_distance_vector").split("\\+");

        for (String branchDistance : branchDistances) {
            branchDistanceVector.add(Double.parseDouble(branchDistance));
        }

        return branchDistanceVector;
    }

    public List<String> getSourceLines() {
        Message response = sendMessage(new Message.MessageBuilder("/coverage/getSourceLines")
                .withParameter("deviceId", emulator)
                .build());
        if (!"/coverage/getSourceLines".equals(response.getSubject())) {
            MATE.log_acc("ERROR: unable to retrieve source lines");
            return null;
        }
        return Arrays.asList(response.getParameter("lines").split("\n"));
    }

    /**
     * Stores the coverage information of the given test case. By storing
     * we mean that a trace/coverage file is generated/fetched from the emulator.
     *
     * @param coverage     The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosomeId Identifies either a test case or a test suite.
     * @param entityId     Identifies the test case if chromosomeId specifies a test suite,
     *                     otherwise {@code null}.
     */
    public void storeCoverageData(Coverage coverage, String chromosomeId, String entityId) {

        if (coverage == Coverage.BRANCH_COVERAGE) {
            // check whether the storing of the traces file has been already requested
            String testcase = entityId == null ? chromosomeId : entityId;
            if (coveredTestCases.contains(testcase)) {
                // don't fetch again traces file from emulator
                return;
            }
            coveredTestCases.add(testcase);
        }

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/store")
                .withParameter("deviceId", emulator)
                .withParameter("coverage_type", coverage.name())
                .withParameter("chromosome", chromosomeId);
        if (entityId != null) {
            messageBuilder.withParameter("entity", entityId);
        }
        sendMessage(messageBuilder.build());
    }

    /**
     * Requests the combined coverage information for the given set of test cases / test suites.
     *
     * @param coverage    The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosomes The list of chromosomes (test cases or test suites) for which the
     *                    coverage should be computed.
     * @param <T>         Refers to a test case or a test suite.
     * @return Returns the combined coverage information for a set of chromosomes.
     */
    public <T> double getCombinedCoverage(Coverage coverage, List<IChromosome<T>> chromosomes) {

        String chromosomesParam = null;

        if (chromosomes != null) {

            // Java 8: String.join("+", chromosomeIds);
            StringBuilder chromosomeIds = new StringBuilder();

            for (IChromosome<T> chromosome : chromosomes) {
                chromosomeIds.append(chromosome.getValue());
                chromosomeIds.append("+");
            }

            // remove '+' at the end
            chromosomeIds.setLength(chromosomeIds.length() - 1);

            chromosomesParam = chromosomeIds.toString();
        }

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/combined")
                .withParameter("deviceId", emulator)
                .withParameter("packageName", MATE.packageName)
                .withParameter("coverage_type", coverage.name());
        if (chromosomesParam != null) {
            messageBuilder.withParameter("chromosomes", chromosomesParam);
        }

        Message response = sendMessage(messageBuilder.build());
        return Double.parseDouble(response.getParameter("coverage"));
    }

    /**
     * Convenient function to request the coverage information for a given chromosome.
     * A chromosome can be either a test case or a test suite.
     *
     * @param coverage     The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosomeId Identifies either a test case or a test suite.
     * @return Returns the coverage of the given test case.
     */
    public double getCoverage(Coverage coverage, String chromosomeId) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/combined")
                .withParameter("deviceId", emulator)
                .withParameter("coverage_type", coverage.name())
                .withParameter("packageName", MATE.packageName)
                .withParameter("chromosomes", chromosomeId);
        Message response = sendMessage(messageBuilder.build());
        return Double.parseDouble(response.getParameter("coverage"));
    }

    public <T> List<Double> getLineCoveredPercentage(IChromosome<T> chromosome, List<String> lines) {

        // concatenate lines
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            sb.append(line);
            sb.append("*");
        }
        if (!lines.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/lineCoveredPercentages")
                .withParameter("deviceId", emulator)
                .withParameter("lines", sb.toString())
                .withParameter("chromosomes", chromosome.toString());
        Message response = sendMessage(messageBuilder.build());

        if (response.getSubject().equals("/error")) {
            MATE.log_acc("Retrieving line covered percentages failed!");
            throw new IllegalStateException(response.getParameter("info"));
        } else {
            // convert result
            List<Double> coveragePercentagesVector = new ArrayList<>();
            String[] coveragePercentages = response.getParameter("coveragePercentages").split("\n");

            for (String coveragePercentage : coveragePercentages) {
                coveragePercentagesVector.add(Double.parseDouble(coveragePercentage));
            }

            return coveragePercentagesVector;
        }
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
        String cmd = "screenshot:" + emulator + ":" + emulator + "_" + packageName + "_" + nodeId + ".png";
        tunnelLegacyCmd(cmd);
    }

    public void screenShotForFlickerDetection(String packageName, String nodeId) {
        String cmd = "flickerScreenshot:" + emulator + ":" + emulator + "_" + packageName + "_" + nodeId + ".png";
        tunnelLegacyCmd(cmd);
    }

    public void clearAppData() {
        Message response = sendMessage(new Message.MessageBuilder("/android/clearApp")
                .withParameter("deviceId", emulator)
                .build());
        if (!"/android/clearApp".equals(response.getSubject())) {
            MATE.log_acc("ERROR: unable clear app data");
        }
    }

    public double matchesSurroundingColor(String packageName, String stateId, Widget widget) {
        String cmd = "surroundingColor:";
        cmd += emulator + "_" + packageName + ":";
        cmd += stateId + ":";
        cmd += widget.getX1() + "," + widget.getY1() + "," + widget.getX2() + "," + widget.getY2();

        Message message = new Message("/accessibility");
        message.addParameter("cmd", cmd);

        //will break commands in several parameters-values in the future.
        //For now I'm sending the whole command with parameters in one single string
        String response = sendMessage(message).getParameter("response");
        return Double.valueOf(response);
    }

    public double getContrastRatio(String packageName, String stateId, Widget widget) {
        int maxw = MATE.device.getDisplayWidth();
        int maxh = MATE.device.getDisplayHeight();
        double contrastRatio = 21;
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

    public String getLuminances(String packageName, String stateId, Widget widget) {
        int maxw = MATE.device.getDisplayWidth();
        int maxh = MATE.device.getDisplayHeight();
        String cmd = "luminance:";
        cmd += emulator + "_" + packageName + ":";
        cmd += stateId + ":";
        int x1 = widget.getX1();
        int x2 = widget.getX2();
        int y1 = widget.getY1();
        int y2 = widget.getY2();
        int borderExpanded = 1;
        if (x1 - borderExpanded >= 0)
            x1 -= borderExpanded;
        if (x2 + borderExpanded <= maxw)
            x2 += borderExpanded;
        if (y1 - borderExpanded >= 0)
            y1 -= borderExpanded;
        if (y2 + borderExpanded <= maxh)
            y2 += borderExpanded;
        cmd += x1 + "," + y1 + "," + x2 + "," + y2;

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

    public void setPortraitMode() {
        Message response = sendMessage(new Message.MessageBuilder("/emulator/interaction")
                .withParameter("deviceId", emulator)
                .withParameter("type", "rotation")
                .withParameter("rotation", "portrait")
                .build());
        if (!"/emulator/interaction".equals(response.getSubject()) ||
                !"portrait".equals(response.getParameter("rotation"))) {
            MATE.log_acc("ERROR: unable to set rotation to portrait mode");
        }
    }

    public void toggleRotation() {
        Message response = sendMessage(new Message.MessageBuilder("/emulator/interaction")
                .withParameter("deviceId", emulator)
                .withParameter("type", "rotation")
                .withParameter("rotation", "toggle")
                .build());
        if (!"/emulator/interaction".equals(response.getSubject())) {
            MATE.log_acc("ERROR: unable to toggle rotation of emulator");
        }
    }
}
