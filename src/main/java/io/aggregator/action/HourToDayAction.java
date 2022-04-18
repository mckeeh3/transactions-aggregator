package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.DayApi;
import io.aggregator.entity.HourEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class HourToDayAction extends AbstractHourToDayAction {
  static final Logger log = LoggerFactory.getLogger(HourToDayAction.class);

  public HourToDayAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onHourActivated(HourEntity.HourActivated event) {
    log.info(Thread.currentThread().getName() + " - ON EVENT: HourActivated");

    return effects().forward(components().day().addHour(
        DayApi.AddHourCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochDay(TimeTo.fromEpochHour(event.getEpochHour()).toEpochDay())
            .setEpochHour(event.getEpochHour())
            .build()));
  }

  @Override
  public Effect<Empty> onHourAggregated(HourEntity.HourAggregated event) {
    log.info(Thread.currentThread().getName() + " - ON EVENT: HourAggregated");

    return effects().forward(components().day().hourAggregation(
        DayApi.HourAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochDay(TimeTo.fromEpochHour(event.getEpochHour()).toEpochDay())
            .setEpochHour(event.getEpochHour())
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
