package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.entity.SecondEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

/** An action. */
public class SecondToSubSecondAction extends AbstractSecondToSubSecondAction {

  public SecondToSubSecondAction(ActionCreationContext creationContext) {}

  /** Handler for "OnSecondAggregationRequested". */
  @Override
  public Effect<Empty> onSecondAggregationRequested(SecondEntity.SecondAggregationRequested secondAggregationRequested) {
    throw new RuntimeException("The command handler for `OnSecondAggregationRequested` is not implemented, yet");
  }
  /** Handler for "IgnoreOtherEvents". */
  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    throw new RuntimeException("The command handler for `IgnoreOtherEvents` is not implemented, yet");
  }
}
