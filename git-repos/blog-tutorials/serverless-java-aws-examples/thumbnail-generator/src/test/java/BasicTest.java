import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.pholser.junit.quickcheck.From;
import serverfuzz.Config;
import serverfuzz.env.Environment;
import serverfuzz.env.Event;
import de.rieckpil.blog.ThumbnailHandler;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import serverfuzz.env.FileTypeUtil;
import serverfuzz.generators.EnvironmentGenerator;
import serverfuzz.generators.LambdaFuzzingInput;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;


@RunWith(JQF.class)
public class BasicTest {

    static int testCount = 0;
    @BeforeClass
    public static void setupEnv() {
        LambdaFuzzingInput.setupS3EventLambda("de.rieckpil.blog.ThumbnailHandler",
                "public java.lang.Void de.rieckpil.blog.ThumbnailHandler.handleRequest(com.amazonaws.services.lambda.runtime.events.S3Event,com.amazonaws.services.lambda.runtime.Context)",
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

        (new ThumbnailHandler()).handleRequest((S3Event) env.event, null);
        System.out.println("after running JQF Testcase " + testCount);
    }
}
