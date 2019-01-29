package com.hyperledger.simplesdk.chaincode;

import java.util.ArrayList;

public class ChaincodeRequest {

    private ChaincodeDefinition chaincodeDefinition;
    private String function;
    private ArrayList<String> argumentList = new ArrayList<>();


    //In milliseconds, timeout to send the proposal request
    private long proposalWaitTime = 120000;

    public long getProposalWaitTime() {
        return proposalWaitTime;
    }

    public void setProposalWaitTime(long proposalWaitTime) {
        this.proposalWaitTime = proposalWaitTime;
    }



    public ChaincodeDefinition getChaincodeDefinition() {
        return chaincodeDefinition;
    }

    public void setChaincodeDefinition(ChaincodeDefinition chaincodeDefinition) {
        this.chaincodeDefinition = chaincodeDefinition;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public ArrayList<String> getArgumentList() {
        return argumentList;
    }

    public void setArgumentList(ArrayList<String> argumentList) {
        this.argumentList = argumentList;
    }
}
