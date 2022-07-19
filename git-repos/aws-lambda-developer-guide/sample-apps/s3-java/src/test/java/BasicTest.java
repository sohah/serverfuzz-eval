import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.pholser.junit.quickcheck.From;
import example.Handler;
import serverfuzz.Config;
import serverfuzz.StatisticsManager;
import serverfuzz.env.Environment;
import serverfuzz.env.Event;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import serverfuzz.env.FileTypeUtil;
import serverfuzz.generators.EnvironmentGenerator;
import serverfuzz.generators.LambdaFuzzingInput;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.util.Arrays;


@RunWith(JQF.class)
public class BasicTest {

    @BeforeClass
    public static void setupEnv() {
        LambdaFuzzingInput.setupS3EventLambda("example.Handler",
                "String example.Handler.handleRequest(com.amazonaws.services.lambda.runtime.events.S3Event,com.amazonaws.services.lambda.runtime.Context)",
                LambdaFuzzingInput.S3EventTypes.OBJECT_CREATED_PUT, "my-bucket");
        Config.isDebugModeOn = true;
        Config.allowErroneousGeneration = true;
        Config.allowFilesOfType = new FileTypeUtil.FileType[]{FileTypeUtil.FileType.IMGE};
        Config.allowedImageExtensions = Arrays.stream(FileTypeUtil.ImageExtensions.values()).map(t -> t.toString()).toArray(String[]::new); //allowing all image formats
        Config.FUZZDIR="inbound";
        Environment.initFUZZ_DIR();
    }

    @After
    public void resetState() {
        Environment.reset();
        StatisticsManager.testCasesCount++;
    }


    @Fuzz
    public void fuzzLambdaS3Event(@From(EnvironmentGenerator.class) Event env) throws IllegalArgumentException {

        (new Handler()).handleRequest((S3Event) env.event, null);
        System.out.printf("finished testcase = %d, erroneous generation = %d, no extension = %d%n",
                StatisticsManager.testCasesCount, StatisticsManager.erronousFilesCount, StatisticsManager.noFileExtensionCount);
    }

    @Fuzz(repro = "target/fuzz-results/BasicTest/fuzzLambdaS3Event/corpus")
    public void fuzzLambdaS3EventRepro(@From(EnvironmentGenerator.class) Event env) throws IllegalArgumentException {

        (new Handler()).handleRequest((S3Event) env.event, null);
        System.out.printf("finished testcase = %d, erroneous generation = %d, no extension = %d%n",
                StatisticsManager.testCasesCount, StatisticsManager.erronousFilesCount, StatisticsManager.noFileExtensionCount);
    }
}
