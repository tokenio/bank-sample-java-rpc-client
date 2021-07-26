package io.token.sample.rpc.client;

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

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Main {

    // Replace with corresponding values for your bank
    static final String bankId = "ob-modelo";
    static final String target = "fank.development:50051";


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

        System.out.println("Opening channel to " + bankId);
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                .sslContext(context)
                .keepAliveTime(50L, TimeUnit.SECONDS)
                .build();

        System.out.println("Connected to " + target);

        try {
            // Create clients for various services supported by the Bank API
            // Here are a few examples. Actual services are configured on a case-by-case basis.
            HealthCheckServiceBlockingStub healthCheckService = HealthCheckServiceGrpc.newBlockingStub(channel);
            TransferServiceBlockingStub transferService = TransferServiceGrpc.newBlockingStub(channel);
            AccountServiceBlockingStub accountService = AccountServiceGrpc.newBlockingStub(channel);

            testHealthCheck(healthCheckService);
            testGetBalance(accountService);
            testGetAccount(accountService);
            testGetTransactions(accountService);

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }


    static void testHealthCheck(HealthCheckServiceBlockingStub healthCheckService) {
        System.out.println("Performing HealthCheck...");
        // We will be using the Health Check service in this example
        HealthCheckRequest request = HealthCheckRequest.newBuilder()
                .setBankId(bankId)
                .build();

        System.out.println("--> OUT: " + request);
        HealthCheckResponse response = healthCheckService.healthCheck(request);
        System.out.println("<-- IN: " + response);
    }

    static void testGetBalance(AccountServiceBlockingStub accounts) {
        System.out.println("\nPerforming GetBalance...");

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
                        .setDomestic(BankAccount.Domestic.newBuilder()
                                .setAccountNumber("12345678")
                                .setBankCode("123456")
                                .build())
                        .build())
                .build();

        System.out.println("--> OUT: " + getBalanceRequest);
        GetBalanceResponse getBalanceResponse = accounts.getBalance(getBalanceRequest);
        System.out.println("<-- IN: " + getBalanceResponse);
    }


    static void testGetAccount(AccountServiceBlockingStub accounts) {
        System.out.println("\nPerforming GetAccount...");

        String consentId = "ta:9tJNYhJ1a4NxPhtgsCqWcLiVGpXJTYjyBUZcr3bNwu9A:5zKtXEAq";
        GetAccountRequest getAccountRequest = GetAccountRequest.newBuilder()
                .setConsentId(consentId)
                .setAccount(BankAccount.newBuilder()
                        .setAccountFeatures(AccountFeatures.newBuilder()
                                .setSupportsInformation(true)
                                .setSupportsSendPayment(true)
                                .setSupportsReceivePayment(true)
                                .build())
                        .setDomestic(BankAccount.Domestic.newBuilder()
                                .setAccountNumber("12345678")
                                .setBankCode("123456")
                                .build())
                        .build())
                .build();

        System.out.println("--> OUT: " + getAccountRequest);
        GetAccountResponse getAccountResponse = accounts.getAccount(getAccountRequest);
        System.out.println("<-- IN: " + getAccountResponse);
    }

    static void testGetTransactions(AccountServiceBlockingStub accounts) {
        System.out.println("\nPerforming GetTransactions...");

        String consentId = "ta:9tJNYhJ1a4NxPhtgsCqWcLiVGpXJTYjyBUZcr3bNwu9A:5zKtXEAq";
        GetTransactionsRequest getTransactionsRequest = GetTransactionsRequest.newBuilder()
                .setConsentId(consentId)
                .setStartDate("2021-07-20")
                .setEndDate("2021-07-26")
                .setLimit(10)
                //.setOffset("") //The offset value depends on the bank-integration implementation.
                .setAccount(BankAccount.newBuilder()
                        .setAccountFeatures(AccountFeatures.newBuilder()
                                .setSupportsInformation(true)
                                .setSupportsSendPayment(true)
                                .setSupportsReceivePayment(true)
                                .build())
                        .setDomestic(BankAccount.Domestic.newBuilder()
                                .setAccountNumber("12345678")
                                .setBankCode("123456")
                                .build())
                        .build())
                .build();

        System.out.println("--> OUT: " + getTransactionsRequest);
        GetTransactionsResponse getTransactionsResponse = accounts.getTransactions(getTransactionsRequest);
        System.out.println("<-- IN: " + getTransactionsResponse);
    }

}
