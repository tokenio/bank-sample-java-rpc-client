package io.token.exmple.client;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.token.proto.bankapi.AccountServiceGrpc;
import io.token.proto.bankapi.AccountServiceGrpc.AccountServiceBlockingStub;
import io.token.proto.bankapi.Bankapi.GetBalanceRequest;
import io.token.proto.bankapi.Bankapi.GetBalanceResponse;
import io.token.proto.bankapi.Bankapi.HealthCheckRequest;
import io.token.proto.bankapi.Bankapi.HealthCheckResponse;
import io.token.proto.bankapi.HealthCheckServiceGrpc;
import io.token.proto.bankapi.HealthCheckServiceGrpc.HealthCheckServiceBlockingStub;
import io.token.proto.bankapi.TransferServiceGrpc;
import io.token.proto.bankapi.TransferServiceGrpc.TransferServiceBlockingStub;
import io.token.proto.common.account.AccountProtos.AccountFeatures;
import io.token.proto.common.account.AccountProtos.BankAccount;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        // Replace with corresponding values for your bank
        String bankId = "coop";
        String target = "localhost:50051";

        File trustCertCollectionFile = new File("config/tls/trusted-certs.pem");
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
            HealthCheckServiceBlockingStub health = HealthCheckServiceGrpc.newBlockingStub(channel);
            TransferServiceBlockingStub transfers = TransferServiceGrpc.newBlockingStub(channel);
            AccountServiceBlockingStub accounts = AccountServiceGrpc.newBlockingStub(channel);

            // We will be using the Health Check service in this example
            HealthCheckRequest request = HealthCheckRequest.newBuilder()
                    .setBankId(bankId)
                    .build();

            System.out.println("--> OUT: " + request);
            HealthCheckResponse response = health.healthCheck(request);
            System.out.println("<-- IN: " + response);


            //If the HealthCheck was successful then we can move on to testing GetBalance

            /* Example of the account object coop currently use (This account object originates from the banks implementation of the Bank SDK (when creating an Access Consent) )
              "account": {
                "accountFeatures": {
                  "supportsInformation": true,
                  "supportsReceivePayment": true,
                  "supportsSendPayment": true
                },
                "custom": {
                  "bankId": "coop-sit2",
                  "payload": "2cnHuGDFpNqyhCwSCnnpKLW/CBOSXsnAVOMe6qKkaM8=|m:3ikLDpahw1Rx11RDMhWQdDF8jpq4:5zKtXEAq"
                }
              }
             */
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
                                    .setBankId("coop-sit2")
                                    .setPayload("2cnHuGDFpNqyhCwSCnnpKLW/CBOSXsnAVOMe6qKkaM8=|m:3ikLDpahw1Rx11RDMhWQdDF8jpq4:5zKtXEAq")
                                    .build())
                            .build())
                    .build();

            GetBalanceResponse getBalanceResponse = accounts.getBalance(getBalanceRequest);
            System.out.println("GetBalance returned: " + getBalanceResponse.getAvailable().getValue() + getBalanceResponse.getAvailable().getCurrency());

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
