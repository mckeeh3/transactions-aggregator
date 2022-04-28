package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.MinuteApi;
import io.aggregator.entity.SecondEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondToMinuteAction extends AbstractSecondToMinuteAction {
  static final Logger log = LoggerFactory.getLogger(SecondToMinuteAction.class);

  public SecondToMinuteAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onSecondActivated(SecondEntity.SecondActivated event) {
    log.info(Thread.currentThread().getName() + " - ON EVENT: SecondActivated");

    return effects().forward(components().minute().addSecond(
        MinuteApi.AddSecondCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochMinute(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochMinute())
            .setEpochSecond(event.getEpochSecond())
            .build()));
  }

  @Override
  public Effect<Empty> onSecondAggregated(SecondEntity.SecondAggregated event) {
    log.info(Thread.currentThread().getName() + " - ON EVENT: SecondAggregated");

    return effects().forward(components().minute().secondAggregation(
        MinuteApi.SecondAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochMinute(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochMinute())
            .setEpochSecond(event.getEpochSecond())
            // TODO
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
