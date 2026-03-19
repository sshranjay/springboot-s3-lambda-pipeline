# Spring Boot + AWS S3 + Lambda File Pipeline

This project demonstrates a simple **event-driven workflow using AWS services and a Spring Boot backend**.

Files uploaded through the API are stored in Amazon S3. Each upload triggers an AWS Lambda function via an S3 event notification, and the Lambda logs details about the file to CloudWatch.

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

### File Upload API

A small Spring Boot application that exposes an endpoint to upload files.

Endpoint:

`POST /files/upload`

The API accepts a multipart file and uploads it to the configured S3 bucket.

### List Uploaded Files

`GET /files`

Returns the list of files currently stored in the S3 bucket along with basic metadata such as file size and last modified timestamp.

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

### Prerequisites

* An AWS account with an **existing S3 bucket**
* AWS credentials configured on your system (`aws configure` or environment variables)
* Java 17 and Maven installed

### Configuration

The Spring Boot API reads its configuration from **environment variables**:

| Variable | Description | Example |
|---|---|---|
| `AWS_REGION` | AWS region where your S3 bucket lives | `us-east-1` |
| `S3_BUCKET_NAME` | Name of your S3 bucket | `my-uploads-bucket` |

AWS credentials are picked up automatically from the default credential chain (`~/.aws/credentials`, environment variables `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY`, or an IAM role).

### Run locally

```bash
# 1. Set your bucket details
export AWS_REGION=us-east-1
export S3_BUCKET_NAME=my-uploads-bucket

# 2. Configure AWS credentials (if not already done)
aws configure

# 3. Start the Spring Boot API
cd s3-file-upload-api
mvn spring-boot:run
```

### API endpoints

```
POST /files/upload   # Upload a file to your S3 bucket
GET  /files          # List files stored in the bucket
```

---

## Deploying the Lambda Function

The Lambda function (`s3-lambda-handler`) processes S3 upload events and logs file metadata to CloudWatch.

### Option 1 — GitHub Actions (recommended)

Add the following **repository secrets** in *Settings → Secrets and variables → Actions*:

| Secret | Description |
|---|---|
| `AWS_ACCESS_KEY_ID` | Your AWS access key |
| `AWS_SECRET_ACCESS_KEY` | Your AWS secret key |
| `AWS_REGION` | AWS region (e.g. `us-east-1`) |
| `S3_BUCKET_NAME` | Name of your S3 bucket |
| `LAMBDA_FUNCTION_NAME` | Desired Lambda function name (e.g. `s3-upload-handler`) |
| `LAMBDA_ROLE_ARN` | ARN of an IAM role with `AWSLambdaBasicExecutionRole` attached |

Then trigger the **Deploy Lambda to AWS** workflow manually from the *Actions* tab, or push a change to `s3-lambda-handler/` on `main`.

The workflow will:
1. Build the Lambda fat JAR
2. Create the Lambda function if it doesn't exist, or update it if it does
3. Grant S3 permission to invoke the Lambda
4. Attach an `s3:ObjectCreated:*` event trigger to your bucket

### Option 2 — Deploy locally

```bash
# 1. Build the fat JAR
cd s3-lambda-handler
mvn clean package

# 2. Create the Lambda function (first time only)
aws lambda create-function \
  --function-name s3-upload-handler \
  --runtime java17 \
  --role arn:aws:iam::YOUR_ACCOUNT_ID:role/YOUR_LAMBDA_ROLE \
  --handler com.example.lambda.S3UploadHandler::handleRequest \
  --zip-file fileb://target/s3-lambda-handler-1.0-SNAPSHOT.jar \
  --timeout 30 \
  --memory-size 512 \
  --region YOUR_REGION

# 3. Grant S3 permission to invoke it
aws lambda add-permission \
  --function-name s3-upload-handler \
  --statement-id s3-trigger \
  --action lambda:InvokeFunction \
  --principal s3.amazonaws.com \
  --source-arn arn:aws:s3:::YOUR_BUCKET_NAME \
  --source-account YOUR_ACCOUNT_ID

# 4. Attach the S3 event notification
aws s3api put-bucket-notification-configuration \
  --bucket YOUR_BUCKET_NAME \
  --notification-configuration '{
    "LambdaFunctionConfigurations": [{
      "LambdaFunctionArn": "arn:aws:lambda:YOUR_REGION:YOUR_ACCOUNT_ID:function:s3-upload-handler",
      "Events": ["s3:ObjectCreated:*"]
    }]
  }'
```

---

## Future Improvements

Some things I may add later:
* API to download files from S3
* Store uploaded file metadata in DynamoDB
* Automate infrastructure setup with Terraform

---

## Docker Image

To run the Spring Boot API in Docker, set the required environment variables at container start:

```bash
docker run -p 8080:8080 \
  -e AWS_REGION=us-east-1 \
  -e S3_BUCKET_NAME=my-uploads-bucket \
  -e AWS_ACCESS_KEY_ID=YOUR_KEY \
  -e AWS_SECRET_ACCESS_KEY=YOUR_SECRET \
  sshranjay/s3-file-api:latest
```
