package io.sigpipe.sing.adapters;

import java.io.File;

import io.sigpipe.sing.dataset.MetaArray;
import io.sigpipe.sing.dataset.Metadata;
import io.sigpipe.sing.serialization.Serializer;

public class ReadMetaBundle {

    public static void main(String[] args)
    throws Exception {

        File bundle = new File(args[0]);
        MetaArray ma = Serializer.restore(MetaArray.class, bundle);
        for (Metadata m : ma) {
            System.out.println(m.getSpatialProperties());
        }

    }

}
