package com.hyperledger.simplesdk.wallet;

import com.alibaba.fastjson.JSONObject;
import com.hyperledger.simplesdk.ca.CaClient;
import com.hyperledger.simplesdk.channel.FabricUser;
import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 钱包仓库
 *
 * @author jinlong
 */
public class WalletRepository {
    private WalletConfig walletConfig;
    private CaClient caClient;
    private NetworkConfig.OrgInfo orgInfo;
    private static final Logger logger = LoggerFactory.getLogger(WalletRepository.class);

    public WalletRepository(WalletConfig walletConfig, NetworkConfig.OrgInfo orgInfo) {
        this.walletConfig = walletConfig;
        this.caClient = new CaClient(orgInfo.getCertificateAuthorities().get(0));
        this.orgInfo = orgInfo;
    }


    public FabricUser registerUser() {
        FabricUser fabricAdminUser = enrollAdmin();
        try {
            File file = new File(walletConfig.getStorePath() + "/" + walletConfig.getName() + ".card");
            if (!file.exists()) {
                FabricUser fabricUser = caClient.registerUser(fabricAdminUser, walletConfig.getName());
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(JSONObject.toJSONString(fabricUser));
                fileWriter.close();
            }
            FabricUser fabricUser = unSerializeUser(file);
            fabricUser.setMspid(orgInfo.getMspId());
            return fabricUser;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private FabricUser unSerializeUser(File file) throws IOException {
        FabricUser fabricUser = JSONObject.parseObject(IOUtils.toString(new FileInputStream(file), Charset.forName("utf-8")), FabricUser.class);
        //避免FASTJSON 自动产生Proxy
        fabricUser.setEnrollment(null);
        return fabricUser;
    }

    private FabricUser enrollAdmin() {
        try {
            File file = new File(walletConfig.getStorePath() + "/admin.card");
            if (!file.exists()) {
                FabricUser fabricUser = caClient.enrollAdmin();
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(JSONObject.toJSONString(fabricUser));
                fileWriter.close();
            }
            FabricUser fabricAdmin = unSerializeUser(file);
            fabricAdmin.setMspid(orgInfo.getMspId());
            return fabricAdmin;
        } catch (Exception e) {
            throw new WalletException(e.getMessage(), e);
        }
    }
}
