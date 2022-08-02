package org.mate.utils.testcase.espresso;

import android.support.annotation.NonNull;

/**
 * Describes the supported espresso dependencies.
 */
public enum EspressoDependency {

    /**
     * On view espresso dependency.
     */
    ON_VIEW(true, "android.support.test.espresso.Espresso", "onView"),

    /**
     * On data espresso dependency.
     */
    ON_DATA(true, "android.support.test.espresso.Espresso", "onData"),

    /**
     * Click espresso dependency.
     */
    CLICK(true, "android.support.test.espresso.action.ViewActions", "click"),

    /**
     * Long click espresso dependency.
     */
    LONG_CLICK(true, "android.support.test.espresso.action.ViewActions", "longClick"),

    /**
     * Swipe up espresso dependency.
     */
    SWIPE_UP(true, "android.support.test.espresso.action.ViewActions", "swipeUp"),

    /**
     * Swipe down espresso dependency.
     */
    SWIPE_DOWN(true, "android.support.test.espresso.action.ViewActions", "swipeDown"),

    /**
     * Swipe left espresso dependency.
     */
    SWIPE_LEFT(true, "android.support.test.espresso.action.ViewActions", "swipeLeft"),

    /**
     * Swipe right espresso dependency.
     */
    SWIPE_RIGHT(true, "android.support.test.espresso.action.ViewActions", "swipeRight"),

    /**
     * Clear text espresso dependency.
     */
    CLEAR_TEXT(true, "android.support.test.espresso.action.ViewActions", "clearText"),

    /**
     * Close soft keyboard espresso dependency.
     */
    CLOSE_SOFT_KEYBOARD(true, "android.support.test.espresso.action.ViewActions", "closeSoftKeyboard"),

    /**
     * Press back espresso dependency.
     */
    PRESS_BACK(true, "android.support.test.espresso.Espresso", "pressBackUnconditionally"),

    /**
     * Press menu espresso dependency.
     */
    PRESS_MENU(true,"android.support.test.espresso.action.ViewActions",  "pressMenuKey"),

    /**
     * Press key espresso dependency.
     */
    PRESS_KEY(true, "android.support.test.espresso.action.ViewActions", "pressKey"),

    /**
     * Scroll to espresso dependency.
     */
    SCROLL_TO(true, "android.support.test.espresso.action.ViewActions", "scrollTo"),

    /**
     * Type text espresso dependency.
     */
    TYPE_TEXT(true, "android.support.test.espresso.action.ViewActions", "typeText"),

    /**
     * Is root espresso dependency.
     */
    IS_ROOT(true, "android.support.test.espresso.matcher.ViewMatchers", "isRoot"),

    /**
     * With text espresso dependency.
     */
    WITH_TEXT(true, "android.support.test.espresso.matcher.ViewMatchers", "withText"),

    /**
     * With id espresso dependency.
     */
    WITH_ID(true, "android.support.test.espresso.matcher.ViewMatchers", "withId"),

    /**
     * All of espresso dependency.
     */
    ALL_OF(true, "org.hamcrest.CoreMatchers", "allOf"),

    /**
     * Contains string espresso dependency.
     */
    CONTAINS_STRING(true, "org.hamcrest.CoreMatchers", "containsString"),

    /**
     * Espresso espresso dependency.
     */
    ESPRESSO(false, "android.support.test.espresso", "Espresso"),

    /**
     * '@LargeTest' annotation.
     */
    LARGE_TEST(false, "android.support.test.filters", "LargeTest"),

    /**
     * Activity test rule espresso dependency.
     */
    ACTIVITY_TEST_RULE(false, "android.support.test.rule", "ActivityTestRule"),

    /**
     * Android junit 4 espresso dependency.
     */
    ANDROID_JUNIT_4(false, "android.support.test.runner", "AndroidJUnit4"),

    /**
     * Key event espresso dependency.
     */
    KEY_EVENT(false, "android.view", "KeyEvent"),

    /**
     * Rule espresso dependency.
     */
    RULE(false, "org.junit", "Rule"),

    /**
     * '@Test' annotation.
     */
    TEST(false, "org.junit", "Test"),

    /**
     * '@RunWith(TestRunner)' annotation.
     */
    RUN_WITH(false, "org.junit.runner", "RunWith");

    /**
     * Whether the dependency is a static dependency.
     */
    private final boolean staticDependency;

    /**
     * The package name of the dependency.
     */
    private final String packageName;

    /**
     * The class name of the dependency.
     */
    private final String className;

    /**
     * This builder keeps track of all used espresso dependencies. Whenever {@link #toString()} is
     * called, the builder records the dependency.
     */
    private static final EspressoDependencyBuilder builder = EspressoDependencyBuilder.getInstance();

    /**
     * Creates a new espresso dependency with the given name.
     *
     * @param staticDependency Whether the dependency is static or not.
     * @param packageName The package name of the dependency.
     * @param className The class name of the dependency.
     */
    EspressoDependency(boolean staticDependency, String packageName, String className) {
        this.staticDependency = staticDependency;
        this.packageName = packageName;
        this.className = className;
    }

    /**
     * Returns the full-qualified name (FQN) consisting of package and class name.
     *
     * @return Returns the FQN of the dependency.
     */
    public String getFullQualifiedName() {
        return packageName + "." + className;
    }

    /**
     * Returns a textual representation of the dependency in the form of the class name.
     * Note that calling this method registers the dependency.
     *
     * @return Returns the string representation of the espresso dependency.
     */
    @NonNull
    @Override
    public String toString() {
        builder.register(this);
        return className;
    }

    /**
     * Whether this dependency is a static dependency or not.
     *
     * @return Returns {@code true} if the dependency is a static dependency, otherwise
     *         {@code false} is returned.
     */
    public boolean isStaticDependency() {
        return staticDependency;
    }
}
