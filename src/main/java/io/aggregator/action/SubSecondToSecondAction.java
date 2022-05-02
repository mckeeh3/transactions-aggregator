package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.SecondApi;
import io.aggregator.entity.SubSecondEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SubSecondToSecondAction extends AbstractSubSecondToSecondAction {
  static final Logger log = LoggerFactory.getLogger(SubSecondToSecondAction.class);

  public SubSecondToSecondAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onSubSecondActivated(SubSecondEntity.SubSecondActivated event) {
    log.debug(Thread.currentThread().getName() + " - SubSecondActivated: {}", event);
    log.info(Thread.currentThread().getName() + " - ON EVENT: SubSecondActivated");

    return effects().forward(components().second().addSubSecond(
        SecondApi.AddSubSecondCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochSecond(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochSecond())
            .setEpochSubSecond(event.getEpochSubSecond())
            .build()));
  }

  @Override
  public Effect<Empty> onSubSecondAggregated(SubSecondEntity.SubSecondAggregated event) {
    log.debug(Thread.currentThread().getName() + " - SubSecondAggregated: {}", event);
    log.info(Thread.currentThread().getName() + " - ON EVENT: SubSecondAggregated");

    return effects().forward(components().second().subSecondAggregation(
        SecondApi.SubSecondAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochSecond(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochSecond())
            .setEpochSubSecond(event.getEpochSubSecond())
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
