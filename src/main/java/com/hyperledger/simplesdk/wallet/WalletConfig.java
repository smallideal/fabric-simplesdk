package com.hyperledger.simplesdk.wallet;

/**
 * 钱包配置
 *
 * @author jinlong
 */
public class WalletConfig {
    private String name;
    private String storePath;

    public WalletConfig(String name, String storePath) {
        this.name = name;
        this.storePath = storePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }
}
