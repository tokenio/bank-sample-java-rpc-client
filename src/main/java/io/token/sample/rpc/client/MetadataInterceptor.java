package io.token.sample.rpc.client;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataInterceptor implements ClientInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(MetadataInterceptor.class);

    private final Metadata.Key<String> tokenBankIdKey = Metadata.Key.of("token-bank-id", ASCII_STRING_MARSHALLER);
    private final String bankId;

    public MetadataInterceptor(String bankId) {
        this.bankId = bankId;
    }

    /**
     * Intercept {@link ClientCall} creation by the {@code next} {@link Channel}.
     *
     * <p>Many variations of interception are possible. Complex implementations may return a wrapper
     * around the result of {@code next.newCall()}, whereas a simpler implementation may just modify
     * the header metadata prior to returning the result of {@code next.newCall()}.
     *
     * <p>{@code next.newCall()} <strong>must not</strong> be called under a different {@link Context}
     * other than the current {@code Context}. The outcome of such usage is undefined and may cause
     * memory leak due to unbounded chain of {@code Context}s.
     *
     * @param method      the remote method to be called.
     * @param callOptions the runtime options to be applied to this call.
     * @param next        the channel which is being intercepted.
     * @return the call object for the remote operation, never {@code null}.
     */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(tokenBankIdKey, bankId);
                super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {

                        /* If you don't need to receive headers from the server,
                         * you can use {@link io.grpc.stub.MetadataUtils#attachHeaders}
                         * directly to send header
                         */
                        logger.debug("Headers received from server: {}", headers);
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }
}
