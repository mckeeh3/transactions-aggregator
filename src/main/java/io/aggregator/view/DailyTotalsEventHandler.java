package io.aggregator.view;

import io.aggregator.TimeTo;
import io.aggregator.entity.DayEntity;
import io.aggregator.entity.TransactionMerchantKey;

class DailyTotalsEventHandler {

  static DailyTotalsModel.DailyTotal handle(DailyTotalsModel.DailyTotal state, DayEntity.DayActivated event) {
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
        .setEpochDay(event.getEpochDay())
        .setDay(TimeTo.fromEpochDay(event.getEpochDay()).toTimestamp())
        .build();
  }

  static DailyTotalsModel.DailyTotal handle(DailyTotalsModel.DailyTotal state, DayEntity.DayAggregated event) {
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
        .setEpochDay(event.getEpochDay())
        .setTransactionTotalAmount(event.getTransactionTotalAmount())
        .setTransactionCount(event.getTransactionCount())
        .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
        .setPaymentId(event.getPaymentId())
        .setAggregationCompletedTimestamp(event.getAggregationCompletedTimestamp())
        .build();
  }
}
