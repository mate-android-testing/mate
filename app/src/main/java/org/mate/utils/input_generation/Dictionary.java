package org.mate.utils.input_generation;


import android.support.test.InstrumentationRegistry;


import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Dictionary {
    //https://github.com/dwyl/english-words
    private static List<String> words = null;

    public static void loadWords() {
        words = new ArrayList<>();
        try (InputStream file = InstrumentationRegistry.getTargetContext().getResources().getAssets().open("words.txt")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getWords() {
        if (words == null)
            loadWords();
        return words;
    }
}
