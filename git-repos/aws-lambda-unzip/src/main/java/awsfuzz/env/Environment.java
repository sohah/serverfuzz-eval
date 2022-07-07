package awsfuzz.env;


import javax.imageio.ImageIO;
import java.io.File;

//this is acting like a controller
public class Environment {
    public static final String LOCAL_STACK_ENDPOINT = "http://192.168.0.63:4566";
    public static final String FUZZDIR = "FUZZ_DIR";


//    public static S3Env s3Env = new S3Env();
//    public static DynamoDbEnv dynamoDBEnv = new DynamoDbEnv();

//
//
//    //sets up the initial state for the environment
//    public static void initialize(){
//        s3Env.initialize();
//        //dynamoDBEnv.initializeDynamoDB();
//    }


    //clears all objects in the environment

    public static void initFUZZ_DIR() {
        ImageIO.setUseCache(false);
        File theDir = new File(FUZZDIR);
        if (!theDir.exists())
            theDir.mkdirs();
        else
            clearFUZZ_DIR();
    }

    public static void reset() {
        clearFUZZ_DIR();
        S3Env.reset();
        //dynamoDbEnv.resetDynamoDB();
    }

    private static void clearFUZZ_DIR() {
        File fuzzDir = new File(FUZZDIR);
        File[] contents = fuzzDir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                f.delete();
            }
        }
    }
}
