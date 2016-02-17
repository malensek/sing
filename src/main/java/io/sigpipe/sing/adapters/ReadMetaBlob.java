
package io.sigpipe.sing.adapters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import io.sigpipe.sing.dataset.Metadata;
import io.sigpipe.sing.graph.FeaturePath;
import io.sigpipe.sing.graph.MetadataGraph;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.Serializer;

public class ReadMetaBlob {

    public static void main(String[] args) throws Exception {
        File bundle = new File(args[0]);

        System.out.println("Reading metadata blob...");
        FileInputStream fIn = new FileInputStream(bundle);
        BufferedInputStream bIn = new BufferedInputStream(fIn);
        SerializationInputStream in = new SerializationInputStream(bIn);

        int num = in.readInt();
        System.out.println("Records: " + num);

        MetadataGraph mdg = new MetadataGraph();

        for (int i = 0; i < num; ++i) {
            float lat = in.readFloat();
            float lon = in.readFloat();
            byte[] payload = in.readField();

            Metadata m = Serializer.deserialize(Metadata.class, payload);
            FeaturePath<String> path = new FeaturePath<>(
                    "x", m.getAttributes().toArray());
            mdg.addPath(path);
            System.out.print('.');
        }

        in.close();
    }

}
