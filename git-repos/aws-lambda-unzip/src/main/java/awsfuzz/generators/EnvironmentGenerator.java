package awsfuzz.generators;

import awsfuzz.env.Event;
import awsfuzz.generators.s3.S3EventGen;
import awsfuzz.generators.s3.S3ObjGenerator;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.ArrayList;
import java.util.List;

import static awsfuzz.generators.LambdaFuzzingInput.eventSource;

public class EnvironmentGenerator extends Generator<Event> {

    public EnvironmentGenerator() {
        super(Event.class);
    }

//    private S3ObjGenerator s3Gen = new S3ObjGenerator();

    //this mutates the environment
    @Override
    public Event generate(SourceOfRandomness r, GenerationStatus status) {

        List<Object> mutatedObjects;
        Object event;

        //It is possible to call that twice for the setup of the environment and another for the fuzzing loop.
//      generateResources(r, status);
        System.out.println("before generate resources");
        mutatedObjects = generateResources(r, status);
        System.out.println("after generate resources and before generate event");
        event = generateEvent(r, status, mutatedObjects);
        System.out.println("after generate event");

        return new Event(event);
    }

    //generate different resources for testing.
    //returns the mutated resources only.
    private List<Object> generateResources(SourceOfRandomness r, GenerationStatus status) {
        List<Object> output = new ArrayList<>();
        output.addAll((gen().make(S3ObjGenerator.class).generate(r, status)));
//        output.addAll((new DynamoDbGenerator().generate(r, status)));
        return output;
    }

    //this picks one of the s3 resources and generates an event matching
    // the lambda handler
    private Object generateEvent(SourceOfRandomness r, GenerationStatus status, List<Object> mutatedResources) {
        switch (eventSource) {
            case S3_EVENT_SOURCE:
                return gen().make(S3EventGen.class).setMutatedResources(mutatedResources).generate(r, status);
            default:
                assert false : "unsupported lambda event";
//                throw new ServerlessGeneratorException("unsupported lambda event.");
        }
        return null;
    }
}
