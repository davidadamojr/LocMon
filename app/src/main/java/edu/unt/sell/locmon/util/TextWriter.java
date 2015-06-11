package edu.unt.sell.locmon.util;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import edu.unt.sell.locmon.MainActivity;

/**
 * Created by davidadamojr on 6/2/15.
 *
 * Writes text to a file
 */
public class TextWriter {
    public static final String TAG = TextWriter.class.getSimpleName();

    private File file;

    public TextWriter(String filename) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        this.file = file;
    }

    public void writeText(String text) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(text);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
