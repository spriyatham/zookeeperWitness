package witness.client;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.zookeeper.server.quorum.WitnessMetadata;
import org.apache.zookeeper.server.quorum.witness.generated.*;
import witness.register.Witness;

import java.io.*;
import java.util.Scanner;

public class TestMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 2890).usePlaintext().build();
        WitnessGrpc.WitnessBlockingStub stub = WitnessGrpc.newBlockingStub(managedChannel);
        Scanner s = new Scanner(System.in);
        while (true) {
            String command = s.nextLine();
            //write <version> <acceptedEpcoh> <currentEpoc> <zxid>
            //read
            //shutdown
            if(command.startsWith("write")) {
                String[] writeParams = command.split("\\s+");
                WitnessMetadata metadata = new WitnessMetadata(Long.parseLong(writeParams[2]), Long.parseLong(writeParams[3]), Long.parseLong(writeParams[4]));
                ByteString metadataBS = ByteString.copyFrom(TestMain.convertToByteArray(metadata));
                WriteRequest writeRequest = WriteRequest.newBuilder()
                        .setMetadata(metadataBS)
                        .setVersion(Long.parseLong(writeParams[1]))
                        .build();
                WriteResponse writeResponse = stub.write(writeRequest);
                System.out.println("WriteResponse: InputVersion = " + writeParams[1] + " , outputVersion = "+ writeResponse.getVersion());
            }
            else if(command.equals("read")) {
                ReadResponse readResponse = stub.read(ReadRequest.newBuilder().build());
                WitnessMetadata metadata = createMetadata(readResponse.getMetadata().toByteArray());
                System.out.println(String.format("ReadResponse: version = %d , zxid = %s , acceptedEpoch = %s , currentEpoch = %s",
                        readResponse.getVersion(), Long.toHexString(metadata.getZxid()), Long.toHexString(metadata.getAcceptedEpoch()), Long.toHexString(metadata.getCurrentEpoch())));
            }
            else if(command.equals("shutdown")) {
                break;
            }
            else {
                System.out.println("command not supported, try enter 'write <version> <acceptedEpcoh> <currentEpoc> <zxid>', read or shutdown");
            }

        }
    }

    static public byte[] convertToByteArray(WitnessMetadata metadata) throws IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(metadata);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            //TODO: Handle Exception
            e.printStackTrace();
            throw e;
        }
    }
    public static WitnessMetadata createMetadata(byte[] metadataByteArray) throws IOException, ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(metadataByteArray));
            return (WitnessMetadata) ois.readObject();
        } catch (Exception e) {
            //TODO: handle execption
            e.printStackTrace();
            throw e;
        }
    }
}
