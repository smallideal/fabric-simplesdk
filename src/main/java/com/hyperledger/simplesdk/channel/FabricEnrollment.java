package com.hyperledger.simplesdk.channel;

import org.hyperledger.fabric.sdk.Enrollment;

import java.io.Serializable;
import java.security.PrivateKey;

/**
 * 默认SDK的构造方法不可访问，必须新建一个类实现enrollment
 *
 * @author jinlong
 */
public class FabricEnrollment implements Enrollment, Serializable {

    private final PrivateKey privateKey;

    private final String certificate;


    FabricEnrollment(PrivateKey privateKey, String certificate) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    @Override
    public PrivateKey getKey() {
        return privateKey;
    }

    @Override
    public String getCert() {
        return certificate;
    }


}
