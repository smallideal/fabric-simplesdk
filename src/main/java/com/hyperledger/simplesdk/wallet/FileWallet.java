package com.hyperledger.simplesdk.wallet;

import com.hyperledger.simplesdk.ConnectionProfile;
import com.hyperledger.simplesdk.ca.CaClient;
import com.hyperledger.simplesdk.channel.EnrollUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 钱包
 *
 * @author jinlong
 */
public class FileWallet {
    private String storePath;
    private static final Logger logger = LoggerFactory.getLogger(FileWallet.class);
    private static final String DEFAULT_PEER_ADMIN_CARD_NAME = "admin";
    public static final String DEFAULT_WORKPLACE=System.getProperty("user.home") + File.separator + ".wallet";
    private static final String DEFAULT_CARD_STORE_PATH = DEFAULT_WORKPLACE+ File.separator + "/cards";

    static {
        File cardsStoreDic = new File(DEFAULT_CARD_STORE_PATH);
        if (!cardsStoreDic.exists()) {
            cardsStoreDic.mkdirs();
        }
    }


    public static FileWallet newDefaultWallet() {
        return new FileWallet(DEFAULT_CARD_STORE_PATH);
    }

    private FileWallet(String storePath) {
        this.storePath = storePath;
    }


    public UserCard readCard(String userCard) {
        File file = new File(storePath + "/" + userCard + ".card");
        return unSerializeUser(file);
    }

    public boolean exists(String userCard) {
        File file = new File(storePath + "/" + userCard + ".card");
        return file.exists();
    }

    public boolean registerCard(ConnectionProfile connectionProfile, String userCard) {
        UserCard adminCard = enrollAdmin(connectionProfile);
        try {
            if (exists(userCard)) {
                throw new IllegalStateException("用户卡已存在");
            } else {
                CaClient caClient = new CaClient(connectionProfile);
                EnrollUser fabricUser = caClient.registerUser(adminCard.getEnrollUser(), userCard);
                fabricUser.setMspid(connectionProfile.getNetworkConfig().getClientOrganization().getMspId());
                writeUserCard(new UserCard(fabricUser, connectionProfile));
                return true;
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
    }

    private UserCard unSerializeUser(File file) {
        try {
            return UserCard.unserialize(new FileInputStream(file));
        } catch (Exception e) {
            throw new WalletException(e.getMessage(), e);
        }
    }

    private UserCard enrollAdmin(ConnectionProfile connectionProfile) {
        try {
            if (!exists(DEFAULT_PEER_ADMIN_CARD_NAME)) {
                CaClient caClient = new CaClient(connectionProfile);
                EnrollUser fabricUser = caClient.enrollAdmin();
                UserCard userCard = new UserCard();
                userCard.setConnectionProfile(connectionProfile);
                userCard.setEnrollUser(fabricUser);
                writeUserCard(userCard);
            }
            return readCard(DEFAULT_PEER_ADMIN_CARD_NAME);
        } catch (Exception e) {
            throw new WalletException(e.getMessage(), e);
        }
    }

    private void writeUserCard(UserCard userCard) throws IOException {
        FileWriter fileWriter = new FileWriter(new File(storePath + "/" + userCard.getEnrollUser().getName() + ".card"));
        fileWriter.write(UserCard.serialize(userCard));
        fileWriter.close();
    }

    public File[] listCards() {
        File file = new File(storePath);
        return file.listFiles();
    }
}
