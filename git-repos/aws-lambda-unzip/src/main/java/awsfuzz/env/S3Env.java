package awsfuzz.env;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static awsfuzz.env.Environment.LOCAL_STACK_ENDPOINT;
import static com.amazonaws.auth.profile.internal.ProfileKeyConstants.REGION;

//holds and controls the S3Environment.
public class S3Env {

    public static final List<Bucket> buckets;
    public static final AmazonS3 s3Client;

    static {
        s3Client = prepareS3();
        buckets = s3Client.listBuckets();
    }

    public static void reset() {
        clearBuckets();
    }

    private static void clearBuckets() {
        System.out.println("before clear buckets");
        for (Bucket bucket : buckets) {
            List<S3ObjectSummary> s3Summaries = s3Client.listObjects(bucket.getName()).getObjectSummaries();
            String[] s3Keys = s3Summaries.stream().map(s3Obj -> s3Obj.getKey()).toArray(String[]::new);

            if (s3Keys.length > 0) {
                DeleteObjectsRequest dor = new DeleteObjectsRequest(bucket.getName())
                        .withKeys(s3Keys);
                s3Client.deleteObjects(dor);
            }
        }
        System.out.println("after clear buckets");
    }


    public static S3Object populateToCloud(File localFile, String fileName, Bucket bucket) {
        assert localFile.exists() : "local file should have been created locally before pushing it onto the cloud. Failing.";
        s3Client.putObject(bucket.getName(), fileName, localFile);

        //returning the object after its creation
        return s3Client.getObject(bucket.getName(), fileName);
    }


    private static AmazonS3 prepareS3() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials("access_key", "secret_key");
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(LOCAL_STACK_ENDPOINT, REGION))
                .withPathStyleAccessEnabled(true)
                .build();
    }

    public static void populateBatchToCloud(Bucket bucket, ArrayList<File> localFiles) {
        TransferManager transfer = TransferManagerBuilder.standard().withS3Client(s3Client).build();

        MultipleFileUpload upload = transfer.uploadFileList(bucket.getName(), "", new File("."), localFiles);
        try {
            upload.waitForCompletion();
            System.out.println("upload is done.");
        } catch (InterruptedException e) {
            assert false : "upload of files for fuzzing failed for bucket = " + bucket;
        }
    }
}
