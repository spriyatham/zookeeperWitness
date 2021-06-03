package witness.register;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Metadata implements Serializable {
    /**
     * These values are required by the leader election thread.
     * So, whenever, a write operation is performed these values should also be updated.,
     * */
    private AtomicLong acceptedEpoch;
    private AtomicLong currentEpoch;
    private AtomicLong zxid;
    /**
     * Callers have explicitly acquire read and write locks before reading or writing metadata.
     * I am not adding the acquire and release logic in getters and setters, because the calling functions many times get these values together.
     * So they can acquire and release locks at once.
    * */
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public Metadata(long acceptedEpoch, long currentEpoch, long zxid) {
        this.acceptedEpoch = new AtomicLong(acceptedEpoch);
        this.currentEpoch = new AtomicLong(currentEpoch);
        this.zxid = new AtomicLong(zxid);
    }

    /*
     * This will be called when you are updating the in memory copy of metadata once you have successfully persisted it.
     * This will synchronized on the @WitnessData object
     * */
    public void updateMetadata(Metadata newMetadata) {
        setAcceptedEpoch(newMetadata.getAcceptedEpoch());
        setCurrentEpoch(newMetadata.getCurrentEpoch());
        setZxid(newMetadata.getZxid());
    }

    public void updateMetadata(long acceptedEpoch, long currentEpoch, long zxid) {
        setAcceptedEpoch(acceptedEpoch);
        setCurrentEpoch(currentEpoch);
        setZxid(zxid);
    }

    public long getAcceptedEpoch() {
        return acceptedEpoch.get();
    }

    public void setAcceptedEpoch(long acceptedEpoch) {
        this.acceptedEpoch.set(acceptedEpoch);
    }

    public long getCurrentEpoch() {
        return currentEpoch.get();
    }

    public void setCurrentEpoch(long currentEpoch) {
        this.currentEpoch.set(currentEpoch);
    }

    public long getZxid() {
        return zxid.get();
    }

    public void setZxid(long zxid) {
        this.zxid.set(zxid);
    }

    public void acquireWriteLock(){
        readWriteLock.writeLock().lock();
    }

    public void releaseWriteLock(){
        readWriteLock.writeLock().unlock();
    }

    public void acquireReadLock() {
        readWriteLock.readLock().lock();
    }

    public void releaseReadLock() {
        readWriteLock.readLock().unlock();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(acceptedEpoch, metadata.acceptedEpoch) &&
                Objects.equals(currentEpoch, metadata.currentEpoch) &&
                Objects.equals(zxid, metadata.zxid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acceptedEpoch, currentEpoch, zxid);
    }
}
