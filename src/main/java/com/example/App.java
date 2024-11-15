package com.example;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.Media;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.TranscriptionJob;

public class App {
    public static void main(String[] args) {
        String accessKey = System.getenv("AWS_ACCESS_KEY");
        String secretKey = System.getenv("AWS_SECRET_KEY");
        String region = "us-east-2"; // Replace with your region
        String bucketName = "bucket_name"; // Replace with your S3 bucket
        String fileUri = "s3://" + bucketName + "/file_name.mp3"; // Replace with your file
        String jobName = "transcription-job-1"; // Give your job a unique name

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AmazonTranscribe transcribeClient = AmazonTranscribeClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(region)
                .build();

        StartTranscriptionJobRequest startRequest = new StartTranscriptionJobRequest()
                .withTranscriptionJobName(jobName)
                .withMedia(new Media().withMediaFileUri(fileUri))
                .withLanguageCode("en-US");

        StartTranscriptionJobResult startResult = transcribeClient.startTranscriptionJob(startRequest);
        System.out.println("Transcription job started: " + startResult.getTranscriptionJob().getTranscriptionJobName());

        while (true) {
            GetTranscriptionJobRequest getRequest = new GetTranscriptionJobRequest().withTranscriptionJobName(jobName);
            GetTranscriptionJobResult getResult = transcribeClient.getTranscriptionJob(getRequest);
            TranscriptionJob job = getResult.getTranscriptionJob();

            if (job.getTranscriptionJobStatus().equals("COMPLETED")) {
                System.out.println("Transcription completed. Transcript available at: " + job.getTranscript().getTranscriptFileUri());
                break;
            } else if (job.getTranscriptionJobStatus().equals("FAILED")) {
                System.out.println("Transcription failed: " + job.getFailureReason());
                break;
            }

            System.out.println("Waiting for transcription to complete...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
