package io.aggregator.entity;

import java.util.*;
import java.util.stream.Collectors;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import io.aggregator.service.RuleService;
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
  public SecondEntity.SecondState secondActivated(SecondEntity.SecondState state, SecondEntity.SecondActivated event) {
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
    log.debug(Thread.currentThread().getName() + " - state: {}\nAddSubSecondCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: AddSubSecondCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    log.debug("state: {}\nAggregateSecondCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: AggregateSecondCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.SubSecondAggregationCommand command) {
    log.debug("state: {}\nSubSecondAggregationCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: SubSecondAggregationCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SecondActivated event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: SecondActivated");

    return state.toBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(event.getMerchantKey().getMerchantId())
                .build())
        .setEpochSecond(event.getEpochSecond())
        .setEpochHour(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochDay())
        .build();
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SubSecondAdded event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: SubSecondAdded");

    var alreadyActivated = state.getActiveSubSecondsList().stream()
        .anyMatch(activeSecond -> activeSecond.getEpochSubSecond() == event.getEpochSubSecond());

    if (alreadyActivated) {
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
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: SecondAggregationRequested");

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
                .setPaymentId(event.getPaymentId())
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
                .addAllMoneyMovements(event.getMoneyMovementsList())
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
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochSubSecond(command.getEpochSubSecond())
        .build();

    if (state.getActiveSubSecondsCount() == 0) {
      var secondActivated = SecondEntity.SecondActivated
          .newBuilder()
          .setMerchantKey(
              TransactionMerchantKey.MerchantKey
                  .newBuilder()
                  .setMerchantId(command.getMerchantId())
                  .build())
          .setEpochSecond(command.getEpochSecond())
          .build();

      return List.of(secondActivated, subSecondAdded);
    } else {
      return List.of(subSecondAdded);
    }
  }

  static List<?> eventsFor(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    if (state.getActiveSubSecondsCount() == 0) {
      var timestamp = command.getAggregateRequestTimestamp();
      return List.of(
          SecondEntity.SecondAggregated
              .newBuilder()
              .setMerchantKey(
                  TransactionMerchantKey.MerchantKey
                      .newBuilder()
                      .setMerchantId(command.getMerchantId())
                      .build())
              .setEpochSecond(command.getEpochSecond())
              .setLastUpdateTimestamp(timestamp)
              .setAggregateRequestTimestamp(timestamp)
              .setPaymentId(command.getPaymentId())
              .build());
    } else {
      return List.of(
          SecondEntity.SecondAggregationRequested
              .newBuilder()
              .setMerchantKey(
                  TransactionMerchantKey.MerchantKey
                      .newBuilder()
                      .setMerchantId(command.getMerchantId())
                      .build())
              .setEpochSecond(command.getEpochSecond())
              .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
              .setPaymentId(command.getPaymentId())
              .addAllEpochSubSeconds(
                  state.getActiveSubSecondsList().stream()
                      .map(SecondEntity.ActiveSubSecond::getEpochSubSecond)
                      .toList())
              .build());
    }
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
                .addAllMoneyMovements(command.getMoneyMovementsList())
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
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochSubSecond(command.getEpochSubSecond())
        .addAllMoneyMovements(command.getMoneyMovementsList())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
  }

  static SecondEntity.SecondAggregated toSecondAggregated(SecondApi.SubSecondAggregationCommand command, List<SecondEntity.ActiveSubSecond> activeSubSeconds) {
    var lastUpdateTimestamp = activeSubSeconds.stream()
        .map(SecondEntity.ActiveSubSecond::getLastUpdateTimestamp)
        .max(TimeTo.comparator())
        .get();

    List<TransactionMerchantKey.MoneyMovement> summarisedMoneyMovements = activeSubSeconds.stream()
        .flatMap(activeSubSecond -> activeSubSecond.getMoneyMovementsList().stream())
        .collect(Collectors.toList());

    return SecondEntity.SecondAggregated
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochSecond(command.getEpochSecond())
        .addAllMoneyMovements(RuleService.mergeMoneyMovements(summarisedMoneyMovements))
        .setLastUpdateTimestamp(lastUpdateTimestamp)
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
  }
}
