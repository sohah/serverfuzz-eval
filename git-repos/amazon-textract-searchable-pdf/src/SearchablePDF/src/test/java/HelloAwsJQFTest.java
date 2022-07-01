import serverfuzz.HelloAwsJQF_;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.pholser.junit.quickcheck.From;
import serverfuzz.env.Environment;
import serverfuzz.env.Event;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import serverfuzz.generators.EnvironmentGenerator;
import serverfuzz.generators.LambdaFuzzingInput;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;


@RunWith(JQF.class)
public class HelloAwsJQFTest {

    static int testCount = 0;
    @BeforeClass
    public static void setupEnv() {
        LambdaFuzzingInput.setupS3EventLambda("DemoLambda",
                "public java.lang.String DemoLambda.handleRequest(com.amazonaws.services.lambda.runtime.events.S3Event,com.amazonaws.services.lambda.runtime.Context)",
                LambdaFuzzingInput.S3EventTypes.OBJECT_CREATED_PUT, "mybucket");
        Environment.initFUZZ_DIR();

    }

    @After
    public void resetState() {
        Environment.reset();
        testCount++;
    }



    @Fuzz
    public void fuzzLambdaS3Event(@From(EnvironmentGenerator.class) Event env) throws IllegalArgumentException {
        System.out.println("before running JQF Testcase " + testCount);

        (new DemoLambda()).handleRequest((S3Event) env.event, null);
        System.out.println("after running JQF Testcase " + testCount);
    }
}
