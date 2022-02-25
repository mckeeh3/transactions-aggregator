package io.aggregator.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.SecondApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

/** An event sourced entity. */
public class Second extends AbstractSecond {
  static final Logger log = LoggerFactory.getLogger(Second.class);

  public Second(EventSourcedEntityContext context) {
  }

  @Override
  public SecondEntity.SecondState emptyState() {
    return SecondEntity.SecondState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> addSubSecond(SecondEntity.SecondState state, SecondApi.AddSubSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> aggregateSecond(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> subSecondAggregation(SecondEntity.SecondState state, SecondApi.SubSecondAggregationCommand command) {
    return handle(state, command);
  }

  @Override
  public SecondEntity.SecondState secondCreated(SecondEntity.SecondState state, SecondEntity.SecondCreated event) {
    return handle(state, event);
  }

  @Override
  public SecondEntity.SecondState subSecondAdded(SecondEntity.SecondState state, SecondEntity.SubSecondAdded event) {
    return handle(state, event);
  }

  @Override
  public SecondEntity.SecondState secondAggregationRequested(SecondEntity.SecondState state, SecondEntity.SecondAggregationRequested event) {
    return handle(state, event);
  }

  @Override
  public SecondEntity.SecondState secondAggregated(SecondEntity.SecondState state, SecondEntity.SecondAggregated event) {
    return handle(state, event);
  }

  @Override
  public SecondEntity.SecondState activeSubSecondAggregated(SecondEntity.SecondState state, SecondEntity.ActiveSubSecondAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.AddSubSecondCommand command) {
    log.info("state: {}\nAddSubSecondCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    log.info("state: {}\nAggregateSecondCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.SubSecondAggregationCommand command) {
    log.info("state: {}\nSubSecondAggregationCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SecondCreated event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantId())
        .setEpochSecond(event.getEpochSecond())
        .setEpochHour(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochDay())
        .build();
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SubSecondAdded event) {
    var alreadyAdded = state.getActiveSubSecondsList().stream()
        .anyMatch(activeSecond -> activeSecond.getEpochSubSecond() == event.getEpochSubSecond());

    if (alreadyAdded) {
      return state;
    } else {
      return state.toBuilder()
          .addActiveSubSeconds(
              SecondEntity.ActiveSubSecond
                  .newBuilder()
                  .setEpochSubSecond(event.getEpochSubSecond())
                  .build())
          .build();
    }
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SecondAggregationRequested event) {
    var activeAlreadyMoved = state.getAggregateSecondsList().stream()
        .anyMatch(aggregatedSecond -> aggregatedSecond.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp()));

    if (activeAlreadyMoved) {
      return state;
    } else {
      return moveActiveSubSecondsToAggregateSecond(state, event);
    }
  }

  static SecondEntity.SecondState moveActiveSubSecondsToAggregateSecond(SecondEntity.SecondState state, SecondEntity.SecondAggregationRequested event) {
    return state.toBuilder()
        .clearActiveSubSeconds()
        .addAggregateSeconds(
            SecondEntity.AggregateSecond
                .newBuilder()
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .addAllActiveSubSeconds(state.getActiveSubSecondsList())
                .build())
        .build();
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SecondAggregated event) {
    return state; // non-state change event
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.ActiveSubSecondAggregated event) {
    return state.toBuilder()
        .clearAggregateSeconds()
        .addAllAggregateSeconds(updateAggregateSeconds(state, event))
        .build();
  }

  static List<SecondEntity.AggregateSecond> updateAggregateSeconds(SecondEntity.SecondState state, SecondEntity.ActiveSubSecondAggregated event) {
    return state.getAggregateSecondsList().stream()
        .map(aggregatedSecond -> {
          if (aggregatedSecond.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp())) {
            return aggregatedSecond.toBuilder()
                .clearActiveSubSeconds()
                .addAllActiveSubSeconds(updateActiveSubSeconds(event, aggregatedSecond))
                .build();
          } else {
            return aggregatedSecond;
          }
        })
        .toList();
  }

  static List<SecondEntity.ActiveSubSecond> updateActiveSubSeconds(SecondEntity.ActiveSubSecondAggregated event, SecondEntity.AggregateSecond aggregateSubSecond) {
    return aggregateSubSecond.getActiveSubSecondsList().stream()
        .map(activeSubSecond -> {
          if (activeSubSecond.getEpochSubSecond() == event.getEpochSubSecond()) {
            return activeSubSecond
                .toBuilder()
                .setTransactionTotalAmount(event.getTransactionTotalAmount())
                .setTransactionCount(event.getTransactionCount())
                .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .build();
          } else {
            return activeSubSecond;
          }
        })
        .toList();
  }

  static List<?> eventsFor(SecondEntity.SecondState state, SecondApi.AddSubSecondCommand command) {
    var subSecondAdded = SecondEntity.SubSecondAdded
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochSubSecond(command.getEpochSubSecond())
        .build();

    if (state.getMerchantId().isEmpty()) {
      var secondCreated = SecondEntity.SecondCreated
          .newBuilder()
          .setMerchantId(command.getMerchantId())
          .setEpochSecond(command.getEpochSecond())
          .build();

      return List.of(secondCreated, subSecondAdded);
    } else {
      return List.of(subSecondAdded);
    }
  }

  static SecondEntity.SecondAggregationRequested eventFor(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    return SecondEntity.SecondAggregationRequested
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochSecond(command.getEpochSecond())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .addAllEpochSubSeconds(
            state.getActiveSubSecondsList().stream()
                .map(activeSubSecond -> activeSubSecond.getEpochSubSecond())
                .toList())
        .build();
  }

  static List<?> eventsFor(SecondEntity.SecondState state, SecondApi.SubSecondAggregationCommand command) {
    var aggregateSecond = state.getAggregateSecondsList().stream()
        .filter(aggSec -> aggSec.getAggregateRequestTimestamp().equals(command.getAggregateRequestTimestamp()))
        .findFirst()
        .orElse(
            SecondEntity.AggregateSecond
                .newBuilder()
                .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
                .addAllActiveSubSeconds(state.getActiveSubSecondsList())
                .build());
    var aggregateRequestTimestamp = aggregateSecond.getAggregateRequestTimestamp();
    var activeSubSeconds = aggregateSecond.getActiveSubSecondsList();

    var alreadyInList = activeSubSeconds.stream()
        .anyMatch(activeSubSecond -> activeSubSecond.getEpochSubSecond() == command.getEpochSubSecond());

    if (!alreadyInList) {
      activeSubSeconds = new ArrayList<>(activeSubSeconds);
      activeSubSeconds.add(
          SecondEntity.ActiveSubSecond
              .newBuilder()
              .setEpochSubSecond(command.getEpochSubSecond())
              .build());
      activeSubSeconds = Collections.unmodifiableList(activeSubSeconds);
    }

    activeSubSeconds = updateActiveSubSeconds(command, activeSubSeconds);

    var allSecondsAggregated = activeSubSeconds.stream()
        .allMatch(activeSecond -> activeSecond.getAggregateRequestTimestamp().equals(aggregateRequestTimestamp));

    if (allSecondsAggregated) {
      return List.of(toSecondAggregated(command, activeSubSeconds), toActiveSubSecondAggregated(command));
    } else {
      return List.of(toActiveSubSecondAggregated(command));
    }
  }

  static List<SecondEntity.ActiveSubSecond> updateActiveSubSeconds(SecondApi.SubSecondAggregationCommand command, List<SecondEntity.ActiveSubSecond> activeSeconds) {
    return activeSeconds.stream()
        .map(activeSecond -> {
          if (activeSecond.getEpochSubSecond() == command.getEpochSubSecond()) {
            return activeSecond
                .toBuilder()
                .setTransactionTotalAmount(command.getTransactionTotalAmount())
                .setTransactionCount(command.getTransactionCount())
                .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
                .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
                .build();
          } else {
            return activeSecond;
          }
        })
        .toList();
  }

  static SecondEntity.ActiveSubSecondAggregated toActiveSubSecondAggregated(SecondApi.SubSecondAggregationCommand command) {
    return SecondEntity.ActiveSubSecondAggregated
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochSubSecond(command.getEpochSubSecond())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .build();
  }

  static SecondEntity.SecondAggregated toSecondAggregated(SecondApi.SubSecondAggregationCommand command, List<SecondEntity.ActiveSubSecond> activeSeconds) {
    var transactionTotalAmount = activeSeconds.stream()
        .reduce(0.0, (amount, activeSecond) -> amount + activeSecond.getTransactionTotalAmount(), Double::sum);

    var transactionCount = activeSeconds.stream()
        .reduce(0, (count, activeSecond) -> count + activeSecond.getTransactionCount(), Integer::sum);

    var lastUpdateTimestamp = activeSeconds.stream()
        .map(activeSecond -> activeSecond.getLastUpdateTimestamp())
        .max(TimeTo.comparator())
        .get();

    return SecondEntity.SecondAggregated
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochSecond(command.getEpochSecond())
        .setTransactionTotalAmount(transactionTotalAmount)
        .setTransactionCount(transactionCount)
        .setLastUpdateTimestamp(lastUpdateTimestamp)
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .build();
  }
}
