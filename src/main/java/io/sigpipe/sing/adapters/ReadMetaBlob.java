
package io.sigpipe.sing.adapters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import io.sigpipe.sing.serialization.SerializationInputStream;

public class ReadMetaBlob {

    public static void main(String[] args) throws Exception {
        File bundle = new File(args[0]);

        System.out.println("Reading metadata blob...");
        FileInputStream fIn = new FileInputStream(bundle);
        BufferedInputStream bIn = new BufferedInputStream(fIn);
        SerializationInputStream in = new SerializationInputStream(bIn);

        int num = in.readInt();
        System.out.println("Records: " + num);
        for (int i = 0; i < num; ++i) {
            float lat = in.readFloat();
            float lon = in.readFloat();
            byte[] payload = in.readField();
            System.out.println(lat + "," + lon + "    " + payload.length);
        }

        in.close();
    }

}
