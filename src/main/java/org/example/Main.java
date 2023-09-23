package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        System.out.println("Hello and welcome!");

        String filename = "C:\\Users\\fang\\Documents\\repo\\01\\test.txt";
        testFile(filename);
        File file = new File(filename);
        // Look Before You Leap
        if(!file.exists()) {
            System.out.println("Can not find this file.");
            return;
        }
        System.out.println("We find the file, good to go.");

    }

    private static void testFile(String filename) {
        Path path = Paths.get(filename);
        try {
            List<String> lines = Files.readAllLines(path);
            System.out.println("File exists and able to use as a resource.");
            System.out.println(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Need to log something.");
        }
    }
}