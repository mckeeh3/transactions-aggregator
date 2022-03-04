package io.aggregator.view;

import io.aggregator.entity.PaymentEntity;
import io.aggregator.entity.TransactionMerchantKey;
import io.aggregator.view.MerchantPaymentsModel.MerchantPayment;

class MerchantPaymentsEventHandler {

  static MerchantPayment handle(MerchantPayment state, PaymentEntity.PaymentAggregated event) {
    return state
        .toBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(event.getMerchantKey().getMerchantId())
                .setServiceCode(event.getMerchantKey().getServiceCode())
                .setAccountFrom(event.getMerchantKey().getAccountFrom())
                .setAccountTo(event.getMerchantKey().getAccountTo())
                .build())
        .setPaymentId(event.getPaymentId())
        .setTransactionTotalAmount(event.getTransactionTotalAmount())
        .setTransactionCount(event.getTransactionCount())
        .setPaymentTimestamp(event.getAggregateRequestTimestamp())
        .build();
  }
}
