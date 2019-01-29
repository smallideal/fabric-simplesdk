package com.hyperledger.simplesdk.chaincode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 链玛初始化请求
 *
 * @author jinlong
 */
public class ChaincodeInstantiateRequest {

    private ChaincodeDefinition chaincodeDefinition;
    private File endorsementPolicyFile;
    private ArrayList<String> initArguments = new ArrayList<>();

    public ArrayList<String> getInitArguments() {
        return initArguments;
    }

    public void setInitArguments(ArrayList<String> initArguments) {
        this.initArguments = initArguments;
    }

    public ChaincodeDefinition getChaincodeDefinition() {
        return chaincodeDefinition;
    }

    public void setChaincodeDefinition(ChaincodeDefinition chaincodeDefinition) {
        this.chaincodeDefinition = chaincodeDefinition;
    }

    public void setEndorsementPolicyFile(File endorsementPolicyFile) {
        this.endorsementPolicyFile = endorsementPolicyFile;
    }

    public File getEndorsementPolicyFile() {
        return endorsementPolicyFile;
    }
}
