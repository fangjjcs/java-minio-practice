package org.example;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

public class FileUpload {
    private static final Logger logger = Logger.getLogger("logger");
    public static void main(String[] args) throws IOException, ParseException {

        FileHandler fileHandler = new FileHandler("C:\\Users\\fang\\Documents\\Logs\\minio.log");
        fileHandler.setLevel(Level.INFO);
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        logger.info("Logger start.");

        String rootPath = "C:\\Users\\fang\\Documents\\repo";
        String trimPath = "C:\\Users\\fang\\Documents\\";
        Path path = Path.of(rootPath);
        System.out.println("Root Path : " + path);

        // Set time threshold if needed
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Timestamp timeThreshold = new Timestamp (dateFormat.parse("23/09/2023 23:00:00").getTime());

        uploadFile(path, trimPath, timeThreshold);
        System.out.println("Upload Done.");
        logger.info("Upload Done.");

    }

    private static void uploadFile(Path path, String trimPath, Timestamp timeThreshold) {

        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint("https://play.min.io")
                        .credentials("Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
                        .build();

        try (Stream<Path> paths = Files.walk(path, 3)){
            paths.filter(Files::isRegularFile).forEach(p -> {
                printFileInfo(p);
                String objectPath = null;
                try {
                    objectPath = p.toString().replace(trimPath, "").replace("\\", "/");
                    String filename = p.toString();

                    // Make bucket if not exist.
                    boolean found =
                            minioClient.bucketExists(BucketExistsArgs.builder().bucket("testadam").build());
                    if (!found) {
                        minioClient.makeBucket(MakeBucketArgs.builder().bucket("testadam").build());
                        System.out.println("Bucket 'testadam' doesn't exist.");
                    }
                    // Upload file as object name to bucket
                    minioClient.uploadObject(
                            UploadObjectArgs.builder()
                                    .bucket("testadam")
                                    .object(objectPath)
                                    .filename(filename)
                                    .build());
                    System.out.println(
                            objectPath + " is successfully uploaded to bucket.");
                    System.out.println("----------------------------------------------");
                    logger.info(objectPath + " is successfully uploaded to bucket.");
                    logger.info("----------------------------------------------");

                } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
                    System.out.println(
                            objectPath + " is uploaded failed.");
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            System.out.println("Files stream error.");
            throw new RuntimeException(e);
        }
    }
    private static void printFileInfo(Path path) {
        try {
            boolean isDir = Files.isDirectory(path);
            FileTime dateField = Files.getLastModifiedTime(path);
            LocalDateTime dt = LocalDateTime.ofInstant(dateField.toInstant(), ZoneId.systemDefault());
            String info = "%tD %tT | %s bytes | %s%n".formatted(dt, dt, (isDir ? "" : Files.size(path)), path);
            System.out.printf(info);
            logger.info(info);
        } catch (IOException e) {
            System.out.println("Not a directory. Path : " + path);
        }
    }
}