package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.HourApi;
import io.aggregator.entity.MinuteEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MinuteToHourAction extends AbstractMinuteToHourAction {
  static final Logger log = LoggerFactory.getLogger(MinuteToHourAction.class);

  public MinuteToHourAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onMinuteActivated(MinuteEntity.MinuteActivated event) {
    log.info(Thread.currentThread().getName() + " - ON EVENT: MinuteActivated");

    return effects().forward(components().hour().addMinute(
        HourApi.AddMinuteCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochHour(TimeTo.fromEpochMinute(event.getEpochMinute()).toEpochHour())
            .setEpochMinute(event.getEpochMinute())
            .build()));
  }

  @Override
  public Effect<Empty> onMinuteAggregated(MinuteEntity.MinuteAggregated event) {
    log.info(Thread.currentThread().getName() + " - ON EVENT: MinuteAggregated");

    return effects().forward(components().hour().minuteAggregation(
        HourApi.MinuteAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochHour(TimeTo.fromEpochMinute(event.getEpochMinute()).toEpochHour())
            .setEpochMinute(event.getEpochMinute())
            .addAllMoneyMovements(event.getMoneyMovementsList())
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
