package org.mate.utils.manifest.element;

import java.util.HashSet;
import java.util.Set;

/**
 * A representation of the data specification, mostly optional. See
 * https://developer.android.com/guide/topics/manifest/data-element for more details.
 */
public class DataDescription {

    // the attributes that describe a data uri
    private final Set<String> schemes;
    private final Set<String> hosts;
    private final Set<String> ports;
    private final Set<String> paths;
    private final Set<String> pathPatterns;
    private final Set<String> pathPrefixes;
    private final Set<String> mimeTypes;

    public DataDescription() {
        schemes = new HashSet<>();
        hosts = new HashSet<>();
        ports = new HashSet<>();
        paths = new HashSet<>();
        pathPatterns = new HashSet<>();
        pathPrefixes = new HashSet<>();
        mimeTypes = new HashSet<>();
    }

    public void addScheme(String scheme) {
        if (scheme != null && !scheme.isEmpty()) {
            schemes.add(scheme);
        }
    }

    public void addHost(String host) {
        if (host != null && !host.isEmpty()) {
            hosts.add(host);
        }
    }

    public void addPort(String port) {
        if (port != null && !port.isEmpty()) {
            ports.add(port);
        }
    }

    public void addPath(String path) {
        if (path != null && !path.isEmpty()) {
            paths.add(path.replace("\\\\", "\\"));
        }
    }

    public void addPathPattern(String pathPattern) {
        if (pathPattern != null && !pathPattern.isEmpty()) {
            pathPatterns.add(pathPattern.replace("\\\\", "\\"));
        }
    }

    public void addPathPrefix(String pathPrefix) {
        if (pathPrefix != null && !pathPrefix.isEmpty()) {
            pathPrefixes.add(pathPrefix.replace("\\\\", "\\"));
        }
    }

    public void addMimeType(String mimeType) {
        if (mimeType != null && !mimeType.isEmpty()) {
            mimeTypes.add(mimeType);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Schemes: " + schemes + ", ");
        builder.append("Hosts: " + hosts + ", ");
        builder.append("Ports: " + ports + ", ");
        builder.append("Paths: " + paths + ", ");
        builder.append("PathPatterns: " + pathPatterns + ", ");
        builder.append("PathPrefixes: " + pathPrefixes + ", ");
        builder.append("MimeTypes: " + mimeTypes);
        return builder.toString();
    }

    // TODO: add boolean contains() method for each attribute (check for != null)

    public boolean hasScheme() {
        return !schemes.isEmpty();
    }

    public boolean hasHost() {
        return !hosts.isEmpty();
    }

    public boolean hasPort() {
        return !ports.isEmpty();
    }

    public boolean hasMimeType() { return !mimeTypes.isEmpty(); }

    public boolean hasPath() { return !paths.isEmpty(); }

    public boolean hasPathPrefix() { return !pathPrefixes.isEmpty(); }

    public boolean hasPathPattern() { return !pathPatterns.isEmpty(); }

    public Set<String> getSchemes() {
        return schemes;
    }

    public Set<String> getHosts() {
        return hosts;
    }

    public Set<String> getPorts() {
        return ports;
    }

    public Set<String> getPaths() {
        return paths;
    }

    public Set<String> getPathPatterns() {
        return pathPatterns;
    }

    public Set<String> getPathPrefixes() {
        return pathPrefixes;
    }

    public Set<String> getMimeTypes() {
        return mimeTypes;
    }
}
