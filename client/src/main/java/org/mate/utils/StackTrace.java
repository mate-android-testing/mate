package org.mate.utils;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents an abstraction of a real stack trace.
 */
public class StackTrace {

    /**
     * The pattern to recognize object references of the form '@e2b062c'.
     */
    private static final Pattern OBJECT_REFERENCE_PATTERN = Pattern.compile("(\\w+\\.)*(\\w+)@[abcdef\\d]+");

    /**
     * The exception type.
     */
    private String type;

    /**
     * The optional message.
     */
    private String message;

    /**
     * The list of method calls.
     */
    private List<String> methodCalls;

    /**
     * The list of 'Caused by' lines.
     */
    private List<String> causedByLines;

    /**
     * The process that has thrown the exception.
     */
    private String process;

    /**
     * The pid of the process.
     */
    private int pid;

    /**
     * Constructs a new stack trace by parsing the given log.
     *
     * @param log The log describing the stack trace..
     */
    public StackTrace(String log) {
        parseStackTrace(log);
    }

    /**
     * Extracts the stack trace from the given log.
     *
     * @param log The given log.
     */
    private void parseStackTrace(String log) {

        String[] lines = Arrays.stream(log.split("\n"))
                .filter(line -> line.contains("E AndroidRuntime:"))
                .map(line -> line.split("E AndroidRuntime:")[1])
                .map(String::trim)
                .toArray(String[]::new);

        // the pid and the process name are contained in the second line
        String[] tokens = lines[1].split(",");

        process = tokens[0].split("Process: ")[1].trim();
        pid = Integer.parseInt(tokens[1].split("PID: ")[1].trim());

        // the exception type and the message are contained in the third line
        parseExceptionLine(lines[2]);

        // each line starting with 'at' describes a method call
        methodCalls = Arrays.stream(lines)
                .filter(line -> line.startsWith("at"))
                .collect(Collectors.toList());

        // TODO: may update type and message with later type and message from 'Caused by' line

        // each line starting with 'Caused by' describes a further
        causedByLines = Arrays.stream(lines)
                .filter(line -> line.startsWith("Caused by"))
                .map(this::removeObjectReferences)
                .collect(Collectors.toList());
    }

    /**
     * Extracts from exception line the type and the message if present.
     *
     * @param exceptionLine The exception line containing the type and the optional message.
     */
    private void parseExceptionLine(String exceptionLine) {

        String[] tokens = exceptionLine.split(":", 2);

        if (tokens.length > 1) {
            // the message is optional
            type = tokens[0].trim();
            message = removeObjectReferences(tokens[1].trim());
        } else {
            type = exceptionLine.trim();
        }
    }

    /**
     * Removes any object reference from the given input string.
     *
     * @param input The input string.
     * @return Returns the input string without the object references.
     */
    private String removeObjectReferences(String input) {
        Matcher matcher = OBJECT_REFERENCE_PATTERN.matcher(input);
        while (matcher.find()) {
            String match = matcher.group(0);
            String objectReference = match.substring(match.indexOf('@'));
            input = input.replace(objectReference, "");
        }
        return input;
    }

    /**
     * Compares two stack traces for equality. We consider two stack traces equal iff they have
     * the same exception type, message and method calls as well as origin from the same process.
     *
     * @param o The other stack trace.
     * @return Returns {@code true} if the stack traces are equal, otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackTrace other = (StackTrace) o;
        return Objects.equals(type, other.type) &&
                Objects.equals(message, other.message) &&
                Objects.equals(methodCalls, other.methodCalls) &&
                Objects.equals(causedByLines, other.causedByLines) &&
                Objects.equals(process, other.process);
    }

    /**
     * Computes a hash code for stack trace.
     *
     * @return Returns the hash code associated with the stack trace.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, message, methodCalls, causedByLines, process);
    }

    /**
     * Provides a simple textual representation of the stack trace.
     *
     * @return Returns the string representation of the stack trace.
     */
    @NonNull
    @Override
    public String toString() {
        return "StackTrace{" +
                "process='" + process + '\'' +
                ", pid=" + pid +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", methodCalls=" + methodCalls +
                ", causedByLines=" + causedByLines +
                '}';
    }
}
