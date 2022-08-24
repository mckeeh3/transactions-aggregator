package io.aggregator.action;

import kalix.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.api.MerchantApi;
import io.aggregator.entity.DayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/day_to_merchant_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class DayToMerchantAction extends AbstractDayToMerchantAction {
  static final Logger log = LoggerFactory.getLogger(DayToMerchantAction.class);

  public DayToMerchantAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onDayActivated(DayEntity.DayActivated event) {
    log.info(Thread.currentThread().getName() + " - ON EVENT: DayActivated");

    return effects().forward(components().merchant().activateDay(
        MerchantApi.ActivateDayCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochDay(event.getEpochDay())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}
