package org.mate.commons.input_generation;


import android.support.test.InstrumentationRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Dictionary that loads and caches all words from a file. The words must be separated by line
 * breaks.
 */
public class Dictionary {

    //https://github.com/dwyl/english-words
    private static List<String> words = null;

    /**
     * Loads the words from the words.txt file into a list and stores them temporarily.
     */
    private static void loadWords() {
        words = new ArrayList<>();
        try (InputStream file = InstrumentationRegistry.getTargetContext().getResources()
                .getAssets().open("words.txt")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * If the words have not yet been loaded, it loads those and then returns them. If this has
     * already happened, the words are returned immediately.
     *
     * @return The loaded words.
     */
    public static List<String> getWords() {
        if (words == null)
            loadWords();
        return words;
    }
}
