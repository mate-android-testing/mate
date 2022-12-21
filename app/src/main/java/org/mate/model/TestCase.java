package org.mate.model;

import android.support.annotation.NonNull;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.crash_reproduction.fitness.CrashDistance;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.interaction.action.ui.PrimitiveAction;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Optional;
import org.mate.utils.Randomness;
import org.mate.utils.StackTrace;
import org.mate.utils.coverage.CoverageUtils;
import org.mate.utils.testcase.TestCaseStatistics;
import org.mate.utils.testcase.serialization.TestCaseSerializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestCase {

    /**
     * A random generated id that uniquely identifies the test case.
     * Also used as the string representation.
     */
    private String id;

    /**
     * The set of visited activities.
     */
    private final Set<String> visitedActivities;

    /**
     * The set of visited screen states (ids).
     */
    private final Set<String> visitedStates;

    /**
     * The actions that has been executed by this test case.
     */
    private final List<Action> eventSequence;

    /**
     * The visited activities in the order they appeared.
     */
    private final List<String> activitySequence;

    private final List<IScreenState> stateSequence = new LinkedList<>();

    public List<IScreenState> getStateSequence() {
        return stateSequence;
    }

    /**
     * Whether a crash has been triggered by an action of the test case.
     */
    private boolean crashDetected;

    /**
     * The desired size of the test case, i.e. the desired length
     * of the test case. This doesn't enforce any size restriction yet.
     */
    private Optional<Integer> desiredSize = Optional.none();

    /**
     * The stack trace that has been triggered by a potential crash.
     * Only recorded when {@link org.mate.Properties#RECORD_STACK_TRACE()} is defined.
     */
    private StackTrace crashStackTrace = null;

    /**
     * Should be used for the creation of dummy test cases.
     * This suppresses the log that indicates a new test case
     * for the AndroidAnalysis framework.
     */
    private TestCase() {
        setId("dummy");
        crashDetected = false;
        visitedActivities = new HashSet<>();
        visitedStates = new HashSet<>();
        eventSequence = new ArrayList<>();
        activitySequence = new ArrayList<>();
    }

    /**
     * Creates a new test case object with the given id.
     *
     * @param id The (unique) test case id.
     */
    public TestCase(String id) {
        MATE.log("Initialising new test case!");
        MATE.log("Testcase has the ID: " + id);
        setId(id);
        crashDetected = false;
        visitedActivities = new HashSet<>();
        visitedStates = new HashSet<>();
        eventSequence = new ArrayList<>();
        activitySequence = new ArrayList<>();
    }

    /**
     * Checks whether this is a dummy test case.
     *
     * @return Returns {@code true} when this test case is a dummy test case,
     *          otherwise {@code false} is returned.
     */
    public boolean isDummy() {
        return getId().equals("dummy");
    }

    /**
     * Should be called (once) after the test case has been created and executed.
     *
     * Among other things, this method is responsible for the serialization
     * of a test case (if desired), the recording of test case stats (if desired)
     * and so on.
     */
    public void finish() {
        MATE.log("Finishing test case!");

        MATE.log("Found crash: " + hasCrashDetected());

        // serialization of test case
        if (Properties.RECORD_TEST_CASE()) {
            TestCaseSerializer.serializeTestCase(this);
        }

        // record stats about a test case, in particular about intent based actions
        if (Properties.RECORD_TEST_CASE_STATS()) {
            TestCaseStatistics.recordStats(this);
        }

        if (Properties.TARGET().equals("stack_trace")) {
            Chromosome<TestCase> chromosome = new Chromosome<>(this);

            MATE.log("Testcase fitness: " + new CrashDistance().getFitness(chromosome));
            Registry.getEnvironmentManager().logReachedTargets(chromosome);
        }
        // TODO: log the test case actions in a proper format
    }

    /**
     * Returns the activity name before the execution of the given action.
     * @param actionIndex The action index.
     * @return Returns the activity in foreground before the given action was executed.
     */
    public String getActivityBeforeAction(int actionIndex) {
        return activitySequence.get(actionIndex);
    }

    /**
     * Returns the name of the activity that is in the foreground after the execution
     * of the n-th {@param actionIndex} action.
     *
     * @param actionIndex The action index.
     * @return Returns the activity name after the execution of the {@param actionIndex} action.
     */
    public String getActivityAfterAction(int actionIndex) {
        // the activity sequence models a 'activity-before-action' relation
        return activitySequence.get(actionIndex + 1);
    }

    /**
     * Returns the activity sequence (in order) that has been covered through the execution
     * of the test case actions.
     *
     * @return Returns the activity sequence in the order they have been visited.
     */
    public List<String> getActivitySequence() {
        return activitySequence;
    }

    /**
     * Sets a desired length for the test case, i.e. the maximum
     * number of of actions. This doesn't enforce any size restriction yet.
     *
     * @param desiredSize A desired length for the test case.
     */
    public void setDesiredSize(Optional<Integer> desiredSize) {
        this.desiredSize = desiredSize;
    }

    /**
     * Returns the desired size for the test case, i.e. a desired
     * length of the test case.
     *
     * @return Returns the desired size.
     */
    @SuppressWarnings("unused")
    public Optional<Integer> getDesiredSize() {
        return desiredSize;
    }

    /**
     * Returns the unique id of the test case.
     *
     * @return Returns the test case id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the test case id to the given value.
     *
     * @param id The new test case id.
     */
    private void setId(String id) {
        this.id = id;
    }

    /**
     * Adds a new action to the list of executed actions.
     *
     * @param event The action to be added.
     */
    public void addEvent(Action event) {
        this.eventSequence.add(event);
    }

    /**
     * Updates the set of visited activities.
     *
     * @param activity A new activity to be added.
     */
    public void updateVisitedActivities(String activity) {
        this.visitedActivities.add(activity);
    }

    /**
     * Returns the set of visited activities.
     *
     * @return Returns the visited activities.
     */
    public Set<String> getVisitedActivities() {
        return visitedActivities;
    }

    /**
     * Updates the visited states with a new screen state.
     *
     * @param GUIState The new screen state.
     */
    public void updateVisitedStates(IScreenState GUIState) {
        this.visitedStates.add(GUIState.getId());
        this.stateSequence.add(GUIState);
    }

    /**
     * Returns the visited screen states, actually the screen state ids.
     *
     * @return Returns the visited states.
     */
    public Set<String> getVisitedStates() {
        return visitedStates;
    }

    /**
     * Returns the list of executed actions.
     *
     * @return Returns the action sequence.
     */
    public List<Action> getEventSequence() {
        return this.eventSequence;
    }

    /**
     * Checks whether the test case caused a crash.
     *
     * @return Returns {@code true} if the test case caused a crash,
     *          otherwise {@code false} is returned.
     */
    public boolean hasCrashDetected() {
        return this.crashDetected;
    }

    /**
     * Sets the crash flag.
     */
    public void setCrashDetected() {
        this.crashDetected = true;
    }

    /**
     * Returns the stack trace triggered by a crash of the test case.
     *
     * @return Returns the stack trace caused by the test case;
     *          this should be typically the last action.
     */
    @SuppressWarnings("unused")
    public StackTrace getCrashStackTrace() {
        if (Properties.RECORD_STACK_TRACE()) {
            return crashStackTrace;
        } else {
            throw new IllegalStateException("Recording stack trace is not enabled!");
        }
    }

    /**
     * Creates a dummy test case intended to be not used for execution.
     *
     * @return Returns a dummy test case.
     */
    public static TestCase newDummy() {
        return new TestCase();
    }

    /**
     * Creates a test case from a given dummy test case. This
     * causes the execution of actions declared by the dummy test case.
     *
     * @param testCase The dummy test case.
     * @return Returns a test case that executed the actions of the dummy.
     */
    public static TestCase fromDummy(TestCase testCase) {

        Registry.getUiAbstractionLayer().resetApp();
        TestCase resultingTc = newInitializedTestCase();

        int finalSize = testCase.eventSequence.size();

        if (testCase.desiredSize.hasValue()) {
            finalSize = testCase.desiredSize.getValue();
        }

        try {
            int count = 0;
            for (Action action0 : testCase.eventSequence) {
                if (count < finalSize) {
                    if (!(action0 instanceof WidgetAction)
                            || Registry.getUiAbstractionLayer().getExecutableActions().contains(action0)) {
                        if (!resultingTc.updateTestCase(action0, count)) {
                            return resultingTc;
                        }
                        count++;
                    } else {
                        break;
                    }
                } else {
                    return resultingTc;
                }
            }
            for (; count < finalSize; count++) {
                Action action;
                if (Properties.WIDGET_BASED_ACTIONS()) {
                    action = Randomness.randomElement(Registry.getUiAbstractionLayer().getExecutableActions());
                } else {
                    action = PrimitiveAction.randomAction();
                }
                if (!resultingTc.updateTestCase(action, count)) {
                    return resultingTc;
                }
            }

            return resultingTc;
        } finally {
            // TODO ugly hack that ensures that coverage+fitness is stored before TestCase#finish is called
            IChromosome<TestCase> chromosome = new Chromosome<>(resultingTc);
            FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);

            // serialize test case, record test case stats, etc.
            resultingTc.finish();
        }
    }

    /**
     * Returns the string representation of a test case.
     * This is the unique test case id for now.
     *
     * @return Returns the test case representation.
     */
    @NonNull
    @Override
    public String toString() {
        return getId();
    }

    /**
     * Initializes a new test case with a random id.
     *
     * @return Returns a new test case with a random id.
     */
    public static TestCase newInitializedTestCase() {
        TestCase tc = new TestCase(UUID.randomUUID().toString());
        tc.updateTestCase("init");
        return tc;
    }

    /**
     * Executes the given action and updates the test case accordingly.
     *
     * @param action The action to be executed.
     * @param actionID The id of the action.
     * @return Returns {@code true} if the given action didn't cause a crash of the app
     *          or left the AUT, otherwise {@code false} is returned.
     */
    public boolean updateTestCase(Action action, int actionID) {

        if (action instanceof WidgetAction
                && !Registry.getUiAbstractionLayer().getExecutableActions().contains(action)) {
            throw new IllegalStateException("Action not applicable to current state!");
        }

        String activityBeforeAction = Registry.getUiAbstractionLayer().getLastScreenState().getActivityName();
        MATE.log("executing action " + actionID + ": " + action);

        addEvent(action);
        ActionResult actionResult = Registry.getUiAbstractionLayer().executeAction(action);

        // track the activity transitions of each action
        String activityAfterAction = Registry.getUiAbstractionLayer().getLastScreenState().getActivityName();

        if (actionID == 0) {
            activitySequence.add(activityBeforeAction);
            activitySequence.add(activityAfterAction);
        } else {
            activitySequence.add(activityAfterAction);
        }

        MATE.log("executed action " + actionID + ": " + action);
        MATE.log("Activity Transition for action " +  actionID
                + ":" + activityBeforeAction  + "->" + activityAfterAction);
        updateTestCase(String.valueOf(actionID));

        switch (actionResult) {
            case SUCCESS:
            case SUCCESS_NEW_STATE:
                return true;
            case FAILURE_APP_CRASH:
                setCrashDetected();
                if (Properties.RECORD_STACK_TRACE()) {
                    crashStackTrace = Registry.getUiAbstractionLayer().getLastCrashStackTrace();
                }
            case SUCCESS_OUTBOUND:
                return false;
            case FAILURE_UNKNOWN:
            case FAILURE_EMULATOR_CRASH:
                return false;
            default:
                throw new UnsupportedOperationException("Encountered an unknown action result. Cannot continue.");
        }
    }

    /**
     * Updates the test case with the given event.
     *
     * @param event A new event, e.g. the action id.
     */
    private void updateTestCase(String event) {
        IScreenState currentScreenState = Registry.getUiAbstractionLayer().getLastScreenState();
        updateVisitedStates(currentScreenState);
        updateVisitedActivities(currentScreenState.getActivityName());
    }

    public boolean reachedTarget(List<String> targetStackTrace) {
        return hasCrashDetected() && stackTraceMatchesTarget(getCrashStackTrace().getMethodCalls(), targetStackTrace);
    }

    private boolean stackTraceMatchesTarget(List<String> detectedStackTrace, List<String> targetStackTrace) {
        String packageName = Registry.getPackageName();
        List<Function<String, String>> allowedAUTTransformations = new LinkedList<>();
        // Exact match
        allowedAUTTransformations.add(Function.identity());
        // Match filename and line number (sometimes the name of anonymous classes is not the same)
        allowedAUTTransformations.add(line -> !line.contains("Native Method") ? line.split("\\(")[1].split("\\)")[0] : line);

        List<Function<String, String>> allowedTransformations = new LinkedList<>(allowedAUTTransformations);
        // Match without linenumber (different java implementations)
        allowedTransformations.add(line -> line.split("\\(")[0]);

        List<String> noMatch = new LinkedList<>();

        for (String line : targetStackTrace) {
            List<Function<String, String>> transformationsToTry = line.contains(packageName)
                    ? allowedAUTTransformations
                    : allowedTransformations;

            if (transformationsToTry.stream().noneMatch(transformation -> detectedStackTrace.stream().map(transformation).anyMatch(l -> l.equals(transformation.apply(line))))) {
                noMatch.add(line);
            }
        }

        // Ignore internal implementation differences
        noMatch.removeIf(line -> line.contains("at dalvik.")
                || line.contains("at java.")
                || line.contains("at android.os")
                || line.contains("at android.widget")
                || line.contains("at android.support")
                || line.contains("at com.android.internal.")
        );

        return noMatch.size() <= 1;
    }
}
