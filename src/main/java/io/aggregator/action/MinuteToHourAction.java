package io.aggregator.action;

import kalix.javasdk.action.ActionCreationContext;
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
  public Effect<Empty> onMinuteActivated(MinuteEntity.MinuteActivated event) {
    return effects().forward(components().hour().addMinute(
        HourApi.AddMinuteCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochHour(TimeTo.fromEpochMinute(event.getEpochMinute()).toEpochHour())
            .setEpochMinute(event.getEpochMinute())
            .build()));
  }

  @Override
  public Effect<Empty> onMinuteAggregated(MinuteEntity.MinuteAggregated event) {
    return effects().forward(components().hour().minuteAggregation(
        HourApi.MinuteAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochHour(TimeTo.fromEpochMinute(event.getEpochMinute()).toEpochHour())
            .setEpochMinute(event.getEpochMinute())
            .setTransactionTotalAmount(event.getTransactionTotalAmount())
            .setTransactionCount(event.getTransactionCount())
            .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
            .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
            .setPaymentId(event.getPaymentId())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}
