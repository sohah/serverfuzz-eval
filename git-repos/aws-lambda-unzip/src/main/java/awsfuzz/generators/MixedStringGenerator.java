package awsfuzz.generators;

import awsfuzz.generators.jqf.AlphaStringGenerator;
import awsfuzz.generators.jqf.AsciiStringGenerator;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.Arrays;

/**
 * used to create different forms of a string for contents of a text file.
 * note that this uses a specific generation status to control the mean of the lines generated in the text file to be around 65 bytes.
 */
public class MixedStringGenerator extends Generator<String> {

    private enum STRING_GENERATOR{
        ASCII, ALPHA, CODE_POINT, EMPTY
    }
    public MixedStringGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness r, GenerationStatus status) {
        GenerationStatus genStatus = new FlexibleNonTrackingGenerationStatus(r);
        STRING_GENERATOR strGenType = r.choose(Arrays.asList(STRING_GENERATOR.ASCII, STRING_GENERATOR.ALPHA, STRING_GENERATOR.CODE_POINT));
        String output = "";
        switch (strGenType) {
            case ASCII:
                output = gen().make(AsciiStringGenerator.class).generate(r, genStatus);
                break;
            case ALPHA:
                output = gen().make(AlphaStringGenerator.class).generate(r, genStatus);
                break;
            case CODE_POINT:
                output = gen().type(String.class).generate(r, genStatus);
                break;
            case EMPTY: //do nothing, we already have output being the empty string.
                break;

        }

        return output;
    }
}
