package io.aggregator.action;

import kalix.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.api.PaymentApi;
import io.aggregator.entity.DayEntity;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/day_to_payment_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class DayToPaymentAction extends AbstractDayToPaymentAction {
  static Logger log = LoggerFactory.getLogger(DayToPaymentAction.class);

  public DayToPaymentAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onDayAggregated(DayEntity.DayAggregated event) {
    log.debug("{} - ON EVENT: DayAggregated: {}", Thread.currentThread().getName(), event);

    return effects().forward(components().payment().dayAggregation(
        PaymentApi.DayAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setPaymentId(event.getPaymentId())
            .setEpochDay(event.getEpochDay())
            .addAllMoneyMovements(event.getMoneyMovementsList())
            .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
            .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}
