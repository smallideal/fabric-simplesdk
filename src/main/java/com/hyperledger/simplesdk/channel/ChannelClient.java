package com.hyperledger.simplesdk.channel;

import com.alibaba.fastjson.JSON;
import com.hyperledger.simplesdk.*;
import com.hyperledger.simplesdk.chaincode.ChaincodeDefinition;
import com.hyperledger.simplesdk.chaincode.ChaincodeRequest;
import com.hyperledger.simplesdk.chaincode.QueryResult;
import com.hyperledger.simplesdk.utils.FileUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.hyperledger.fabric.sdk.Channel.TransactionOptions.createTransactionOptions;


/**
 * 通道客户端，用于构造通道通信对象
 *
 * @author jinlong
 */
public class ChannelClient {

    private static final Logger logger = LoggerFactory.getLogger(ChannelClient.class);
    //fabric sdk client
    private HFClient hfClient;
    //fabric sdk channel
    private Channel channel;

    private int proposalWaitTime = 300;

    public ChannelClient(FabricUser fabricUser, ConnectionProfile connectionProfile, String channelName) throws Exception {
        this.hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        hfClient.setUserContext(fabricUser);
        this.channel = this.hfClient.loadChannelFromConfig(channelName, connectionProfile.getNetworkConfig());
        this.channel.initialize();
        this.proposalWaitTime = Integer.valueOf(connectionProfile.getJsonObject().
                getJsonObject("client").
                getJsonObject("connection").
                getJsonObject("timeout").
                getJsonObject("peer").getString("endorser"));
    }

    public void instantiateChainCode(ChaincodeRequest request, File endorsementPolicyFile) {
        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        InstantiateProposalRequest instantiateProposalRequest = hfClient.newInstantiationProposalRequest();
        instantiateProposalRequest.setProposalWaitTime(proposalWaitTime);
        instantiateProposalRequest.setChaincodeID(request.getChaincodeDefinition().toSdkID());
        instantiateProposalRequest.setChaincodeLanguage(request.getChaincodeDefinition().getLanguage());
        instantiateProposalRequest.setFcn(request.getFunction());
        instantiateProposalRequest.setArgs(request.getArgumentList());
        try {
            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(endorsementPolicyFile);
            instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

            logger.info("Sending instantiateProposalRequest to all peers with arguments");
            responses = channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
            for (ProposalResponse response : responses) {
                if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(response);
                    logger.info("Succesful instantiate proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                } else {
                    failed.add(response);
                }
            }
            logger.info("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(), failed.size());
            if (failed.size() > 0) {
                for (ProposalResponse fail : failed) {

                    logger.info("Not enough endorsers for instantiate :" + successful.size() + "endorser failed with " + fail.getMessage() + ", on peer" + fail.getPeer());

                }
                ProposalResponse first = failed.iterator().next();
                throw new IllegalStateException("Not enough endorsers for instantiate :" + successful.size() + "endorser failed with " + first.getMessage() + ". Was verified:" + first.isVerified());
            }
            CompletableFuture<BlockEvent.TransactionEvent> completableFuture = channel.sendTransaction(successful); // specify the orderers we want to try this transaction. Fails once all Orderers are tried.
                    // The events to signal the completion of the interest in the transaction
            BlockEvent.TransactionEvent event = completableFuture.get(32000, TimeUnit.SECONDS);
            if (event.isValid() && event.getSignature() != null) {
                logger.info("Finished instantiate transaction with transaction id %s", event.getTransactionID());
            } else {
                throw new IllegalStateException("Not enough endorsers for instantiate ");
            }

        } catch (Exception e) {
            throw new IllegalStateException("Not enough endorsers for instantiate ", e);
        } finally {
            logger.info("Sending instantiateTransaction to orderer");
        }

    }

    public void installChainCode(ChaincodeDefinition chaincodeDefinition, File chaincodeFile) {
        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        int numInstallProposal = 0;
        try {
            InstallProposalRequest installProposalRequest = hfClient.newInstallProposalRequest();
            ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(chaincodeDefinition.getName())
                    .setVersion(chaincodeDefinition.getVersion());
            String packagePath = "github/"+chaincodeDefinition.getName();
            chaincodeIDBuilder.setPath(packagePath);
            installProposalRequest.setChaincodeID(chaincodeIDBuilder.build());
            installProposalRequest.setChaincodeInputStream(FileUtils.generateTarGzInputStream(chaincodeFile,Paths.get("src",packagePath).toString()));
            installProposalRequest.setChaincodeVersion(chaincodeDefinition.getVersion());
            installProposalRequest.setChaincodeLanguage(chaincodeDefinition.getLanguage());
            Collection<Peer> peers = channel.getPeers();
            numInstallProposal = numInstallProposal + peers.size();
            responses = hfClient.sendInstallProposal(installProposalRequest, peers);
            for (ProposalResponse response : responses) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    logger.info("Successful install proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                    successful.add(response);
                } else {
                    failed.add(response);
                }
            }
            //   }
            logger.info("Received %d install proposal responses. Successful+verified: %d . Failed: %d", numInstallProposal, successful.size(), failed.size());

        } catch (Exception e) {
            throw new IllegalStateException("chaincode install error", e);
        }

        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            throw new IllegalStateException("Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage());
        }
    }

    /**
     * 执行链玛查询操作，非事务性操作
     *
     * @param chaincodeRequest 链玛请求
     * @return 提案响应
     * @throws Exception
     */
    public QueryResult query(ChaincodeRequest chaincodeRequest) {
        logger.info("Send Query Proposal to all peers");
        QueryByChaincodeRequest queryByChaincodeRequest = this.hfClient.newQueryProposalRequest();
        queryByChaincodeRequest.setArgs(chaincodeRequest.getArgumentList());
        queryByChaincodeRequest.setFcn(chaincodeRequest.getFunction());
        queryByChaincodeRequest.setChaincodeID(chaincodeRequest.getChaincodeDefinition().toSdkID());
        try {
            Collection<ProposalResponse> proposalResponseCollection = this.channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
            String payload = null;
            for (ProposalResponse proposalResponse : proposalResponseCollection) {
                if (!(proposalResponse.isVerified() && proposalResponse.getStatus() == ProposalResponse.Status.SUCCESS)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " +
                                proposalResponse.getStatus() + ". Messages: " + proposalResponse.getMessage() + ". Was verified : " + proposalResponse.isVerified());
                    }
                } else {
                    payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Query payload of b from peer %s returned %s", proposalResponse.getPeer().getName(), payload);
                    }
                    break;
                }
            }
            QueryResult queryResult = new QueryResult();
            if (payload == null || "".equals(payload)) {
                return queryResult;
            }
            queryResult.setData(payload.startsWith("[") ? JSON.parseArray(payload) : JSON.parseObject(payload));
            logger.info("Successfully sent Proposal and received ProposalResponse");
            return queryResult;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }

    /**
     * 执行链玛提交操作，事务性操作
     *
     * @param chaincodeRequest 链玛请求
     * @return 提案事件监听
     * @throws Exception
     */
    public void submit(ChaincodeRequest chaincodeRequest) {
        ChaincodeID chaincodeID = chaincodeRequest.getChaincodeDefinition().toSdkID();
        TransactionProposalRequest transactionProposalRequest = this.hfClient.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setChaincodeLanguage(chaincodeRequest.getChaincodeDefinition().getLanguage());
        transactionProposalRequest.setFcn(chaincodeRequest.getFunction());
        transactionProposalRequest.setProposalWaitTime(chaincodeRequest.getProposalWaitTime());
        transactionProposalRequest.setArgs(chaincodeRequest.getArgumentList());
        try {
            //FIXME sent to channel.getPeers() or getEndorsingPeers()?
            //  Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposalToEndorsers(transactionProposalRequest);
            Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest);
            // Check that all the proposals are consistent with each other. We should have only one set
            // where all the proposals above are consistent. Note the when sending to Orderer this is done automatically.
            //  Shown here as an example that applications can invoke and select.
            // See org.hyperledger.fabric.sdk.proposal.consistency_validation config property.
            Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(transactionPropResp);
            if (proposalConsistencySets.size() != 1) {
                String error = "Expected only one set of consistent proposal responses but got " + proposalConsistencySets.size();
                logger.error(error);
                //FabricUtils.debugProposalResponse(transactionPropResp);
                throw new IllegalStateException(error);
            }

            Collection<ProposalResponse> successful = new ArrayList<>(transactionPropResp.size());
            Collection<ProposalResponse> failed = new ArrayList<>(transactionPropResp.size());
            for (ProposalResponse response : transactionPropResp) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successful transaction proposal response Txid: " + response.getTransactionID() + " from peer " + response.getPeer().getName());
                    }
                    successful.add(response);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed transaction proposal response " + response);
                    }
                    failed.add(response);
                }
            }
            //
            if (failed.size() > 0) {
                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
                String error = "Not enough endorsers for invoke " + chaincodeRequest.getFunction() + " " + failed.size() + " endorser error: " +
                        firstTransactionProposalResponse.getMessage() + ". Was verified: " + firstTransactionProposalResponse.isVerified();
                logger.error(error);
                //FabricUtils.debugProposalResponse(transactionPropResp);
                throw new IllegalStateException(error);
            }
            //BlockEvent.TransactionEvent event = channel.sendTransaction(successful).get(32000, TimeUnit.SECONDS);
            channel.sendTransaction(successful).thenApply(event -> {
                if (logger.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Transaction event detail:");
                    sb.append("txId=").append(event.getTransactionID());
                    sb.append(",peer=").append(event.getPeer().getName());
                    sb.append(",channelId=").append(event.getChannelId());
                    logger.debug(sb.toString());
                }
                if (!event.isValid()) {
                    throw new IllegalArgumentException("Failed to send Proposal or receive valid response. ");
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Successfully sent Proposal and received ProposalResponse");
                }
                return null;
            }).exceptionally(e -> {
                if (e instanceof TransactionEventException) {
                    BlockEvent.TransactionEvent te = ((TransactionEventException) e).getTransactionEvent();
                    if (te != null) {
                        throw new IllegalStateException(format("Transaction with txid %s failed. %s", te.getTransactionID(), e.getMessage()), e);
                    }
                }

                throw new IllegalStateException(format("sent Transaction failed with %s exception %s", e.getClass().getName(), e.getMessage()), e);

            }).get(proposalWaitTime, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }

    public Channel getChannel() {
        return channel;
    }

    public void close() {
        this.channel.shutdown(true);
        //FIXME Do more research
        try {
            ExecutorService service = (ExecutorService) MethodUtils.invokeMethod(this.hfClient, true, "getExecutorService");
            service.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
