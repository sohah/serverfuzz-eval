import awsfuzz.env.Environment;
import awsfuzz.env.Event;
import awsfuzz.generators.EnvironmentGenerator;
import awsfuzz.generators.LambdaFuzzingInput;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import kornell.S3EventProcessorUnzip;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;


@RunWith(JQF.class)
public class BasicTest {

    static int testCount = 0;
    @BeforeClass
    public static void setupEnv() {
        LambdaFuzzingInput.setupS3EventLambda("kornell.S3EventProcessorUnzip",
                "public java.lang.String kornell.S3EventProcessorUnzip.handleRequest(com.amazonaws.services.lambda.runtime.events.S3Event,com.amazonaws.services.lambda.runtime.Context)",
                LambdaFuzzingInput.S3EventTypes.OBJECT_CREATED_PUT, "mybucket");
        Environment.initFUZZ_DIR();

    }

    @After
    public void resetState() {
        Environment.reset();
        testCount++;
    }


//    @Fuzz
//    public void fuzzLambda(@From(MixedStringGenerator.class) MyString s3Event) throws IllegalArgumentException {
//        System.out.println("before running JQF Testcase");
//        (new HelloAwsJQF_()).handleRequest(s3Event.myStr, null);
//        System.out.println("after running JQF Testcase");
//    }

    @Fuzz
    public void fuzzLambdaS3Event(@From(EnvironmentGenerator.class) Event env) throws IllegalArgumentException {
        System.out.println("before running JQF Testcase " + testCount);

        (new S3EventProcessorUnzip()).handleRequest((S3Event) env.event, null);
        System.out.println("after running JQF Testcase " + testCount);
    }
}
