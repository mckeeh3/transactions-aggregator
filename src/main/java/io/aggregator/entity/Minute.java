package io.aggregator.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.api.MinuteApi;
import io.aggregator.entity.MinuteEntity.ActiveSecondAggregated;
import io.aggregator.entity.MinuteEntity.MinuteState;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

/** An event sourced entity. */
public class Minute extends AbstractMinute {
  static final Logger log = LoggerFactory.getLogger(Minute.class);

  public Minute(EventSourcedEntityContext context) {
  }

  @Override
  public MinuteEntity.MinuteState emptyState() {
    return MinuteEntity.MinuteState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> addSecond(MinuteEntity.MinuteState state, MinuteApi.AddSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> aggregateMinute(MinuteEntity.MinuteState state, MinuteApi.AggregateMinuteCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> secondAggregation(MinuteEntity.MinuteState state, MinuteApi.SecondAggregationCommand command) {
    return handle(state, command);
  }

  @Override
  public MinuteEntity.MinuteState minuteCreated(MinuteEntity.MinuteState state, MinuteEntity.MinuteCreated event) {
    return handle(state, event);
  }

  @Override
  public MinuteEntity.MinuteState secondAdded(MinuteEntity.MinuteState state, MinuteEntity.SecondAdded event) {
    return handle(state, event);
  }

  @Override
  public MinuteEntity.MinuteState minuteAggregated(MinuteEntity.MinuteState state, MinuteEntity.MinuteAggregated event) {
    return handle(state, event);
  }

  @Override
  public MinuteState activeSecondAggregated(MinuteState state, ActiveSecondAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(MinuteEntity.MinuteState state, MinuteApi.AddSecondCommand command) {
    log.info("state: {}\nAddSecondCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(MinuteEntity.MinuteState state, MinuteApi.AggregateMinuteCommand command) {
    log.info("state: {}\nAggregateMinuteCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(MinuteEntity.MinuteState state, MinuteApi.SecondAggregationCommand command) {
    log.info("state: {}\nSecondAggregationCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.MinuteCreated event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantId())
        .setEpochMinute(event.getEpochMinute())
        .build();
  }

  private MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.SecondAdded event) {
    var alreadyAdded = state.getActiveSecondsList().stream()
        .anyMatch(activeSecond -> activeSecond.getEpochSecond() == event.getEpochSecond());

    if (alreadyAdded) {
      return state;
    } else {
      return state.toBuilder()
          .addActiveSeconds(
              MinuteEntity.ActiveSecond
                  .newBuilder()
                  .setEpochSecond(event.getEpochSecond())
                  .build())
          .build();
    }
  }

  private MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.MinuteAggregated event) {
    return state; // no state change event
  }

  private MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.ActiveSecondAggregated event) {
    return state.toBuilder()
        .clearActiveSeconds()
        .addAllActiveSeconds(state.getActiveSecondsList().stream()
            .map(activeSecond -> {
              if (activeSecond.getEpochSecond() == event.getEpochSecond()) {
                return activeSecond
                    .toBuilder()
                    .setTransactionTotalAmount(event.getTransactionTotalAmount())
                    .setTransactionCount(event.getTransactionCount())
                    .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
                    .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                    .build();
              } else {
                return activeSecond;
              }
            })
            .collect(Collectors.toList()))
        .build();
  }

  private List<?> eventsFor(MinuteEntity.MinuteState state, MinuteApi.AddSecondCommand command) {
    var secondAdded = MinuteEntity.SecondAdded
        .newBuilder()
        .build();

    if (state.getMerchantId().isEmpty()) {
      var minuteCreated = MinuteEntity.MinuteCreated
          .newBuilder()
          .setMerchantId(command.getMerchantId())
          .build();

      return List.of(minuteCreated, secondAdded);
    } else {
      return List.of(secondAdded);
    }
  }

  private MinuteEntity.MinuteAggregationRequested eventFor(MinuteEntity.MinuteState state, MinuteApi.AggregateMinuteCommand command) {
    return MinuteEntity.MinuteAggregationRequested
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochMinute(command.getEpochMinute())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .addAllEpochSeconds(
            state.getActiveSecondsList().stream()
                .map(activeSecond -> activeSecond.getEpochSecond())
                .collect(Collectors.toList()))
        .build();
  }

  private List<?> eventsFor(MinuteEntity.MinuteState state, MinuteApi.SecondAggregationCommand command) {
    var alreadyInList = state.getActiveSecondsList().stream()
        .anyMatch(activeSecond -> activeSecond.getEpochSecond() == command.getEpochSecond());

    if (!alreadyInList) {
      state = state.toBuilder()
          .addActiveSeconds(
              MinuteEntity.ActiveSecond.newBuilder()
                  .setEpochSecond(command.getEpochSecond())
                  .setTransactionTotalAmount(command.getTransactionTotalAmount())
                  .setTransactionCount(command.getTransactionCount())
                  .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
                  .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
                  .build())
          .build();
    }

    final var aggregateRequestTimestamp = state.getAggregateRequestTimestamp();

    var allAggregated = state.getActiveSecondsList().stream()
        .allMatch(activeSecond -> activeSecond.getAggregateRequestTimestamp().equals(aggregateRequestTimestamp));

    var activeSecondAggregated = MinuteEntity.ActiveSecondAggregated
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochSecond(command.getEpochSecond())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .build();

    if (allAggregated) {
      var minuteAggregated = MinuteEntity.MinuteAggregated
          .newBuilder()
          .setMerchantId(command.getMerchantId())
          .setEpochMinute(command.getEpochMinute())
          .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
          .build();

      return List.of(minuteAggregated, activeSecondAggregated);
    } else {
      return List.of(activeSecondAggregated);
    }
  }
}
