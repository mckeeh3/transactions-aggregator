package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.HourApi;
import io.aggregator.entity.MinuteEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MinuteToHourAction extends AbstractMinuteToHourAction {

  public MinuteToHourAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onMinuteCreated(MinuteEntity.MinuteCreated minuteCreated) {
    return effects().forward(components().hour().addMinute(
        HourApi.AddMinuteCommand.newBuilder()
            .setMerchantId(minuteCreated.getMerchantId())
            .setEpochHour(TimeTo.epochHourFor(minuteCreated.getEpochMinute()))
            .setEpochMinute(minuteCreated.getEpochMinute())
            .build()));
  }

  @Override
  public Effect<Empty> onMinuteAggregated(MinuteEntity.MinuteAggregated minuteAggregated) {
    return effects().forward(components().hour().minuteAggregation(
        HourApi.MinuteAggregationCommand.newBuilder()
            .setMerchantId(minuteAggregated.getMerchantId())
            .setEpochHour(TimeTo.epochHourFor(minuteAggregated.getEpochMinute()))
            .setEpochMinute(minuteAggregated.getEpochMinute())
            .setTransactionTotalAmount(minuteAggregated.getTransactionTotalAmount())
            .setTransactionCount(minuteAggregated.getTransactionCount())
            .setLastUpdateTimestamp(minuteAggregated.getLastUpdateTimestamp())
            .setAggregateRequestTimestamp(minuteAggregated.getAggregateRequestTimestamp())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}
