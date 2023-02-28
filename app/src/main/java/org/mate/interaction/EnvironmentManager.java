package org.mate.interaction;

import android.app.Instrumentation;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.graph.DrawType;
import org.mate.graph.GraphType;
import org.mate.interaction.action.ui.Widget;
import org.mate.message.Message;
import org.mate.message.serialization.Parser;
import org.mate.message.serialization.Serializer;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.MateInterruptedException;
import org.mate.utils.Objective;
import org.mate.utils.Utils;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageDTO;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides the interface to communicate with the MATE server.
 */
public class EnvironmentManager {
    public static final String ACTIVITY_UNKNOWN = "unknown";
    private static final String DEFAULT_SERVER_IP = "10.0.2.2";
    private static final int DEFAULT_PORT = 12345;
    private static final String METADATA_PREFIX = "__meta__";
    private static final String MESSAGE_PROTOCOL_VERSION = "3.1";
    private static final String MESSAGE_PROTOCOL_VERSION_KEY = "version";

    /**
     * The maximal number of how often a request is sent to MATE-Server in case of a fault.
     */
    private static final int MAX_NUMBER_OF_TRIES = 3;

    private String emulator = null;
    private Socket server;
    private final int port;
    private Parser messageParser;
    private boolean active;

    /**
     * Tracks for which test case the pulling of traces files have been already performed.
     * This is necessary that BranchDistance and BranchCoverage don't try to fetch for the same
     * test case the traces file multiple times. Otherwise, the last fetch trial overwrites
     * the traces file for the given test case with an empty file.
     */
    private final Set<String> coveredTestCases = new HashSet<>();

    /**
     * Initialises a new environment manager communicating with
     * the MATE server on the default port.
     *
     * @throws IOException If no connection could be established with the MATE server.
     */
    public EnvironmentManager() throws IOException {
        this(DEFAULT_PORT);
    }

    /**
     * Initialises a new environment manager communicating with the MATE server on the given port.
     *
     * @param port The MATE server port.
     * @throws IOException If no connection could be established with the MATE server.
     */
    public EnvironmentManager(int port) throws IOException {
        active = true;
        server = new Socket(DEFAULT_SERVER_IP, port);
        this.port = port;

        /*
         * The input stream obtained from server.getInputStream() cannot be interrupted when
         * waiting for a response from the server. We need to wrap the input stream around a
         * channel to make this possible. If an interrupt happens, a ClosedInterruptException
         * will be thrown.
         */
        final InputStream interruptibleInputStream = Channels.newInputStream(
                Channels.newChannel(server.getInputStream()));
        messageParser = new Parser(interruptibleInputStream);
        server.setSoTimeout(0);
    }

    /**
     * Re-connects to Mate-Server if the connection has been closed.
     *
     * @throws IOException If the connection is closed and cannot be re-established.
     */
    public void reconnect() throws IOException {

        Utils.sleep(1000);

        if (server.isClosed()) {
            MATE.log_debug("Re-connecting to MATE-Server.");
            server = new Socket(DEFAULT_SERVER_IP, port);

            /*
             * The input stream obtained from server.getInputStream() cannot be interrupted when
             * waiting for a response from the server. We need to wrap the input stream around a
             * channel to make this possible. If an interrupt happens, a ClosedInterruptException
             * will be thrown.
             */
            final InputStream interruptibleInputStream = Channels.newInputStream(
                    Channels.newChannel(server.getInputStream()));
            messageParser = new Parser(interruptibleInputStream);
            server.setSoTimeout(0);
        } else {
            MATE.log_debug("No need to re-connect to MATE-Server.");
        }
    }

    /**
     * Closes the connection to the MATE server.
     *
     * @throws IOException If closing connection fails.
     */
    public void close() throws IOException {
        sendMessage(new Message("/close"));
        active = false;
        server.close();
    }

    /**
     * Get the name of the emulator.
     *
     * @return Returns the name of the emulator.
     */
    public String getEmulator() {
        return emulator;
    }

    /**
     * Closes the socket to MATE-Server and tries to re-connect.
     */
    private void closeAndReconnect() {
        try {
            server.close();
            reconnect();
        } catch (final IOException e) {
            throw new RuntimeException("Could not establish a new connection to MATE-Server!", e);
        }
    }

    /**
     * Sends a {@link org.mate.message.Message} to the server and returns the response of the server.
     *
     * @param message The {@link org.mate.message.Message} that will be send to the server.
     * @return Returns the response {@link org.mate.message.Message} of the server.
     */
    public synchronized Message sendMessage(Message message) {

        if (!active) {
            throw new IllegalStateException("EnvironmentManager is no longer active and can not " +
                    "be used for communication!");
        }

        MATE.log_debug("Send message with subject " + message.getSubject());
        addMetadata(message);

        int numberOfTries = 0;

        while (true) {

            try {
                server.getOutputStream().write(Serializer.serialize(message));
                server.getOutputStream().flush();
            } catch (IOException e) {

                MATE.log_debug("socket error sending");
                closeAndReconnect();

                try {
                    server.getOutputStream().write(Serializer.serialize(message));
                    server.getOutputStream().flush();
                } catch (IOException exception) {
                    /*
                     * If we can't send the request the second time, it's likely that we can't
                     * recover from this fault and thus abort the execution.
                     */
                    MATE.log_debug("socket error sending");
                    throw new IllegalStateException(exception);
                }
            }

            Message response;

            try {
                response = messageParser.nextMessage();
            } catch (IllegalStateException lexerException) {

                MATE.log_debug("Lexing response failed: " + lexerException);

                /*
                 * MATE-Server might still be processing the old request and trying to send data
                 * through the socket, so to ensure MATE-Server does not confuse the different
                 * requests we just close the socket and open a new one.
                 */
                closeAndReconnect();

                numberOfTries++;

                if (numberOfTries < MAX_NUMBER_OF_TRIES) {
                    continue;
                } else {
                    throw new IllegalStateException(lexerException);
                }
            }

            verifyMetadata(response);

            if (response.getSubject().equals("/error")) {
                MATE.log_debug("Received error message from MATE-Server: "
                        + response.getParameter("info"));
                throw new IllegalStateException("MATE-Server couldn't send a successful response!");
            }

            stripMetadata(response);
            return response;
        }
    }

    /**
     * Tunnels a request over the legacy end point.
     *
     * @param cmd The command string.
     * @return Returns the response as a string.
     */
    // TODO: remove once all requests are ported to the individual end points
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

    /**
     * Releases the emulator. This doesn't have any effect on the real emulator, just sets some
     * internal properties.
     */
    public void releaseEmulator() {
        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/emulator/interaction")
                .withParameter("deviceId", emulator)
                .withParameter("type", "release_emulator");
        sendMessage(messageBuilder.build());
    }

    /**
     * Allocates the emulator that is running the given app.
     *
     * @param packageName The package name of the AUT.
     * @return Returns the name of the emulator.
     */
    public String allocateEmulator(String packageName) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/emulator/interaction")
                .withParameter("type", "allocate_emulator")
                .withParameter("packageName", packageName);

        String response = sendMessage(messageBuilder.build()).getParameter("emulator");

        if (response != null && !response.isEmpty()) {
            emulator = response;
        }
        if (emulator != null)
            emulator = emulator.replace(" ", "");
        return emulator;
    }

    /**
     * Copies the test cases fitness data belonging to the source chromosome over to the given target chromosome.
     * This is necessary when a new chromosome is created but not executed, e.g. a chromosome is duplicated
     * (which doesn't require execution since the fitness is identical).
     *
     * @param sourceChromosome The source chromosome.
     * @param targetChromosome The target chromosome.
     * @param testCases The test cases belonging to the source chromosome.
     * @param fitnessFunction The underlying fitness function.
     */
    public void copyFitnessData(final IChromosome<TestSuite> sourceChromosome,
                                final IChromosome<TestSuite> targetChromosome,
                                final List<TestCase> testCases,
                                final FitnessFunction fitnessFunction) {

        // concatenate test cases
        StringBuilder sb = new StringBuilder();

        String prefix = "";
        for (TestCase testCase : testCases) {
            // there is no coverage data for dummy test cases
            if (!testCase.isDummy()) {
                sb.append(prefix);
                prefix = ",";
                sb.append(testCase.getId());
            }
        }

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fitness/copy_fitness_data")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("fitnessFunction", fitnessFunction.name())
                .withParameter("chromosome_src", sourceChromosome.getValue().getId())
                .withParameter("chromosome_target", targetChromosome.getValue().getId())
                .withParameter("entities", sb.toString());

        Message response = sendMessage(messageBuilder.build());
        if (response.getSubject().equals("/error")) {
            MATE.log_acc("Copying fitness data failed!");
            throw new IllegalStateException(response.getParameter("info"));
        }
    }

    /**
     * Copies the test cases coverage data belonging to the source chromosome over to the given target chromosome.
     * This is necessary when a new chromosome is created but not executed, e.g. a chromosome is duplicated
     * (which doesn't require execution since the coverage is identical).
     *
     * @param sourceChromosome The source chromosome.
     * @param targetChromosome The target chromosome.
     * @param testCases The test cases belonging to the source chromosome.
     */
    public void copyCoverageData(final IChromosome<TestSuite> sourceChromosome,
                                 final IChromosome<TestSuite> targetChromosome,
                                 final List<TestCase> testCases) {

        // concatenate test cases
        StringBuilder sb = new StringBuilder();

        String prefix = "";
        for (TestCase testCase : testCases) {
            // there is no coverage data for dummy test cases
            if (!testCase.isDummy()) {
                sb.append(prefix);
                prefix = ",";
                sb.append(testCase.getId());
            }
        }

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/copy")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("coverage_type", Properties.COVERAGE().name())
                .withParameter("chromosome_src", sourceChromosome.getValue().getId())
                .withParameter("chromosome_target", targetChromosome.getValue().getId())
                .withParameter("entities", sb.toString());

        Message response = sendMessage(messageBuilder.build());
        if (response.getSubject().equals("/error")) {
            MATE.log_acc("Copying coverage data failed!");
            throw new IllegalStateException(response.getParameter("info"));
        }
    }

    /**
     * Retrieves the name of the currently visible activity. It can happen that the AUT just crashed
     * and the activity name wasn't updated, then the string 'unknown' is returned.
     *
     * @return Returns the name of the current activity or the string 'unknown' if extraction failed.
     */
    public String getCurrentActivityName() {

        if (emulator == null || emulator.isEmpty()) {
            return ACTIVITY_UNKNOWN;
        }

        Message.MessageBuilder messageBuilder
                = new Message.MessageBuilder("/android/get_current_activity")
                .withParameter("deviceId", emulator);
        Message response = sendMessage(messageBuilder.build());
        return response.getParameter("activity");
    }

    /**
     * Returns the list of activities belonging to the AUT.
     *
     * @return Returns the list of activities of the AUT.
     */
    @SuppressWarnings("unused")
    public List<String> getActivityNames() {
        Message.MessageBuilder messageBuilder
                = new Message.MessageBuilder("/android/get_activities")
                .withParameter("deviceId", emulator);
        Message response = sendMessage(messageBuilder.build());
        return Arrays.asList(response.getParameter("activities").split("\n"));
    }

    /**
     * Returns the number of objectives based on objective property.
     *
     * @param objective The specified objective property.
     * @return Returns the number of objectives, e.g. the number of branches.
     */
    public int getNumberOfObjectives(Objective objective) {

        if (objective == null) {
            throw new IllegalStateException("Objective property not defined!");
        }

        MATE.log_acc("Getting objectives...!");

        int numberOfObjectives;

        if (objective == Objective.LINES) {
            numberOfObjectives = getNumberOfSourceLines();
        } else if (objective == Objective.BRANCHES) {
            numberOfObjectives = getNumberOfBranches();
        } else if (objective == Objective.BLOCKS) {
            numberOfObjectives = getNumberOfBasicBlocks();
        } else {
            throw new UnsupportedOperationException("Objective " + objective + " not yet supported!");
        }

        MATE.log_acc("Number of objectives: " + numberOfObjectives);
        return numberOfObjectives;
    }

    /**
     * Requests the list of basic blocks of the AUT. Each basic block typically represents
     * an objective in the context of MIO/MOSA. Requires that the AUT has been instrumented
     * with the basic block coverage module.
     *
     * @return Returns the list of basic blocks.
     */
    @SuppressWarnings("unused")
    public List<String> getBasicBlocks() {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fitness/get_basic_blocks")
                .withParameter("packageName", Registry.getPackageName());

        Message response = sendMessage(messageBuilder.build());

        return Arrays.asList(response.getParameter("blocks").split("\\+"));
    }

    /**
     * Requests the number of basic blocks of the AUT. Each basic block typically represents a
     * testing target in the context of MIO/MOSA.
     *
     * @return Returns the number of branches.
     */
    public int getNumberOfBasicBlocks() {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fitness/get_number_of_basic_blocks")
                .withParameter("packageName", Registry.getPackageName());

        Message response = sendMessage(messageBuilder.build());
        return Integer.parseInt(response.getParameter("blocks"));
    }

    /**
     * Fetches a serialized test case from the internal storage of the emulator.
     * Also removes the serialized test case afterwards.
     *
     * @param testcaseDir The test case directory.
     * @param testCase The name of the test case file.
     * @return Returns {@code true} if the test case could be successfully fetched and removed,
     *         otherwise {@code false} is returned.
     */
    public boolean fetchTestCase(String testcaseDir, String testCase) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder(
                "/utility/fetch_test_case")
                .withParameter("deviceId", emulator)
                .withParameter("testcaseDir", testcaseDir)
                .withParameter("testcase", testCase);
        Message response = sendMessage(messageBuilder.build());
        boolean success = Boolean.parseBoolean(response.getParameter("response"));
        MATE.log("Fetching TestCase from emulator succeeded: " + success);
        return success;
    }

    /**
     * Fetches and removes an espresso test from the internal storage of the emulator.
     *
     * @param espressoDir The espresso tests directory on the emulator.
     * @param testCase The name of the espresso test.
     * @return Returns {@code true} if the espresso test could be successfully fetched and removed,
     *         otherwise {@code false} is returned.
     */
    public boolean fetchEspressoTest(String espressoDir, String testCase) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder(
                "/utility/fetch_espresso_test")
                .withParameter("deviceId", emulator)
                .withParameter("espressoDir", espressoDir)
                .withParameter("testcase", testCase);
        Message response = sendMessage(messageBuilder.build());
        boolean success = Boolean.parseBoolean(response.getParameter("response"));
        MATE.log("Fetching espresso test from emulator succeeded: " + success);
        return success;
    }

    /**
     * Simulates a system event by broadcasting the notification of the occurrence of
     * a system event to a certain receiver.
     *
     * @param packageName The package name of the AUT.
     * @param receiver The receiver listening for the system event.
     * @param action The system event.
     * @param dynamic Whether the receiver is a dynamic receiver or not.
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
     * API level 23 and higher requires that permissions are also granted at runtime. This can be
     * done at install time with the flag -g, i.e. adb install -g apk, or via adb shell pm grant
     * packageName permission. However, the intermediate reset/restart of the app causes the
     * loss of those runtime permissions.
     *
     * @param packageName The app that requires the permissions.
     * @return Returns {@code true} if the granting permissions succeeded, otherwise {@code false}.
     */
    @SuppressWarnings("unused")
    public boolean grantRuntimePermissions(String packageName) {

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        final String readPermission = "android.permission.READ_EXTERNAL_STORAGE";
        final String writePermission = "android.permission.WRITE_EXTERNAL_STORAGE";

        // this method is far faster than the request via the server
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            instrumentation.getUiAutomation().grantRuntimePermission(packageName, readPermission);
            instrumentation.getUiAutomation().grantRuntimePermission(packageName, writePermission);
            return true;
        }

        Message.MessageBuilder messageBuilder
                = new Message.MessageBuilder("/android/grant_runtime_permissions")
                .withParameter("deviceId", emulator)
                .withParameter("packageName", packageName);

        Message response = null;
        MateInterruptedException mateInterruptedException = null;

        try {
            response = sendMessage(messageBuilder.build());
        } catch (MateInterruptedException e) {
            /*
             * We still need to wait for mate-server to complete the request, so we just wait for a
             * few seconds. This is important, because if we continue immediately and try to get the
             * coverage of the last incomplete test case by invoking the tracer, then the tracer's
             * permissions to write to the external storage might get dropped while it tries to write,
             * because clearing the app data also revokes the tracer's permissions. This in turn
             * causes a failure of the tracer to write its traces.
             */
            mateInterruptedException = e;
            Utils.sleepWithoutInterrupt(5000);
        }

        if (response != null && !"/android/grant_runtime_permissions".equals(response.getSubject())) {
            MATE.log_acc("ERROR: Unable to grant runtime permissions!");
        }

        if (mateInterruptedException != null) {
            throw mateInterruptedException;
        }

        return Boolean.parseBoolean(response.getParameter("response"));
    }

    /**
     * Pushes dummy files for various data types, e.g. video, onto the external storage (sd card).
     * This method should be only used in combination with the intent fuzzing functionality.
     *
     * @return Returns whether the push operation succeeded or not.
     */
    public boolean pushDummyFiles() {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fuzzer/push_dummy_files")
                .withParameter("deviceId", emulator);
        Message response = sendMessage(messageBuilder.build());
        return Boolean.parseBoolean(response.getParameter("response"));
    }

    public double getCallGraphDistance(IChromosome<?> chromosome) {
        String chromosomeId = getChromosomeId(chromosome);

        return Double.parseDouble(sendMessage(new Message.MessageBuilder("/graph/call_graph_distance").withParameter("chromosome", chromosomeId).withParameter("packageName", Registry.getPackageName()).build()).getParameter("distance"));
    }

    public double getReachedRequiredConstructors(IChromosome<?> chromosome) {
        String chromosomeId = getChromosomeId(chromosome);

        return Double.parseDouble(sendMessage(new Message.MessageBuilder("/graph/reached_required_constructors").withParameter("chromosome", chromosomeId).withParameter("packageName", Registry.getPackageName()).build()).getParameter("reached"));
    }

    public double getMergedBasicBlockDistance(IChromosome<?> chromosome) {
        return Double.parseDouble(
                sendMessage(new Message.MessageBuilder("/graph/basic_block_distance")
                        .withParameter("chromosome", getChromosomeId(chromosome))
                        .withParameter("packageName", Registry.getPackageName())
                        .build()
                ).getParameter("mergedNormalizedDistance"));
    }

    /**
     * Initialises a graph.
     */
    public void initGraph() {

        GraphType graphType = Properties.GRAPH_TYPE();

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/init")
                .withParameter("deviceId", emulator)
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("graph_type", graphType.name())
                .withParameter("apk", Properties.APK())
                .withParameter("target", Properties.TARGET())
                .withParameter("stack_trace_path", Properties.STACK_TRACE_PATH())
                ;

        if (graphType == GraphType.INTRA_CFG) {
            messageBuilder.withParameter("method", Properties.METHOD_NAME());
            messageBuilder.withParameter("basic_blocks", String.valueOf(Properties.BASIC_BLOCKS()));
        } else if (graphType == GraphType.INTER_CFG) {
            messageBuilder.withParameter("basic_blocks", String.valueOf(Properties.BASIC_BLOCKS()));
            messageBuilder.withParameter("exclude_art_classes", String.valueOf(Properties.EXCLUDE_ART_CLASSES()));
            messageBuilder.withParameter("resolve_only_aut_classes", String.valueOf(Properties.RESOLVE_ONLY_AUT_CLASSES()));
        }

        sendMessage(messageBuilder.build());
    }

    public void logReachedTargets(IChromosome<?> chromosome) {
        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/reached_targets")
                .withParameter("chromosome", getChromosomeId(chromosome))
                .withParameter("packageName", Registry.getPackageName());

        if (chromosome.getValue() instanceof TestCase) {
            TestCase testCase = (TestCase) chromosome.getValue();

            if (testCase.hasCrashDetected()) {
                messageBuilder.withParameter("stackTrace", testCase.getCrashStackTrace().getMethodCalls().stream()
                        .collect(Collectors.joining("\n")));
            }
        }

        Message response = sendMessage(messageBuilder.build());

        logMap(response, "reachedTargetComponents");
        logMap(response, "reachedTargetMethods");
        logMap(response, "reachedTargetLines");
    }

    private void logMap(Message response, String mapName) {
        int entries = Integer.parseInt(response.getParameter(mapName + ".size"));

        StringJoiner map = new StringJoiner(", ");

        for (int i = 0; i < entries; i++) {
            String key = response.getParameter(mapName + ".k" + i);
            String value = response.getParameter(mapName + ".v" + i);
            map.add(key + "=" + value);
        }

        MATE.log(mapName + "{" + map + "}");
    }

    private Set<String> tokens = null;
    public Set<String> getStackTraceTokens() {
        if (emulator == null) {
            return Collections.emptySet();
        }

        if (tokens == null) {
            Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/stack_trace_tokens")
                    .withParameter("package", Registry.getPackageName());

            Message response = sendMessage(messageBuilder.build());
            int numTokens = Integer.parseInt(response.getParameter("tokens"));

            tokens = new HashSet<>();
            for (int i = 0; i < numTokens; i++) {
                tokens.add(response.getParameter("token_" + i));
            }
        }

        MATE.log("Tokens are: " + tokens);

        return Collections.unmodifiableSet(tokens);
    }

    private Set<String> userInputTokens = null;
    public Set<String> getStackTraceUserInput() {
        if (userInputTokens == null) {
            Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/stack_trace_user_tokens")
                    .withParameter("package", Registry.getPackageName());

            Message response = sendMessage(messageBuilder.build());
            userInputTokens = new HashSet<>(Arrays.asList(response.getParameter("tokens").split(",")));
        }

        return userInputTokens;
    }

    /**
     * Requests the drawing of the graph.
     */
    public void drawGraph() {

        MATE.log_acc("Drawing graph!");

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/draw")
                .withParameter("raw", String.valueOf(Properties.DRAW_GRAPH() == DrawType.RAW));
        sendMessage(messageBuilder.build());
    }

    public <T> void drawCallGraph(String id, IChromosome<T> chromosome) {
        sendMessage(new Message.MessageBuilder("/graph/callGraph/draw")
                .withParameter("id", id)
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosome", getChromosomeId(chromosome))
                .build());
    }

    /**
     * Requests the list of branches of the AUT. Each branch typically represents a testing target
     * into the context of MIO/MOSA.
     *
     * @return Returns the list of branches.
     */
    @SuppressWarnings("unused")
    public List<String> getBranches() {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fitness/get_branches")
                .withParameter("packageName", Registry.getPackageName());

        Message response = sendMessage(messageBuilder.build());
        return Arrays.asList(response.getParameter("branches").split("\\+"));
    }

    /**
     * Requests the number of branches of the AUT. Each branch typically represents a testing target
     * in the context of MIO/MOSA.
     *
     * @return Returns the number of branches.
     */
    public int getNumberOfBranches() {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fitness/get_number_of_branches")
                .withParameter("packageName", Registry.getPackageName());

        Message response = sendMessage(messageBuilder.build());
        return Integer.parseInt(response.getParameter("branches"));
    }

    public List<String> getStackTrace() {
        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/stack_trace")
                .withParameter("packageName", Registry.getPackageName());

        Message response = sendMessage(messageBuilder.build());
        return Arrays.asList(response.getParameter("stack_trace").split(","));
    }

    public void storeActionFitnessData(IChromosome<TestCase> chromosome) {
        storeFitnessData(chromosome, getActionEntityId(chromosome));
    }

    public String getActionEntityId(IChromosome<TestCase> chromosome) {
        return getActionEntityId(chromosome, chromosome.getValue().getEventSequence().size());
    }

    public String getActionEntityId(IChromosome<TestCase> chromosome, int numActions) {
        return numActions + "_" + getChromosomeId(chromosome);
    }

    /**
     * Stores the fitness data for the given chromosome.
     *
     * @param chromosome Refers either to a test case or to a test suite.
     * @param entityId Identifies the test case if chromosomeId specifies a test suite,
     *         otherwise {@code null}.
     * @param fitnessFunction The underlying fitness function.
     */
    public <T> void storeFitnessData(final IChromosome<T> chromosome, final String entityId,
                                     final FitnessFunction fitnessFunction) {

        if (entityId != null && entityId.equals("dummy")) {
            MATE.log_warn("Trying to store fitness data of dummy test case...");
            return;
        } else if (chromosome.getValue() instanceof TestCase) {
            // there is no fitness data to store for dummy test cases
            if (((TestCase) chromosome.getValue()).isDummy()) {
                MATE.log_warn("Trying to store fitness data of dummy test case...");
                return;
            }
        }

        String chromosomeId = getChromosomeId(chromosome);

        String testcase = entityId == null ? chromosomeId : entityId;
        if (coveredTestCases.contains(testcase)) {
            // don't fetch again traces file from emulator
            return;
        }
        coveredTestCases.add(testcase);

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fitness/store_fitness_data")
                .withParameter("fitnessFunction", fitnessFunction.name())
                .withParameter("deviceId", emulator)
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosome", chromosomeId);
        if (entityId != null) {
            messageBuilder.withParameter("entity", entityId);
        }

        Message response = sendMessage(messageBuilder.build());

        if (response.getSubject().equals("/error")) {
            MATE.log_acc("Storing fitness data failed!");
            throw new IllegalStateException(response.getParameter("info"));
        }
    }

    /**
     * Retrieves the branch distance for the given chromosome. Note that
     * {@link #storeFitnessData(IChromosome, String, FitnessFunction)} has to be called previously.
     *
     * @param chromosome Refers either to a test case or to a test suite.
     * @return Returns the branch distance for the given chromosome.
     */
    public <T> double getBranchDistance(IChromosome<T> chromosome) {

        if (chromosome.getValue() instanceof TestCase) {
            if (((TestCase) chromosome.getValue()).isDummy()) {
                MATE.log_warn("Trying to retrieve branch distance of dummy test case...");
                // a dummy test case has a branch distance of 1.0 (worst value)
                return 1.0;
            }
        }

        String chromosomeId = getChromosomeId(chromosome);

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/get_branch_distance")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosome", chromosomeId);

        Message response = sendMessage(messageBuilder.build());
        return Double.parseDouble(response.getParameter("branch_distance"));
    }

    /**
     * Retrieves the branch fitness vector for the given chromosome. A branch fitness vector consists
     * of n entries, where n refers to the number of branches. The nth entry in the vector refers to
     * the fitness value of the nth branch.
     *
     * @param chromosome The given chromosome.
     * @param numberOfBranches The number of branches.
     * @param <T> Specifies whether the chromosome refers to a test case or a test suite.
     * @return Returns the branch fitness vector for the given chromosome.
     */
    public <T> BitSet getBranchFitnessVector(IChromosome<T> chromosome, int numberOfBranches) {

        if (chromosome.getValue() instanceof TestCase) {
            if (((TestCase) chromosome.getValue()).isDummy()) {
                MATE.log_warn("Trying to retrieve branch fitness vector of dummy test case...");
                // a dummy test case has a branch fitness of 0.0 for each objective (0.0 == worst)
                return new BitSet(numberOfBranches);
            }
        }

        String chromosomeId = getChromosomeId(chromosome);

        Message.MessageBuilder messageBuilder
                = new Message.MessageBuilder("/fitness/get_branch_fitness_vector")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosome", chromosomeId);

        Message response = sendMessage(messageBuilder.build());

        String[] branchFitnessValues = response.getParameter("branch_fitness_vector").split("\\+");
        assert branchFitnessValues.length == numberOfBranches;

        final BitSet branchFitnessVector = new BitSet(branchFitnessValues.length);

        for (int i = 0; i < branchFitnessValues.length; ++i) {
            final String fitness = branchFitnessValues[i];
            if (fitness.equals("1")) {
                branchFitnessVector.set(i);
            }
        }

        return branchFitnessVector;
    }

    /**
     * Retrieves the basic block fitness vector for the given chromosome. A basic block fitness
     * vector consists of n entries, where n refers to the number of basic blocks.
     * The nth entry in the vector refers to the fitness value of the nth basic block.
     *
     * @param chromosome The given chromosome.
     * @param numberOfBasicBlocks The number of basic blocks.
     * @param <T> Specifies whether the chromosome refers to a test case or a test suite.
     * @return Returns the basic block fitness vector for the given chromosome.
     */
    public <T> BitSet getBasicBlockFitnessVector(IChromosome<T> chromosome, int numberOfBasicBlocks) {

        if (chromosome.getValue() instanceof TestCase) {
            if (((TestCase) chromosome.getValue()).isDummy()) {
                MATE.log_warn("Trying to retrieve basic block fitness vector of dummy test case...");
                // a dummy test case has a basic block fitness of 0.0 for each objective (0.0 == worst)
                return new BitSet(numberOfBasicBlocks);
            }
        }

        String chromosomeId = getChromosomeId(chromosome);

        Message.MessageBuilder messageBuilder
                = new Message.MessageBuilder("/fitness/get_basic_block_fitness_vector")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosome", chromosomeId);

        Message response = sendMessage(messageBuilder.build());

        String[] basicBlockFitnessValues = response.getParameter("basic_block_fitness_vector").split("\\+");
        assert basicBlockFitnessValues.length == numberOfBasicBlocks;

        final BitSet basicBlockFitnessVector = new BitSet(basicBlockFitnessValues.length);

        for (int i = 0; i < basicBlockFitnessValues.length; ++i) {
            final String fitness = basicBlockFitnessValues[i];
            if (fitness.equals("1")) {
                basicBlockFitnessVector.set(i);
            }
        }

        return basicBlockFitnessVector;
    }

    /**
     * Retrieves the branch distance vector for the given chromosome. A branch distance vector
     * consists of n entries, where n refers to the number of branches. The nth entry in the vector
     * refers to the fitness value of the nth branch.
     *
     * @param chromosome The given chromosome.
     * @param numberOfBranches The number of branches.
     * @param <T> Specifies whether the chromosome refers to a test case or a test suite.
     * @return Returns the branch distance vector for the given chromosome.
     */
    public <T> List<Float> getBranchDistanceVector(IChromosome<T> chromosome, int numberOfBranches) {

        if (chromosome.getValue() instanceof TestCase) {
            if (((TestCase) chromosome.getValue()).isDummy()) {
                MATE.log_warn("Trying to retrieve branch distance vector of dummy test case...");
                // a dummy test case has a branch distance of 1.0 (worst value) for each objective
                return Collections.nCopies(numberOfBranches, 1.0f);
            }
        }

        String chromosomeId = getChromosomeId(chromosome);

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/graph/get_branch_distance_vector")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosome", chromosomeId);

        Message response = sendMessage(messageBuilder.build());
        String[] branchDistances = response.getParameter("branch_distance_vector").split("\\+");
        assert branchDistances.length == numberOfBranches;

        List<Float> branchDistanceVector = new ArrayList<>();

        for (String branchDistance : branchDistances) {
            branchDistanceVector.add(Float.parseFloat(branchDistance));
        }

        return branchDistanceVector;
    }

    /**
     * Returns the list of source lines of the AUT. A single source line has the following format:
     * package name + class name + line number
     *
     * @return Returns the sources lines of the AUT.
     */
    @SuppressWarnings("unused")
    public List<String> getSourceLines() {

        Message response = sendMessage(new Message.MessageBuilder("/coverage/getSourceLines")
                .withParameter("packageName", Registry.getPackageName())
                .build());

        if (response.getSubject().equals("/error")) {
            MATE.log_acc("Unable to retrieve source lines!");
            throw new IllegalStateException(response.getParameter("info"));
        } else {
            return Arrays.asList(response.getParameter("lines").split("\n"));
        }
    }

    /**
     * Returns the number of source lines of the AUT.
     *
     * @return Returns the sources lines of the AUT.
     */
    public int getNumberOfSourceLines() {

        Message response = sendMessage(new Message.MessageBuilder("/coverage/getNumberOfSourceLines")
                .withParameter("packageName", Registry.getPackageName())
                .build());

        if (response.getSubject().equals("/error")) {
            MATE.log_acc("Unable to retrieve number of source lines!");
            throw new IllegalStateException(response.getParameter("info"));
        } else {
            return Integer.parseInt(response.getParameter("lines"));
        }
    }

    /**
     * Stores the coverage information of the given test case. By storing
     * we mean that a trace/coverage file is generated/fetched from the emulator.
     * This method is used to store the coverage data for the last incomplete test case.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosomeId Identifies either a test case or a test suite.
     * @param entityId Identifies the test case if chromosomeId specifies a test suite,
     *         otherwise {@code null}.
     */
    public void storeCoverageData(Coverage coverage, String chromosomeId, String entityId) {

        if (coverage == Coverage.BRANCH_COVERAGE || coverage == Coverage.LINE_COVERAGE
                || coverage == Coverage.METHOD_COVERAGE
                || coverage == Coverage.BASIC_BLOCK_LINE_COVERAGE
                || coverage == Coverage.BASIC_BLOCK_BRANCH_COVERAGE
                || coverage == Coverage.ALL_COVERAGE) {
            // check whether the storing of the traces/coverage file has been already requested
            String testcase = entityId == null ? chromosomeId : entityId;
            if (coveredTestCases.contains(testcase)) {
                // don't fetch again traces/coverage file from emulator
                return;
            }
            coveredTestCases.add(testcase);
        }

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/store")
                .withParameter("deviceId", emulator)
                .withParameter("coverage_type", coverage.name())
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosome", chromosomeId);
        if (entityId != null) {
            messageBuilder.withParameter("entity", entityId);
        }
        sendMessage(messageBuilder.build());
    }

    /**
     * Stores the coverage information of the given test case. By storing
     * we mean that a trace/coverage file is generated/fetched from the emulator.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosome Refers either to a test case or to a test suite.
     * @param entityId Identifies the test case if chromosomeId specifies a test suite,
     *         otherwise {@code null}.
     */
    public <T> void storeCoverageData(Coverage coverage, IChromosome<T> chromosome, String entityId) {

        if (entityId != null && entityId.equals("dummy")) {
            MATE.log_warn("Trying to store coverage data of dummy test case...");
            return;
        } else if (chromosome.getValue() instanceof TestCase) {
            // there is no coverage data to store for dummy test cases
            if (((TestCase) chromosome.getValue()).isDummy()) {
                MATE.log_warn("Trying to store coverage data of dummy test case...");
                return;
            }
        }

        String chromosomeId = getChromosomeId(chromosome);

        if (coverage == Coverage.BRANCH_COVERAGE || coverage == Coverage.LINE_COVERAGE
                || coverage == Coverage.METHOD_COVERAGE
                || coverage == Coverage.BASIC_BLOCK_LINE_COVERAGE
                || coverage == Coverage.BASIC_BLOCK_BRANCH_COVERAGE
                || coverage == Coverage.ALL_COVERAGE) {
            // check whether the storing of the traces/coverage file has been already requested
            String testcase = entityId == null ? chromosomeId : entityId;
            if (coveredTestCases.contains(testcase)) {
                // don't fetch again traces/coverage file from emulator
                return;
            }
            coveredTestCases.add(testcase);
        }

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/store")
                .withParameter("deviceId", emulator)
                .withParameter("coverage_type", coverage.name())
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosome", chromosomeId);
        if (entityId != null) {
            messageBuilder.withParameter("entity", entityId);
        }
        sendMessage(messageBuilder.build());
    }

    /**
     * Requests the combined coverage information for the given set of test cases / test suites.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosomes The list of chromosomes (test cases or test suites) for which the
     *         coverage should be computed.
     * @param <T> Refers to a test case or a test suite.
     * @return Returns the combined coverage information for a set of chromosomes.
     */
    public <T> CoverageDTO getCombinedCoverage(Coverage coverage, List<IChromosome<T>> chromosomes) {

        String chromosomesParam = null;

        if (chromosomes != null) {

            chromosomesParam = chromosomes.stream()
                    .filter(chromosome -> {
                        if (chromosome.getValue() instanceof TestCase) {
                            // there is no coverage data to store for dummy test cases
                            if (((TestCase) chromosome.getValue()).isDummy()) {
                                MATE.log_warn("Trying to retrieve combined coverage of dummy test case...");
                                return false;
                            }
                        }
                        return true;
                    })
                    .map(this::getChromosomeId)
                    .collect(Collectors.joining("+"));

            if (chromosomesParam.isEmpty()) {
                // only dummy test cases
                return CoverageDTO.getDummyCoverageDTO(coverage);
            }
        }

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/combined")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("coverage_type", coverage.name());
        if (chromosomesParam != null) {
            messageBuilder.withParameter("chromosomes", chromosomesParam);
        }

        Message response = sendMessage(messageBuilder.build());
        return extractCoverage(response);
    }

    public <T> T askUserToPick(List<T> options, Function<T, String> toString) {
        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/utility/let_user_pick")
                .withParameter("options", String.valueOf(options.size()));

        for (int i = 0; i < options.size(); i++) {
            messageBuilder.withParameter("option_" + i, toString.apply(options.get(i)));
        }

        return options.get(Integer.parseInt(sendMessage(messageBuilder.build()).getParameter("picked_option")));
    }

    /**
     * Fills the coverage dto with the obtained coverage information from the coverage response.
     *
     * @param response The response to a coverage request.
     * @return Returns the filled coverage dto.
     */
    private CoverageDTO extractCoverage(Message response) {

        CoverageDTO coverageDTO = new CoverageDTO();

        String methodCoverage = response.getParameter("method_coverage");
        if (methodCoverage != null) {
            coverageDTO.setMethodCoverage(Double.parseDouble(methodCoverage));
        }

        String branchCoverage = response.getParameter("branch_coverage");
        if (branchCoverage != null) {
            coverageDTO.setBranchCoverage(Double.parseDouble(branchCoverage));
        }

        String lineCoverage = response.getParameter("line_coverage");
        if (lineCoverage != null) {
            coverageDTO.setLineCoverage(Double.parseDouble(lineCoverage));
        }

        return coverageDTO;
    }

    /**
     * A convenient function to retrieve the coverage of a single test case within
     * a test suite.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @param testSuiteId Identifies the test suite.
     * @param testCaseId Identifies the individual test case.
     * @return Returns the coverage of the given test case.
     */
    public CoverageDTO getCoverage(Coverage coverage, String testSuiteId, String testCaseId) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/get")
                .withParameter("deviceId", emulator)
                .withParameter("coverage_type", coverage.name())
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("testSuiteId", testSuiteId)
                .withParameter("testCaseId", testCaseId);
        Message response = sendMessage(messageBuilder.build());
        return extractCoverage(response);
    }

    /**
     * Convenient function to request the coverage information for a given chromosome.
     * A chromosome can be either a test case or a test suite. This method is used
     * to retrieve the coverage of the last incomplete test case.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosomeId Identifies either a test case or a test suite.
     * @return Returns the coverage of the given test case.
     */
    public CoverageDTO getCoverage(Coverage coverage, String chromosomeId) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/combined")
                .withParameter("deviceId", emulator)
                .withParameter("coverage_type", coverage.name())
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosomes", chromosomeId);
        Message response = sendMessage(messageBuilder.build());
        return extractCoverage(response);
    }

    /**
     * Convenient function to request the coverage information for a given chromosome.
     * A chromosome can be either a test case or a test suite.
     *
     * @param coverage The coverage type, e.g. BRANCH_COVERAGE.
     * @param chromosome Refers either to a test case or to a test suite.
     * @return Returns the coverage of the given test case.
     */
    public <T> CoverageDTO getCoverage(Coverage coverage, IChromosome<T> chromosome) {

        if (chromosome.getValue() instanceof TestCase) {
            // a dummy test case has a coverage of 0%
            if (((TestCase) chromosome.getValue()).isDummy()) {
                MATE.log_warn("Trying to retrieve coverage of dummy test case...");
                return CoverageDTO.getDummyCoverageDTO(coverage);
            }
        }

        String chromosomeId = getChromosomeId(chromosome);

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/combined")
                .withParameter("deviceId", emulator)
                .withParameter("coverage_type", coverage.name())
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosomes", chromosomeId);
        Message response = sendMessage(messageBuilder.build());
        return extractCoverage(response);
    }

    /**
     * Returns a fitness vector for the given chromosome and the specified lines where each entry
     * indicates to which degree the line was covered.
     *
     * @param chromosome The given chromosome.
     * @param numberOfLines The number of lines.
     * @param <T> Indicates the type of the chromosome, i.e. test case or test suite.
     * @return Returns line percentage coverage vector.
     */
    public <T> List<Float> getLinePercentageVector(IChromosome<T> chromosome, int numberOfLines) {

        if (chromosome.getValue() instanceof TestCase) {
            if (((TestCase) chromosome.getValue()).isDummy()) {
                MATE.log_warn("Trying to retrieve line percentage vector of dummy test case...");
                // a dummy test case has a line percentage of 0.0 for each objective (0.0 == worst)
                return Collections.nCopies(numberOfLines, 0.0f);
            }
        }

        String chromosomeId = getChromosomeId(chromosome);

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/coverage/lineCoveredPercentages")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosomes", chromosomeId);
        Message response = sendMessage(messageBuilder.build());

        if (response.getSubject().equals("/error")) {
            MATE.log_acc("Retrieving line covered percentages failed!");
            throw new IllegalStateException(response.getParameter("info"));
        } else {
            List<Float> coveragePercentagesVector = new ArrayList<>();
            String[] coveragePercentages = response.getParameter("coveragePercentages").split("\n");

            for (String coveragePercentage : coveragePercentages) {
                coveragePercentagesVector.add(Float.parseFloat(coveragePercentage));
            }

            return coveragePercentagesVector;
        }
    }

    public void writeFile(String fileName, String content) {
        MATE.log("Writing file " + fileName);
        sendMessage(new Message.MessageBuilder("/utility/write_file").withParameter("fileName", fileName).withParameter("content", content).build());
    }

    /**
     * Extracts the last stack trace from the logcat.
     *
     * @return Returns the last discovered stack trace.
     */
    public String getLastCrashStackTrace() {
        return sendMessage(new Message.MessageBuilder("/crash/stacktrace")
                .withParameter("deviceId", emulator)
                .build()).getParameter("stacktrace");
    }

    /**
     * Returns the key-value pairs defined in the mate.properties file.
     *
     * @return Returns a mapping of the specified properties.
     */
    public Map<String, String> getProperties() {
        return sendMessage(new Message("/properties/get_mate_properties")).getParameters();
    }

    /**
     * Records a screenshot.
     *
     * @param packageName The package name of the AUT.
     * @param nodeId Some id of the screen state.
     */
    public void takeScreenshot(String packageName, String nodeId) {

        sendMessage(new Message.MessageBuilder("/emulator/interaction")
                .withParameter("deviceId", emulator)
                .withParameter("type", "take_screenshot")
                .withParameter("packageName", packageName)
                .withParameter("nodeId", nodeId)
                .build());
    }

    public void markOnImage(List<Widget> widgets, String stateId) {
        sendMessage(new Message.MessageBuilder("/emulator/interaction")
                .withParameter("type", "mark_on_screenshot")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("state", stateId)
                .withParameter("rectangles", widgets.stream().map(w -> String.format("%d,%d,%d,%d", w.getX1(), w.getY1(), w.getX2(), w.getY2())).collect(Collectors.joining(";")))
                .build());
    }

    /**
     * Checks whether a flickering of a screen state can be detected.
     *
     * @param packageName The package name of the screen state.
     * @param stateId Identifies the screen state.
     * @return Returns {@code true} if flickering was detected, otherwise
     *         {@code false} is returned.
     */
    public boolean checkForFlickering(String packageName, String stateId) {

        Message response = sendMessage(new Message.MessageBuilder("/accessibility/check_flickering")
                .withParameter("deviceId", emulator)
                .withParameter("packageName", packageName)
                .withParameter("stateId", stateId)
                .build());

        return Boolean.parseBoolean(response.getParameter("flickering"));
    }

    /**
     * Clears the app cache.
     */
    public void clearAppData() {

        Utils.throwOnInterrupt();

        Message request = new Message.MessageBuilder("/android/clearApp")
                .withParameter("deviceId", emulator)
                .build();

        Message response = null;
        MateInterruptedException mateInterruptedException = null;

        try {
            response = sendMessage(request);
        } catch (MateInterruptedException e) {
            /*
             * We still need to wait for mate-server to complete the request, so we just wait for a
             * few seconds. This is important, because if we continue immediately and try to get the
             * coverage of the last incomplete test case by invoking the tracer, then the tracer's
             * permissions to write to the external storage might get dropped while it tries to write,
             * because clearing the app data also revokes the tracer's permissions. This in turn
             * causes a failure of the tracer to write its traces.
             */
            mateInterruptedException = e;
            Utils.sleepWithoutInterrupt(5000);
        }

        if (response != null && !"/android/clearApp".equals(response.getSubject())) {
            MATE.log_acc("ERROR: unable clear app data");
        }

        if (mateInterruptedException != null) {
            throw mateInterruptedException;
        }
    }

    /**
     * Another accessibility function.
     *
     * @param packageName The package name corresponding to the screen state.
     * @param stateId The id of the screen state.
     * @param widget The widget for which this metric should be evaluated.
     * @return Returns ...
     */
    public double matchesSurroundingColor(String packageName, String stateId, Widget widget) {

        Message response = sendMessage(new Message.MessageBuilder("/accessibility/matches_surrounding_color")
                .withParameter("packageName", packageName)
                .withParameter("stateId", stateId)
                .withParameter("x1", String.valueOf(widget.getX1()))
                .withParameter("x2", String.valueOf(widget.getX2()))
                .withParameter("y1", String.valueOf(widget.getY1()))
                .withParameter("y2", String.valueOf(widget.getY2()))
                .build());

        return Double.parseDouble(response.getParameter("match"));
    }

    /**
     * Retrieves the contrast ratio of the widget residing on the screen state.
     *
     * @param packageName The package name corresponding to the screen state.
     * @param stateId Identifies the screens state.
     * @param widget The widget on which the contrast ratio should be evaluated.
     * @return Returns the contrast ratio.
     */
    public double getContrastRatio(String packageName, String stateId, Widget widget) {

        int maxw = Registry.getUiAbstractionLayer().getScreenWidth();
        int maxh = Registry.getUiAbstractionLayer().getScreenHeight();

        int x1 = widget.getX1();
        int x2 = widget.getX2();
        int y1 = widget.getY1();
        int y2 = widget.getY2();

        // TODO: fix this parameter, it is static and doesn't seem to make sense...
        int borderExpanded = 0;

        if (x1 - borderExpanded >= 0)
            x1 -= borderExpanded;

        if (x2 + borderExpanded <= maxw)
            x2 += borderExpanded;

        if (y1 - borderExpanded >= 0)
            y1 -= borderExpanded;

        if (y2 + borderExpanded <= maxh)
            y2 += borderExpanded;

        Message response = sendMessage(new Message.MessageBuilder("/accessibility/get_contrast_ratio")
                .withParameter("packageName", packageName)
                .withParameter("stateId", stateId)
                .withParameter("x1", String.valueOf(x1))
                .withParameter("x2", String.valueOf(x2))
                .withParameter("y1", String.valueOf(y1))
                .withParameter("y2", String.valueOf(y2))
                .build());

        return Double.parseDouble(response.getParameter("contrastRatio"));
    }

    /**
     * Gets the luminance of the given widget.
     *
     * @param packageName The package name corresponding to the screen state.
     * @param stateId Identifies the screens state.
     * @param widget The widget on which the contrast ratio should be evaluated.
     * @return Returns the luminance of the given widget.
     */
    public String getLuminance(String packageName, String stateId, Widget widget) {

        int maxw = Registry.getUiAbstractionLayer().getScreenWidth();
        int maxh = Registry.getUiAbstractionLayer().getScreenHeight();

        int x1 = widget.getX1();
        int x2 = widget.getX2();
        int y1 = widget.getY1();
        int y2 = widget.getY2();

        // TODO: how was this value chosen?
        int borderExpanded = 1;

        if (x1 - borderExpanded >= 0)
            x1 -= borderExpanded;
        if (x2 + borderExpanded <= maxw)
            x2 += borderExpanded;
        if (y1 - borderExpanded >= 0)
            y1 -= borderExpanded;
        if (y2 + borderExpanded <= maxh)
            y2 += borderExpanded;

        Message response = sendMessage(new Message.MessageBuilder("/accessibility/get_luminance")
                .withParameter("packageName", packageName)
                .withParameter("stateId", stateId)
                .withParameter("x1", String.valueOf(x1))
                .withParameter("x2", String.valueOf(x2))
                .withParameter("y1", String.valueOf(y1))
                .withParameter("y2", String.valueOf(y2))
                .build());

        return response.getParameter("luminance");
    }

    public void sendFlawToServer(String msg) {
        String cmd = "reportFlaw:" + emulator + ":" + msg;
        tunnelLegacyCmd(cmd);
    }

    /**
     * Rotates the emulator into portrait mode.
     * NOTE: The rotation operations are now directly performed by MATE itself.
     */
    @SuppressWarnings("unused")
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

    /**
     * Toggles rotation.
     * NOTE: The rotation operations are now directly performed by MATE itself.
     */
    @SuppressWarnings("unused")
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

    /**
     * Returns the chromosome id of the given chromosome.
     *
     * @param chromosome The chromosome.
     * @param <T> Refers either to a {@link TestCase} or a {@link TestSuite}.
     * @return Returns the chromosome id of the given chromosome.
     */
    private <T> String getChromosomeId(IChromosome<T> chromosome) {

        String chromosomeId = null;

        if (chromosome.getValue() instanceof TestCase) {
            chromosomeId = ((TestCase) chromosome.getValue()).getId();
        } else if (chromosome.getValue() instanceof TestSuite) {
            chromosomeId = ((TestSuite) chromosome.getValue()).getId();
        } else {
            throw new IllegalStateException("Couldn't derive chromosome id for chromosome "
                    + chromosome + "!");
        }
        return chromosomeId;
    }

    /**
     * Returns the novelty vector for the given chromosomes.
     * Note that {@link #storeFitnessData(IChromosome, String, FitnessFunction)} has to be called previously.
     *
     * @param chromosomes The list of chromosomes for which the novelty should be computed.
     * @param nearestNeighbours The number of nearest neighbours k that should be considered.
     * @param objectives The objectives type, e.g. branches.
     * @param <T> Refers to the type of the chromosomes.
     * @return Returns the novelty vector.
     */
    public <T> List<Double> getNoveltyVector(List<IChromosome<T>> chromosomes,
                                             int nearestNeighbours, String objectives) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fitness/get_novelty_vector")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosomes", getChromosomeIds(chromosomes))
                .withParameter("nearestNeighbours", String.valueOf(nearestNeighbours))
                .withParameter("objectives", objectives);

        Message response = sendMessage(messageBuilder.build());

        List<Double> noveltyVector = new ArrayList<>();
        String[] noveltyScores = response.getParameter("novelty_vector").split("\\+");

        for (String noveltyScore : noveltyScores) {
            noveltyVector.add(Double.parseDouble(noveltyScore));
        }

        return noveltyVector;
    }

    /**
     * Retrieves the novelty score for the given chromosome.
     *
     * @param chromosome The chromosome for which the novelty should be evaluated.
     * @param population The current population.
     * @param archive The current archive.
     * @param nearestNeighbours The number of nearest neighbours k.
     * @param objectives The kind of objectives, e.g. branches.
     * @param <T> The type of the chromosomes.
     * @return Returns the novelty for the given chromosome.
     */
    public <T> double getNovelty(IChromosome<T> chromosome, List<IChromosome<T>> population,
                                 List<IChromosome<T>> archive, int nearestNeighbours,
                                 String objectives) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/fitness/get_novelty")
                .withParameter("packageName", Registry.getPackageName())
                .withParameter("chromosome", getChromosomeId(chromosome))
                .withParameter("population", getChromosomeIds(population))
                .withParameter("archive", getChromosomeIds(archive))
                .withParameter("nearestNeighbours", String.valueOf(nearestNeighbours))
                .withParameter("objectives", objectives);

        Message response = sendMessage(messageBuilder.build());
        return Double.parseDouble(response.getParameter("novelty"));
    }

    /**
     * Concatenates the given chromosomes separated by '+' into a single {@link String}.
     *
     * @param chromosomes A list of chromosomes.
     * @param <T> Refers either to a {@link TestCase} or a {@link TestSuite}.
     * @return Returns a single {@link String} containing the chromosome ids.
     */
    private <T> String getChromosomeIds(List<IChromosome<T>> chromosomes) {

        // Java 8: String.join("+", chromosomeIds);
        StringBuilder chromosomeIds = new StringBuilder();

        for (IChromosome<T> chromosome : chromosomes) {
            chromosomeIds.append(getChromosomeId(chromosome));
            chromosomeIds.append("+");
        }

        // remove '+' at the end
        if (chromosomeIds.length() > 0) {
            chromosomeIds.setLength(chromosomeIds.length() - 1);
        }

        return chromosomeIds.toString();
    }

    /**
     * Commands the server to fetch and remove the dot graph from the emulator.
     *
     * @param graphDir The directory on the emulator with the dot file.
     * @param graphFile The file name of the dot graph.
     * @return {@code true}, if the server reports that the operation was successful.
     *         Otherwise, {@code false}.
     */
    public boolean fetchDotGraph(String graphDir, String graphFile) {

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder("/utility/fetch_dot_graph")
                .withParameter("deviceId", emulator)
                .withParameter("dirName", graphDir)
                .withParameter("fileName", graphFile);

        Message response = sendMessage(messageBuilder.build());
        boolean success = Boolean.parseBoolean(response.getParameter("response"));
        MATE.log("Fetching dot file from emulator succeeded: " + success);

        return success;
    }
}
