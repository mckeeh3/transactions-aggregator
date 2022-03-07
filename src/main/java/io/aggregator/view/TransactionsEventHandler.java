package io.aggregator.view;

import io.aggregator.entity.TransactionEntity;

public class TransactionsEventHandler {

  public static TransactionModel.Transaction handle(TransactionModel.Transaction state, TransactionEntity.TransactionCreated event) {
    return state.toBuilder()
        .setTransactionId(event.getTransactionKey().getTransactionId())
        .setServiceCode(event.getTransactionKey().getServiceCode())
        .setAccountFrom(event.getTransactionKey().getAccountFrom())
        .setAccountTo(event.getTransactionKey().getAccountTo())
        .setMerchantId(event.getMerchantId())
        .setShopId(event.getShopId())
        .setTransactionAmount(event.getTransactionAmount())
        .setTransactionTimestamp(event.getTransactionTimestamp())
        .build();
  }

  public static TransactionModel.Transaction handle(TransactionModel.Transaction state, TransactionEntity.PaymentAdded event) {
    return state.toBuilder()
        .setPaymentId(event.getPaymentId())
        .build();
  }
}
