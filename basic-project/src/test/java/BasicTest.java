import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.pholser.junit.quickcheck.From;
import aws.env.Environment;
import aws.env.Event;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import aws.generators.EnvironmentGenerator;
import aws.generators.LambdaFuzzingInput;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;


@RunWith(JQF.class)
public class BasicTest {

    static int testCount = 0;
    @BeforeClass
    public static void setupEnv() {
        LambdaFuzzingInput.setupS3EventLambda("BasicClass",
                "public java.lang.String BasicClass.handleRequest(com.amazonaws.services.lambda.runtime.events.S3Event,com.amazonaws.services.lambda.runtime.Context)",
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

        (new BasicClass()).handleRequest((S3Event) env.event, null);
        System.out.println("after running JQF Testcase " + testCount);
    }
}
