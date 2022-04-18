package io.aggregator.view;

import io.aggregator.entity.PaymentEntity;
import io.aggregator.view.MerchantPaymentsModel.MerchantPayment;

class MerchantPaymentsEventHandler {

  static MerchantPayment handle(MerchantPayment state, PaymentEntity.PaymentAggregated event) {
    var amount = state.getTransactionTotalAmount() + event.getTransactionTotalAmount();
    var count = state.getTransactionCount() + event.getTransactionCount();

    return state.toBuilder()
        .setMerchantId(event.getMerchantKey().getMerchantId())
        .setPaymentId(event.getPaymentId())
        .setTransactionTotalAmount(amount)
        .setTransactionCount(count)
        .setPaymentTimestamp(event.getAggregateRequestTimestamp())
        .build();
  }
}
