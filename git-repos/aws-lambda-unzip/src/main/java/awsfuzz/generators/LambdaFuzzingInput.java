package awsfuzz.generators;




//input to fuzzing of the lambda, simulating setup of the aws lambda
public class LambdaFuzzingInput {

    //specifies the name of the lambda that we want to fuzz.
    public static String lambdaClassStr;

    public static String handlerHeader;

    //specifies the name of the bucket if the lambda is triggered on an S3Event.
    public static String lambdaBucketStr;

    public static S3EventTypes lambdaEventType;

    public static EventSource eventSource;

    //this is the name of the lambda class we want to fuzz.
    // we will use that to figure out the event we need to trigger the lambda with
    public static Class<?> lambdaKlass;

    //a subset of events found https://docs.aws.amazon.com/AmazonS3/latest/userguide/notification-how-to-event-types-and-destinations.html
    public enum S3EventTypes {
        OBJECT_CREATED_STAR //("ObjectCreated:*"),
        , OBJECT_CREATED_PUT //("ObjectCreated:Put"),
        , OBJECT_CREATED_POST //("ObjectCreated:Post"),
        , OBJECT_CREATED_COPY //("ObjectCreated:Copy"),
        , OBJECT_REMOVED_STAR //("ObjectRemoved:*"),
        , OBJECT_REMOVED_DELETE  //("ObjectRemoved:Delete"),
        , OBJECT_REMOVED_DELETE_MARKER_CREATED //("ObjectRemoved:DeleteMarkerCreated"),
    }

    ;

    public enum EventSource {
        S3_EVENT_SOURCE;
    }


    //these should be passed in as parameter for the fuzzer.
    public static void setupS3EventLambda(String lambdaClass, String hander, S3EventTypes s3EventTypes, String bucketName) {

        lambdaClassStr = lambdaClass;

        //specifies the name of the bucket if the lambda is triggered on an S3Event.
        lambdaBucketStr = bucketName;

        lambdaEventType = s3EventTypes;

        eventSource = EventSource.S3_EVENT_SOURCE;
        handlerHeader = hander;


        // this discovers the list of existing resources as well as finding the reflective class of the lambda
        // this should be invoked once before all test cases.
        try {
            lambdaKlass = Class.forName(LambdaFuzzingInput.lambdaClassStr);
        } catch (ClassNotFoundException e) {

        }
    }
}


