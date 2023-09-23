package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        String rootPath = "C:\\Users\\fang\\Documents\\repo";
        Path path = Path.of(rootPath).toAbsolutePath();
        System.out.println("Root Path : " + path);

        System.out.println("LIST -------------------------------------------------------");

        try (Stream<Path> paths = Files.list(path)){
            paths
                    .map(Main::listDir)
                    .forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("WALK -------------------------------------------------------");

        try (Stream<Path> paths = Files.walk(path, 2)){
            paths
                    .filter(Files::isRegularFile)
                    .map(Main::listDir)
                    .forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("FIND -------------------------------------------------------");

        try (Stream<Path> paths = Files.find(path, 3, (p, attr) -> {
            // Any filter logic, return a Boolean
            return attr.isRegularFile();
        })){
            paths
                    .map(Main::listDir)
                    .forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("DIRECTORY STREAM  ------------------------------------------");

        path = path.resolve("02");
        try (var dirs = Files.newDirectoryStream(path, "*.jpg")){
            dirs.forEach(d -> System.out.println(Main.listDir(d)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String listDir(Path path) {
        try {
            boolean isDir = Files.isDirectory(path);
            FileTime dateField = Files.getLastModifiedTime(path);
            LocalDateTime dt = LocalDateTime.ofInstant(dateField.toInstant(), ZoneId.systemDefault());
            return "%tD %tT %-5s %12s %s"
                    .formatted(dt, dt, (isDir ? "<DIR>" : ""), (isDir ? "" : Files.size(path)) + " Bytes", path);
        } catch (IOException e) {
            System.out.println("Not a directory. Path : " + path);
            return path.toString();
        }
    }

}