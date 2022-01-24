package org.mate.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Message {
    private final String subject;
    private final Map<String, String> parameters;

    public Message(String subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Subject cannot be null");
        }
        this.subject = subject;
        this.parameters = new HashMap<>();
    }

    public void addParameter(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Parameter key and value must not be null");
        }
        parameters.put(key, value);
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return subject.equals(message.subject) &&
                parameters.equals(message.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, parameters);
    }

    public static class MessageBuilder {
        private Message message;

        public MessageBuilder(String subject) {
            message = new Message(subject);
        }

        public MessageBuilder withParameter(String key, String value) {
            message.addParameter(key, value);
            return this;
        }

        public Message build() {
            return message;
        }
    }
}
