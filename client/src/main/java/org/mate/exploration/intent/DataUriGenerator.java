package org.mate.exploration.intent;

import android.net.Uri;

import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Randomness;
import org.mate.commons.utils.SetUtils;
import org.mate.commons.utils.manifest.element.DataDescription;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

/**
 * Generates a random but valid data uri matching the data specification tag within a given intent
 * filter.
 */
public final class DataUriGenerator {

    /**
     * The supported image files.
     */
    private static final String[] imageFiles = {"Bmp.bmp", "Gif.gif", "Jpg.jpg", "Png.png", "Tiff.tiff"};

    /**
     * The supported audio files.
     */
    private static final String[] audioFiles = {"Wav.wav", "Mid.mid", "Ogg.ogg", "Mp3.mp3"};

    /**
     * The supported application files.
     */
    private static final String[] applicationFiles = {"Json.json", "Pdf.pdf", "Xml.xml"};

    /**
     * The supported text files.
     */
    private static final String[] textFiles = {"Txt.txt", "Csv.csv", "Xml.xml"};

    /**
     * The supported files.
     */
    private static final String[] allFiles = {"Bmp.bmp", "Gif.gif", "Jpg.jpg", "Png.png", "Tiff.tiff",
            "Wav.wav", "Mid.mid", "Json.json", "Pdf.pdf", "Xml.xml", "Txt.txt", "Csv.csv", "Mp3.mp3",
            "Ogg.ogg"};

    /**
     * Generates a random but valid URI, i.e. a URI matching the data tag inside the intent filter.
     *
     * @return Returns the generated URI or {@code null} if no URI could be derived.
     */
    public static Uri generateRandomUri(DataDescription data) {

        Random rand = new Random();
        StringBuilder uriBuilder = new StringBuilder();

        // no scheme => no uri
        if (data.hasScheme()) {
            String scheme = Randomness.randomElement(data.getSchemes());
            String host = "";
            String port = "";
            String path = "";

            // no host => no port && no path
            if (data.hasHost()) {
                host = Randomness.randomElement(data.getHosts());

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

                if (data.hasPort()) {
                    port = ":" + Randomness.randomElement(data.getPorts());
                }

                // select random path, pathPrefix or pathPattern if present
                if (data.hasPath() || data.hasPathPrefix() ||data.hasPathPattern()) {
                    int pathIndex = rand.nextInt(data.getPaths().size()
                            + data.getPathPrefixes().size() + data.getPathPatterns().size());
                    if (pathIndex < data.getPaths().size())
                        path = SetUtils.getElementAtIndex(data.getPaths(), pathIndex);
                    else if (pathIndex < data.getPaths().size() + data.getPathPrefixes().size())
                        path = SetUtils.getElementAtIndex(data.getPathPrefixes(),
                                pathIndex - data.getPaths().size());
                    else {
                        path = pathFromPattern(data.getPathPatterns(),
                                pathIndex - data.getPaths().size() - data.getPathPrefixes().size());
                    }
                }
            }

            // TODO: check whether it make sense to construct a file if no host was specified
            // check whether one of the pre-generated files matches
            String possibleFile = findFileForUri(data.getMimeTypes(), scheme, host, port, path);

            if (possibleFile == null) {
                // TODO: check whether leading slash before path is redundant
                // no match found -> leave URI unchanged (might be empty)
                uriBuilder.append(scheme).append("://").append(host).append(port).append("/").append(path);
            } else {
                // matches one of the pre-generated files located on the sd card (external storage)
                uriBuilder.append(Uri.fromFile(new File("/sdcard/", possibleFile)).toString());
            }

            return Uri.parse(uriBuilder.toString());
        } else {

            // no scheme specified -> check whether a mimeType is defined
            if (data.hasMimeType()) {

                // find all possible files first, then select one instead of checking for files
                // only in one mimeType => lower chance to get null
                Set<String> suitableFiles = new LinkedHashSet<>();
                for (int i = 0; i < data.getMimeTypes().size(); i++) {
                    suitableFiles.add(findSuitableFile(SetUtils.getElementAtIndex(data.getMimeTypes(), i)));
                }

                String fileName = null;

                if (!suitableFiles.isEmpty()) {
                    fileName = (String) suitableFiles.toArray()[new Random().nextInt(suitableFiles.size())];
                }

                if (fileName != null) {
                    String[] pathParts = Uri.fromFile(
                            new File(Registry.getDeviceMgr().getTargetPackageFilesDir(),
                                    fileName)).toString().split("/");

                    pathParts[pathParts.length - 3] = Registry.getPackageName();

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
     * Generates a URI matching the path pattern.
     *
     * @param pathPatterns The available path patterns.
     * @param patternIndex The selected path pattern (the index of it).
     * @return Returns a URI matching a given path pattern.
     */
    private static String pathFromPattern(Set<String> pathPatterns, int patternIndex) {

        StringBuilder path = new StringBuilder();
        String pattern = SetUtils.getElementAtIndex(pathPatterns, patternIndex);

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

    /**
     * Checks whether we can supply a file based on the given mimeType.
     *
     * @param mimeType The given mimeType.
     * @return Returns the file name if we have a match for the given mimeType, otherwise
     *          {@code null} is returned.
     */
    private static String findSuitableFile(String mimeType) {

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

        MATELog.log_warn("No file found for mimeType: " + mimeType);
        return null;
    }

    /**
     * Checks if the given URI matches one of the pre-generated files located on the sd card.
     *
     * @param mimeTypes The set of mime types.
     * @param scheme The generated data specification scheme.
     * @param host  The generated data specification host.
     * @param port The generated data specification port.
     * @param path The generated data specification path.
     * @return Returns the file name if the URI matches one of the pre-generated files,
     *         otherwise {@code null} is returned.
     */
    private static String findFileForUri(Set<String> mimeTypes, String scheme, String host,
                                         String port, String path) {

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
                            String file = findSuitableFile(SetUtils.getElementAtIndex(mimeTypes, i));
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
}
