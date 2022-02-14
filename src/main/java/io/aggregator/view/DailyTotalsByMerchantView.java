package io.aggregator.view;

import com.akkaserverless.javasdk.view.View;
import com.akkaserverless.javasdk.view.ViewContext;
import com.google.protobuf.Any;

import io.aggregator.TimeTo;
import io.aggregator.entity.DayEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class DailyTotalsByMerchantView extends AbstractDailyTotalsByMerchantView {

  public DailyTotalsByMerchantView(ViewContext context) {
  }

  @Override
  public DailyTotalsModel.DailyTotal emptyState() {
    return DailyTotalsModel.DailyTotal.getDefaultInstance();
  }

  @Override
  public UpdateEffect<DailyTotalsModel.DailyTotal> onDayCreated(DailyTotalsModel.DailyTotal state, DayEntity.DayCreated dayCreated) {
    return effects()
        .updateState(
            state.toBuilder()
                .setMerchantId(dayCreated.getMerchantId())
                .setEpochDay(dayCreated.getEpochDay())
                .setDay(TimeTo.dayTimeStampFromDay(dayCreated.getEpochDay()))
                .build());
  }

  @Override
  public View.UpdateEffect<DailyTotalsModel.DailyTotal> onDayAggregated(DailyTotalsModel.DailyTotal state, DayEntity.DayAggregated dayAggregated) {
    return effects()
        .updateState(
            state.toBuilder()
                .setMerchantId(dayAggregated.getMerchantId())
                .setEpochDay(dayAggregated.getEpochDay())
                .setTransactionTotalAmount(dayAggregated.getTransactionTotalAmount())
                .setTransactionCount(dayAggregated.getTransactionCount())
                .setLastUpdateTimestamp(dayAggregated.getLastUpdateTimestamp())
                .build());
  }

  @Override
  public View.UpdateEffect<DailyTotalsModel.DailyTotal> ignoreOtherEvents(DailyTotalsModel.DailyTotal state, Any any) {
    return effects().ignore();
  }
}
