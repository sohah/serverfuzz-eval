import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import serverfuzz.Config;
import serverfuzz.env.Environment;
import serverfuzz.env.Event;
import serverfuzz.env.FileTypeUtil;
import serverfuzz.generators.EnvironmentGenerator;
import serverfuzz.generators.LambdaFuzzingInput;


@RunWith(JQF.class)
public class BasicTest {

    static int testCount = 0;

    @BeforeClass
    public static void setupEnv() {
        LambdaFuzzingInput.setupS3EventLambda("DemoLambda",
                "public java.lang.String DemoLambda.handleRequest(com.amazonaws.services.lambda.runtime.events.S3Event,com.amazonaws.services.lambda.runtime.Context)",
                LambdaFuzzingInput.S3EventTypes.OBJECT_CREATED_PUT, "mybucket");
        Environment.initFUZZ_DIR();
        Config.isDebugModeOn = true;
        Config.allowErroneousGeneration = false;
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

//    @Fuzz(repro = "target/fuzz-results/BasicTest/fuzzLambdaS3Event/corpus")
//    public void fuzzLambdaS3EventRepro(@From(EnvironmentGenerator.class) Event env) throws IllegalArgumentException {
//        System.out.println("before running JQF Testcase " + testCount);
//
//        (new DemoLambda()).handleRequest((S3Event) env.event, null);
//        System.out.println("after running JQF Testcase " + testCount);
//    }
}
