package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.entity.SubSecondEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

/** An action. */
public class SubSecondToSecondAction extends AbstractSubSecondToSecondAction {

  public SubSecondToSecondAction(ActionCreationContext creationContext) {}

  /** Handler for "OnSubSecondCreated". */
  @Override
  public Effect<Empty> onSubSecondCreated(SubSecondEntity.SubSecondCreated subSecondCreated) {
    throw new RuntimeException("The command handler for `OnSubSecondCreated` is not implemented, yet");
  }
  /** Handler for "OnSubSecondAggregated". */
  @Override
  public Effect<Empty> onSubSecondAggregated(SubSecondEntity.SubSecondAggregated subSecondAggregated) {
    throw new RuntimeException("The command handler for `OnSubSecondAggregated` is not implemented, yet");
  }
  /** Handler for "IgnoreOtherEvents". */
  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    throw new RuntimeException("The command handler for `IgnoreOtherEvents` is not implemented, yet");
  }
}
