package org.mate.commons.utils.manifest.element;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A representation of the data specification, mostly optional. See
 * https://developer.android.com/guide/topics/manifest/data-element for more details.
 */
public class DataDescription implements Parcelable {

    // the attributes that describe a data uri
    private final Set<String> schemes;
    private final Set<String> hosts;
    private final Set<String> ports;
    private final Set<String> paths;
    private final Set<String> pathPatterns;
    private final Set<String> pathPrefixes;
    private final Set<String> mimeTypes;

    /**
     * Initialises an empty data specification.
     */
    public DataDescription() {
        schemes = new HashSet<>();
        hosts = new HashSet<>();
        ports = new HashSet<>();
        paths = new HashSet<>();
        pathPatterns = new HashSet<>();
        pathPrefixes = new HashSet<>();
        mimeTypes = new HashSet<>();
    }

    /**
     * Adds a scheme to the data specification.
     *
     * @param scheme The scheme to be added.
     */
    public void addScheme(String scheme) {
        if (scheme != null && !scheme.isEmpty()) {
            schemes.add(scheme);
        }
    }

    /**
     * Adds a host to the data specification.
     *
     * @param host The host to be added.
     */
    public void addHost(String host) {
        if (host != null && !host.isEmpty()) {
            hosts.add(host);
        }
    }

    /**
     * Adds a port to the data specification.
     *
     * @param port The port to be added.
     */
    public void addPort(String port) {
        if (port != null && !port.isEmpty()) {
            ports.add(port);
        }
    }

    /**
     * Adds a path to the data specification.
     *
     * @param path The path to be added.
     */
    public void addPath(String path) {
        if (path != null && !path.isEmpty()) {
            paths.add(path.replace("\\\\", "\\"));
        }
    }

    /**
     * Adds a path pattern to the data specification.
     *
     * @param pathPattern The path pattern to be added.
     */
    public void addPathPattern(String pathPattern) {
        if (pathPattern != null && !pathPattern.isEmpty()) {
            pathPatterns.add(pathPattern.replace("\\\\", "\\"));
        }
    }

    /**
     * Adds a path prefix to the data specification.
     *
     * @param pathPrefix The path prefix to be added.
     */
    public void addPathPrefix(String pathPrefix) {
        if (pathPrefix != null && !pathPrefix.isEmpty()) {
            pathPrefixes.add(pathPrefix.replace("\\\\", "\\"));
        }
    }

    /**
     * Adds a mime type to the data specification.
     *
     * @param mimeType The mime type to be added.
     */
    public void addMimeType(String mimeType) {
        if (mimeType != null && !mimeType.isEmpty()) {
            mimeTypes.add(mimeType);
        }
    }

    /**
     * Provides a textual representation of the data specification.
     *
     * @return Returns the string representation of the data specification.
     */
    @NonNull
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

    /**
     * Whether a scheme was added to the data specification.
     *
     * @return Returns {@code true} if a scheme was added, otherwise {@code false} is returned.
     */
    public boolean hasScheme() {
        return !schemes.isEmpty();
    }

    /**
     * Whether a host was added to the data specification.
     *
     * @return Returns {@code true} if a host was added, otherwise {@code false} is returned.
     */
    public boolean hasHost() {
        return !hosts.isEmpty();
    }

    /**
     * Whether a port was added to the data specification.
     *
     * @return Returns {@code true} if a port was added, otherwise {@code false} is returned.
     */
    public boolean hasPort() {
        return !ports.isEmpty();
    }

    /**
     * Whether a mime type was added to the data specification.
     *
     * @return Returns {@code true} if a mime type was added, otherwise {@code false} is returned.
     */
    public boolean hasMimeType() { return !mimeTypes.isEmpty(); }

    /**
     * Whether a path was added to the data specification.
     *
     * @return Returns {@code true} if a path was added, otherwise {@code false} is returned.
     */
    public boolean hasPath() { return !paths.isEmpty(); }

    /**
     * Whether a path prefix was added to the data specification.
     *
     * @return Returns {@code true} if a path prefix was added, otherwise {@code false} is returned.
     */
    public boolean hasPathPrefix() { return !pathPrefixes.isEmpty(); }

    /**
     * Whether a path pattern was added to the data specification.
     *
     * @return Returns {@code true} if a path pattern was added, otherwise {@code false} is returned.
     */
    public boolean hasPathPattern() { return !pathPatterns.isEmpty(); }

    /**
     * Returns the added schemes.
     *
     * @return Returns the schemes of the data specification.
     */
    public Set<String> getSchemes() {
        return schemes;
    }

    /**
     * Returns the added hosts.
     *
     * @return Returns the hosts of the data specification.
     */
    public Set<String> getHosts() {
        return hosts;
    }

    /**
     * Returns the added ports.
     *
     * @return Returns the ports of the data specification.
     */
    public Set<String> getPorts() {
        return ports;
    }

    /**
     * Returns the added paths.
     *
     * @return Returns the paths of the data specification.
     */
    public Set<String> getPaths() {
        return paths;
    }

    /**
     * Returns the added path patterns.
     *
     * @return Returns the path patterns of the data specification.
     */
    public Set<String> getPathPatterns() {
        return pathPatterns;
    }

    /**
     * Returns the added path prefixes.
     *
     * @return Returns the path prefixes of the data specification.
     */
    public Set<String> getPathPrefixes() {
        return pathPrefixes;
    }

    /**
     * Returns the added mime types.
     *
     * @return Returns the mime types of the data specification.
     */
    public Set<String> getMimeTypes() {
        return mimeTypes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(new ArrayList<>(this.schemes));
        dest.writeStringList(new ArrayList<>(this.hosts));
        dest.writeStringList(new ArrayList<>(this.ports));
        dest.writeStringList(new ArrayList<>(this.paths));
        dest.writeStringList(new ArrayList<>(this.pathPatterns));
        dest.writeStringList(new ArrayList<>(this.pathPrefixes));
        dest.writeStringList(new ArrayList<>(this.mimeTypes));
    }

    protected DataDescription(Parcel in) {
        this.schemes =new HashSet<>(in.createStringArrayList());
        this.hosts =new HashSet<>(in.createStringArrayList());
        this.ports =new HashSet<>(in.createStringArrayList());
        this.paths =new HashSet<>(in.createStringArrayList());
        this.pathPatterns =new HashSet<>(in.createStringArrayList());
        this.pathPrefixes =new HashSet<>(in.createStringArrayList());
        this.mimeTypes =new HashSet<>(in.createStringArrayList());
    }

    public static final Creator<DataDescription> CREATOR = new Creator<DataDescription>() {
        @Override
        public DataDescription createFromParcel(Parcel source) {
            return new DataDescription(source);
        }

        @Override
        public DataDescription[] newArray(int size) {
            return new DataDescription[size];
        }
    };
}
