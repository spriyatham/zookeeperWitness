package witness;

import org.apache.zookeeper.audit.ZKAuditProvider;
import org.apache.zookeeper.jmx.ManagedUtil;
import org.apache.zookeeper.metrics.MetricsProvider;
import org.apache.zookeeper.server.*;
import org.apache.zookeeper.server.admin.AdminServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import witness.register.Witness;

import javax.management.JMException;
import javax.security.sasl.SaslException;
import java.io.IOException;

public class WitnessMain {

    private static final Logger LOG = LoggerFactory.getLogger(WitnessMain.class);

    private static final String USAGE = "Usage: QuorumPeerMain configfile";

    protected Witness witness;

    /**
     * To start the replicated server specify the configuration file name on
     * the command line.
     * @param args path to the configfile
     */
    public static void main(String[] args) {
        WitnessMain main = new WitnessMain();
        try {
            main.initializeAndRun(args);
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid arguments, exiting abnormally", e);
            LOG.info(USAGE);
            System.err.println(USAGE);
            ZKAuditProvider.addServerStartFailureAuditLog();
            ServiceUtils.requestSystemExit(ExitCode.INVALID_INVOCATION.getValue());
        } catch (QuorumPeerConfig.ConfigException e) {
            LOG.error("Invalid config, exiting abnormally", e);
            System.err.println("Invalid config, exiting abnormally");
            ZKAuditProvider.addServerStartFailureAuditLog();
            ServiceUtils.requestSystemExit(ExitCode.INVALID_INVOCATION.getValue());
        } catch (FileTxnSnapLog.DatadirException e) {
            LOG.error("Unable to access datadir, exiting abnormally", e);
            System.err.println("Unable to access datadir, exiting abnormally");
            ZKAuditProvider.addServerStartFailureAuditLog();
            ServiceUtils.requestSystemExit(ExitCode.UNABLE_TO_ACCESS_DATADIR.getValue());
        } catch (AdminServer.AdminServerException e) {
            LOG.error("Unable to start AdminServer, exiting abnormally", e);
            System.err.println("Unable to start AdminServer, exiting abnormally");
            ZKAuditProvider.addServerStartFailureAuditLog();
            ServiceUtils.requestSystemExit(ExitCode.ERROR_STARTING_ADMIN_SERVER.getValue());
        } catch (Exception e) {
            LOG.error("Unexpected exception, exiting abnormally", e);
            ZKAuditProvider.addServerStartFailureAuditLog();
            ServiceUtils.requestSystemExit(ExitCode.UNEXPECTED_ERROR.getValue());
        }
        LOG.info("Exiting normally");
        ServiceUtils.requestSystemExit(ExitCode.EXECUTION_FINISHED.getValue());
    }

    protected void initializeAndRun(String[] args) throws QuorumPeerConfig.ConfigException, IOException, AdminServer.AdminServerException {
        WitnessConfigLoader config = new WitnessConfigLoader();
        if (args.length == 1) {
            config.parse(args[0]);
        }

        // Start and schedule the the purge task
        /*DatadirCleanupManager purgeMgr = new DatadirCleanupManager(
                config.getDataDir(),
                config.getDataLogDir(),
                config.getSnapRetainCount(),
                config.getPurgeInterval());
        purgeMgr.start();*/

        if (args.length == 1 && config.isDistributed()) {
            runFromConfig(config);
        } else {
            LOG.warn("Either no config or no quorum defined in config, running in standalone mode");
            // there is only server in the quorum -- run as standalone
            //ZooKeeperServerMain.main(args);
        }
    }

    public void runFromConfig(WitnessConfigLoader config) throws IOException, AdminServer.AdminServerException {
        try {
            ManagedUtil.registerLog4jMBeans();
        } catch (JMException e) {
            LOG.warn("Unable to register log4j JMX control", e);
        }

        LOG.info("Starting quorum peer, myid=" + config.getServerId());

        MetricsProvider metricsProvider = null;
        /*
        try {
            metricsProvider = MetricsProviderBootstrap.startMetricsProvider(
                    config.getMetricsProviderClassName(),
                    config.getMetricsProviderConfiguration());
        } catch (MetricsProviderLifeCycleException error) {
            throw new IOException("Cannot boot MetricsProvider " + config.getMetricsProviderClassName(), error);
        }

         */
        try {
            //ServerMetrics.metricsProviderInitialized(metricsProvider);
            ServerCnxnFactory cnxnFactory = null;
            ServerCnxnFactory secureCnxnFactory = null;

            witness = getWitness();
            //witness.setTxnFactory(new FileTxnSnapLog(config.getDataLogDir(), config.getDataDir()));
            //witness.enableLocalSessions(config.areLocalSessionsEnabled());
            //witness.enableLocalSessionsUpgrading(config.isLocalSessionsUpgradingEnabled());
            //quorumPeer.setQuorumPeers(config.getAllMembers());
            witness.setDataDir(config.getDataDir().toString());
            witness.setDataFileName(config.dataFileName);
            witness.setElectionType(config.getElectionAlg());
            witness.setMyid(config.getServerId());
            witness.setTickTime(config.getTickTime());
            //witness.setMinSessionTimeout(config.getMinSessionTimeout());
            //witness.setMaxSessionTimeout(config.getMaxSessionTimeout());
            witness.setInitLimit(config.getInitLimit());
            witness.setPingLimit(config.getPingLimit());
            //witness.setConnectToLearnerMasterLimit(config.getConnectToLearnerMasterLimit());
            //witness.setObserverMasterPort(config.getObserverMasterPort());
            witness.setConfigFileName(config.getConfigFilename());
            //witness.setClientPortListenBacklog(config.getClientPortListenBacklog());
            //witness.setZKDatabase(new ZKDatabase(witness.getTxnFactory()));
            witness.setQuorumVerifier(config.getQuorumVerifier(), false);
            if (config.getLastSeenQuorumVerifier() != null) {
                witness.setLastSeenQuorumVerifier(config.getLastSeenQuorumVerifier(), false);
            }
            //witness.initConfigInZKDatabase();
            //witness.setCnxnFactory(cnxnFactory);
            //witness.setSecureCnxnFactory(secureCnxnFactory);
            witness.setSslQuorum(config.isSslQuorum());
            //witness.setUsePortUnification(config.shouldUsePortUnification());
            witness.setLearnerType(config.getPeerType());
            //witness.setSyncEnabled(config.getSyncEnabled());
            witness.setQuorumListenOnAllIPs(config.getQuorumListenOnAllIPs());
            if (config.sslQuorumReloadCertFiles) {
                witness.getX509Util().enableCertFileReloading();
            }
            /*
            witness.setMultiAddressEnabled(config.isMultiAddressEnabled());
            witness.setMultiAddressReachabilityCheckEnabled(config.isMultiAddressReachabilityCheckEnabled());
            witness.setMultiAddressReachabilityCheckTimeoutMs(config.getMultiAddressReachabilityCheckTimeoutMs());
            */

            // sets quorum sasl authentication configurations
            /*
            witness.setQuorumSaslEnabled(config.quorumEnableSasl);
            if (witness.isQuorumSaslAuthEnabled()) {
                witness.setQuorumServerSaslRequired(config.quorumServerRequireSasl);
                witness.setQuorumLearnerSaslRequired(config.quorumLearnerRequireSasl);
                witness.setQuorumServicePrincipal(config.quorumServicePrincipal);
                witness.setQuorumServerLoginContext(config.quorumServerLoginContext);
                witness.setQuorumLearnerLoginContext(config.quorumLearnerLoginContext);
            }
            */
            //witness.setQuorumCnxnThreadsSize(config.quorumCnxnThreadsSize);
            witness.initialize();

            /*if (config.jvmPauseMonitorToRun) {
                witness.setJvmPauseMonitor(new JvmPauseMonitor(config));
            }*/

            witness.start();
            ZKAuditProvider.addZKStartStopAuditLog();
            witness.join();
        } catch (InterruptedException e) {
            // warn, but generally this is ok
            LOG.warn("Quorum Peer interrupted", e);
        } finally {
            if (metricsProvider != null) {
                try {
                    metricsProvider.stop();
                } catch (Throwable error) {
                    LOG.warn("Error while stopping metrics", error);
                }
            }
        }
    }

    // @VisibleForTesting
    protected Witness getWitness() throws SaslException {
        return new Witness();
    }

}
