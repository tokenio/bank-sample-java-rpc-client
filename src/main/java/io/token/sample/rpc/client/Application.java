package io.token.sample.rpc.client;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.token.proto.bankapi.AccountServiceGrpc;
import io.token.proto.bankapi.AccountServiceGrpc.AccountServiceBlockingStub;
import io.token.proto.bankapi.Bankapi;
import io.token.proto.bankapi.Bankapi.GetAccountRequest;
import io.token.proto.bankapi.HealthCheckServiceGrpc;
import io.token.proto.bankapi.HealthCheckServiceGrpc.HealthCheckServiceBlockingStub;
import io.token.proto.bankapi.TransferServiceGrpc;
import io.token.proto.bankapi.TransferServiceGrpc.TransferServiceBlockingStub;
import io.token.proto.common.account.AccountProtos.BankAccount;
import io.token.proto.common.account.AccountProtos.BankAccount.Custom;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Application {
    public static void main(String[] args) throws Exception {
        // Replace with corresponding values for your bank
        String bankId = "coop";
        String target = "fank.development:50051";

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

            // We will be using the Account service in this example
            GetAccountRequest request = GetAccountRequest.newBuilder()
                    .setAccount(BankAccount.newBuilder()
                            .setCustom(Custom.newBuilder()
                                    .setBankId(bankId)
                                    .setPayload("a-valid-account-identifier")
                                    .build())
                            .build())
                    .setConsentId("a-valid-consent-id")
                    .build();

            System.out.println("--> OUT: " + request);
            Bankapi.GetAccountResponse response = accounts.getAccount(request);
            System.out.println("<-- IN: " + response);

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
