package witness.register;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import jline.internal.Log;
import org.apache.zookeeper.server.quorum.witness.generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;

public class WitnessService extends WitnessGrpc.WitnessImplBase {
    private static final Logger LOG = LoggerFactory.getLogger(WitnessService.class);
    Witness witness;
    ReadWriteLock readWriteLock; //do  witness.getReadWriteLock() from the witness in the constructor.
    AtomicBoolean pingReceived;

    public WitnessService(Witness witness) {
        this.witness = witness;
        this.readWriteLock = witness.getReadWriteLock();
        this.pingReceived = witness.getPingTimeoutThread().getPingReceived();
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        LOG.debug("Received read request");
        ReadResponse.Builder responsebuilder = ReadResponse.newBuilder();
        /*
        Note: Commenting it out for testing purposes.
        if(!witness.isFollowing()) {
            //Dont service requests, when witness is not in FOLLOWING state
            responsebuilder.setVersion(-1);
            responseObserver.onNext(responsebuilder.build());
            responseObserver.onCompleted();
            LOG.debug("Witness is not in following state, returned an empty response with version = -1");
            return;
        }
        */
        notifyPingRecievedThread();
        LOG.debug("Read: Notified ping received thread");
        readWriteLock.readLock().lock();
        LOG.debug("Read : Acquired metadata read lock");
        WitnessData witnessData = witness.getWitnessData();
        ReadResponse response = responsebuilder
                .setVersion(witnessData.getVersion())
                .setMetadata(ByteString.copyFrom(witnessData.getMetadataByteArray()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        LOG.debug("Read : Returned current metadata to the caller");
        readWriteLock.readLock().unlock();
        LOG.debug("Read : Released metadata read lock");
        LOG.debug("Read : END");
    }

    /**
     * message WriteRequest {
     *   uint64 version = 1;
     *   bytes metadata = 2;
     * }
     *
     * message WriteResponse {
     *   // request.version != response.version when, witness has a more latest version than the one in the response..
     *   // the caller(witnessHandler) can take necessary action.
     *   uint64 version = 1;
        }
     * */
    @Override
    public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
        String logMessagePrefix = "WriteRequest : version = " + request.getVersion() + ": ";
        WriteResponse.Builder responseBuilder = WriteResponse.newBuilder();
        /*
        Note: Commenting it out for testing purposes.
        if(!witness.isFollowing()) {
            //Dont service requests, when witness is not in FOLLOWING state
            responseBuilder.setVersion(-1);
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }

         */
        notifyPingRecievedThread();
        LOG.debug(logMessagePrefix+ " Notified ping received thread");
        readWriteLock.writeLock().lock();
        Log.debug(logMessagePrefix + "Acquired metadata write lock");
        long requestVersion = request.getVersion();
        long localVersion = witness.getWitnessData().getVersion();

        if(requestVersion > localVersion) {
            LOG.debug(logMessagePrefix + " greater than current metadata version " + localVersion);
            try {
                witness.writeWitnessData(request);
                LOG.debug(logMessagePrefix + " metadata successfully written to witness");
                responseBuilder.setVersion(requestVersion);
            } catch (Exception e) {
                LOG.warn("Write request with version {} failed", requestVersion);
                responseObserver.onError(e);
                responseObserver.onCompleted();
                return;
            }
        }
        else {
            //requestVersion<=localVersion;
            /**
             * TODO: Its a bug when requestVersion==localVersion, but the localVersion was updated due to a write made by a leader
             * of different epoch..So in the caller, doing equals check on the version wouldn't suffice..We should either, use a more
             * sophisticated version (something that would help in identifying the server which performed that particular write) or
             * return the metadata as well in the WriteResponse. So the caller can check if a correct write has happened or not.
             * */
            LOG.debug(logMessagePrefix + " <= localVersion :" + localVersion + " Ignoring the write request and just returning the current version number");
            //Empty writes and writes with old versions..
            responseBuilder.setVersion(localVersion);
        }
        readWriteLock.writeLock().unlock();
        LOG.debug(logMessagePrefix + " metadata write lock released");
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
        LOG.debug(logMessagePrefix + " response returned to the caller.");
        LOG.debug(logMessagePrefix + " call END");
    }

    private void notifyPingRecievedThread() {
        synchronized (pingReceived) {
            pingReceived.set(true);
            pingReceived.notify();
        }
    }

}
