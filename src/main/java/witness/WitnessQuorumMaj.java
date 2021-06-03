package witness;

import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import witness.register.Witness;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


//TODO: Make QuorumServer a more generic interface, which can be implemented in both witness and in a normal zookeeper server..
//TODO: Modify the QuorumVerifier to return this generic interface instead of QuroumPeer
public class WitnessQuorumMaj{

    private Map<Long, Witness.QuorumServer> allMembers = new HashMap<Long, Witness.QuorumServer>();
    private Map<Long, Witness.QuorumServer> votingMembers = new HashMap<Long, Witness.QuorumServer>(); //TODO: Priyatham: Witness should also be a voting member..
    private Map<Long, Witness.QuorumServer> observingMembers = new HashMap<Long, Witness.QuorumServer>();
    private long version = 0;
    private int half;

    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    public boolean equals(Object o) {
        if (!(o instanceof WitnessQuorumMaj)) {
            return false;
        }
        WitnessQuorumMaj qm = (WitnessQuorumMaj) o;
        if (qm.getVersion() == version) {
            return true;
        }
        if (allMembers.size() != qm.getAllMembers().size()) {
            return false;
        }
        for (Witness.QuorumServer qs : allMembers.values()) {
            Witness.QuorumServer qso = qm.getAllMembers().get(qs.id);
            if (qso == null || !qs.equals(qso)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Defines a majority to avoid computing it every time.
     *
     */
    public WitnessQuorumMaj(Map<Long, Witness.QuorumServer > allMembers) {
        this.allMembers = allMembers;
        for (Witness.QuorumServer qs : allMembers.values()) {
            if (qs.type == QuorumPeer.LearnerType.PARTICIPANT || qs.type == QuorumPeer.LearnerType.WITNESS) {
                votingMembers.put(Long.valueOf(qs.id), qs);
            } else {
                observingMembers.put(Long.valueOf(qs.id), qs);
            }
        }
        half = votingMembers.size() / 2;
    }

    public WitnessQuorumMaj(Properties props) throws QuorumPeerConfig.ConfigException {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            if (key.startsWith("server.")) {
                int dot = key.indexOf('.');
                long sid = Long.parseLong(key.substring(dot + 1));
                Witness.QuorumServer qs = new Witness.QuorumServer(sid, value);
                allMembers.put(Long.valueOf(sid), qs);
                //From my(witness's) perspective, I am a voting member without any restrictions.
                if (qs.type == QuorumPeer.LearnerType.PARTICIPANT || qs.type == QuorumPeer.LearnerType.WITNESS) {
                    votingMembers.put(Long.valueOf(sid), qs);
                } else {
                    observingMembers.put(Long.valueOf(sid), qs);
                }
            } else if (key.equals("version")) {
                version = Long.parseLong(value, 16);
            }
        }
        half = votingMembers.size() / 2;
    }

    /**
     * Returns weight of 1 by default.
     *
     * @param id
     */
    public long getWeight(long id) {
        return 1;
    }

    public String toString() {
        StringBuilder sw = new StringBuilder();

        for (Witness.QuorumServer member : getAllMembers().values()) {
            String key = "server." + member.id;
            String value = member.toString();
            sw.append(key);
            sw.append('=');
            sw.append(value);
            sw.append('\n');
        }
        String hexVersion = Long.toHexString(version);
        sw.append("version=");
        sw.append(hexVersion);
        return sw.toString();
    }

    //TODO: Priyatham: This can be modified to use witness's vote incase a Natural Quorum cannot be formed,
    /**
     * if(does not contain natural quorum && (witnessActive and witnessAcked)) {
     *     return ackset.size() + 1 > half.
     * }
     * */
    /**
     * Verifies if a set is a majority. Assumes that ackSet contains acks only
     * from votingMembers
     */

    public boolean containsQuorum(Set<Long> ackSet) {
        return (ackSet.size() > half);
    }

    public Map<Long, Witness.QuorumServer> getAllMembers() {
        return allMembers;
    }

    public Map<Long, Witness.QuorumServer> getVotingMembers() {
        return votingMembers;
    }

    public Map<Long, Witness.QuorumServer> getObservingMembers() {
        return observingMembers;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long ver) {
        version = ver;
    }


}
