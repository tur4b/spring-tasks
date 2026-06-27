package org.example.common.logging;

public final class TransactionContext {

    public static final String TRANSACTION_HEADER = "X-Transaction-Id";
    public static final String TRANSACTION_MDC_KEY = "transactionId";

    private TransactionContext() {
    }
}
