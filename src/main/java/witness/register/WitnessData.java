package witness.register;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class WitnessData implements Serializable {
    private AtomicLong version;

    /*
        This blob as of now will contain a serialized version of Metadata as we need this info for leader election..this
        can be changed to anything later on..
     */
    byte[] metadataByteArray;

    public WitnessData(long version, byte[] metadataByteArray) {
        this.version = new AtomicLong(version);
        this.metadataByteArray = metadataByteArray;
    }


    public void setVersion(long version) {
        this.version.set(version);
    }

    public long getVersion() {
        return this.version.get();
    }

    public byte[] getMetadataByteArray() {
        return metadataByteArray;
    }

    public void setMetadataByteArray(byte[] metadataByteArray) {
        this.metadataByteArray = metadataByteArray;
    }
}
