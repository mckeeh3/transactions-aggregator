package io.aggregator.view;

import io.aggregator.TimeTo;
import io.aggregator.entity.DayEntity;

class DailyTotalsEventHandler {

  static DailyTotalsModel.DailyTotal handle(DailyTotalsModel.DailyTotal state, DayEntity.DayCreated dayCreated) {
    return state
        .toBuilder()
        .setMerchantId(dayCreated.getMerchantId())
        .setEpochDay(dayCreated.getEpochDay())
        .setDay(TimeTo.dayTimeStampFromDay(dayCreated.getEpochDay()))
        .build();
  }

  static DailyTotalsModel.DailyTotal handle(DailyTotalsModel.DailyTotal state, DayEntity.DayAggregated dayAggregated) {
    return state
        .toBuilder()
        .setMerchantId(dayAggregated.getMerchantId())
        .setEpochDay(dayAggregated.getEpochDay())
        .setTransactionTotalAmount(dayAggregated.getTransactionTotalAmount())
        .setTransactionCount(dayAggregated.getTransactionCount())
        .setLastUpdateTimestamp(dayAggregated.getLastUpdateTimestamp())
        .build();
  }
}
