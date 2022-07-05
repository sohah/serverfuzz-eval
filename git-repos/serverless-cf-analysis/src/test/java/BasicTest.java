import com.amazonaws.services.lambda.CreateAthenaPartitionsBasedOnS3Event;
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
        LambdaFuzzingInput.setupS3EventLambda("com.amazonaws.services.lambda.CreateAthenaPartitionsBasedOnS3Event",
                "public java.lang.Void com.amazonaws.services.lambda.CreateAthenaPartitionsBasedOnS3Event.handleRequest(com.amazonaws.services.lambda.runtime.events.S3Event,com.amazonaws.services.lambda.runtime.Context)",
                LambdaFuzzingInput.S3EventTypes.OBJECT_CREATED_PUT, "my-bucket");
        Environment.initFUZZ_DIR();
      Config.isDebugModeOn = true;
      Config.allowErroneousGeneration = false;
      Config.allowFilesOfType = new FileTypeUtil.FileType[]{FileTypeUtil.FileType.IMGE};
      Config.allowedImageExtensions = new String[]{".png"};

    }

    @After
    public void resetState() {
        Environment.reset();
        testCount++;
    }


  @Fuzz
  public void fuzzLambdaS3Event(@From(EnvironmentGenerator.class) Event env) throws IllegalArgumentException {
    System.out.println("before running JQF Testcase " + testCount);

    (new CreateAthenaPartitionsBasedOnS3Event()).handleRequest((S3Event) env.event, null);
    System.out.println("after running JQF Testcase " + testCount);
  }

  /*@Fuzz(repro = "target/fuzz-results/BasicTest/fuzzLambdaS3Event/corpus")
  public void fuzzLambdaS3EventRepro(@From(EnvironmentGenerator.class) Event env) throws IllegalArgumentException {
    System.out.println("before running JQF Testcase " + testCount);

    (new CreateAthenaPartitionsBasedOnS3Event()).handleRequest((S3Event) env.event, null);
    System.out.println("after running JQF Testcase " + testCount);
  }*/
}
