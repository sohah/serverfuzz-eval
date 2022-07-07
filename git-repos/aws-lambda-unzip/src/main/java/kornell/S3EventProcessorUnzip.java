package kornell;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import org.apache.commons.io.FilenameUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import static awsfuzz.env.Environment.LOCAL_STACK_ENDPOINT;


public class S3EventProcessorUnzip implements RequestHandler<S3Event, String> {
    public static final String AWS_REGION = "us-east-1";
//    public final String S3_ENDPOINT = "http://" + System.getenv("LOCALSTACK_HOSTNAME") + ":4566";

    public static AmazonS3 s3Client = prepareS3();
    public String handleRequest(S3Event s3Event, Context context) {
        byte[] buffer = new byte[1024];
        try {
            for (S3EventNotification.S3EventNotificationRecord record: s3Event.getRecords()) {
                String srcBucket = record.getS3().getBucket().getName();

                // Object key may have spaces or unicode non-ASCII characters.
                String srcKey = record.getS3().getObject().getKey()
                        .replace('+', ' ');
                srcKey = URLDecoder.decode(srcKey, "UTF-8");

                // Detect file type
                Matcher matcher = Pattern.compile(".*\\.([^\\.]*)").matcher(srcKey);
                if (!matcher.matches()) {
                    System.out.println("Unable to detect file type for key " + srcKey);
                    return "";
                }
                String extension = matcher.group(1).toLowerCase();
                if (!"zip".equals(extension)) {
                    System.out.println("Skipping non-zip file " + srcKey + " with extension " + extension);
                    return "";
                }
                System.out.println("Extracting zip file " + srcBucket + "/" + srcKey);
                
                // Download the zip from S3 into a stream
                // AmazonS3 s3Client = new AmazonS3Client();
                S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));
                ZipInputStream zis = new ZipInputStream(s3Object.getObjectContent());
                ZipEntry entry = zis.getNextEntry();

                while(entry != null) {
                    String fileName = entry.getName();
                    String mimeType = FileMimeType.fromExtension(FilenameUtils.getExtension(fileName)).mimeType();
                    System.out.println("Extracting " + fileName + ", compressed: " + entry.getCompressedSize() + " bytes, extracted: " + entry.getSize() + " bytes, mimetype: " + mimeType);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                    InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(outputStream.size());
                    meta.setContentType(mimeType);
                    s3Client.putObject(srcBucket, FilenameUtils.getFullPath(srcKey) + fileName, is, meta);
                    is.close();
                    outputStream.close();
                    entry = zis.getNextEntry();
                }
                zis.closeEntry();
                zis.close();
                
                //delete zip file when done
                System.out.println("Deleting zip file " + srcBucket + "/" + srcKey + "...");
                s3Client.deleteObject(new DeleteObjectRequest(srcBucket, srcKey));
                System.out.println("Done deleting");
            }
            return "Ok";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static AmazonS3 prepareS3() {
        BasicAWSCredentials credentials = new BasicAWSCredentials("foo", "bar");

        AwsClientBuilder.EndpointConfiguration config =
                new AwsClientBuilder.EndpointConfiguration(LOCAL_STACK_ENDPOINT, AWS_REGION);

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.withEndpointConfiguration(config);
        builder.withPathStyleAccessEnabled(true);
        builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
        return builder.build();
    }

}
