package io.aggregator.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import io.aggregator.service.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.MinuteApi;

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
  public MinuteEntity.MinuteState minuteActivated(MinuteEntity.MinuteState state, MinuteEntity.MinuteActivated event) {
    return handle(state, event);
  }

  @Override
  public MinuteEntity.MinuteState secondAdded(MinuteEntity.MinuteState state, MinuteEntity.SecondAdded event) {
    return handle(state, event);
  }

  @Override
  public MinuteEntity.MinuteState minuteAggregationRequested(MinuteEntity.MinuteState state, MinuteEntity.MinuteAggregationRequested event) {
    return handle(state, event);
  }

  @Override
  public MinuteEntity.MinuteState minuteAggregated(MinuteEntity.MinuteState state, MinuteEntity.MinuteAggregated event) {
    return handle(state, event);
  }

  @Override
  public MinuteEntity.MinuteState activeSecondAggregated(MinuteEntity.MinuteState state, MinuteEntity.ActiveSecondAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(MinuteEntity.MinuteState state, MinuteApi.AddSecondCommand command) {
    log.debug("state: {}\nAddSecondCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: AddSecondCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(MinuteEntity.MinuteState state, MinuteApi.AggregateMinuteCommand command) {
    log.debug("state: {}\nAggregateMinuteCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: AggregateMinuteCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(MinuteEntity.MinuteState state, MinuteApi.SecondAggregationCommand command) {
    log.debug("state: {}\nSecondAggregationCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: SecondAggregationCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.MinuteActivated event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: MinuteActivated");

    return state.toBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(event.getMerchantKey().getMerchantId())
                .build())
        .setEpochMinute(event.getEpochMinute())
        .setEpochHour(TimeTo.fromEpochMinute(event.getEpochMinute()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochMinute(event.getEpochMinute()).toEpochDay())
        .build();
  }

  static MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.SecondAdded event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: SecondAdded");

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

  static MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.MinuteAggregationRequested event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: MinuteAggregationRequested");

    var activeAlreadyMoved = state.getAggregateMinutesList().stream()
        .anyMatch(activeMinute -> activeMinute.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp()));

    if (activeAlreadyMoved) {
      return state;
    } else {
      return moveActiveSecondsToAggregateMinute(state, event);
    }
  }

  static MinuteEntity.MinuteState moveActiveSecondsToAggregateMinute(MinuteEntity.MinuteState state, MinuteEntity.MinuteAggregationRequested event) {
    return state.toBuilder()
        .clearActiveSeconds()
        .addAggregateMinutes(
            MinuteEntity.AggregateMinute
                .newBuilder()
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .setPaymentId(event.getPaymentId())
                .addAllActiveSeconds(state.getActiveSecondsList())
                .build())
        .build();
  }

  static MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.MinuteAggregated event) {
    return state; // non-state change event
  }

  static MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.ActiveSecondAggregated event) {
    return state.toBuilder()
        .clearAggregateMinutes()
        .addAllAggregateMinutes(updateAggregateMinutes(state, event))
        .build();
  }

  static List<MinuteEntity.AggregateMinute> updateAggregateMinutes(MinuteEntity.MinuteState state, MinuteEntity.ActiveSecondAggregated event) {
    return state.getAggregateMinutesList().stream()
        .map(aggregatedMinute -> {
          if (aggregatedMinute.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp())) {
            return aggregatedMinute.toBuilder()
                .clearActiveSeconds()
                .addAllActiveSeconds(updateActiveSeconds(event, aggregatedMinute))
                .build();
          } else {
            return aggregatedMinute;
          }
        })
        .toList();
  }

  static List<MinuteEntity.ActiveSecond> updateActiveSeconds(MinuteEntity.ActiveSecondAggregated event, MinuteEntity.AggregateMinute aggregateMinute) {
    return aggregateMinute.getActiveSecondsList().stream()
        .map(activeSecond -> {
          if (activeSecond.getEpochSecond() == event.getEpochSecond()) {
            return activeSecond
                .toBuilder()
                .addAllMoneyMovements(event.getMoneyMovementsList())
                .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .build();
          } else {
            return activeSecond;
          }
        })
        .toList();
  }

  static List<?> eventsFor(MinuteEntity.MinuteState state, MinuteApi.AddSecondCommand command) {
    var secondAdded = MinuteEntity.SecondAdded
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochSecond(command.getEpochSecond())
        .build();

    if (state.getActiveSecondsCount() == 0) {
      var minuteActivated = MinuteEntity.MinuteActivated
          .newBuilder()
          .setMerchantKey(
              TransactionMerchantKey.MerchantKey
                  .newBuilder()
                  .setMerchantId(command.getMerchantId())
                  .build())
          .setEpochMinute(command.getEpochMinute())
          .build();

      return List.of(minuteActivated, secondAdded);
    } else {
      return List.of(secondAdded);
    }
  }

  static List<?> eventsFor(MinuteEntity.MinuteState state, MinuteApi.AggregateMinuteCommand command) {
    if (state.getActiveSecondsCount() == 0) {
      var timestamp = command.getAggregateRequestTimestamp();
      return List.of(
          MinuteEntity.MinuteAggregated
              .newBuilder()
              .setMerchantKey(
                  TransactionMerchantKey.MerchantKey
                      .newBuilder()
                      .setMerchantId(command.getMerchantId())
                      .build())
              .setEpochMinute(command.getEpochMinute())
              .setLastUpdateTimestamp(timestamp)
              .setAggregateRequestTimestamp(timestamp)
              .setPaymentId(command.getPaymentId())
              .build());
    } else {
      return List.of(
          MinuteEntity.MinuteAggregationRequested
              .newBuilder()
              .setMerchantKey(
                  TransactionMerchantKey.MerchantKey
                      .newBuilder()
                      .setMerchantId(command.getMerchantId())
                      .build())
              .setEpochMinute(command.getEpochMinute())
              .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
              .setPaymentId(command.getPaymentId())
              .addAllEpochSeconds(
                  state.getActiveSecondsList().stream()
                      .map(MinuteEntity.ActiveSecond::getEpochSecond)
                      .toList())
              .build());
    }
  }

  static List<?> eventsFor(MinuteEntity.MinuteState state, MinuteApi.SecondAggregationCommand command) {
    var aggregateMinute = state.getAggregateMinutesList().stream()
        .filter(aggMin -> aggMin.getAggregateRequestTimestamp().equals(command.getAggregateRequestTimestamp()))
        .findFirst()
        .orElse(
            MinuteEntity.AggregateMinute
                .newBuilder()
                .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
                .addAllActiveSeconds(state.getActiveSecondsList())
                .build());
    var aggregateRequestTimestamp = aggregateMinute.getAggregateRequestTimestamp();
    var activeSeconds = aggregateMinute.getActiveSecondsList();

    var alreadyInList = activeSeconds.stream()
        .anyMatch(activeSecond -> activeSecond.getEpochSecond() == command.getEpochSecond());

    if (!alreadyInList) {
      activeSeconds = new ArrayList<>(activeSeconds);
      activeSeconds.add(
          MinuteEntity.ActiveSecond
              .newBuilder()
              .setEpochSecond(command.getEpochSecond())
              .build());
      activeSeconds = Collections.unmodifiableList(activeSeconds);
    }

    activeSeconds = updateActiveSeconds(command, activeSeconds);

    var allSecondsAggregated = activeSeconds.stream()
        .allMatch(activeSecond -> activeSecond.getAggregateRequestTimestamp().equals(aggregateRequestTimestamp));

    if (allSecondsAggregated) {
      return List.of(toMinuteAggregated(command, activeSeconds), toActiveSecondAggregated(command));
    } else {
      return List.of(toActiveSecondAggregated(command));
    }
  }

  static List<MinuteEntity.ActiveSecond> updateActiveSeconds(MinuteApi.SecondAggregationCommand command, List<MinuteEntity.ActiveSecond> activeSeconds) {
    return activeSeconds.stream()
        .map(activeSecond -> {
          if (activeSecond.getEpochSecond() == command.getEpochSecond()) {
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

  static MinuteEntity.ActiveSecondAggregated toActiveSecondAggregated(MinuteApi.SecondAggregationCommand command) {
    return MinuteEntity.ActiveSecondAggregated
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochSecond(command.getEpochSecond())
        .addAllMoneyMovements(command.getMoneyMovementsList())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
  }

  static MinuteEntity.MinuteAggregated toMinuteAggregated(MinuteApi.SecondAggregationCommand command, List<MinuteEntity.ActiveSecond> activeSeconds) {
    var lastUpdateTimestamp = activeSeconds.stream()
        .map(MinuteEntity.ActiveSecond::getLastUpdateTimestamp)
        .max(TimeTo.comparator())
        .get();

    List<TransactionMerchantKey.MoneyMovement> summarisedMoneyMovements = activeSeconds.stream()
        .flatMap(activeSecond -> activeSecond.getMoneyMovementsList().stream())
        .collect(Collectors.toList());

    return MinuteEntity.MinuteAggregated
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochMinute(command.getEpochMinute())
        .addAllMoneyMovements(RuleService.mergeMoneyMovements(summarisedMoneyMovements))
        .setLastUpdateTimestamp(lastUpdateTimestamp)
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
  }
}
