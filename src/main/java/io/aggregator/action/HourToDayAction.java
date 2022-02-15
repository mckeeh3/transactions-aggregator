package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.DayApi;
import io.aggregator.entity.HourEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class HourToDayAction extends AbstractHourToDayAction {

  public HourToDayAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onHourCreated(HourEntity.HourCreated hourCreated) {
    return effects().forward(components().day().addHour(
        DayApi.AddHourCommand
            .newBuilder()
            .setMerchantId(hourCreated.getMerchantId())
            .setEpochDay(TimeTo.fromEpochHour(hourCreated.getEpochHour()).toEpochDay())
            .setEpochHour(hourCreated.getEpochHour())
            .build()));
  }

  @Override
  public Effect<Empty> onHourAggregated(HourEntity.HourAggregated hourAggregated) {
    return effects().forward(components().day().hourAggregation(
        DayApi.HourAggregationCommand
            .newBuilder()
            .setMerchantId(hourAggregated.getMerchantId())
            .setEpochDay(TimeTo.fromEpochHour(hourAggregated.getEpochHour()).toEpochDay())
            .setEpochHour(hourAggregated.getEpochHour())
            .setTransactionTotalAmount(hourAggregated.getTransactionTotalAmount())
            .setTransactionCount(hourAggregated.getTransactionCount())
            .setLastUpdateTimestamp(hourAggregated.getLastUpdateTimestamp())
            .setAggregateRequestTimestamp(hourAggregated.getAggregateRequestTimestamp())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}
