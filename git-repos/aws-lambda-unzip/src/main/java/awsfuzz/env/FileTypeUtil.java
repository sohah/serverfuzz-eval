package awsfuzz.env;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileTypeUtil {

    public static String[] getExtensions(FileType fileType) {
        switch (fileType) {
            case XML:
                return new String[]{".xml"};
            case IMGE:
                return new String[]{".png", ".gif", ".gpeg", ".tiff"};
            case JAVA_CLASS:
                return new String[]{".class"};
            case JAVA_SCRIPT:
                return new String[]{".js"};
            case TEXT:
                return new String[]{".txt"};
            case ZIP:
                return new String[]{".zip"};
        }
        assert false : "unexpected type. failing.";
        return null;
    }

    public static List<String> getAllExtensions() {
        return Arrays.stream(FileType.values()).map(type -> getExtensions(type)).flatMap(arr -> Arrays.stream(arr)).collect(Collectors.toList());
    }


    public static void mutateByteAtIndex(SourceOfRandomness r, byte[] originalBytes, int index) {
        assert index < originalBytes.length : "index of mutation must be within the length of the bytes to change";

        originalBytes[index] = r.nextByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
        System.out.println("generated a file with bytestream mutation.");
    }

    public static String makeValidOrErroneousFilename(FileTypeUtil.FileType fileType, SourceOfRandomness r, boolean erroneousExtensionGeneration) {
        String filename;
        if (erroneousExtensionGeneration) {
            filename = r.choose(getAllExtensions());
            filename = r.nextBoolean() ? filename.replace(".", "") : filename;
        } else
            filename = getExtensions(fileType)[0];
        return filename;
    }

    public enum FileType {
        XML,
        IMGE,
        JAVA_CLASS,
        JAVA_SCRIPT,
        TEXT,
        ZIP
    }
}
