package io.aggregator.view;

import io.aggregator.entity.PaymentEntity.PaymentAggregated;
import io.aggregator.view.MerchantPaymentsModel.MerchantPayment;

class MerchantPaymentsEventHandler {

  static MerchantPayment handle(MerchantPayment state, PaymentAggregated event) {
    var amount = state.getTransactionTotalAmount() + event.getTransactionTotalAmount();
    var count = state.getTransactionCount() + event.getTransactionCount();

    return state.toBuilder()
        .setMerchantId(event.getMerchantKey().getMerchantId())
        .setServiceCode(event.getMerchantKey().getServiceCode())
        .setAccountFrom(event.getMerchantKey().getAccountFrom())
        .setAccountTo(event.getMerchantKey().getAccountTo())
        .setPaymentId(event.getPaymentId())
        .setTransactionTotalAmount(amount)
        .setTransactionCount(count)
        .setPaymentTimestamp(event.getAggregateRequestTimestamp())
        .build();
  }
}
