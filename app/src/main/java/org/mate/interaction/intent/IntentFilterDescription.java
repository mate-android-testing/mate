package org.mate.interaction.intent;

import android.net.Uri;
import android.support.test.InstrumentationRegistry;

import org.mate.MATE;
import org.mate.utils.Randomness;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Represents a single intent-filter tag and the information it contains,
 * i.e. the declared actions, categories and data attributes. Each
 * intent-filter is typically represent by an arbitrary combination
 * of action, category and data tags.
 */
public class IntentFilterDescription {

    private Set<String> actions = new HashSet<>();
    private Set<String> categories = new HashSet<>();
    private Data data;

    void addAction(String action) {
        actions.add(action);
    }

    void addCategory(String category) {
        categories.add(category);
    }

    void addData(String scheme, String host, String port, String path, String pathPattern,
                 String pathPrefix, String mimeType) {

        if (data == null) {
            // lazy initialisation
            data = new Data();
        }

        data.addScheme(scheme);
        data.addHost(host);
        data.addPort(port);
        data.addPath(path);
        data.addPathPattern(pathPattern);
        data.addPathPrefix(pathPrefix);
        data.addMimeType(mimeType);
    }

    boolean hasAction() {
        return !actions.isEmpty();
    }

    boolean hasCategory() {
        return !categories.isEmpty();
    }

    boolean hasData() {
        // this only makes sense with lazy initialization
        return data != null;
    }

    public Set<String> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    public Set<String> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Actions: " + actions + System.lineSeparator());
        builder.append("Categories: " + categories + System.lineSeparator());
        builder.append("Data: " + System.lineSeparator());
        builder.append(data + System.lineSeparator());
        return builder.toString();
    }


    /**
     * A representation for the data tag potentially contained
     * in an intent-filter tag.
     */
    public static class Data {

        private Set<String> schemes;
        private Set<String> hosts;
        private Set<String> ports;
        private Set<String> paths;
        private Set<String> pathPatterns;
        private Set<String> pathPrefixes;
        private Set<String> mimeTypes;

        private static final String[] imageFiles = {"Bmp.bmp", "Gif.gif", "Jpg.jpg", "Png.png", "Tiff.tiff"};
        private static final String[] audioFiles = {"Wav.wav", "Mid.mid", "Ogg.ogg", "Mp3.mp3"};
        private static final String[] applicationFiles = {"Json.json", "Pdf.pdf", "Xml.xml"};
        private static final String[] textFiles = {"Txt.txt", "Csv.csv", "Xml.xml"};
        private static final String[] allFiles = {"Bmp.bmp", "Gif.gif", "Jpg.jpg", "Png.png", "Tiff.tiff",
                "Wav.wav", "Mid.mid", "Json.json", "Pdf.pdf", "Xml.xml", "Txt.txt", "Csv.csv", "Mp3.mp3", "Ogg.ogg"};

        Data() {
            schemes = new HashSet<>();
            hosts = new HashSet<>();
            ports = new HashSet<>();
            paths = new HashSet<>();
            pathPatterns = new HashSet<>();
            pathPrefixes = new HashSet<>();
            mimeTypes = new HashSet<>();
        }

        void addScheme(String scheme) {
            if (scheme != null) {
                schemes.add(scheme);
            }
        }

        void addHost(String host) {
            if (host != null) {
                hosts.add(host);
            }
        }

        void addPort(String port) {
            if (port != null) {
                ports.add(port);
            }
        }

        void addPath(String path) {
            if (path != null) {
                paths.add(path.replace("\\\\", "\\"));
            }
        }

        void addPathPattern(String pathPattern) {
            if (pathPattern != null) {
                pathPatterns.add(pathPattern.replace("\\\\", "\\"));
            }
        }

        void addPathPrefix(String pathPrefix) {
            if (pathPrefix != null) {
                pathPrefixes.add(pathPrefix.replace("\\\\", "\\"));
            }
        }

        void addMimeType(String mimeType) {
            if (mimeType != null) {
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

        boolean hasScheme() {
            return !schemes.isEmpty();
        }

        boolean hasHost() {
            return !hosts.isEmpty();
        }

        boolean hasPort() {
            return !ports.isEmpty();
        }

        /**
         * Retrieves the element specified by {@param index} in a given collection.
         *
         * @param collection The collection from which an element is requested.
         * @param index      The position of the desired element in the given collection.
         * @return Returns the element located at the given index from a collection.
         */
        private String getElementAtIndex(Set<String> collection, int index) {

            int counter = 0;

            for (String element : collection) {
                if (counter == index) {
                    return element;
                } else {
                    counter++;
                }
            }
            throw new IllegalStateException("Couldn't retrieve element at position " + index
                    + " from collection with size: " + collection.size());
        }

        /**
         * Generates a random but valid URI, i.e. a URI matching the data tag
         * inside the intent filter.
         *
         * @return Returns the generated URI or {@code null} if no URI could be derived.
         */
        Uri generateRandomUri() {

            Random rand = new Random();
            StringBuilder uriBuilder = new StringBuilder();

            // no scheme => no uri
            if (!schemes.isEmpty()) {
                String scheme = Randomness.randomElement(schemes);
                String host = "";
                String port = "";
                String path = "";

                // no host => no port && no path
                if (!hosts.isEmpty()) {
                    host = Randomness.randomElement(hosts);

                    if (host.charAt(0) == '*') {
                        if (host.length() == 1) {
                            // try to use local file if possible, see method findFileForUri()
                            host = "localhost";
                        } else {
                            // TODO: may find better prefix, may use list of prefixes
                            // append 'www' plus remaining host string, e.g. *.wikipedia.de
                            host = "www" + host.substring(1);
                        }
                    }

                    if (!ports.isEmpty()) {
                        port = ":" + Randomness.randomElement(ports);
                    }

                    // select random path, pathPrefix or pathPattern if present
                    if (!(paths.isEmpty() && pathPrefixes.isEmpty() && pathPatterns.isEmpty())) {
                        int pathIndex = rand.nextInt(paths.size() + pathPrefixes.size() + pathPatterns.size());
                        if (pathIndex < paths.size())
                            path = getElementAtIndex(paths, pathIndex);
                        else if (pathIndex < paths.size() + pathPrefixes.size())
                            path = getElementAtIndex(pathPrefixes, pathIndex - paths.size());
                        else {
                            path = pathFromPattern(pathIndex - paths.size() - pathPrefixes.size());
                        }
                    }
                }

                // TODO: check whether it make sense to construct a file if no host was specified
                // check whether one of the pre-generated files matches
                String possibleFile = findFileForUri(scheme, host, port, path);

                if (possibleFile == null) {
                    // TODO: check whether leading slash before path is redundant
                    // no match found -> leave URI unchanged (might be empty)
                    uriBuilder.append(scheme).append("://").append(host).append(port).append("/").append(path);
                } else {
                    // matches one of the pre-generated files located on the sd card (external storage)
                    uriBuilder.append(Uri.fromFile(new File("/sdcard/", possibleFile)).toString());
                }

                // MATE.log("Generated URI: " + Uri.parse(uriBuilder.toString()));
                return Uri.parse(uriBuilder.toString());

            } else {

                // no scheme specified -> check whether a mimeType is defined
                if (!mimeTypes.isEmpty()) {

                    // find all possible files first, then select one instead of checking for files only in one mimeType => lower chance to get null
                    Set<String> suitableFiles = new LinkedHashSet<>();
                    for (int i = 0; i < mimeTypes.size(); i++) {
                        suitableFiles.add(findSuitableFile(getElementAtIndex(mimeTypes, i)));
                    }

                    String fileName = null;

                    if (!suitableFiles.isEmpty()) {
                        fileName = (String) suitableFiles.toArray()[new Random().nextInt(suitableFiles.size())];
                    }

                    if (fileName != null) {
                        String[] pathParts = Uri.fromFile(
                                new File(InstrumentationRegistry.getTargetContext().getFilesDir(), fileName)).toString().split("/");

                        pathParts[pathParts.length - 3] = MATE.packageName;

                        for (int i = 0; i < pathParts.length - 1; i++) {
                            uriBuilder.append(pathParts[i]).append("/");
                        }

                        uriBuilder.append(pathParts[pathParts.length - 1]);
                        return Uri.parse(uriBuilder.toString());
                    }
                }
            }
            // no appropriate URI could be generated
            return null;
        }

        /**
         * Checks if the given URI matches one of the pre-generated files located on the sd card.
         *
         * @return Returns the file name if the URI matches one of the pre-generated files,
         * otherwise {@code null} is returned.
         */
        private String findFileForUri(String scheme, String host, String port, String path) {

            // scheme must be of type file, TODO: could be 'content' valid as well???
            if (scheme.equals("file")) {
                if (host.equals("") || host.equals("localhost")) {
                    if (port.equals("")) {

                        // select file depending on file ending
                        if (path.endsWith(".bmp"))
                            return "mateTestBmp.bmp";
                        else if (path.endsWith(".gif"))
                            return "mateTestGif.gif";
                        else if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".jpe"))
                            return "mateTestJpg.jpg";
                        else if (path.endsWith(".png"))
                            return "mateTestPng.png";
                        else if (path.endsWith(".tiff") || path.endsWith(".tif"))
                            return "mateTestTiff.tiff";
                        else if (path.endsWith(".wav"))
                            return "mateTestWav.wav";
                        else if (path.endsWith(".mid") || path.endsWith(".midi"))
                            return "mateTestMid.mid";
                        else if (path.endsWith(".mp3") || path.endsWith(".mpeg"))
                            return "mateTestMp3.mp3";
                        else if (path.endsWith(".ogg"))
                            return "mateTestOgg.ogg";
                        else if (path.endsWith(".json"))
                            return "mateTestJson.json";
                        else if (path.endsWith(".pdf"))
                            return "mateTestPdf.pdf";
                        else if (path.endsWith(".xml"))
                            return "mateTestXml.xml";
                        else if (path.endsWith(".txt"))
                            return "mateTestTxt.txt";
                        else if (path.endsWith(".csv"))
                            return "mateTestCsv.csv";

                        // no path -> try to generate file based on mimeType (if present)
                        if (path.equals("") && !mimeTypes.isEmpty()) {

                            Set<String> suitableFiles = new LinkedHashSet<>();
                            for (int i = 0; i < mimeTypes.size(); i++) {
                                // construct file based on mimeType if possible
                                String file = findSuitableFile(getElementAtIndex(mimeTypes, i));
                                if (file != null) {
                                    suitableFiles.add(file);
                                }
                            }

                            if (!suitableFiles.isEmpty()) {
                                // select randomly file
                                return (String) suitableFiles.toArray()[new Random().nextInt(suitableFiles.size())];
                            }
                        }
                    }
                }
            }
            // no appropriate file could be found
            return null;
        }

        /**
         * Checks whether we can supply a file based on the given mimeType.
         *
         * @param mimeType The given mimeType.
         * @return Returns the file name if we have a match for the given mimeType,
         * otherwise {@code null} is returned.
         */
        private String findSuitableFile(String mimeType) {
            Random random = new Random();

            if (mimeType.startsWith("*/")) {
                // select arbitrary file
                return "mateTest" + allFiles[random.nextInt(allFiles.length)];
            } else if (mimeType.startsWith("image")) {
                if (mimeType.endsWith("*"))
                    // select arbitrary image file
                    return "mateTest" + imageFiles[random.nextInt(imageFiles.length)];
                else if (mimeType.endsWith("jpeg") || mimeType.endsWith("jpg"))
                    return "mateTestJpg.jpg";
                else if (mimeType.endsWith("png"))
                    return "mateTestPng.png";
                else if (mimeType.endsWith("gif"))
                    return "mateTestGif.gif";
                else if (mimeType.endsWith("bmp"))
                    return "mateTestBmp.bmp";
                else if (mimeType.endsWith("tiff") || mimeType.endsWith("tif"))
                    return "mateTestTiff.tiff";
            } else if (mimeType.startsWith("audio")) {
                if (mimeType.endsWith("*"))
                    return "mateTest" + audioFiles[random.nextInt(audioFiles.length)];
                else if (mimeType.endsWith("wav"))
                    return "mateTestWav.wav";
                else if (mimeType.endsWith("midi") || mimeType.endsWith("mid"))
                    return "mateTestMid.mid";
                else if (mimeType.endsWith("mp3") || mimeType.endsWith("mpeg"))
                    return "mateTestMp3.mp3";
                else if (mimeType.endsWith("ogg"))
                    return "mateTestOgg.ogg";
            } else if (mimeType.startsWith("application")) {
                if (mimeType.endsWith("*"))
                    // select arbitrary application file
                    return "mateTest" + applicationFiles[random.nextInt(applicationFiles.length)];
                else if (mimeType.endsWith("json"))
                    return "mateTestJson.json";
                else if (mimeType.endsWith("pdf"))
                    return "mateTestPdf.pdf";
                else if (mimeType.endsWith("xml"))
                    return "mateTestXml.xml";
            } else if (mimeType.startsWith("text")) {
                if (mimeType.endsWith("*"))
                    // select arbitrary txt file
                    return "mateTest" + textFiles[random.nextInt(textFiles.length)];
                else if (mimeType.endsWith("plain"))
                    return "mateTestTxt.txt";
                else if (mimeType.endsWith("comma-separated-values"))
                    return "mateTestCsv.csv";
                else if (mimeType.endsWith("xml"))
                    return "mateTestXml.xml";
            }

            MATE.log_acc("No file found for mimeType: " + mimeType);
            return null;
        }


        /**
         * Generates a URI matching the path pattern.
         *
         * @param patternIndex The selected path pattern (the index of it).
         * @return Returns a URI matching a given path pattern.
         */
        private String pathFromPattern(int patternIndex) {

            StringBuilder path = new StringBuilder();
            String pattern = getElementAtIndex(pathPatterns, patternIndex);

            boolean escaped = false;
            boolean consumed = false; // verify that dot is actually a dot

            for (int i = 0; i < pattern.length(); i++) {

                if (pattern.charAt(i) == '\\' && !escaped) {
                    escaped = true;
                    continue;
                }

                if (pattern.charAt(i) == '.' && pattern.length() > i + 1 && pattern.charAt(i + 1) == '*') {

                    if (consumed || escaped) {
                        path.append('.');
                    } else {
                        path.append('a');
                    }

                    escaped = false;
                    consumed = true;
                    continue;

                } else if (pattern.charAt(i) == '.') {

                    if (consumed) {
                        path.append('.');
                    } else {
                        path.append('a');
                    }

                } else if (pattern.charAt(i) == '*') {

                    if (escaped) {
                        path.append('*');
                    }
                    escaped = false;
                    continue;

                } else {
                    path.append(pattern.charAt(i));
                }

                consumed = false;
                escaped = false;
            }
            return path.toString();
        }
    }
}
