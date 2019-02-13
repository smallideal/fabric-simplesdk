package com.hyperledger.simplesdk.chaincode;

import org.hyperledger.fabric.protos.peer.FabricProposalResponse;

/**
 * 链玛查询结果
 *
 * @author jinlong
 */
public class TransactionResult {
    private String payload;
    private String message;
    private String transactionId;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void parse(FabricProposalResponse.Response response) {
        setPayload(response.getPayload().toStringUtf8());
        setMessage(response.getMessage());
    }
}
