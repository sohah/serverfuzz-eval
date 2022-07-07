package awsfuzz.generators.s3;

import awsfuzz.env.FileTypeUtil;
import awsfuzz.env.S3Env;
import awsfuzz.generators.jqf.files.*;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.internal.GeometricDistribution;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static awsfuzz.env.FileTypeUtil.mutateByteAtIndex;
import static awsfuzz.env.S3Env.buckets;
import static awsfuzz.env.S3Env.s3Client;

//Generate objects inside the bucket, by either updating existing objects or adding new objects.
public class S3ObjGenerator extends Generator<List> {

    private static GeometricDistribution geometricDistribution = new GeometricDistribution();

    private static final int MEAN_FILES_IN_BUCKET = 3;
    private static final double PROBABILITY_OF_ERRONEOUS_FILE = 1.0;

    public S3ObjGenerator() {
        super(List.class);
    }

    //TODO: generate a list that is not a singleton.
    @Override
    public ArrayList<S3Object> generate(SourceOfRandomness r, GenerationStatus status) {
        ArrayList<S3Object> output = new ArrayList<>();

        for (Bucket bucket : buckets) {
            ArrayList<File> localFiles = new ArrayList<>();

            int numOfFiles = Math.max(0, geometricDistribution.sampleWithMean(MEAN_FILES_IN_BUCKET, r));

            for (int i = 0; i < numOfFiles; i++)
                localFiles.add(createFile(r, status));
//                output.add(generateS3forBucket(r, status, bucket));

            S3Env.populateBatchToCloud(bucket, localFiles);
            localFiles.forEach(f -> output.add(s3Client.getObject(bucket.getName(), "ZZ_DIR/" + f.getName())));
        }
        return output;
    }

    private File createFile(SourceOfRandomness r, GenerationStatus status) {
        FileTypeUtil.FileType fileType = r.choose(FileTypeUtil.FileType.values());
        File localFile = null;

        boolean erroneousGeneration = r.nextDouble(0, 1) <= PROBABILITY_OF_ERRONEOUS_FILE;
        switch (fileType) {
            case XML:
            case IMGE:
            case JAVA_CLASS:
            case JAVA_SCRIPT:
            case TEXT:
                localFile = gen().make(AnyNonZipFileGenerator.class).setFileType(fileType).setErroneousExtensionGeneration(erroneousGeneration).generate(r, status);
                break;
            case ZIP:
                localFile = gen().make(ZipFileGenerator.class).setErroneousExtensionGeneration(erroneousGeneration).generate(r, status);
                break;
        }

        if (erroneousGeneration && r.nextBoolean()) {
            //attempts to make the byte stream erroneous too.
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(localFile.getPath()));
                //mutate 3 bytes at random.
                if (bytes.length > 4) {
                    for (int i = 0; i < 4; i++) {
                        int indexToMutate = r.nextInt(0, bytes.length - 1);
                        mutateByteAtIndex(r, bytes, indexToMutate);
                    }
                    try (FileOutputStream updatedFileStream = new FileOutputStream(localFile)) {
                        updatedFileStream.write(bytes);
                    }
                }
            } catch (IOException e) {
                assert false : "problem creating mutated files. failing.";
                throw new RuntimeException(e);
            }
        }
        assert localFile != null : "generated file should not be null. failing.";
        return localFile;
    }
}
