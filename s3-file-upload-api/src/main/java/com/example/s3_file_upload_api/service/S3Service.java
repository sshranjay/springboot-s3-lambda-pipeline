package com.example.s3_file_upload_api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public void uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String safeFilename = (originalFilename == null || originalFilename.isBlank())
                ? "upload.bin"
                : originalFilename;
        File convertedFile = File.createTempFile("upload-", safeFilename);

        try {
            file.transferTo(convertedFile);

            amazonS3.putObject(new PutObjectRequest(
                    bucketName,
                    safeFilename,
                    convertedFile
            ));
        } finally {
            Files.deleteIfExists(convertedFile.toPath());
        }
    }

}
