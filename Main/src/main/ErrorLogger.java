package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ErrorLogger {
    private BufferedWriter bw;

    public ErrorLogger(String logFileName) {
        try {
            bw = new BufferedWriter(new FileWriter(logFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logError(String message) {
        try {
            bw.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logLine(String lineNumber, String originalLine) {
        try {
            bw.write(lineNumber + " " + originalLine + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (bw != null) {
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
