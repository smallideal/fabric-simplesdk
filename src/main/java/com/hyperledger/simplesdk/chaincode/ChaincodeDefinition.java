package com.hyperledger.simplesdk.chaincode;

import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.TransactionRequest;

/**
 * 链玛定义
 *
 * @author jinlong
 */
public class ChaincodeDefinition {
    //链玛名称
    private String name;
    //链玛版本
    private String version;

    private TransactionRequest.Type language = TransactionRequest.Type.GO_LANG;

    public TransactionRequest.Type getLanguage() {
        return language;
    }

    public void setLanguage(TransactionRequest.Type language) {
        this.language = language;
    }

    public ChaincodeDefinition() {
    }

    public ChaincodeDefinition(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ChaincodeID toSdkID() {
        ChaincodeID.Builder builder = ChaincodeID.newBuilder();
        builder.setName(name);
        if(!StringUtils.isEmpty(version)){
            builder.setVersion(version);
        }
        return builder.build();
    }
}
