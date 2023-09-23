package org.example;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {

        String rootPath = "C:\\Users\\fang\\Documents\\repo";
        String trimPath = "C:\\Users\\fang\\Documents\\";
        Path path = Path.of(rootPath).toAbsolutePath();
        System.out.println("Root Path : " + path);
        uploadFile(path, trimPath);
    }
    private static void uploadFile(Path path, String trimPath) throws IOException, NoSuchAlgorithmException, InvalidKeyException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {

            // Create a minioClient with the MinIO server playground, its access key and secret key.
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint("https://play.min.io")
                            .credentials("Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
                            .build();

            // Make bucket if not exist.
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket("testadam").build());
            if (!found) {
                // Make a new bucket.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("testadam").build());
            } else {
                System.out.println("Bucket 'testadam' already exists.");
            }

            try (Stream<Path> paths = Files.walk(path, 3)){
                paths
                        .filter(Files::isRegularFile)
                        .forEach(p -> {
                            listDir(p);
                            try {
                                String objectPath = p.toString().replace(trimPath, "").replace("\\","/");
                                String filename = p.toString();
                                // Upload file as object name to bucket
                                minioClient.uploadObject(
                                        UploadObjectArgs.builder()
                                                .bucket("testadam")
                                                .object(objectPath)
                                                .filename(filename)
                                                .build());
                                System.out.println(
                                        objectPath + " is successfully uploaded to bucket 'testadam'.");
                                System.out.println("----------------------------------------------");
                            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException |
                                     ErrorResponseException | InsufficientDataException | InternalException |
                                     InvalidResponseException | ServerException | XmlParserException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
    }
    private static void listDir(Path path) {
        try {
            boolean isDir = Files.isDirectory(path);
            FileTime dateField = Files.getLastModifiedTime(path);
            LocalDateTime dt = LocalDateTime.ofInstant(dateField.toInstant(), ZoneId.systemDefault());
            System.out.printf("%tD %tT %12s %s%n", dt, dt, (isDir ? "" : Files.size(path)), path);
        } catch (IOException e) {
            System.out.println("Not a directory. Path : " + path);
        }
    }

}