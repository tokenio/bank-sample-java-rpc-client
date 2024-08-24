package io.token.sample.rpc.client;

import static io.netty.handler.ssl.SslProvider.OPENSSL;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.token.proto.bankapi.AccountServiceGrpc;
import io.token.proto.bankapi.AccountServiceGrpc.AccountServiceBlockingStub;
import io.token.proto.bankapi.Bankapi.CreateBulkTransferRequest;
import io.token.proto.bankapi.Bankapi.CreateBulkTransferResponse;
import io.token.proto.bankapi.Bankapi.GetAccountRequest;
import io.token.proto.bankapi.Bankapi.GetAccountResponse;
import io.token.proto.bankapi.Bankapi.GetBalanceRequest;
import io.token.proto.bankapi.Bankapi.GetBalanceResponse;
import io.token.proto.bankapi.Bankapi.GetTransactionsRequest;
import io.token.proto.bankapi.Bankapi.GetTransactionsResponse;
import io.token.proto.bankapi.Bankapi.GetTransferStatusRequest;
import io.token.proto.bankapi.Bankapi.GetTransferStatusResponse;
import io.token.proto.bankapi.Bankapi.HealthCheckRequest;
import io.token.proto.bankapi.Bankapi.HealthCheckResponse;
import io.token.proto.bankapi.Bankapi.TransferRequest;
import io.token.proto.bankapi.Bankapi.TransferResponse;
import io.token.proto.bankapi.HealthCheckServiceGrpc;
import io.token.proto.bankapi.HealthCheckServiceGrpc.HealthCheckServiceBlockingStub;
import io.token.proto.bankapi.TransferServiceGrpc;
import io.token.proto.bankapi.TransferServiceGrpc.TransferServiceBlockingStub;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    // Replace with corresponding values for your bank
    static String bankId = "ruby";
    static String target = "localhost:9300";
    
    private static final String OUT = "--> OUT: \n{}";
    private static final String IN = "<-- IN: \n{}";

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            bankId = args[0];
            if (args.length > 1) {
                target = args[1];
            }
        }

        File trustCertCollectionFile = new File("config/tls/trusted-certs.pem"); //Banks SSL cert

        //A cert and key for this client, keyCertChainFile must be trusted by the bank
        File keyCertChainFile = new File("config/tls/cert.pem");
        File keyFile = new File("config/tls/key.pem");

        // Add SSL support if required. Add keys and certificates for mTLS
        SslContext context = GrpcSslContexts
                .forClient()
                .sslProvider(OPENSSL)
                .keyManager(keyCertChainFile, keyFile)
                .trustManager(trustCertCollectionFile)
                .build();

        logger.warn("Opening channel to {}", bankId);
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                .intercept(new MetadataInterceptor(bankId))
                .sslContext(context)
                .keepAliveTime(50L, TimeUnit.SECONDS)
                .build();

        logger.warn("Connected to {}", target);

        try {
            // Create clients for various services supported by the Bank API
            // Here are a few examples. Actual services are configured on a case-by-case basis.
            HealthCheckServiceBlockingStub healthCheckService = HealthCheckServiceGrpc.newBlockingStub(channel);
            TransferServiceBlockingStub transferService = TransferServiceGrpc.newBlockingStub(channel);
            AccountServiceBlockingStub accountService = AccountServiceGrpc.newBlockingStub(channel);

            testHealthCheck(healthCheckService);
            testTransfer(transferService);
            testGetTransferStatus(transferService);
            testStripedTransfer(transferService);
            testBulkTransfer(transferService);
            testGetBalance(accountService);
            testGetAccount(accountService);
            testGetTransactions(accountService);

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    static void testHealthCheck(HealthCheckServiceBlockingStub healthCheckService) throws InvalidProtocolBufferException {
        logger.warn("Performing HealthCheck...");
        // We will be using the Health Check service in this example
        HealthCheckRequest request = HealthCheckRequest.newBuilder()
                .setBankId(bankId)
                .build();

        logger.warn(OUT, JsonFormat.printer().print(request));
        try {
            HealthCheckResponse response = healthCheckService.healthCheck(request);
            logger.warn(IN, JsonFormat.printer().print(response));
        } catch (Exception e) {
            logger.error("HealthCheck failed", e);
        }
    }

    static void testTransfer(TransferServiceBlockingStub transferService) throws InvalidProtocolBufferException {
        logger.warn("Performing Transfer...");
        TransferRequest transferRequest = TestRequests.transferRequest();

        logger.warn(OUT, JsonFormat.printer().print(transferRequest));
        try {
            TransferResponse transferResponse = transferService.transfer(transferRequest);
            logger.warn(IN, JsonFormat.printer().print(transferResponse));
        } catch (Exception e) {
            logger.error("Transfer failed", e);
        }
    }

    private static void testGetTransferStatus(TransferServiceBlockingStub transferService) throws InvalidProtocolBufferException {
        logger.warn("Performing GetTransferStatus...");
        GetTransferStatusRequest getTransferStatusRequest = TestRequests.getTransferStatusRequest();

        logger.warn(OUT, JsonFormat.printer().print(getTransferStatusRequest));
        try {
            GetTransferStatusResponse getTransferStatusResponse = transferService.getTransferStatus(getTransferStatusRequest);
            logger.warn(IN, JsonFormat.printer().print(getTransferStatusResponse));
        } catch (Exception e) {
            logger.error("GetTransferStatus failed", e);
        }
    }

    /* A createTransaction request with duplicate data removed
     * This will work if the Bank Integration SDK has been implemented with the latest SDK,
     * if the implementation uses depreciated code then you will need to include duplicate data
     */
    static void testStripedTransfer(TransferServiceBlockingStub transferService) throws InvalidProtocolBufferException {
        logger.warn("Performing Transfer...");
        TransferRequest transferRequest = TestRequests.minimalTransferRequest();

        logger.warn(OUT, JsonFormat.printer().print(transferRequest));
        try {
            TransferResponse transferResponse = transferService.transfer(transferRequest);
            logger.warn(IN, JsonFormat.printer().print(transferResponse));
        } catch (Exception e) {
            logger.error("Transfer failed", e);
        }
    }

    static void testBulkTransfer(TransferServiceBlockingStub transferService) throws InvalidProtocolBufferException {
        logger.warn("Performing BulkTransfer...");
        CreateBulkTransferRequest bulkTransferRequest = TestRequests.bulkTransferRequest();

        logger.warn(OUT, JsonFormat.printer().print(bulkTransferRequest));
        try {
            CreateBulkTransferResponse bulkTransferResponse = transferService.createBulkTransfer(bulkTransferRequest);
            logger.warn(IN, JsonFormat.printer().print(bulkTransferResponse));
        } catch (Exception e) {
            logger.error("BulkTransfer failed", e);
        }
    }

    static void testGetBalance(AccountServiceBlockingStub accounts) throws InvalidProtocolBufferException {
        logger.warn("\nPerforming GetBalance...");

        /* A GetBalanceRequest has 2 possible fields: consentId and account
            The consentId is a string whereas the account is a BankAccount object, you can see an example of this object above and you can set the same values using the builder:
            BankAccount.newBuilder()
            //Set the values for the BankAccount object here
            .build()
           Protobuf for reference: https://developer.token.io/bank-integration/pbdoc/io_token_proto_bankapi.html#GetBalanceRequest
         */
        GetBalanceRequest getBalanceRequest = TestRequests.getBalanceRequest();

        logger.warn(OUT, JsonFormat.printer().print(getBalanceRequest));
        try {
            GetBalanceResponse getBalanceResponse = accounts.getBalance(getBalanceRequest);
            logger.warn(IN, JsonFormat.printer().print(getBalanceResponse));
        } catch (Exception e) {
            logger.error("GetBalance failed", e);
        }
    }


    static void testGetAccount(AccountServiceBlockingStub accounts) throws InvalidProtocolBufferException {
        logger.warn("Performing GetAccount...");

        GetAccountRequest getAccountRequest = TestRequests.getAccountRequest();

        logger.warn(OUT, JsonFormat.printer().print(getAccountRequest));
        try {
            GetAccountResponse getAccountResponse = accounts.getAccount(getAccountRequest);
            logger.warn(IN, JsonFormat.printer().print(getAccountResponse));
        } catch (Exception e) {
            logger.error("GetAccount failed", e);
        }
    }

    static void testGetTransactions(AccountServiceBlockingStub accounts) throws InvalidProtocolBufferException {
        logger.warn("Performing GetTransactions...");
        GetTransactionsRequest getTransactionsRequest = TestRequests.getTransactionsRequest();

        logger.warn(OUT, JsonFormat.printer().print(getTransactionsRequest));
        try {
            GetTransactionsResponse getTransactionsResponse = accounts.getTransactions(getTransactionsRequest);
            logger.warn(IN, JsonFormat.printer().print(getTransactionsResponse));
        } catch (Exception e) {
            logger.error("GetTransactions failed", e);
        }
    }

}
