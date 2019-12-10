package org.mate.interaction.intent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a single intent-filter tag and the information it contains,
 * i.e. the declared actions, categories and data attributes. Each
 * intent-filter is typically represent by an arbitrary combination
 * of action, category and data tags.
 */
class IntentFilterDescription {

    private Set<String> actions = new HashSet<>();
    private Set<String> categories = new HashSet<>();
    private Set<Data> data = new HashSet<>();

    void addAction(String action) {
        actions.add(action);
    }

    void addCategory(String category) {
        categories.add(category);
    }

    void addData(String scheme, String host, String port, String path, String pathPattern,
                 String pathPrefix, String mimeType) {
        // TODO: if already some data tag has been added, may merge with new data tag or keep them separate as right now
        data.add(new Data(scheme, host, port, path, pathPattern, pathPrefix, mimeType));
    }

    boolean hasAction() {
        return !actions.isEmpty();
    }

    boolean hasCategory() {
        return !categories.isEmpty();
    }

    boolean hasData() {
        return !data.isEmpty();
    }

    Set<String> getActions() { return actions; }

    Set<String> getCategories() { return categories; }

    Set<Data> getData() { return data; }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Actions: " + actions + System.lineSeparator());
        builder.append("Categories: " + categories + System.lineSeparator());
        builder.append("Data: " + System.lineSeparator());
        for (Data data : data) {
            builder.append(data + System.lineSeparator());
        }
        return builder.toString();
    }


    /**
     * A representation for the data tag potentially contained
     * in an intent-filter tag.
     */
    public class Data {

        private final String scheme;
        private final String host;
        private final String port;
        private final String path;
        private final String pathPattern;
        private final String pathPrefix;
        private final String mimeType;

        Data(String scheme, String host, String port, String path, String pathPattern,
                    String pathPrefix, String mimeType) {
            this.scheme = scheme;
            this.host = host;
            this.port = port;
            this.path = path;
            this.pathPattern = pathPattern;
            this.pathPrefix = pathPrefix;
            this.mimeType = mimeType;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Scheme: " + scheme + ", ");
            builder.append("Host: " + host + ", ");
            builder.append("Port: " + port + ", ");
            builder.append("Path: " + path + ", ");
            builder.append("PathPattern: " + pathPattern + ", ");
            builder.append("PathPrefix: " + pathPrefix + ", ");
            builder.append("MimeType: " + mimeType);
            return builder.toString();
        }

        // TODO: add boolean contains() method for each attribute (check for != null)
    }


}
