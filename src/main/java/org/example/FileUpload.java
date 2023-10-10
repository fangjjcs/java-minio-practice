package org.example;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

public class FileUpload {
    private static final Logger logger = LoggerFactory.getLogger(FileUpload.class);
    public static void main(String[] args) throws IOException, ParseException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {


        logger.trace("Logger start.");

        String rootPath = "C:\\Users\\fang\\Documents\\repo";
        String trimPath = "C:\\Users\\fang\\Documents\\";
        Path path = Path.of(rootPath);
        System.out.println("Root Path : " + path);

        // Set time threshold if needed
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Timestamp timeThreshold = new Timestamp (dateFormat.parse("23/09/2023 23:00:00").getTime());

        MinioClient minioClient = initMinioClient();

        // Upload Files
//        uploadFile(minioClient, path, trimPath, timeThreshold);
//        System.out.println("Upload Done.");
//        logger.info("Upload Done.");

        // Download Files
        ListAndDownloadMinioFiles(minioClient);
    }

    private static void uploadFile(MinioClient minioClient, Path path, String trimPath, Timestamp timeThreshold) {

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

    private static MinioClient initMinioClient() {
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint("https://play.min.io")
                        .credentials("Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
                        .build();
        return minioClient;
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

    private static void ListAndDownloadMinioFiles(MinioClient minioClient) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket("testadam")
                .recursive(true)
                .build();
        Iterable<Result<Item>> response = minioClient.listObjects(args);

        for (Result<Item> itemResult : response) {
            Item item = itemResult.get();
            System.out.println(item.objectName() + " | " + item.size() + " bytes");
            downloadFiles(minioClient, item.objectName());
        }
    }

    private static void downloadFiles(MinioClient minioClient, String objectName) {

        String filePath = "C:/Users/fang/Documents/download/" + objectName;
        Path directory = Paths.get(filePath).getParent();
        System.out.println("Download Directory : " + directory);

        try{

            File newDirectory = new File(String.valueOf(directory));
            if (!newDirectory.exists()){
                newDirectory.mkdirs();
                System.out.println("Directory doesn't exist and is created.");
            }

            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()){
                System.out.println(filePath + " is already exist and be replaced by new version.");
            }

            System.out.println("File on bucket : " + objectName);

            DownloadObjectArgs args = DownloadObjectArgs.builder()
                    .bucket("testadam")
                    .object(objectName)
                    .filename(filePath)
                    .overwrite(true)
                    .build();
            minioClient.downloadObject(args);
            System.out.println(filePath + " is downloaded successfully.");
            System.out.println("----------------------------------------------------------------");

        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            System.out.println(
                    filePath + " download failed.");
            throw new RuntimeException(e);
        }

    }
}