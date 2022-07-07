package awsfuzz.generators.jqf.files;

import awsfuzz.env.FileTypeUtil;
import awsfuzz.generators.jqf.AlphaStringGenerator;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static awsfuzz.env.Environment.FUZZDIR;
import static awsfuzz.env.FileTypeUtil.*;

public class ZipFileGenerator extends Generator<File> {
    private Boolean erroneousExtensionGeneration = null;

    public ZipFileGenerator() {
        super(File.class);
    }

    @Override
    public File generate(SourceOfRandomness r, GenerationStatus status) {
        assert erroneousExtensionGeneration != null : "erroneousGeneration flag must be set to use this generator. failing.";
        int numZipEntry = r.nextInt(0, 2);
        List<File> files = new ArrayList<>();

        for (int i = 0; i <= numZipEntry; i++) {
            //choosing any non-zip file
            FileTypeUtil.FileType fileType = r.choose(Arrays.stream(FileTypeUtil.FileType.values()).filter(type -> type != FileTypeUtil.FileType.ZIP).toArray(FileTypeUtil.FileType[]::new));
            files.add(gen().make(AnyNonZipFileGenerator.class).setFileType(fileType).setErroneousExtensionGeneration(erroneousExtensionGeneration).generate(r, status));
        }

        //the probability of producing erroneous file extension is actually less than 1% due to the way we are doing the choice below for the file type.
        String fileType = makeValidOrErroneousFilename(FileTypeUtil.FileType.ZIP, r, erroneousExtensionGeneration);

        String zipFullFileName = FUZZDIR + "/" + gen().make(AlphaStringGenerator.class).generate(r, status) + fileType;

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFullFileName))) {
            for (File fileToZip : files) {
                zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
                Files.copy(fileToZip.toPath(), zipOut);
                fileToZip.delete();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new File(zipFullFileName);
    }

    public ZipFileGenerator setErroneousExtensionGeneration(boolean erroneousGeneration) {
        this.erroneousExtensionGeneration = erroneousGeneration;
        return this;
    }
}