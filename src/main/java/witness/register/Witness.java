package witness.register;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.common.AtomicFileWritingIdiom;
//import org.apache.zookeeper.server.quorum.QuorumPeer;


import org.apache.zookeeper.common.QuorumX509Util;
import org.apache.zookeeper.server.ServerMetrics;
import org.apache.zookeeper.server.ZooKeeperThread;
import org.apache.zookeeper.server.quorum.*;
import org.apache.zookeeper.server.quorum.auth.*;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;
import org.apache.zookeeper.server.quorum.witness.generated.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import witness.MultipleAddresses;
import witness.WitnessConfigLoader;
import witness.WitnessQuorumMaj;
import witness.QuorumCnxManager;
import witness.FastLeaderElection;

import javax.security.sasl.SaslException;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Witness extends ZooKeeperThread {
    private static final Logger LOG = LoggerFactory.getLogger(Witness.class);

    public LocalPeerBean jmxLocalPeerBean;
    public LeaderElectionBean jmxLeaderElectionBean;

    //In memory copies of metadata
    WitnessData witnessData;
    //Metadata just holds witnessData.metadataByteArray in an object form to be used in Leader Election
    WitnessMetadata metadata;
    String dataDir;
    String dataFileName; //This file will contain the WitnessData.
    private String configFilename = null;

    //read-write lock to guard the witnessData.
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    final public Object QV_LOCK = new Object();

    /*public enum WitnessState {
        LOOKING,
        FOLLOWING,
        OBSERVING //TODO: From the description of Observers, It doesn't make sense to use Wintess as an observer, because observers are primarily configured
        // to reduce load on quorum participant servers but still server client requests . But keeping it for now
        ////TODO: But if you have more multiple witness nodes configured, but at a time, you configure only one of them to be actually participate as witness,
        //but others will just be observing...so when one witness fails..may be the leader can activate another witness..but...a leader cant know for sure that
        //the current witness has failed..Becuase leader could be the one, that got partitioned. So, if two witness nodes are in FOLLOWING state at the same time, then,
        //we could run into split brain sort of situation.
    }*/

    //TODO: This should actually go into QuorumPeer code..
    //A witness can only be Witness or Observer (this also dose not make sense )
    /*public enum  LearnerType {
        WITNESS, // Witness in a way subsumes PARTICIPANT type, but it has bunch of restrictions.
        PARTICIPANT,
        OBSERVER
    }*/
    public QuorumPeer.LearnerType learnerType = QuorumPeer.LearnerType.WITNESS;
    long myid;
    private QuorumPeer.ServerState state = QuorumPeer.ServerState.LOOKING;
    private volatile boolean running = true;
    private boolean reconfigFlag = false; // indicates that a reconfig just committed

    /**
     * The number of milliseconds of each tick
     */
    protected int tickTime;

    /**
     * The number of ticks that can pass between receiving a message from the leader.
     * The pingTimeout/Heartbeat thread will rely on this to determine whether to transition to LOOKING state or continue
     * in FOLLOWING state
     */
    protected volatile int pingLimit;
    protected volatile int initLimit;


    private boolean sslQuorum;
    private boolean shouldUsePortUnification;

    /**
     * Whether or not to listen on all IPs for the two quorum ports
     * (broadcast and fast leader election).
     */
    protected boolean quorumListenOnAllIPs = false;

    private final QuorumX509Util x509Util;

    private boolean multiAddressEnabled = true;

    private boolean multiAddressReachabilityCheckEnabled = true;

    private final boolean reconfigEnabled;

    //TODO: Directly use the MultiAddress class available in the Zookeeper server..
    private int multiAddressReachabilityCheckTimeoutMs = (int) MultipleAddresses.DEFAULT_TIMEOUT.toMillis();

    /**
     * Enable/Disables quorum authentication using sasl. Defaulting to false.
     */
    protected boolean quorumSaslEnableAuth;

    private static final int QUORUM_CNXN_THREADS_SIZE_DEFAULT_VALUE = 20;
    /**
     * The maximum number of threads to allow in the connectionExecutors thread
     * pool which will be used to initiate quorum server connections.
     */
    protected int quorumCnxnThreadsSize = QUORUM_CNXN_THREADS_SIZE_DEFAULT_VALUE;

    public static final String QUORUM_CNXN_TIMEOUT_MS = "zookeeper.quorumCnxnTimeoutMs";
    private static int quorumCnxnTimeoutMs;

    static {
        quorumCnxnTimeoutMs = Integer.getInteger(QUORUM_CNXN_TIMEOUT_MS, -1);
        LOG.info("{}={}", QUORUM_CNXN_TIMEOUT_MS, quorumCnxnTimeoutMs);
    }

    private int electionType = 3;
    Election electionAlg;
    //TODO: Make this field default accessor type once you move this class to the same package..
    public boolean shuttingDownLE = false;

    /*
     * Record leader election time
     */
    public long start_fle, end_fle; // fle = fast leader election
    public static final String FLE_TIME_UNIT = "MS";

    //TODO: In init
    PingTimeoutThread pingTimeoutThread;
    WitnessService witnessService;
    Server grpcServer;

    /**
     * Witness version of he quorum verifier..For now directly initialized to Majority Quorum Verfier..
     * We can later change to Witness's version Generic Quorum Verifier interface that will be implemented by both
     * Hierachical and Majority Quorums.
     * */
    WitnessQuorumMaj quorumVerifier;
    WitnessQuorumMaj lastSeenQuorumVerifier;

    QuorumAuthServer authServer;
    QuorumAuthLearner authLearner;

    // The QuorumCnxManager is held through an AtomicReference to ensure cross-thread visibility
    // of updates; see the implementation comment at setLastSeenQuorumVerifier().
    private AtomicReference<QuorumCnxManager> qcmRef = new AtomicReference<>();

    /**
     * This is who I think the leader currently is.
     */
    private volatile Vote currentVote;

    /*
     * To enable observers to have no identifier, we need a generic identifier
     * at least for QuorumCnxManager. We use the following constant to as the
     * value of such a generic identifier.
     */

    public static final long OBSERVER_ID = Long.MAX_VALUE;

    public static final class AddressTuple {

        public final MultipleAddresses electionAddr;
        public final String[] grpcAddress;

        public AddressTuple(MultipleAddresses electionAddr, String[] grpcAddress) {
            this.electionAddr = electionAddr;
            this.grpcAddress = grpcAddress;
        }

    }

    public static class QuorumServer {

        public MultipleAddresses electionAddr = new MultipleAddresses();

        public MultipleAddresses addr = new MultipleAddresses();

        //public MultipleAddresses grpcAddr = new MultipleAddresses();

        public String[] grpcAddress;

        public long id;

        public String hostname;

        public QuorumPeer.LearnerType type = QuorumPeer.LearnerType.PARTICIPANT;

        private List<InetSocketAddress> myAddrs;

        // VisibleForTesting
        //public QuorumServer(long id, InetSocketAddress addr) {
          //  this(id, addr, null, null, QuorumPeer.LearnerType.PARTICIPANT);
        //}

        public long getId() {
            return id;
        }

        public String[] getGrpcPort() {
            return grpcAddress;
        }
        /**
         * Performs a DNS lookup for server address and election address.
         *
         * If the DNS lookup fails, this.addr and electionAddr remain
         * unmodified.
         */
        public void recreateSocketAddresses() {
            if (this.electionAddr.isEmpty()) {
                LOG.warn("Election address has not been initialized");
                return;
            }
            this.electionAddr.recreateSocketAddresses();
        }

        private QuorumPeer.LearnerType getType(String s) throws QuorumPeerConfig.ConfigException {
            switch (s.trim().toLowerCase()) {
                case "observer":
                    return QuorumPeer.LearnerType.OBSERVER;
                case "participant":
                    return QuorumPeer.LearnerType.PARTICIPANT;
                case "witness":
                    return QuorumPeer.LearnerType.WITNESS;
                default:
                    throw new QuorumPeerConfig.ConfigException("Unrecognised peertype: " + s);
            }
        }

        private static final String wrongFormat =
                " does not have the form server_config or server_config;client_config"
                        + " where server_config is the pipe separated list of host:port:port or host:port:port:type"
                        + " and client_config is port or host:port";


        /*public QuorumServer(long sid, String addressStr) throws QuorumPeerConfig.ConfigException {
            this.id = sid;
            QuorumPeer.LearnerType newType = null;
            String[] serverAddresses = addressStr.split("\\|");

            boolean multiAddressEnabled = false;//Boolean.parseBoolean(
                    //System.getProperty(QuorumPeer.CONFIG_KEY_MULTI_ADDRESS_ENABLED, QuorumPeer.CONFIG_DEFAULT_MULTI_ADDRESS_ENABLED));
            if (!multiAddressEnabled && serverAddresses.length > 1) {
                throw new QuorumPeerConfig.ConfigException("Multiple address feature is disabled, but multiple addresses were specified for sid " + sid);
            }
            //for a normal server server.x=<hostname>:<electionPort>:[type-optional]
            //for a witness server server.w = <hostname>:<grpcPort>:<electionPort>:[type-optional]
            //TODO: For normal server keep the old config itself which is server.x=<hostname>:<normalPort>:<electionPort>:[type-optional]
            for (String serverAddress : serverAddresses) {
                String serverParts[] = getHostAndPort(serverAddress);
                if ((serverParts.length < 3)
                        || (serverParts.length > 4)) {
                    throw new QuorumPeerConfig.ConfigException(addressStr + wrongFormat);
                }

                //It must be a witness....only witness will have 4 parts..in the witnessConfigFile..
                //for a witness server server.w = <hostname>:<grpcPort>:<electionPort>:[type-mandatory]
                if(serverParts.length == 4) {
                    QuorumPeer.LearnerType tempType = getType(serverParts[3]);
                    if (newType == null) {
                        newType = tempType;
                    }
                    if(tempType != QuorumPeer.LearnerType.WITNESS) {
                        throw new QuorumPeerConfig.ConfigException("In the Witness's config file, only the witness server should have 4 server parts");
                    }
                    if (newType != tempType) {
                        throw new QuorumPeerConfig.ConfigException("Multiple addresses should have similar roles: " + type + " vs " + tempType);
                    }
                    grpcAddress = new String[]{serverParts[0], serverParts[1]};
                    addElectionAddr(serverParts[0], serverParts[2]);
                }
                else {
                    // server_config should be either host:electionPort or host:electionPort:type
                    addElectionAddr(serverParts[0], serverParts[1]);

                    if (serverParts.length == 3) {
                        QuorumPeer.LearnerType tempType = getType(serverParts[2]);
                        if (newType == null) {
                            newType = tempType;
                        }

                        if (newType != tempType) {
                            throw new QuorumPeerConfig.ConfigException("Multiple addresses should have similar roles: " + type + " vs " + tempType);
                        }
                    }
                }
                this.hostname = serverParts[0];
            }

            if (newType != null) {
                type = newType;
            }

            setMyAddrs();
        }
        */

        public QuorumServer(long sid, String addressStr) throws QuorumPeerConfig.ConfigException {
            this.id = sid;
            QuorumPeer.LearnerType newType = null;
            String[] serverAddresses = addressStr.split("\\|");

            //For now disabling multiple addresses
            boolean multiAddressEnabled = false;//Boolean.parseBoolean(
            //System.getProperty(QuorumPeer.CONFIG_KEY_MULTI_ADDRESS_ENABLED, QuorumPeer.CONFIG_DEFAULT_MULTI_ADDRESS_ENABLED));
            if (!multiAddressEnabled && serverAddresses.length > 1) {
                throw new QuorumPeerConfig.ConfigException("Multiple address feature is disabled, but multiple addresses were specified for sid " + sid);
            }

            //for a witness server server.w = <hostname>:<grpcPort>:<electionPort>:[type-optional]
            //for normal server keep the old config itself which is server.x=<hostname>:<normalPort>:<electionPort>:[type-optional]
            for (String serverAddress : serverAddresses) {
                String serverParts[] = getHostAndPort(serverAddress);
                if ((serverParts.length < 3)
                        || (serverParts.length > 4)) {
                    throw new QuorumPeerConfig.ConfigException(addressStr + wrongFormat);
                }

                if (serverParts.length == 4) {
                    QuorumPeer.LearnerType tempType = getType(serverParts[3]);
                    if (newType == null) {
                        newType = tempType;
                    }

                    if (newType != tempType) {
                        throw new QuorumPeerConfig.ConfigException("Multiple addresses should have similar roles: " + type + " vs " + tempType);
                    }
                }

                // server_config should be either host:port:port or host:port:port:type
                InetSocketAddress tempAddress;
                InetSocketAddress tempElectionAddress;
                try {
                    tempAddress = new InetSocketAddress(serverParts[0], Integer.parseInt(serverParts[1]));
                    if(isWitness(newType)) {
                        LOG.debug("Witness GrpcAddress Resolved: " + tempAddress.getHostName() + tempAddress.getPort());
                        grpcAddress = new String[] {serverParts[0], serverParts[1]};
                    }
                    else {
                        addr.addAddress(tempAddress);
                    }
                } catch (NumberFormatException e) {
                    throw new QuorumPeerConfig.ConfigException("Address unresolved: " + serverParts[0] + ":" + serverParts[1]);
                }
                try {
                    tempElectionAddress = new InetSocketAddress(serverParts[0], Integer.parseInt(serverParts[2]));
                    electionAddr.addAddress(tempElectionAddress);
                } catch (NumberFormatException e) {
                    throw new QuorumPeerConfig.ConfigException("Address unresolved: " + serverParts[0] + ":" + serverParts[2]);
                }

                if (tempAddress.getPort() == tempElectionAddress.getPort()) {
                    if(isWitness(newType)) {
                        LOG.debug("Witness GrpcAddress and Election address are the same");
                        throw new QuorumPeerConfig.ConfigException("Witness grpc service and election port must be different! Please update the "
                                + "configuration file on server." + sid);
                    } else {
                        throw new QuorumPeerConfig.ConfigException("Client and election port must be different! Please update the "
                                + "configuration file on server." + sid);
                    }
                }

                this.hostname = serverParts[0];
            }

            if (newType != null) {
                type = newType;
            }

            setMyAddrs();
        }

        private boolean isWitness(QuorumPeer.LearnerType type) {
            if(type != null && type == QuorumPeer.LearnerType.WITNESS) {
                return  true;
            }
            return  false;
        }

        public void addElectionAddr(String host, String port) throws QuorumPeerConfig.ConfigException {
            InetSocketAddress tempElectionAddress;
            try {
                tempElectionAddress = new InetSocketAddress(host, Integer.parseInt(port));
                electionAddr.addAddress(tempElectionAddress);
            } catch (NumberFormatException e) {
                throw new QuorumPeerConfig.ConfigException("Address unresolved: " + host + ":" + port);
            }
        }

        public QuorumServer(long id, InetSocketAddress electionAddr, QuorumPeer.LearnerType type) {
            this.id = id;
            if (electionAddr != null) {
                this.electionAddr.addAddress(electionAddr);
            }
            this.type = type;
            setMyAddrs();
        }

        private void setMyAddrs() {
            this.myAddrs = new ArrayList<>();
            this.myAddrs.addAll(this.electionAddr.getAllAddresses());
            this.myAddrs = excludedSpecialAddresses(this.myAddrs);
        }

        public static String delimitedHostString(InetSocketAddress addr) {
            String host = addr.getHostString();
            if (host.contains(":")) {
                return "[" + host + "]";
            } else {
                return host;
            }
        }

        public String toString() {
            StringWriter sw = new StringWriter();
            List<InetSocketAddress> electionAddrList = new LinkedList<>(electionAddr.getAllAddresses());
            List<InetSocketAddress> addrList = new LinkedList<>(addr.getAllAddresses());

            //TODO: THis is just a quick hack to make things work. I currently assume that multiple addresses are not configured
            // for witnesses..
            if(type == QuorumPeer.LearnerType.WITNESS) {
                sw.append(String.format("%s:%d:%d", delimitedHostString(electionAddrList.get(0)), Integer.parseInt(grpcAddress[1]), electionAddrList.get(0).getPort()));
                sw.append(":witness");
            }
            else {
                if (addrList.size() > 0 && electionAddrList.size() > 0) {
                    electionAddrList.sort(Comparator.comparing(InetSocketAddress::getHostString));
                    sw.append(IntStream.range(0, electionAddrList.size()).mapToObj(i -> String.format("%s:%d:%d",
                            delimitedHostString(electionAddrList.get(i)), addrList.get(i).getPort(), electionAddrList.get(i).getPort()))
                            .collect(Collectors.joining("|")));
                }

                if (type == QuorumPeer.LearnerType.OBSERVER) {
                    sw.append(":observer");
                } else if (type == QuorumPeer.LearnerType.PARTICIPANT) {
                    sw.append(":participant");
                }
            }
            return sw.toString();
        }

        public int hashCode() {
            assert false : "hashCode not designed";
            return 42; // any arbitrary constant will do
        }

        private boolean checkAddressesEqual(InetSocketAddress addr1, InetSocketAddress addr2) {
            return (addr1 != null || addr2 == null)
                    && (addr1 == null || addr2 != null)
                    && (addr1 == null || addr2 == null || addr1.equals(addr2));
        }

        public boolean equals(Object o) {
            if (!(o instanceof Witness.QuorumServer)) {
                return false;
            }
            Witness.QuorumServer qs = (Witness.QuorumServer) o;
            if ((qs.id != id) || (qs.type != type)) {
                return false;
            }

            if (!electionAddr.equals(qs.electionAddr)) {
                return false;
            }
            return  true;
        }

        public void checkAddressDuplicate(Witness.QuorumServer s) throws KeeperException.BadArgumentsException {
            List<InetSocketAddress> otherAddrs = new ArrayList<>();
            otherAddrs.addAll(s.electionAddr.getAllAddresses());
            otherAddrs = excludedSpecialAddresses(otherAddrs);

            for (InetSocketAddress my : this.myAddrs) {

                for (InetSocketAddress other : otherAddrs) {
                    if (my.equals(other)) {
                        String error = String.format("%s of server.%d conflicts %s of server.%d", my, this.id, other, s.id);
                        throw new KeeperException.BadArgumentsException(error);
                    }
                }
            }
        }

        private List<InetSocketAddress> excludedSpecialAddresses(List<InetSocketAddress> addrs) {
            List<InetSocketAddress> included = new ArrayList<>();

            for (InetSocketAddress addr : addrs) {
                if (addr == null) {
                    continue;
                }
                InetAddress inetaddr = addr.getAddress();

                if (inetaddr == null || inetaddr.isAnyLocalAddress() || // wildCard addresses (0.0.0.0 or [::])
                        inetaddr.isLoopbackAddress()) { // loopback address(localhost/127.0.0.1)
                    continue;
                }
                included.add(addr);
            }
            return included;
        }

    }

    public QuorumPeer.ServerState getWitnessState() {
        return state;
    }

    public void setWitnessState(QuorumPeer.ServerState newState) {
        state = newState;
        if (newState == QuorumPeer.ServerState.LOOKING) {
            //setLeaderIdAndAddress(-1, null);
            //TODO: Determine if witness can know who its leader is..i.e atleast the leader nodID...to prevent itself from
            //serving requests to fruadulent readers or writers causing information leak.
        } else {
            //LOG.info("Peer state changed: {}", getDetailedPeerState());
        }
    }

    public Witness() throws SaslException {
        super("Witness");
        x509Util = createX509Util();
        //initialize();
        reconfigEnabled = WitnessConfigLoader.isReconfigEnabled();
        pingTimeoutThread = new PingTimeoutThread(this);
        witnessService = new WitnessService(this);
    }

    public void initialize() throws SaslException {
        // init quorum auth server & learner
        if (isQuorumSaslAuthEnabled()) {
            //TODO: For a witness, Quorum Sasl Auth is disabled for now
            /*
            Set<String> authzHosts = new HashSet<String>();
            for (Witness.QuorumServer qs : getView().values()) {
                authzHosts.add(qs.hostname);
            }
            authServer = new SaslQuorumAuthServer(isQuorumServerSaslAuthRequired(), quorumServerLoginContext, authzHosts);
            authLearner = new SaslQuorumAuthLearner(isQuorumLearnerSaslAuthRequired(), quorumServicePrincipal, quorumLearnerLoginContext);
             */
        } else {
            authServer = new NullQuorumAuthServer();
            authLearner = new NullQuorumAuthLearner();
        }
    }

    @Override
    public synchronized void start() {
        if (!getView().containsKey(myid)) {
            throw new RuntimeException("My id " + myid + " not in the peer list");
        }
        loadWitnessData();
        LOG.info("Loaded Witness's metadata: zxid = " + metadata.getZxid() + ", acceptedEpoch = " + metadata.getAcceptedEpoch() + " currentEpoch = " + metadata.getCurrentEpoch());
        /*
        startServerCnxnFactory();
        try {
            adminServer.start();
        } catch (AdminServer.AdminServerException e) {
            LOG.warn("Problem starting AdminServer", e);
            System.out.println(e);
        }*/
        grpcServer = ServerBuilder.forPort(Integer.parseInt(getGrpcAddress()[1])).addService(witnessService).build();
        try {
            grpcServer.start();
            LOG.info("Started grpcServer on port " + getGrpcAddress()[1]);
        } catch (IOException e) {
            //e.printStackTrace();
            throw new RuntimeException("Unable to start the witness grpc server", e);
        }
        startLeaderElection();
        //startJvmPauseMonitor();
        super.start();
    }



    @Override
    public void run() {
        updateThreadName();

        LOG.debug("Witness Started");
        //NOTE: Priyatham: Ignoring JMX beans for now
        //try {
            //jmxQuorumBean = new QuorumBean(this);
            //MBeanRegistry.getInstance().register(jmxQuorumBean, null);
            /*
            for (Witness.QuorumServer s : getView().values()) {
                ZKMBeanInfo p;
                if (getId() == s.id) {
                    p = jmxLocalPeerBean = new LocalPeerBean(this);
                    try {
                        MBeanRegistry.getInstance().register(p, jmxQuorumBean);
                    } catch (Exception e) {
                        LOG.warn("Failed to register with JMX", e);
                        jmxLocalPeerBean = null;
                    }
                } else {
                    RemotePeerBean rBean = new RemotePeerBean(this, s);
                    try {
                        MBeanRegistry.getInstance().register(rBean, jmxQuorumBean);
                        jmxRemotePeerBean.put(s.id, rBean);
                    } catch (Exception e) {
                        LOG.warn("Failed to register with JMX", e);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to register with JMX", e);
            jmxQuorumBean = null;
        }
            */
        try {
            /*
             * Main loop
             */
            while (running) {
                switch (getWitnessState()) {
                    case LOOKING:
                        LOG.info("LOOKING");
                        ServerMetrics.getMetrics().LOOKING_COUNT.add(1);
                        try {
                            reconfigFlagClear();
                            if (shuttingDownLE) {
                                shuttingDownLE = false;
                                startLeaderElection();
                            }
                            setCurrentVote(electionAlg.lookForLeader());
                        } catch (Exception e) {
                            LOG.warn("Unexpected exception", e);
                            setWitnessState(QuorumPeer.ServerState.LOOKING);
                        }
                        break;
                    /*case OBSERVING:
                        try {
                            LOG.info("OBSERVING");
                            setObserver(makeObserver(logFactory));
                            observer.observeLeader();
                        } catch (Exception e) {
                            LOG.warn("Unexpected exception", e);
                        } finally {
                            observer.shutdown();
                            setObserver(null);
                            updateServerState();

                            // Add delay jitter before we switch to LOOKING
                            // state to reduce the load of ObserverMaster
                            if (isRunning()) {
                                Observer.waitForObserverElectionDelay();
                            }
                        }
                        break;*/
                    case FOLLOWING:
                        try {
                            LOG.info("FOLLOWING");
                            /**
                             * 1. My State is already set to following.
                             * //GRPC server will always be running, no need     to do anything there.
                             * 2. Start ping timeout thread.
                             * 3. Join..
                             * */
                            Thread pingTimeoutThreadObj = new Thread(pingTimeoutThread);
                            pingTimeoutThreadObj.start();
                            pingTimeoutThreadObj.join();
                        } catch (Exception e) {
                            LOG.warn("Unexpected exception", e);
                            updateServerState();
                        }
                        break;
                }
            }
        } finally {
            LOG.warn("QuorumPeer main thread exited");
            /*
            MBeanRegistry instance = MBeanRegistry.getInstance();
            instance.unregister(jmxQuorumBean);
            instance.unregister(jmxLocalPeerBean);

            for (RemotePeerBean remotePeerBean : jmxRemotePeerBean.values()) {
                instance.unregister(remotePeerBean);
            }

            jmxQuorumBean = null;
            jmxLocalPeerBean = null;
            jmxRemotePeerBean = null;

             */
        }
    }

    public void shutdown() {
        running = false;
        x509Util.close();

        /*
        if (jvmPauseMonitor != null) {
            jvmPauseMonitor.serviceStop();
        }
        */

        if (getElectionAlg() != null) {
            this.interrupt();
            getElectionAlg().shutdown();
        }
        grpcServer.shutdown();
        pingTimeoutThread.shutdown();
    }

    public synchronized void startLeaderElection() {
        if (getWitnessState() == QuorumPeer.ServerState.LOOKING) {
            //A witness's initial vote would be empty, when startng the election, it
            //does not vote for itself.
            currentVote = new Vote(-1, -1, -1);
        }
        this.electionAlg = createElectionAlgorithm(3);
    }

    protected Election createElectionAlgorithm(int electionAlgorithm) {
        Election le = null;

        //TODO: use a factory rather than a switch
        switch (electionAlgorithm) {
            case 1:
                throw new UnsupportedOperationException("Election Algorithm 1 is not supported.");
            case 2:
                throw new UnsupportedOperationException("Election Algorithm 2 is not supported.");
            case 3:
                QuorumCnxManager qcm = createCnxnManager();
                QuorumCnxManager oldQcm = qcmRef.getAndSet(qcm);
                if (oldQcm != null) {
                    LOG.warn("Clobbering already-set QuorumCnxManager (restarting leader election?)");
                    oldQcm.halt();
                }
                QuorumCnxManager.Listener listener = qcm.listener;
                if (listener != null) {
                    listener.start();
                    FastLeaderElection fle = new FastLeaderElection(this, qcm);
                    fle.start();
                    le = fle;
                } else {
                    LOG.error("Null listener when initializing cnx manager");
                }
                break;
            default:
                assert false;
        }
        return le;
    }

    /**
     * NOTE:
     *  server.1=zoo1-net1:2888:3888
     *  The first port will be used for exposing the GRPC Service.., and the second is for leader election.
     *  If you want to test multiple servers on a single machine, then different ports can be used for each server.
     * */

    /**
     * When the witness is loaded for the first time, the dataFile will not be present.
     * When the dataFile is absent I create a metadata with reasonable default values (like how we do it in zookeeper code)
     * and write it to the file.
     * Default Values:
     *  version = 0,
     *  zxid = 0,
     *  currentEpoch = 0,
     *  acceptedEpoch = 0.
     * **/
    public void loadWitnessData() {
        try {
            File dataFile = new File(dataDir, dataFileName);
            if (dataFile.exists()) {
                //load the data from the file.
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile));
                    witnessData = (WitnessData) ois.readObject();
                    metadata = createMetadata(witnessData.getMetadataByteArray());
                } catch (Exception e) {
                    //Ignore: if this is a fileNotFound exception This will not happen because we know that the file exists
                    LOG.error("Unable to load WitnessData from disk", e);
                    throw e;
                }
            } else {
                //create Metadata and WitnessData Objects with default values
                metadata = new WitnessMetadata(0, 0, 0);
                try {
                    byte[] metaDataByteArray = convertToByteArray(metadata);
                    witnessData = new WitnessData(0, metaDataByteArray);
                    //write witness data to file.
                    writeWitnessDataToFile(witnessData);
                }
                catch (IOException ie) {
                    LOG.error("Failed to initialize witness with default data", ie);
                    throw  ie;
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to run witness server ", e);
        }
    }

    public WitnessQuorumMaj setQuorumVerifier(WitnessQuorumMaj qv, boolean writeToDisk) {
        synchronized (QV_LOCK) {
            if ((quorumVerifier != null) && (quorumVerifier.getVersion() >= qv.getVersion())) {
                // this is normal. For example - server found out about new config through FastLeaderElection gossiping
                // and then got the same config in UPTODATE message so its already known
                LOG.debug(
                        "{} setQuorumVerifier called with known or old config {}. Current version: {}",
                        getId(),
                        qv.getVersion(),
                        quorumVerifier.getVersion());
                return quorumVerifier;
            }
            WitnessQuorumMaj prevQV = quorumVerifier;
            quorumVerifier = qv;
            if (lastSeenQuorumVerifier == null || (qv.getVersion() > lastSeenQuorumVerifier.getVersion())) {
                lastSeenQuorumVerifier = qv;
            }

            /*
            if (writeToDisk) {
                // some tests initialize QuorumPeer without a static config file
                if (configFilename != null) {
                    try {
                        String dynamicConfigFilename = makeDynamicConfigFilename(qv.getVersion());
                        QuorumPeerConfig.writeDynamicConfig(dynamicConfigFilename, qv, false);
                        QuorumPeerConfig.editStaticConfig(configFilename, dynamicConfigFilename, needEraseClientInfoFromStaticConfig());
                    } catch (IOException e) {
                        LOG.error("Error closing file", e);
                    }
                } else {
                    LOG.info("writeToDisk == true but configFilename == null");
                }
            }
            */

            /*if (qv.getVersion() == lastSeenQuorumVerifier.getVersion()) {
                QuorumPeerConfig.deleteFile(getNextDynamicConfigFilename());
            }*/
            Witness.QuorumServer qs = qv.getAllMembers().get(getId());
            if (qs != null) {
                setAddrs(qs.electionAddr, qs.grpcAddress);
            }
            //updateObserverMasterList();
            return prevQV;
        }
    }

    public void setLastSeenQuorumVerifier(WitnessQuorumMaj qv, boolean writeToDisk) {
        if (!isReconfigEnabled()) {
            LOG.info("Dynamic reconfig is disabled, we don't store the last seen config.");
            return;
        }

        // If qcm is non-null, we may call qcm.connectOne(), which will take the lock on qcm
        // and then take QV_LOCK.  Take the locks in the same order to ensure that we don't
        // deadlock against other callers of connectOne().  If qcmRef gets set in another
        // thread while we're inside the synchronized block, that does no harm; if we didn't
        // take a lock on qcm (because it was null when we sampled it), we won't call
        // connectOne() on it.  (Use of an AtomicReference is enough to guarantee visibility
        // of updates that provably happen in another thread before entering this method.)
        QuorumCnxManager qcm = qcmRef.get();
        Object outerLockObject = (qcm != null) ? qcm : QV_LOCK;
        synchronized (outerLockObject) {
            synchronized (QV_LOCK) {
                if (lastSeenQuorumVerifier != null && lastSeenQuorumVerifier.getVersion() > qv.getVersion()) {
                    LOG.error("setLastSeenQuorumVerifier called with stale config "
                            + qv.getVersion()
                            + ". Current version: "
                            + quorumVerifier.getVersion());
                }
                // assuming that a version uniquely identifies a configuration, so if
                // version is the same, nothing to do here.
                if (lastSeenQuorumVerifier != null && lastSeenQuorumVerifier.getVersion() == qv.getVersion()) {
                    return;
                }
                lastSeenQuorumVerifier = qv;
                if (qcm != null) {
                    connectNewPeers(qcm);
                }

                if (writeToDisk) {
                    try {
                        String fileName = getNextDynamicConfigFilename();
                        if (fileName != null) {
                            WitnessConfigLoader.writeDynamicConfig(fileName, qv, true);
                        }
                    } catch (IOException e) {
                        LOG.error("Error writing next dynamic config file to disk", e);
                    }
                }
            }
        }
    }

    // On entry to this method, qcm must be non-null and the locks on both qcm and QV_LOCK
    // must be held.  We don't want quorumVerifier/lastSeenQuorumVerifier to change out from
    // under us, so we have to hold QV_LOCK; and since the call to qcm.connectOne() will take
    // the lock on qcm (and take QV_LOCK again inside that), the caller needs to have taken
    // qcm outside QV_LOCK to avoid a deadlock against other callers of qcm.connectOne().
    private void connectNewPeers(QuorumCnxManager qcm) {
        if (quorumVerifier != null && lastSeenQuorumVerifier != null) {
            Map<Long, Witness.QuorumServer> committedView = quorumVerifier.getAllMembers();
            for (Map.Entry<Long, Witness.QuorumServer> e : lastSeenQuorumVerifier.getAllMembers().entrySet()) {
                if (e.getKey() != getId() && !committedView.containsKey(e.getKey())) {
                    qcm.connectOne(e.getKey());
                }
            }
        }
    }

    public String getNextDynamicConfigFilename() {
        if (configFilename == null) {
            LOG.warn("configFilename is null! This should only happen in tests.");
            return null;
        }
        return configFilename + QuorumPeerConfig.nextDynamicConfigFileSuffix;
    }

    public void writeWitnessData(WriteRequest writeRequest) throws IOException, ClassNotFoundException {
        long version = writeRequest.getVersion();
        byte[] metadataByteArr = writeRequest.getMetadata().toByteArray();

        //create a new WitnessData object and populate it.
        WitnessData tempWitnessData = new WitnessData(version, metadataByteArr);

        //write the new WitnessData object to the fileSystem.
        try {
            writeWitnessDataToFile(tempWitnessData);
        } catch (IOException e) {
            LOG.error("Writing WitnessData to file failed. version = {}", version, e);
            throw e;
        }

        //then update the local copy with new information.
        this.witnessData.setMetadataByteArray(metadataByteArr);
        this.witnessData.setVersion(version);

        //Then finally update the Metadata object with the new data; this will be used by the FastLeader election
        WitnessMetadata tempMetadata =  createMetadata(metadataByteArr);
        this.metadata.acquireWriteLock();
        this.metadata.updateMetadata(tempMetadata);
        this.metadata.releaseWriteLock();
    }

    public WitnessMetadata createMetadata(byte[] metadataByteArray) throws IOException, ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(metadataByteArray));
            return (WitnessMetadata) ois.readObject();
        } catch (Exception e) {
            //TODO: handle execption
            e.printStackTrace();
            throw e;
        }
    }

    public byte[] convertToByteArray(WitnessMetadata metadata) throws IOException {
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

    /**
     * Write the WitnessData object to the target data file..
     * **/
    public void writeWitnessDataToFile(WitnessData toWriteWitnessData) throws IOException {
        File file = new File(dataDir, dataFileName);
        new AtomicFileWritingIdiom(file, new AtomicFileWritingIdiom.OutputStreamStatement() {
            @Override
            public void write(OutputStream outputStream) throws IOException {
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject(toWriteWitnessData);
            }
        });
    }

    public WitnessData getWitnessData() {
        return witnessData;
    }

    public void setWitnessData(WitnessData witnessData) {
        this.witnessData = witnessData;
    }

    public WitnessMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(WitnessMetadata metadata) {
        this.metadata = metadata;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    public void setReadWriteLock(ReadWriteLock readWriteLock) {
        this.readWriteLock = readWriteLock;
    }

    public int getTickTime() {
        return tickTime;
    }

    public void setTickTime(int tickTime) {
        this.tickTime = tickTime;
    }

    public int getPingLimit() {
        return pingLimit;
    }

    public void setPingLimit(int pingLimit) {
        this.pingLimit = pingLimit;
    }

    public int getInitLimit() {
        return initLimit;
    }

    public void setInitLimit(int initLimit) {
        this.initLimit = initLimit;
    }

    public PingTimeoutThread getPingTimeoutThread() {
        return pingTimeoutThread;
    }

    public boolean isFollowing() {
        return false;
    }

    /**
     * Gets host and port by splitting server config
     * with support for IPv6 literals
     * @return String[] first element being the
     *  IP address and the next being the port
     * @param s server config, server:port
     */
    public static String[] getHostAndPort(String s) throws QuorumPeerConfig.ConfigException {
        if (s.startsWith("[")) {
            int i = s.indexOf("]");
            if (i < 0) {
                throw new QuorumPeerConfig.ConfigException(s + " starts with '[' but has no matching ']:'");
            }
            if (i + 2 == s.length()) {
                throw new QuorumPeerConfig.ConfigException(s + " doesn't have a port after colon");
            }
            if (i + 2 < s.length()) {
                String[] sa = s.substring(i + 2).split(":");
                String[] nsa = new String[sa.length + 1];
                nsa[0] = s.substring(1, i);
                System.arraycopy(sa, 0, nsa, 1, sa.length);
                return nsa;
            }
            return new String[]{s.replaceAll("\\[|\\]", "")};
        } else {
            return s.split(":");
        }
    }

    /**
     * get the id of this witness
     */
    public long getId() {
        return myid;
    }

    private final AtomicReference<Witness.AddressTuple> myAddrs = new AtomicReference<>();

    /**
     * Resolves hostname for a given server ID.
     *
     * This method resolves hostname for a given server ID in both quorumVerifer
     * and lastSeenQuorumVerifier. If the server ID matches the local server ID,
     * it also updates myAddrs.
     */
    public void recreateSocketAddresses(long id) {
        WitnessQuorumMaj qv = getQuorumVerifier();
        if (qv != null) {
            Witness.QuorumServer qs = qv.getAllMembers().get(id);
            if (qs != null) {
                qs.recreateSocketAddresses();
                if (id == getId()) {
                    setAddrs(qs.electionAddr, qs.grpcAddress);
                }
            }
        }
        qv = getLastSeenQuorumVerifier();
        if (qv != null) {
            Witness.QuorumServer qs = qv.getAllMembers().get(id);
            if (qs != null) {
                qs.recreateSocketAddresses();
            }
        }
    }

    private Witness.AddressTuple getAddrs() {
        Witness.AddressTuple addrs = myAddrs.get();
        if (addrs != null) {
            return addrs;
        }
        try {
            synchronized (QV_LOCK) {
                addrs = myAddrs.get();
                while (addrs == null) {
                    QV_LOCK.wait();
                    addrs = myAddrs.get();
                }
                return addrs;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public String[] getGrpcAddress() {
        return getAddrs().grpcAddress;
    }

    public MultipleAddresses getElectionAddress() {
        return getAddrs().electionAddr;
    }

    private void setAddrs(MultipleAddresses electionAddr, String[] grpcAddress) {
        synchronized (QV_LOCK) {
            myAddrs.set(new AddressTuple(electionAddr, grpcAddress));
            QV_LOCK.notifyAll();
        }
    }

    /**
     * Return QuorumVerifier object for the last committed configuration.
     */
    public WitnessQuorumMaj getQuorumVerifier() {
        synchronized (QV_LOCK) {
            return quorumVerifier;
        }
    }

    /**
     * Return QuorumVerifier object for the last proposed configuration.
     */
    public WitnessQuorumMaj getLastSeenQuorumVerifier() {
        synchronized (QV_LOCK) {
            return lastSeenQuorumVerifier;
        }
    }

    /**
     * Observers are not contained in this view, only nodes with
     * PeerType=PARTICIPANT.
     */
    public Map<Long, Witness.QuorumServer> getVotingView() {
        return getQuorumVerifier().getVotingMembers();
    }

    /**
     * A 'view' is a node's current opinion of the membership of the entire
     * ensemble.
     */
    public Map<Long, Witness.QuorumServer> getView() {
        return Collections.unmodifiableMap(getQuorumVerifier().getAllMembers());
    }


    public boolean isSslQuorum() {
        return sslQuorum;
    }

    public void setSslQuorum(boolean sslQuorum) {
        if (sslQuorum) {
            LOG.info("Using TLS encrypted quorum communication");
        } else {
            LOG.info("Using insecure (non-TLS) quorum communication");
        }
        this.sslQuorum = sslQuorum;
    }

    public boolean shouldUsePortUnification() {
        return shouldUsePortUnification;
    }

    public void setUsePortUnification(boolean shouldUsePortUnification) {
        LOG.info("Port unification {}", shouldUsePortUnification ? "enabled" : "disabled");
        this.shouldUsePortUnification = shouldUsePortUnification;
    }

    public boolean getQuorumListenOnAllIPs() {
        return quorumListenOnAllIPs;
    }

    public void setQuorumListenOnAllIPs(boolean quorumListenOnAllIPs) {
        this.quorumListenOnAllIPs = quorumListenOnAllIPs;
    }

    QuorumX509Util createX509Util() {
        return new QuorumX509Util();
    }

    public QuorumX509Util getX509Util() {
        return x509Util;
    }

    public boolean isMultiAddressEnabled() {
        return multiAddressEnabled;
    }

    public void setMultiAddressEnabled(boolean multiAddressEnabled) {
        this.multiAddressEnabled = multiAddressEnabled;
        LOG.info("multiAddress.enabled set to {}", multiAddressEnabled);
    }

    public int getMultiAddressReachabilityCheckTimeoutMs() {
        return multiAddressReachabilityCheckTimeoutMs;
    }

    public void setMultiAddressReachabilityCheckTimeoutMs(int multiAddressReachabilityCheckTimeoutMs) {
        this.multiAddressReachabilityCheckTimeoutMs = multiAddressReachabilityCheckTimeoutMs;
        LOG.info("multiAddress.reachabilityCheckTimeoutMs set to {}", multiAddressReachabilityCheckTimeoutMs);
    }

    public boolean isMultiAddressReachabilityCheckEnabled() {
        return multiAddressReachabilityCheckEnabled;
    }

    public void setMultiAddressReachabilityCheckEnabled(boolean multiAddressReachabilityCheckEnabled) {
        this.multiAddressReachabilityCheckEnabled = multiAddressReachabilityCheckEnabled;
        LOG.info("multiAddress.reachabilityCheckEnabled set to {}", multiAddressReachabilityCheckEnabled);
    }

    public boolean isReconfigEnabled() {
        return reconfigEnabled;
    }

    boolean isQuorumSaslAuthEnabled() {
        return quorumSaslEnableAuth;
    }

    void setQuorumSaslEnabled(boolean enableAuth) {
        quorumSaslEnableAuth = enableAuth;
        if (!quorumSaslEnableAuth) {
            LOG.info("QuorumPeer communication is not secured! (SASL auth disabled)");
        } else {
            LOG.info("{} set to {}", QuorumAuth.QUORUM_SASL_AUTH_ENABLED, enableAuth);
        }
    }

    public synchronized Vote getCurrentVote() {
        return currentVote;
    }

    public synchronized void setCurrentVote(Vote v) {
        currentVote = v;
    }

    public QuorumCnxManager createCnxnManager() {
        int timeout = quorumCnxnTimeoutMs > 0 ? quorumCnxnTimeoutMs : this.tickTime * this.pingLimit;
        LOG.info("Using {}ms as the quorum cnxn socket timeout", timeout);
        return new QuorumCnxManager(
                this,
                this.getId(),
                this.getView(),
                this.authServer,
                this.authLearner,
                timeout,
                this.getQuorumListenOnAllIPs(),
                this.quorumCnxnThreadsSize,
                this.isQuorumSaslAuthEnabled());
    }

    public synchronized Set<Long> getCurrentAndNextConfigVoters() {
        Set<Long> voterIds = new HashSet<Long>(getQuorumVerifier().getVotingMembers().keySet());
        if (getLastSeenQuorumVerifier() != null) {
            voterIds.addAll(getLastSeenQuorumVerifier().getVotingMembers().keySet());
        }
        return voterIds;
    }

    public WitnessQuorumMaj configFromString(String s) throws IOException, QuorumPeerConfig.ConfigException {
        Properties props = new Properties();
        props.load(new StringReader(s));
        return WitnessConfigLoader.parseDynamicConfig(props, electionType, false, false);
    }

    /**
     * Get an instance of LeaderElection
     */
    public Election getElectionAlg() {
        return electionAlg;
    }

    public QuorumPeer.LearnerType getLearnerType() {
        return learnerType;
    }

    public boolean processReconfig(WitnessQuorumMaj qv, Long suggestedLeaderId, Long zxid, boolean restartLE) {
        return false;
        /*
        if (!isReconfigEnabled()) {
            LOG.debug("Reconfig feature is disabled, skip reconfig processing.");
            return false;
        }

        InetSocketAddress oldClientAddr = getClientAddress();

        // update last committed quorum verifier, write the new config to disk
        // and restart leader election if config changed.
        QuorumVerifier prevQV = setQuorumVerifier(qv, true);

        // There is no log record for the initial config, thus after syncing
        // with leader
        // /zookeeper/config is empty! it is also possible that last committed
        // config is propagated during leader election
        // without the propagation the corresponding log records.
        // so we should explicitly do this (this is not necessary when we're
        // already a Follower/Observer, only
        // for Learner):
        initConfigInZKDatabase();

        if (prevQV.getVersion() < qv.getVersion() && !prevQV.equals(qv)) {
            Map<Long, QuorumPeer.QuorumServer> newMembers = qv.getAllMembers();
            updateRemotePeerMXBeans(newMembers);
            if (restartLE) {
                restartLeaderElection(prevQV, qv);
            }

            QuorumPeer.QuorumServer myNewQS = newMembers.get(getId());
            if (myNewQS != null && myNewQS.clientAddr != null && !myNewQS.clientAddr.equals(oldClientAddr)) {
                cnxnFactory.reconfigure(myNewQS.clientAddr);
                updateThreadName();
            }

            boolean roleChange = updateLearnerType(qv);
            boolean leaderChange = false;
            if (suggestedLeaderId != null) {
                // zxid should be non-null too
                leaderChange = updateVote(suggestedLeaderId, zxid);
            } else {
                long currentLeaderId = getCurrentVote().getId();
                QuorumPeer.QuorumServer myleaderInCurQV = prevQV.getVotingMembers().get(currentLeaderId);
                QuorumPeer.QuorumServer myleaderInNewQV = qv.getVotingMembers().get(currentLeaderId);
                leaderChange = (myleaderInCurQV == null
                        || myleaderInCurQV.addr == null
                        || myleaderInNewQV == null
                        || !myleaderInCurQV.addr.equals(myleaderInNewQV.addr));
                // we don't have a designated leader - need to go into leader
                // election
                reconfigFlagClear();
            }

            return roleChange || leaderChange;
        }
        return false;
      */
    }

    public synchronized void reconfigFlagSet() {
        reconfigFlag = true;
    }
    public synchronized void reconfigFlagClear() {
        reconfigFlag = false;
    }

    private boolean isRunning() {
        return running;
    }

    private synchronized void updateServerState() {
        if (!reconfigFlag) {
            setWitnessState(QuorumPeer.ServerState.LOOKING);
            LOG.warn("PeerState set to LOOKING");
            return;
        }

        if (getId() == getCurrentVote().getId()) {
            setWitnessState(QuorumPeer.ServerState.LEADING);
            LOG.debug("PeerState set to LEADING");
        } else if (getLearnerType() == QuorumPeer.LearnerType.PARTICIPANT) {
            setWitnessState(QuorumPeer.ServerState.FOLLOWING);
            LOG.debug("PeerState set to FOLLOWING");
        } else if (getLearnerType() == QuorumPeer.LearnerType.OBSERVER) {
            setWitnessState(QuorumPeer.ServerState.OBSERVING);
            LOG.debug("PeerState set to OBSERVER");
        } else { // currently shouldn't happen since there are only 2 learner types
            setWitnessState(QuorumPeer.ServerState.LOOKING);
            LOG.debug("Should not be here");
        }
        reconfigFlag = false;
    }

    private void updateThreadName() {
        /*String plain = cnxnFactory != null
                ? cnxnFactory.getLocalAddress() != null
                ? formatInetAddr(cnxnFactory.getLocalAddress())
                : "disabled"
                : "disabled";
        String secure = secureCnxnFactory != null ? formatInetAddr(secureCnxnFactory.getLocalAddress()) : "disabled";
         */
        setName(String.format("QuorumPeer[myid=%d]", getId()));
    }

    public long getMyid() {
        return myid;
    }

    public void setMyid(long myid) {
        this.myid = myid;
    }

    public int getElectionType() {
        return electionType;
    }

    public void setElectionType(int electionType) {
        this.electionType = electionType;
    }

    public void setLearnerType(QuorumPeer.LearnerType learnerType) {
        this.learnerType = learnerType;
    }



    public synchronized void setConfigFileName(String s) {
        configFilename = s;
    }
}
