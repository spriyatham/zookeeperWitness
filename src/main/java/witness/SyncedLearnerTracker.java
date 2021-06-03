package witness;

import witness.WitnessQuorumMaj;

import java.util.ArrayList;
import java.util.HashSet;

public class SyncedLearnerTracker {

    protected ArrayList<QuorumVerifierAcksetPair> qvAcksetPairs = new ArrayList<QuorumVerifierAcksetPair>();

    public void addQuorumVerifier(WitnessQuorumMaj qv) {
        qvAcksetPairs.add(new QuorumVerifierAcksetPair(qv, new HashSet<Long>(qv.getVotingMembers().size())));
    }

    public boolean addAck(Long sid) {
        boolean change = false;
        for (QuorumVerifierAcksetPair qvAckset : qvAcksetPairs) {
            if (qvAckset.getQuorumVerifier().getVotingMembers().containsKey(sid)) {
                qvAckset.getAckset().add(sid);
                change = true;
            }
        }
        return change;
    }

    public boolean hasSid(long sid) {
        for (QuorumVerifierAcksetPair qvAckset : qvAcksetPairs) {
            if (!qvAckset.getQuorumVerifier().getVotingMembers().containsKey(sid)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasAllQuorums() {
        for (QuorumVerifierAcksetPair qvAckset : qvAcksetPairs) {
            if (!qvAckset.getQuorumVerifier().containsQuorum(qvAckset.getAckset())) {
                return false;
            }
        }
        return true;
    }

    public String ackSetsToString() {
        StringBuilder sb = new StringBuilder();

        for (QuorumVerifierAcksetPair qvAckset : qvAcksetPairs) {
            sb.append(qvAckset.getAckset().toString()).append(",");
        }

        return sb.substring(0, sb.length() - 1);
    }

    public static class QuorumVerifierAcksetPair {

        private final WitnessQuorumMaj qv;
        private final HashSet<Long> ackset;

        public QuorumVerifierAcksetPair(WitnessQuorumMaj qv, HashSet<Long> ackset) {
            this.qv = qv;
            this.ackset = ackset;
        }

        public WitnessQuorumMaj getQuorumVerifier() {
            return this.qv;
        }

        public HashSet<Long> getAckset() {
            return this.ackset;
        }

    }

}
