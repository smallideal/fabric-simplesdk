package com.hyperledger.simplesdk.chaincode;

import java.io.File;

public class ChaincodeInstallRequest {
    private ChaincodeDefinition chaincodeDefinition;
    private File chaincodeDir;

    public ChaincodeDefinition getChaincodeDefinition() {
        return chaincodeDefinition;
    }

    public void setChaincodeDefinition(ChaincodeDefinition chaincodeDefinition) {
        this.chaincodeDefinition = chaincodeDefinition;
    }

    public File getChaincodeDir() {
        return chaincodeDir;
    }

    public void setChaincodeDir(File chaincodeDir) {
        this.chaincodeDir = chaincodeDir;
    }
}
