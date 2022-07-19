import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import kornell.S3EventProcessorUnzip;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import serverfuzz.Config;
import serverfuzz.StatisticsManager;
import serverfuzz.env.Environment;
import serverfuzz.env.Event;
import serverfuzz.env.FileTypeUtil;
import serverfuzz.generators.EnvironmentGenerator;
import serverfuzz.generators.LambdaFuzzingInput;

import java.util.Arrays;

import static serverfuzz.env.FileTypeUtil.getExtensions;


@RunWith(JQF.class)
public class BasicTest {

    @BeforeClass
    public static void setupEnv() {
        LambdaFuzzingInput.setupS3EventLambda("kornell.S3EventProcessorUnzip",
                "public java.lang.String kornell.S3EventProcessorUnzip.handleRequest(com.amazonaws.services.lambda.runtime.events.S3Event,com.amazonaws.services.lambda.runtime.Context)",
                LambdaFuzzingInput.S3EventTypes.OBJECT_CREATED_PUT, "mybucket");
        Environment.initFUZZ_DIR();
        Config.isDebugModeOn = true;
        Config.allowErroneousGeneration = true;
        Config.allowFilesOfType = new FileTypeUtil.FileType[]{FileTypeUtil.FileType.ZIP};
        Config.allowedImageExtensions = getExtensions(FileTypeUtil.FileType.ZIP); //allowing all image formats
    }

    @After
    public void resetState() {
        Environment.reset();
        StatisticsManager.testCasesCount++;
    }


    @Fuzz
    public void fuzzLambdaS3Event(@From(EnvironmentGenerator.class) Event env) throws IllegalArgumentException {

        (new S3EventProcessorUnzip()).handleRequest((S3Event) env.event, null);
        System.out.printf("finished testcase = %d, erroneous generation = %d, no extension = %d%n",
                StatisticsManager.testCasesCount, StatisticsManager.erronousFilesCount, StatisticsManager.noFileExtensionCount);
    }

    @Fuzz(repro = "target/fuzz-results/BasicTest/fuzzLambdaS3Event/corpus")
    public void fuzzLambdaS3EventRepro(@From(EnvironmentGenerator.class) Event env) throws IllegalArgumentException {

        (new S3EventProcessorUnzip()).handleRequest((S3Event) env.event, null);
        System.out.printf("finished testcase = %d, erroneous generation = %d, no extension = %d%n",
                StatisticsManager.testCasesCount, StatisticsManager.erronousFilesCount, StatisticsManager.noFileExtensionCount);

    }
}
