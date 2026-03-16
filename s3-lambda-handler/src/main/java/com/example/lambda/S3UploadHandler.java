package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

public class S3UploadHandler implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event event, Context context) {

        event.getRecords().forEach(record -> {

            String bucket = record.getS3().getBucket().getName();
            String key = record.getS3().getObject().getKey();

            context.getLogger().log("File uploaded to S3\n");
            context.getLogger().log("Bucket: " + bucket + "\n");
            context.getLogger().log("Key: " + key + "\n");

        });

        return "Processed S3 event";
    }
}