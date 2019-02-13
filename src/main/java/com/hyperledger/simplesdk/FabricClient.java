package com.hyperledger.simplesdk;

import com.hyperledger.simplesdk.chaincode.ChaincodeInstallRequest;
import com.hyperledger.simplesdk.chaincode.ChaincodeInstantiateRequest;
import com.hyperledger.simplesdk.chaincode.ChaincodeRequest;
import com.hyperledger.simplesdk.chaincode.TransactionResult;
import com.hyperledger.simplesdk.channel.ChannelClient;
import com.hyperledger.simplesdk.channel.EnrollUser;
import org.hyperledger.fabric.sdk.Channel;

import java.util.ArrayList;

/**
 * SimpleSdk 暴露的超级账本客户端
 *
 * @author jinlong
 */
public class FabricClient {


    private ChannelClient peerAdminChannelClient;
    private ChannelClient userChannelClient;



    public FabricClient(EnrollUser enrollUser, ConnectionProfile connectionProfile, String channelName) throws Exception {
        peerAdminChannelClient = new ChannelClient(connectionProfile.getPeerAdmin(), connectionProfile, channelName);
        userChannelClient = new ChannelClient(enrollUser,connectionProfile, channelName);
    }


    public void installChainCode(ChaincodeInstallRequest chaincodeInstallRequest) {
        peerAdminChannelClient.installChainCode(chaincodeInstallRequest.getChaincodeDefinition(),
                chaincodeInstallRequest.getChaincodeDir());
    }

    public void instantiateChainCode(ChaincodeInstantiateRequest chaincodeInstantiateRequest) {
        ChaincodeRequest chaincodeRequest = new ChaincodeRequest();
        chaincodeRequest.setFunction("init");
        chaincodeRequest.setArgumentList(chaincodeInstantiateRequest.getInitArguments());
        chaincodeRequest.setChaincodeDefinition(chaincodeInstantiateRequest.getChaincodeDefinition());
        peerAdminChannelClient.instantiateChainCode(chaincodeRequest, chaincodeInstantiateRequest.getEndorsementPolicyFile());

    }

    public TransactionResult query(ChaincodeRequest chaincodeRequest) {
        return userChannelClient.query(chaincodeRequest);
    }

    public TransactionResult submit(ChaincodeRequest chaincodeRequest) {
        return userChannelClient.submit(chaincodeRequest);
    }

    public void upgradeChainCode(ChaincodeInstantiateRequest chaincodeInstantiateRequest) {
        ChaincodeRequest chaincodeRequest = new ChaincodeRequest();
        chaincodeRequest.setFunction("init");
        chaincodeRequest.setArgumentList(new ArrayList<>());
        chaincodeRequest.setChaincodeDefinition(chaincodeInstantiateRequest.getChaincodeDefinition());
        peerAdminChannelClient.upgradeChainCode(chaincodeRequest, chaincodeInstantiateRequest.getEndorsementPolicyFile());
    }

    public Channel getChannel() {
        return userChannelClient.getChannel();
    }

}
