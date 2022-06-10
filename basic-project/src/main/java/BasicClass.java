
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.List;

import static com.amazonaws.auth.profile.internal.ProfileKeyConstants.REGION;



public class BasicClass implements RequestHandler<S3Event, String> {
    public final String S3_ENDPOINT = "http://192.168.0.63:4566";

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        System.out.println("I am the lambda. I received this event: " + s3Event.toString());
        final AmazonS3 s3 = prepareS3();
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket bucket : buckets) {
            ObjectListing listing = s3.listObjects(bucket.getName(), "");
            List<S3ObjectSummary> summaries = listing.getObjectSummaries();
            for (S3ObjectSummary s3ObjectSummary : summaries)
                System.out.println("the number of buckets that you have is:" + s3ObjectSummary.toString());
        }
        return "";
    }


    private AmazonS3 prepareS3() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials("access_key", "secret_key");
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(S3_ENDPOINT, REGION))
                .withPathStyleAccessEnabled(true)
                .build();
    }
}
