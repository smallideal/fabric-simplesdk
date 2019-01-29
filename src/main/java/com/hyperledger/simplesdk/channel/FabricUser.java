package com.hyperledger.simplesdk.channel;

import com.hyperledger.simplesdk.utils.PemUtils;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.security.PrivateKey;
import java.util.Set;

/**
 * 基于fabric sdk 实现授权用户信息
 *
 * @author jinlong
 */
public class FabricUser implements User {
    private String name;
    private String mspid;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private String privateKey;
    private String signedCert;
    private Enrollment enrollment;

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public String getMspid() {
        return mspid;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getSignedCert() {
        return signedCert;
    }

    public void setSignedCert(String signedCert) {
        this.signedCert = signedCert;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setMspid(String mspid) {
        this.mspid = mspid;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        if (this.enrollment == null) {
            try {
                PrivateKey privateKey = null;
                if (this.privateKey != null && !this.privateKey.isEmpty()) {
                    privateKey = PemUtils.getPrivateKeyFromPEMString(this.privateKey);
                }
                this.enrollment = new FabricEnrollment(privateKey, signedCert);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return this.enrollment;
    }

    @Override
    public String getMspId() {
        return mspid;
    }
}
