### Hyperledger Fabric Simple JAVA SDK
#### 简介
simplesdk 是基于fabric 官方SDK 进行简单封装的易用版本。   
提供链玛安装，初始化，交易提交，交易查询四个基本接口。
 
#### Checkout   
```
git clone https://github.com/wjinlg/fabric-simplesdk.git

```
#### 示例代码    
````
InputStream inputStream = FabricClientTest.class.getResourceAsStream("connection.json");
ConnectionProfile connectionProfile = new ConnectionProfile(inputStream);
WalletConfig walletConfig = new WalletConfig("test", PROJECT_PATH+"/fabcar/test/cards");
FabricClient fabricClient = new FabricClient(connectionProfile, walletConfig,"mychannel");
ChaincodeRequest request = new ChaincodeRequest();
request.setChaincodeDefinition(new ChaincodeDefinition("fabcar", "1.0"));
request.setFunction("initLedger");
fabricClient.submit(request);
````
connection.json 是区块链连接配置文件，配置信息可参考 [hyperlegder fabric connection profile 说明]("https://hyperledger-fabric.readthedocs.io/en/latest/developapps/connectionprofile.html?highlight=connection")    
WalletConfig 用户配置区块链链接用户卡信息，包括用于连接操作区块链的用户名以及用户卡存储路径    
com.hyperledger.simplesdk.FabricClient 客户端类，提供链玛安装，初始化，交易提交，交易查询四个基本接口
