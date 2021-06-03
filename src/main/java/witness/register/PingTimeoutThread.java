package witness.register;

import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class PingTimeoutThread implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Witness.class);
    Witness witness;
    AtomicBoolean pingReceived;
    boolean isRunning = true;

    public PingTimeoutThread(Witness witness) {
        this.witness = witness;

        pingReceived = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        isRunning = true;
        boolean transitionToLooking = false;
        LOG.debug("PingTimeoutThread started. transitionToLooking = " + transitionToLooking);
        /*Once you transition to Following,
        * 1. wait for the leader to create its WitnessHandler thread and read your metadata for the first time (equivalent to LEARNER INFO) -
        * 2. wait for the   potential leader to decided on a new epoch (initlimit) +  propose that new epoch (initlimit) - 2* initlimit
        * 3. wait for the potential leader to get quorum of NEWLEADER ACKS and write its last committed log to you (SYNCH) - initlimit
        *
        * So you wait for 4 times on initlimit.
        * Note: I have increased  the initLimit of the witness to 2 ticks above a normal server because, the leader waits until its initLimit has reached to decide if witness's vote has to be used or not
        *  */
        long initLimitInMillisecs = witness.getInitLimit() * witness.getTickTime();
        for(int i=0; i<4 ; i++) {
            if (witness.getWitnessState() == QuorumPeer.ServerState.FOLLOWING && waitForPing(initLimitInMillisecs)) {
                witness.setWitnessState(QuorumPeer.ServerState.LOOKING);
                return;
            }
        }

        //From this point on, you will follow the leader, as long as you recieve pings from the leader at regular intervals of time.
        long pingTimeoutInMillisecs = witness.getPingLimit() * witness.getTickTime();
        while (isRunning && witness.getWitnessState() == QuorumPeer.ServerState.FOLLOWING) {
            transitionToLooking = waitForPing(pingTimeoutInMillisecs);
            if(transitionToLooking) {
                //TODO: Check if you need to increment the currentEpoch here itself..
                //Answer: No. That should happen during the first write operation performed by the Leader
                witness.setWitnessState(QuorumPeer.ServerState.LOOKING);
                break;
            }
        }
        //No need to explicitly stop this thread, this thread exits when the loop breaks..
        isRunning = false;
    }

    public AtomicBoolean getPingReceived() {
        return pingReceived;
    }

    private boolean waitForPing(long waitLimit) {
        synchronized (pingReceived) {
            try {
                pingReceived.wait(waitLimit);
                if(!pingReceived.get()) {
                    //did not recieve ping from the leader within the timeout, transition to Looking
                    LOG.debug("Did not hear from the Leader within the timeout of "+ waitLimit + ". Transitioning to looking");
                    return true;
                }
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting for ping from leader. Exiting");
                isRunning = false;
            } finally {
                //reset the flag
                pingReceived.set(false);
            }
        }
        return false;
    }

    public void shutdown(){
        isRunning = false;
    }
}
