package awsfuzz.generators.s3;

import awsfuzz.env.S3Env;
import awsfuzz.generators.LambdaFuzzingInput;
import awsfuzz.generators.jqf.AlphaStringGenerator;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.joda.time.DateTime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static awsfuzz.generators.LambdaFuzzingInput.handlerHeader;
import static awsfuzz.generators.LambdaFuzzingInput.lambdaKlass;
import static com.amazonaws.auth.profile.internal.ProfileKeyConstants.REGION;

public class S3EventGen extends Generator<S3Event> {

    private List<Object> mutatedResources;

    public S3EventGen() {
        super(S3Event.class);
    }

    public S3EventGen setMutatedResources(List<Object> mutatedResources) {
        this.mutatedResources = mutatedResources;
        return this;
    }

    @Override
    public S3Event generate(SourceOfRandomness r, GenerationStatus status) {

        Method[] methods = lambdaKlass.getDeclaredMethods();

        List<Method> entryPoints = Arrays.stream(methods)
                .filter(m -> m.toString().replace(" ", "")
                        .contains(handlerHeader.replace(" ", "")))
                .collect(Collectors.toList());

        if (entryPoints.size() != 1)
            assert false : "lambda should have a single entry point.";
//            throw new ServerlessGeneratorException("lambda should have a single entry point.");

        if (!entryPoints.get(0).getParameterTypes()[0].isAssignableFrom(S3Event.class))
            assert false : "unsupported event notification";
//            throw new ServerlessGeneratorException("unsupported event notification");

        return generateS3Event(r, status);
    }

    private S3Event generateS3Event(SourceOfRandomness r, GenerationStatus status) {

        List<Object> mutatedS3Objects = mutatedResources.stream().filter(obj -> obj instanceof S3Object).collect(Collectors.toList());
        List<S3EventNotification.S3EventNotificationRecord> records = new ArrayList<>();

        for (Object s3Object : mutatedS3Objects) {
            if (r.nextBoolean())
                records.add(generateS3EventNotificationRecord((S3Object) s3Object, r, status));
        }

        mutatedS3Objects.stream().forEach(s3Obj -> {
            try {
                ((S3Object) s3Obj).close();
            } catch (java.io.IOException e) {
                assert false : "unable to close s3 object. assumption violated. failing";
            }
        });


        return new S3Event(records);
    }


    private S3EventNotification.S3EventNotificationRecord generateS3EventNotificationRecord(S3Object s3Object, SourceOfRandomness r, GenerationStatus status) {
        ObjectMetadata objMeta = s3Object.getObjectMetadata();

        String eventName = LambdaFuzzingInput.lambdaEventType.toString();
        String eventSource = LambdaFuzzingInput.eventSource.toString();
        DateTime eventTime = r.choose(new DateTime[]{new DateTime(r.nextLong()), new DateTime(System.currentTimeMillis())});
        String eventVersion = objMeta.getVersionId();
        S3EventNotification.RequestParametersEntity requestParameters = generateRequestParametersEntity(r, status);

        AlphaStringGenerator strGen = new AlphaStringGenerator();
        S3EventNotification.ResponseElementsEntity responseElements =
                new S3EventNotification.ResponseElementsEntity(gen().make(AlphaStringGenerator.class).generate(r, status),
                        gen().make(AlphaStringGenerator.class).generate(r, status));

        Object[] buckets = S3Env.buckets.stream().filter(b -> b.getName().equals(s3Object.getBucketName())).toArray();
        assert (buckets.length == 1) : "unexpected bucket size. We should find exactly one bucket matching to the event";
        Bucket bucket = (Bucket) buckets[0];
        String bucketArn = "arn:aws:s3:::" + bucket.getName() + "/" + s3Object.getKey();
        S3EventNotification.S3BucketEntity bucketEntity = new S3EventNotification.S3BucketEntity(bucket.getName(),
                new S3EventNotification.UserIdentityEntity(bucket.getOwner().getDisplayName()),
                bucketArn);

        S3EventNotification.S3ObjectEntity s3ObjectEntity = new S3EventNotification.S3ObjectEntity(s3Object.getKey(),
                objMeta.getContentLength(),
                objMeta.getETag(),
                objMeta.getVersionId(),
                objMeta.getArchiveStatus());

        S3EventNotification.S3Entity s3 = new S3EventNotification.S3Entity("", bucketEntity, s3ObjectEntity, eventVersion);


        S3EventNotification.UserIdentityEntity userIdentity = new S3EventNotification.UserIdentityEntity(
                r.choose(new String[]{new AlphaStringGenerator().generate(r, status),
                        bucket.getOwner().getDisplayName()}));

        return new S3EventNotification.S3EventNotificationRecord(
                REGION,
                eventName,
                eventSource,
                eventTime.toString(),
                eventVersion,
                requestParameters,
                responseElements,
                s3,
                userIdentity
        );
    }

    private S3EventNotification.RequestParametersEntity generateRequestParametersEntity(SourceOfRandomness r, GenerationStatus status) {
        boolean isIpv4 = r.nextBoolean();

        int subnet1 = isIpv4 ? r.nextInt(0, 255) : r.nextInt(0, 65535);
        int subnet2 = isIpv4 ? r.nextInt(0, 255) : r.nextInt(0, 65535);
        int subnet3 = isIpv4 ? r.nextInt(0, 255) : r.nextInt(0, 65535);
        int subnet4 = isIpv4 ? r.nextInt(0, 255) : r.nextInt(0, 65535);
        return new S3EventNotification.RequestParametersEntity(subnet1 + "." + subnet2 + "." + subnet3 + "." + subnet4);
    }
}
