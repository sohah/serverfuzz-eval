package awsfuzz.generators;


import awsfuzz.env.FileTypeUtil;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import static awsfuzz.env.FileTypeUtil.getAllExtensions;
import static awsfuzz.env.FileTypeUtil.getExtensions;

public class Commons {

    public static final String[] awsRegions = {"us-east-1", "us-west-2", "fake"};



    //available versions for s3, the last one being a fake version.
    public static final String[] eventVersions = {"2.1", " 2.1", "2.2", "2.3", "0.-1"};

}
