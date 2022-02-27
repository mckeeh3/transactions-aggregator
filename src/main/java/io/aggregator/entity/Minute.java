package io.aggregator.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

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
  public MinuteEntity.MinuteState minuteCreated(MinuteEntity.MinuteState state, MinuteEntity.MinuteCreated event) {
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
    log.info("state: {}\nAddSecondCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(MinuteEntity.MinuteState state, MinuteApi.AggregateMinuteCommand command) {
    log.info("state: {}\nAggregateMinuteCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(MinuteEntity.MinuteState state, MinuteApi.SecondAggregationCommand command) {
    log.info("state: {}\nSecondAggregationCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.MinuteCreated event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantId())
        .setEpochMinute(event.getEpochMinute())
        .setEpochHour(TimeTo.fromEpochMinute(event.getEpochMinute()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochMinute(event.getEpochMinute()).toEpochDay())
        .build();
  }

  static MinuteEntity.MinuteState handle(MinuteEntity.MinuteState state, MinuteEntity.SecondAdded event) {
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
                .setTransactionTotalAmount(event.getTransactionTotalAmount())
                .setTransactionCount(event.getTransactionCount())
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
        .setMerchantId(command.getMerchantId())
        .setEpochSecond(command.getEpochSecond())
        .build();

    if (state.getMerchantId().isEmpty()) {
      var minuteCreated = MinuteEntity.MinuteCreated
          .newBuilder()
          .setMerchantId(command.getMerchantId())
          .setEpochMinute(command.getEpochMinute())
          .build();

      return List.of(minuteCreated, secondAdded);
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
              .setMerchantId(command.getMerchantId())
              .setEpochMinute(command.getEpochMinute())
              .setTransactionTotalAmount(0.0)
              .setTransactionCount(0)
              .setLastUpdateTimestamp(timestamp)
              .setAggregateRequestTimestamp(timestamp)
              .setPaymentId(command.getPaymentId())
              .build());
    } else {
      return List.of(
          MinuteEntity.MinuteAggregationRequested
              .newBuilder()
              .setMerchantId(command.getMerchantId())
              .setEpochMinute(command.getEpochMinute())
              .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
              .setPaymentId(command.getPaymentId())
              .addAllEpochSeconds(
                  state.getActiveSecondsList().stream()
                      .map(activeSecond -> activeSecond.getEpochSecond())
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

  static MinuteEntity.ActiveSecondAggregated toActiveSecondAggregated(MinuteApi.SecondAggregationCommand command) {
    var activeSecondAggregated = MinuteEntity.ActiveSecondAggregated
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochSecond(command.getEpochSecond())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
    return activeSecondAggregated;
  }

  static MinuteEntity.MinuteAggregated toMinuteAggregated(MinuteApi.SecondAggregationCommand command, List<MinuteEntity.ActiveSecond> activeSeconds) {
    var transactionTotalAmount = activeSeconds.stream()
        .reduce(0.0, (amount, activeSecond) -> amount + activeSecond.getTransactionTotalAmount(), Double::sum);

    var transactionCount = activeSeconds.stream()
        .reduce(0, (count, activeSecond) -> count + activeSecond.getTransactionCount(), Integer::sum);

    var lastUpdateTimestamp = activeSeconds.stream()
        .map(activeSecond -> activeSecond.getLastUpdateTimestamp())
        .max(TimeTo.comparator())
        .get();

    return MinuteEntity.MinuteAggregated
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochMinute(command.getEpochMinute())
        .setTransactionTotalAmount(transactionTotalAmount)
        .setTransactionCount(transactionCount)
        .setLastUpdateTimestamp(lastUpdateTimestamp)
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
  }
}
