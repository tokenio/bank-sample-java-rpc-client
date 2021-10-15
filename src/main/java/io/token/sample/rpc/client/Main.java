package io.token.sample.rpc.client;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.token.proto.bankapi.AccountServiceGrpc;
import io.token.proto.bankapi.AccountServiceGrpc.AccountServiceBlockingStub;
import io.token.proto.bankapi.Bankapi.*;
import io.token.proto.bankapi.HealthCheckServiceGrpc;
import io.token.proto.bankapi.HealthCheckServiceGrpc.HealthCheckServiceBlockingStub;
import io.token.proto.bankapi.TransferServiceGrpc;
import io.token.proto.bankapi.TransferServiceGrpc.TransferServiceBlockingStub;
import io.token.proto.common.account.AccountProtos.AccountFeatures;
import io.token.proto.common.account.AccountProtos.BankAccount;
import io.token.proto.common.address.AddressProtos;
import io.token.proto.common.money.MoneyProtos;
import io.token.proto.common.token.TokenProtos;
import io.token.proto.common.transferinstructions.TransferInstructionsProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    // Replace with corresponding values for your bank
    static final String bankId = "ruby";
    static final String target = "localhost:9300";


    public static void main(String[] args) throws Exception {


        File trustCertCollectionFile = new File("config/tls/trusted-certs.pem"); //Banks SSL cert

        //A cert and key for this client, keyCertChainFile must be trusted by the bank
        File keyCertChainFile = new File("config/tls/cert.pem");
        File keyFile = new File("config/tls/key.pem");

        // Add SSL support if required. Add keys and certificates for mTLS
        SslContext context = GrpcSslContexts
                .forClient()
                .sslProvider(SslProvider.OPENSSL)
                .keyManager(keyCertChainFile, keyFile)
                .trustManager(trustCertCollectionFile)
                .build();

        logger.warn("Opening channel to " + bankId);
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                .sslContext(context)
                .keepAliveTime(50L, TimeUnit.SECONDS)
                .build();

        logger.warn("Connected to " + target);

        try {
            // Create clients for various services supported by the Bank API
            // Here are a few examples. Actual services are configured on a case-by-case basis.
            HealthCheckServiceBlockingStub healthCheckService = HealthCheckServiceGrpc.newBlockingStub(channel);
            TransferServiceBlockingStub transferService = TransferServiceGrpc.newBlockingStub(channel);
            AccountServiceBlockingStub accountService = AccountServiceGrpc.newBlockingStub(channel);

            testHealthCheck(healthCheckService);
            testTransfer(transferService);
            testStripedTransfer(transferService);
            testBulkTransfer(transferService);
            testGetBalance(accountService);
            testGetAccount(accountService);
            testGetTransactions(accountService);

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    static void testBulkTransfer(TransferServiceBlockingStub transferService) throws InvalidProtocolBufferException {
        CreateBulkTransferRequest bulkTransferRequest = CreateBulkTransferRequest.newBuilder()
                .setTokenBulkTransferId("bt:C9JaUJL7AgdfowvhK7KpXxwTkHqLYW119FV6UctsUokC:3VMUBNCZMURCaZw")// Token reference for the bulk transfer
                .setRefId("ghnvs7ha29bsb9m0v9")
                .setTokenInitiatorId("m:48KR5bH9xco7DZ59FuWaiviLcdnC:5zKtXEAq")// ID of member who requested token creation
                .setPayload(TokenProtos.BulkTransferBody.newBuilder()
                        .addTransfers(TokenProtos.BulkTransferBody.Transfer.newBuilder()
                                .setAmount("1.99")
                                .setCurrency("GBP")
                                .setRefId("2")
                                //.setDescription("")
                                .setDestination(TransferInstructionsProtos.TransferDestination.newBuilder()
                                        .setFasterPayments(TransferInstructionsProtos.TransferDestination.FasterPayments.newBuilder()
                                                .setAccountNumber("55656666")
                                                .setSortCode("400400")//400400
                                                .build())
                                        .setCustomerData(TransferInstructionsProtos.CustomerData.newBuilder()
                                                .addLegalNames("Southside")
                                                .setAddress(AddressProtos.Address.newBuilder()
                                                        .setHouseNumber("10")
                                                        .setHouseName("xyz")
                                                        .setFlats("1")
                                                        .setConscriptionNumber("1")
                                                        .setStreet("John Street")
                                                        .setPlace("15 Bishopsgate")
                                                        .setPostCode("WC1N 2EB")
                                                        .setCity("London")
                                                        .setCountry("GB")
                                                        .setFull("10 John Street, London, WC1N")
                                                        .setHamlet("xyz")
                                                        .setSuburb("xyz")
                                                        .setSubdistrict("xyz")
                                                        .setDistrict("xyz")
                                                        .setProvince("xyz")
                                                        .setState("UK")
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        /*.addTransfers(BulkTransferBody.Transfer.newBuilder()
                                .setAmount("1.00")
                                .setCurrency("GBP")
                                .setRefId("3")
                                .setDescription("")
                                .setDestination(TransferDestination.newBuilder()
                                        .setFasterPayments(TransferDestination.FasterPayments.newBuilder()
                                                .setAccountNumber("400400")
                                                .setSortCode("55657777")
                                                .build())
                                        .setCustomerData(CustomerData.newBuilder()
                                                .addLegalNames("Southside")
                                                .setAddress(Address.newBuilder()
                                                        .setHouseNumber("11")
                                                        .setHouseName("xyz")
                                                        .setFlats("1")
                                                        .setConscriptionNumber("1")
                                                        .setStreet("John Street")
                                                        .setPlace("16 Bishopsgate")
                                                        .setPostCode("WC1N 2EB")
                                                        .setCity("London")
                                                        .setCountry("GB")
                                                        .setFull("11 John Street, London, WC1N")
                                                        .setHamlet("xyz")
                                                        .setSuburb("xyz")
                                                        .setSubdistrict("xyz")
                                                        .setDistrict("xyz")
                                                        .setProvince("xyz")
                                                        .setState("UK")
                                                        .build())
                                                .build())
                                        .build())
                                .build())*/
                        .setTotalAmount("1.99")
                        .setSource(TransferInstructionsProtos.TransferEndpoint.newBuilder()
                                .setAccount(BankAccount.newBuilder()
                                        .setAccountFeatures(AccountFeatures.newBuilder()
                                                .setSupportsInformation(true)
                                                .setSupportsSendPayment(true)
                                                .setSupportsReceivePayment(true)
                                                .build())
                                        .setCustom(BankAccount.Custom.newBuilder()
                                                .setBankId("coop-sit2")
                                                .setPayload("zEkDvb9tgJUPfOCk1AbTsbW/CBOSXsnAVOMe6qKkaM8=|m:2X7zH9tFewVRPNQvqHUNycmShdb6:5zKtXEAq")//|14a50827-a0ae-3584-91f1-673e3c7e9b6a
                                                .build())
                                        .build())
                                .setBankId("coop-sit2")
                                .build())
                        .build())
                .setSource(BankAccount.newBuilder()
                                .setAccountFeatures(AccountFeatures.newBuilder()
                                        .setSupportsInformation(true)
                                        .setSupportsSendPayment(true)
                                        .setSupportsReceivePayment(true)
                                        .build())
                                .setCustom(BankAccount.Custom.newBuilder()
                                        .setBankId("coop-sit2")
                                        .setPayload("zEkDvb9tgJUPfOCk1AbTsbW/CBOSXsnAVOMe6qKkaM8=|m:2X7zH9tFewVRPNQvqHUNycmShdb6:5zKtXEAq")//|14a50827-a0ae-3584-91f1-673e3c7e9b6a
                                        .build())
                                .build())
                .setDescription("southside.com.noverifyICJON")
                .setSourceAccount(TransferInstructionsProtos.TransferEndpoint.newBuilder()
                        .setAccount(BankAccount.newBuilder()
                                .setAccountFeatures(AccountFeatures.newBuilder()
                                        .setSupportsInformation(true)
                                        .setSupportsSendPayment(true)
                                        .setSupportsReceivePayment(true)
                                        .build())
                                .setCustom(BankAccount.Custom.newBuilder()
                                        .setBankId("coop-sit2")
                                        .setPayload("zEkDvb9tgJUPfOCk1AbTsbW/CBOSXsnAVOMe6qKkaM8=|m:2X7zH9tFewVRPNQvqHUNycmShdb6:5zKtXEAq")//|14a50827-a0ae-3584-91f1-673e3c7e9b6a
                                        .build())
                                .build())
                        .setBankId("coop-sit2")
                        .build())
                .setConsentId("tt:C9JaUJL7AgdfowvhK7KpXxwTkHqLYW119FV6UctsUokC:3VMUBNCZMURCaZw")
                .build();


        logger.warn("--> OUT: \n" + JsonFormat.printer().print(bulkTransferRequest));
        CreateBulkTransferResponse bulkTransferResponse = transferService.createBulkTransfer(bulkTransferRequest);
        logger.warn("<-- IN: \n" + JsonFormat.printer().print(bulkTransferResponse));
    }

    static void testTransfer(TransferServiceBlockingStub transferService) throws InvalidProtocolBufferException {
        TransferRequest transferRequest = TransferRequest.newBuilder()
                .setTransferId("t:9zdwiPVSTfyetJJx58CGA5juDfjmKeyjEC7V2SQBJZtH:3VMUBNCZMURCaZw")
                .setRequestedAmount(MoneyProtos.Money.newBuilder()
                        .setCurrency("GBP")
                        .setValue("1.9900")
                        .build())
                .setTransactionAmount(MoneyProtos.Money.newBuilder()
                        .setCurrency("GBP")
                        .setValue("1.9900")
                        .build())
                .setSource(BankAccount.newBuilder()
                        .setAccountFeatures(AccountFeatures.newBuilder()
                                .setSupportsInformation(true)
                                .setSupportsSendPayment(true)
                                .setSupportsReceivePayment(true)
                                .build())
                        .setCustom(BankAccount.Custom.newBuilder()
                                .setPayload("KrOojigVsGkuaSL12P37mLW/CBOSXsnAVOMe6qKkaM8=|m:2X7zH9tFewVRPNQvqHUNycmShdb6:5zKtXEAq")
                                .setBankId("coop-sit2")
                                .build())
                        .build())
                .addDestinations(TransferInstructionsProtos.TransferEndpoint.newBuilder()
                        .setAccount(BankAccount.newBuilder().setFasterPayments(BankAccount.FasterPayments.newBuilder()
                                .setAccountNumber("12345678")
                                .setSortCode("123456")
                                .build()).build())
                        .setCustomerData(TransferInstructionsProtos.CustomerData.newBuilder()
                                .addLegalNames( "Southside")
                                .setAddress(AddressProtos.Address.newBuilder()
                                        .setHouseNumber("10")
                                        .setHouseName("xyz")
                                        .setFlats("1")
                                        .setConscriptionNumber("1")
                                        .setStreet("Test Street")
                                        .setPlace("15 Testgate")
                                        .setPostCode("WC1B 2EB")
                                        .setCity("London")
                                        .setCountry("GB")
                                        .setFull("10 John Street, London WC1N")
                                        .setHamlet("xyz")
                                        .setSuburb("xyc")
                                        .setSubdistrict("xyz")
                                        .setDistrict("London")
                                        .setProvince("xyz")
                                        .setState("UK")
                                        .build())
                                .build())
                        .build())
                .setDescription("southside.com.noverifyQSB4X")
                .setTokenRefId("ot33poxr5d3ohgb5os")
                .setMetadata(TransferInstructionsProtos.TransferInstructions.Metadata.newBuilder()
                        .build())
                .setTokenInitiatorId("m:test")
                .addTransferDestinations(TransferInstructionsProtos.TransferDestination.newBuilder()
                        .setFasterPayments(TransferInstructionsProtos.TransferDestination.FasterPayments.newBuilder()
                                .setAccountNumber("12345678")
                                .setSortCode("123456")
                                .build())
                        .setCustomerData(TransferInstructionsProtos.CustomerData.newBuilder()
                                .addLegalNames( "Southside")
                                .setAddress(AddressProtos.Address.newBuilder()
                                        .setHouseNumber("10")
                                        .setHouseName("xyz")
                                        .setFlats("1")
                                        .setConscriptionNumber("1")
                                        .setStreet("Test Street")
                                        .setPlace("15 Testgate")
                                        .setPostCode("WC1B 2EB")
                                        .setCity("London")
                                        .setCountry("GB")
                                        .setFull("10 John Street, London WC1N")
                                        .setHamlet("xyz")
                                        .setSuburb("xyc")
                                        .setSubdistrict("xyz")
                                        .setDistrict("London")
                                        .setProvince("xyz")
                                        .setState("UK")
                                        .build())
                                .build())
                        .build())
                .setTransferInstructions(TransferInstructionsProtos.TransferInstructions.newBuilder()
                        .setSource(TransferInstructionsProtos.TransferEndpoint.newBuilder().setAccount(BankAccount.newBuilder()
                                .setAccountFeatures(AccountFeatures.newBuilder()
                                        .setSupportsInformation(true)
                                        .setSupportsSendPayment(true)
                                        .setSupportsReceivePayment(true)
                                        .build())
                                .setCustom(BankAccount.Custom.newBuilder()
                                        .setPayload("KrOojigVsGkuaSL12P37mLW/CBOSXsnAVOMe6qKkaM8=|m:2X7zH9tFewVRPNQvqHUNycmShdb6:5zKtXEAq")
                                        .setBankId("coop-sit2")
                                        .build())
                                .build())

                        .build())
                        .addTransferDestinations(TransferInstructionsProtos.TransferDestination.newBuilder()
                                .setFasterPayments(TransferInstructionsProtos.TransferDestination.FasterPayments.newBuilder()
                                        .setAccountNumber("12345678")
                                        .setSortCode("123456")
                                        .build())
                                .setCustomerData(TransferInstructionsProtos.CustomerData.newBuilder()
                                        .addLegalNames( "Southside")
                                        .setAddress(AddressProtos.Address.newBuilder()
                                                .setHouseNumber("10")
                                                .setHouseName("xyz")
                                                .setFlats("1")
                                                .setConscriptionNumber("1")
                                                .setStreet("Test Street")
                                                .setPlace("15 Testgate")
                                                .setPostCode("WC1B 2EB")
                                                .setCity("London")
                                                .setCountry("GB")
                                                .setFull("10 John Street, London WC1N")
                                                .setHamlet("xyz")
                                                .setSuburb("xyc")
                                                .setSubdistrict("xyz")
                                                .setDistrict("London")
                                                .setProvince("xyz")
                                                .setState("UK")
                                                .build())
                                        .build())
                                .build()))
                        .setConsentId("tt:C9JaUJL7AgdfowvhK7KpXxwTkHqLYW119FV6UctsUokC:3VMUBNCZMURCaZw")
                .build();

        logger.warn("--> OUT: \n" + JsonFormat.printer().print(transferRequest));
        TransferResponse transferResponse = transferService.transfer(transferRequest);
        logger.warn("<-- IN: \n" + JsonFormat.printer().print(transferResponse));
    }

    /* A createTransaction request with duplicate data removed
     * This will work if the Bank Integration SDK has been implemented with the latest SDK,
     * if the implementation uses depreciated code they you will need to include duplicate data
     */
    static void testStripedTransfer(TransferServiceBlockingStub transferService) throws InvalidProtocolBufferException {
        TransferRequest transferRequest = TransferRequest.newBuilder()
                .setTransferId("t:9zdwiPVSTfyetJJx58CGA5juDfjmKeyjEC7V2SQBJZtH:3VMUBNCZMURCaZw")
                .setTransactionAmount(MoneyProtos.Money.newBuilder()
                        .setCurrency("GBP")
                        .setValue("1.9900")
                        .build())
                .setDescription("southside.com.noverifyQSB4X")
                .setTokenRefId("ot33poxr5d3ohgb5os")
                .setTokenInitiatorId("m:test")
                .setTransferInstructions(TransferInstructionsProtos.TransferInstructions.newBuilder()
                        .setSource(TransferInstructionsProtos.TransferEndpoint.newBuilder().setAccount(BankAccount.newBuilder()
                                .setAccountFeatures(AccountFeatures.newBuilder()
                                        .setSupportsInformation(true)
                                        .setSupportsSendPayment(true)
                                        .setSupportsReceivePayment(true)
                                        .build())
                                .setCustom(BankAccount.Custom.newBuilder()
                                        .setPayload("KrOojigVsGkuaSL12P37mLW/CBOSXsnAVOMe6qKkaM8=|m:2X7zH9tFewVRPNQvqHUNycmShdb6:5zKtXEAq")
                                        .setBankId("coop-sit2")
                                        .build())
                                .build())
                                .build())
                        .addTransferDestinations(TransferInstructionsProtos.TransferDestination.newBuilder()
                                .setFasterPayments(TransferInstructionsProtos.TransferDestination.FasterPayments.newBuilder()
                                        .setAccountNumber("12345678")
                                        .setSortCode("123456")
                                        .build())
                                .setCustomerData(TransferInstructionsProtos.CustomerData.newBuilder()
                                        .addLegalNames( "Southside")
                                        .setAddress(AddressProtos.Address.newBuilder()
                                                .setHouseNumber("10")
                                                .setHouseName("xyz")
                                                .setFlats("1")
                                                .setConscriptionNumber("1")
                                                .setStreet("Test Street")
                                                .setPlace("15 Testgate")
                                                .setPostCode("WC1B 2EB")
                                                .setCity("London")
                                                .setCountry("GB")
                                                .setFull("10 John Street, London WC1N")
                                                .setHamlet("xyz")
                                                .setSuburb("xyc")
                                                .setSubdistrict("xyz")
                                                .setDistrict("London")
                                                .setProvince("xyz")
                                                .setState("UK")
                                                .build())
                                        .build())
                                .build()))
                .setConsentId("tt:C9JaUJL7AgdfowvhK7KpXxwTkHqLYW119FV6UctsUokC:3VMUBNCZMURCaZw")
                .build();

        logger.warn("--> OUT: \n" + JsonFormat.printer().print(transferRequest));
        TransferResponse transferResponse = transferService.transfer(transferRequest);
        logger.warn("<-- IN: \n" + JsonFormat.printer().print(transferResponse));
    }

    static void testHealthCheck(HealthCheckServiceBlockingStub healthCheckService) throws InvalidProtocolBufferException {
        logger.warn("Performing HealthCheck...");
        // We will be using the Health Check service in this example
        HealthCheckRequest request = HealthCheckRequest.newBuilder()
                .setBankId(bankId)
                .build();

        logger.warn("--> OUT: \n" + JsonFormat.printer().print(request));
        HealthCheckResponse response = healthCheckService.healthCheck(request);
        logger.warn("<-- IN: \n" + JsonFormat.printer().print(response));
    }

    static void testGetBalance(AccountServiceBlockingStub accounts) throws InvalidProtocolBufferException {
        logger.warn("\nPerforming GetBalance...");

        String consentId = "ta:9tJNYhJ1a4NxPhtgsCqWcLiVGpXJTYjyBUZcr3bNwu9A:5zKtXEAq"; //This is the token id (usually Token will automatically populate this value), it depends on how the Bank-Integration SDK is used by the bank as to whether this is required (it's likely it is not required).
            /* A GetBalanceRequest has 2 possible fields: consentId and account
                The consentId is a string whereas the account is a BankAccount object, you can see an example of this object above and you can set the same values using the builder:
                BankAccount.newBuilder()
                //Set the values for the BankAccount object here
                .build()
               Protobuf for reference: https://developer.token.io/bank-integration/pbdoc/io_token_proto_bankapi.html#GetBalanceRequest
             */
        GetBalanceRequest getBalanceRequest = GetBalanceRequest.newBuilder()
                .setConsentId(consentId)
                .setAccount(BankAccount.newBuilder()
                        .setAccountFeatures(AccountFeatures.newBuilder()
                                .setSupportsInformation(true)
                                .setSupportsSendPayment(true)
                                .setSupportsReceivePayment(true)
                                .build())
                        .setCustom(BankAccount.Custom.newBuilder()
                                .setPayload("KrOojigVsGkuaSL12P37mLW/CBOSXsnAVOMe6qKkaM8=|m:2X7zH9tFewVRPNQvqHUNycmShdb6:5zKtXEAq")
                                .setBankId("coop-sit2")
                                .build())
                        .build())
                .build();

        logger.warn("--> OUT: \n" + JsonFormat.printer().print(getBalanceRequest));
        GetBalanceResponse getBalanceResponse = accounts.getBalance(getBalanceRequest);
        logger.warn("<-- IN: \n" + JsonFormat.printer().print(getBalanceResponse));
    }


    static void testGetAccount(AccountServiceBlockingStub accounts) throws InvalidProtocolBufferException {
        logger.warn("\nPerforming GetAccount...");

        String consentId = "ta:9tJNYhJ1a4NxPhtgsCqWcLiVGpXJTYjyBUZcr3bNwu9A:5zKtXEAq";
        GetAccountRequest getAccountRequest = GetAccountRequest.newBuilder()
                .setConsentId(consentId)
                .setAccount(BankAccount.newBuilder()
                        .setAccountFeatures(AccountFeatures.newBuilder()
                                .setSupportsInformation(true)
                                .setSupportsSendPayment(true)
                                .setSupportsReceivePayment(true)
                                .build())
                        .setCustom(BankAccount.Custom.newBuilder()
                                .setPayload("KrOojigVsGkuaSL12P37mLW/CBOSXsnAVOMe6qKkaM8=|m:2X7zH9tFewVRPNQvqHUNycmShdb6:5zKtXEAq")
                                .setBankId("coop-sit2")
                                .build())
                        .build())
                .build();

        logger.warn("--> OUT: \n" + JsonFormat.printer().print(getAccountRequest));
        GetAccountResponse getAccountResponse = accounts.getAccount(getAccountRequest);
        logger.warn("<-- IN: \n" + JsonFormat.printer().print(getAccountResponse));
    }

    static void testGetTransactions(AccountServiceBlockingStub accounts) throws InvalidProtocolBufferException {
        logger.warn("\nPerforming GetTransactions...");

        String consentId = "ta:9tJNYhJ1a4NxPhtgsCqWcLiVGpXJTYjyBUZcr3bNwu9A:5zKtXEAq";
        GetTransactionsRequest getTransactionsRequest = GetTransactionsRequest.newBuilder()
                .setConsentId(consentId)
                .setLimit(10)
                //.setOffset("") //The offset value depends on the bank-integration implementation.
                .setAccount(BankAccount.newBuilder()
                        .setAccountFeatures(AccountFeatures.newBuilder()
                                .setSupportsInformation(true)
                                .setSupportsSendPayment(true)
                                .setSupportsReceivePayment(true)
                                .build())
                        .setCustom(BankAccount.Custom.newBuilder()
                                .setPayload("KrOojigVsGkuaSL12P37mLW/CBOSXsnAVOMe6qKkaM8=|m:2X7zH9tFewVRPNQvqHUNycmShdb6:5zKtXEAq")
                                .setBankId("coop-sit2")
                                .build())
                        .build())
                .build();

        logger.warn("--> OUT: \n" + JsonFormat.printer().print(getTransactionsRequest));
        GetTransactionsResponse getTransactionsResponse = accounts.getTransactions(getTransactionsRequest);
        logger.warn("<-- IN: \n" + JsonFormat.printer().print(getTransactionsResponse));
    }

}
