package awsfuzz.generators.jqf.files;

import awsfuzz.env.FileTypeUtil;
import awsfuzz.generators.FlexibleNonTrackingGenerationStatus;
import awsfuzz.generators.MixedStringGenerator;
import awsfuzz.generators.jqf.AlphaStringGenerator;
import awsfuzz.generators.jqf.ImageInputStreamGenerator;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.apache.bcel.classfile.JavaClass;
import org.w3c.dom.Document;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static awsfuzz.env.Environment.FUZZDIR;
import static awsfuzz.env.FileTypeUtil.*;

public class AnyNonZipFileGenerator extends Generator<File> {
    FileTypeUtil.FileType fileType;
    private Boolean erroneousExtensionGeneration = null;

    public AnyNonZipFileGenerator() {
        super(File.class);
    }

    @Override
    public File generate(SourceOfRandomness r, GenerationStatus status) {
        assert fileType != null : "setFileType is required before generation";
        assert erroneousExtensionGeneration != null : "erroneousGeneration flag must be set to use this generator. failing.";

        File localFile = null;
        String fileName = FUZZDIR + "/" + gen().make(AlphaStringGenerator.class).generate(r, status);
        fileName += makeValidOrErroneousFilename(fileType, r, erroneousExtensionGeneration);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(fileName);
            switch (fileType) {
                case XML:
                    Document xmlDoc = gen().make(XmlDocumentGenerator.class).generate(r, status);
                    writeXml(xmlDoc, output);
                    break;
                case IMGE: //needs debugging, something is going wrong here.
//                    ImageInputStream imageDoc = ImageIO.createImageInputStream(gen().make(InputStreamGenerator.class).generate(r, status));
//                    fileName += erroneousGeneration? r.choose(getAllExtensions()) : r.choose(getExtensions(FileTypeUtil.FileType.IMGE));
//                    fileName += r.choose(new String[]{".png", ".gif", ".gpeg", ".tiff"});
//                    output = new FileOutputStream(fileName);
//                    byte[] buffer = new byte[512];
//                    for (int length; (length = imageDoc.read(buffer)) > 0; ) {
//                        output.write(buffer, 0, length);
//                    }
//                    localFile = new File(fileName);
//                    break;
                case JAVA_CLASS:
                    JavaClass javaDoc = gen().make(JavaClassGenerator.class).generate(r, status);
                    output.write(javaDoc.getBytes());
                    break;
                case JAVA_SCRIPT:
                    String javaScriptDoc = gen().make(JavaScriptCodeGenerator.class).generate(r, status);
                    output.write(javaScriptDoc.getBytes(StandardCharsets.UTF_8));
                    break;
                case TEXT:
                    String textDoc = gen().make(MixedStringGenerator.class).generate(r, status);
                    output.write(textDoc.getBytes(StandardCharsets.UTF_8));
                    break;
                default:
                    assert false : "unexpected file type in non-zip file creation";
            }
            localFile = new File(fileName);
        } catch (IOException | TransformerException e) {
            assert false : "Unable to create local file for s3. Failing";
        } finally {
            if (output != null) try {
                output.close();
            } catch (IOException e) {
                assert false : "problem closing stream. assumptions violated. failing.";
            }
        }
        assert localFile != null : "generated file should not be null. failing.";
        return localFile;
    }

    public AnyNonZipFileGenerator setFileType(FileTypeUtil.FileType fileType) {
        this.fileType = fileType;
        return this;
    }

    // write doc to output stream
    private static void writeXml(Document doc, OutputStream output) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
    }

    public AnyNonZipFileGenerator setErroneousExtensionGeneration(boolean erroneousGeneration) {
        this.erroneousExtensionGeneration = erroneousGeneration;
        return this;
    }
}
