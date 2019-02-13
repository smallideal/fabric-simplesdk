package com.hyperledger.simplesdk.wallet;

import com.alibaba.fastjson.JSONObject;
import com.hyperledger.simplesdk.ConnectionProfile;
import com.hyperledger.simplesdk.channel.EnrollUser;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

public class UserCard implements Serializable {
    private static final long serialVersionUID = -4936447224508890607L;
    private EnrollUser enrollUser;
    private ConnectionProfile connectionProfile;


    public UserCard() {
    }

    public UserCard(EnrollUser fabricUser, ConnectionProfile connectionProfile) {
        this.enrollUser = fabricUser;
        this.connectionProfile = connectionProfile;
    }

    public EnrollUser getEnrollUser() {
        return enrollUser;
    }

    public void setEnrollUser(EnrollUser enrollUser) {
        this.enrollUser = enrollUser;
    }

    public ConnectionProfile getConnectionProfile() {
        return connectionProfile;
    }

    public void setConnectionProfile(ConnectionProfile connectionProfile) {
        this.connectionProfile = connectionProfile;
    }

    public static String serialize(UserCard userCard) {
        JSONObject jsonObject = new JSONObject();
        userCard.getEnrollUser().setEnrollment(null);
        jsonObject.put("user", JSONObject.toJSONString(userCard.getEnrollUser()));
        jsonObject.put("connection", userCard.getConnectionProfile().getJsonObject().toString());
        return jsonObject.toJSONString();
    }

    public static UserCard unserialize(InputStream inputStream) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(IOUtils.toString(inputStream,"utf-8"));
        EnrollUser enrollUser = JSONObject.parseObject(jsonObject.getString("user"), EnrollUser.class);
        ConnectionProfile connectionProfile = new ConnectionProfile(new ByteArrayInputStream(jsonObject.getString("connection").getBytes()));
        return new UserCard(enrollUser, connectionProfile);
    }
}
