package org.mate.utils;

import android.support.annotation.NonNull;

import org.mate.MATE;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents an abstraction of a real stack trace.
 */
public class StackTrace {

    /**
     * The exception type.
     */
    private String type;

    /**
     * The optional message.
     */
    private String message;

    /**
     * The actual stack trace.
     */
    private List<String> stackTrace;

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

        // TODO: remove object references from the exception message to enable a valid comparison

        // TODO: may update type and message with later type and message from 'Caused by' line

        String[] lines = Arrays.stream(log.split("\n"))
                .filter(line -> line.contains("E AndroidRuntime:"))
                .map(line -> line.split("E AndroidRuntime:")[1])
                .map(String::trim)
                .toArray(String[]::new);

        MATE.log_acc("Lines: ");
        MATE.log_acc("" + Arrays.asList(lines));

        // the pid and the process name are contained in the second line
        String[] tokens = lines[1].split(",");

        process = tokens[0].split("Process: ")[1].trim();
        pid = Integer.parseInt(tokens[1].split("PID: ")[1].trim());

        // the exception type and the message are contained in the third line
        String thirdLine = lines[2];
        tokens = thirdLine.split(":", 1);

        if (tokens.length > 1) {
            // the message is optional
            type = tokens[0].trim();
            message = tokens[1].trim();
        } else {
            type = thirdLine.trim();
        }

        // each line starting with 'at' or 'Caused by' belongs to the stack trace
        stackTrace = Arrays.stream(lines)
                .filter(line -> line.startsWith("at") || line.startsWith("Caused by"))
                .collect(Collectors.toList());
    }

    /**
     * Compares two stack traces for equality. We consider two stack traces equal iff they have
     * the same exception type, message and stack trace lines as well as origin from the same process.
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
                Objects.equals(stackTrace, other.stackTrace) &&
                Objects.equals(process, other.process);
    }

    /**
     * Computes a hash code for stack trace.
     *
     * @return Returns the hash code associated with the stack trace.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, message, stackTrace, process);
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
                "type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", stackTrace=" + stackTrace +
                ", process='" + process + '\'' +
                ", pid=" + pid +
                '}';
    }
}
