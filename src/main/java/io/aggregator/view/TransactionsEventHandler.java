package io.aggregator.view;

import io.aggregator.entity.TransactionEntity;

public class TransactionsEventHandler {

  public static TransactionModel.Transaction handle(TransactionModel.Transaction state, TransactionEntity.TransactionCreated event) {
    return state.toBuilder()
//        .setTransactionId(event.getTransactionKey().getTransactionId())
        .setTransactionId("")
//        .setServiceCode(event.getTransactionKey().getServiceCode())
        .setServiceCode("")
//        .setAccountFrom(event.getTransactionKey().getAccountFrom())
        .setAccountFrom("")
//        .setAccountTo(event.getTransactionKey().getAccountTo())
        .setAccountTo("")
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
