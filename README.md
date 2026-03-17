# Spring Boot + AWS S3 + Lambda File Pipeline

This project demonstrates a simple **event-driven workflow using AWS services and a Spring Boot backend**.

The idea behind the project is straightforward: a REST API accepts a file upload, stores the file in Amazon S3, and the upload automatically triggers a Lambda function which processes the event and logs information to CloudWatch.

The goal of the project was to get hands-on experience with AWS services and understand how event-driven architectures work.

---

## Project Flow

Client → Spring Boot API → Amazon S3 → S3 Event Notification → AWS Lambda → CloudWatch Logs

1. A client uploads a file using a REST API.
2. The Spring Boot service uploads the file to an S3 bucket using the AWS SDK.
3. S3 emits an **ObjectCreated event** when the file is stored.
4. The event triggers a Lambda function.
5. The Lambda function extracts metadata from the event and logs it to CloudWatch.

---

## Components

### File Upload API (Spring Boot)

A small Spring Boot application that exposes an endpoint to upload files.

Endpoint:

POST /files/upload

The API accepts a multipart file and uploads it to the configured S3 bucket.

---

### S3 Event Processor (AWS Lambda)

A Java-based Lambda function that listens to S3 **ObjectCreated events**.

When triggered, it reads the event data and logs details like:

* Bucket name
* Object key (file name)

The logs can be viewed in **CloudWatch**.

---

## Tech Stack

* Java
* Spring Boot
* AWS S3
* AWS Lambda
* AWS CloudWatch
* Maven

---

## Running the Project

1. Configure AWS credentials locally using:

```
aws configure
```

2. Start the Spring Boot API.

3. Send a file upload request:

```
POST /files/upload
```

4. Verify the file appears in the S3 bucket.

5. Check the Lambda logs in CloudWatch to confirm the event was processed.

---

## Future Improvements

Some ideas for extending the project:

* Add an API to list files stored in S3
* Store uploaded file metadata in DynamoDB
* Add Docker support for the API
* Automate infrastructure setup with Terraform
