package io.aggregator.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.HourApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

/** An event sourced entity. */
public class Hour extends AbstractHour {
  static final Logger log = LoggerFactory.getLogger(Hour.class);

  public Hour(EventSourcedEntityContext context) {
  }

  @Override
  public HourEntity.HourState emptyState() {
    return HourEntity.HourState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> addMinute(HourEntity.HourState state, HourApi.AddMinuteCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> aggregateHour(HourEntity.HourState state, HourApi.AggregateHourCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> minuteAggregation(HourEntity.HourState state, HourApi.MinuteAggregationCommand command) {
    return handle(state, command);
  }

  @Override
  public HourEntity.HourState hourActivated(HourEntity.HourState state, HourEntity.HourActivated event) {
    return handle(state, event);
  }

  @Override
  public HourEntity.HourState minuteAdded(HourEntity.HourState state, HourEntity.MinuteAdded event) {
    return handle(state, event);
  }

  @Override
  public HourEntity.HourState hourAggregationRequested(HourEntity.HourState state, HourEntity.HourAggregationRequested event) {
    return handle(state, event);
  }

  @Override
  public HourEntity.HourState hourAggregated(HourEntity.HourState state, HourEntity.HourAggregated event) {
    return handle(state, event);
  }

  @Override
  public HourEntity.HourState activeMinuteAggregated(HourEntity.HourState state, HourEntity.ActiveMinuteAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(HourEntity.HourState state, HourApi.AddMinuteCommand command) {
    log.debug("state: {}\nAddMinuteCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: AddMinuteCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(HourEntity.HourState state, HourApi.AggregateHourCommand command) {
    log.debug("state: {}\nAggregateHourCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: AggregateHourCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(HourEntity.HourState state, HourApi.MinuteAggregationCommand command) {
    log.debug("state: {}\nMinuteAggregationCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: MinuteAggregationCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static HourEntity.HourState handle(HourEntity.HourState state, HourEntity.HourActivated event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: HourActivated");

    return state.toBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(event.getMerchantKey().getMerchantId())
                .build())
        .setEpochHour(event.getEpochHour())
        .setEpochDay(TimeTo.fromEpochHour(event.getEpochHour()).toEpochDay())
        .build();
  }

  static HourEntity.HourState handle(HourEntity.HourState state, HourEntity.MinuteAdded event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: MinuteAdded");

    var alreadyAdded = state.getActiveMinutesList().stream()
        .anyMatch(activeMinute -> activeMinute.getEpochMinute() == event.getEpochMinute());

    if (alreadyAdded) {
      return state;
    } else {
      return state.toBuilder()
          .addActiveMinutes(
              HourEntity.ActiveMinute
                  .newBuilder()
                  .setEpochMinute(event.getEpochMinute())
                  .build())
          .build();
    }
  }

  static HourEntity.HourState handle(HourEntity.HourState state, HourEntity.HourAggregationRequested event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: HourAggregationRequested");

    var activeAlreadyMoved = state.getAggregateHoursList().stream()
        .anyMatch(aggregateHour -> aggregateHour.getAggregateRequestTimestamp() == event.getAggregateRequestTimestamp());

    if (activeAlreadyMoved) {
      return state;
    } else {
      return moveActiveMinutesToAggregateHour(state, event);
    }
  }

  static HourEntity.HourState moveActiveMinutesToAggregateHour(HourEntity.HourState state, HourEntity.HourAggregationRequested event) {
    return state.toBuilder()
        .clearActiveMinutes()
        .addAggregateHours(
            HourEntity.AggregateHour
                .newBuilder()
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .setPaymentId(event.getPaymentId())
                .addAllActiveMinutes(state.getActiveMinutesList())
                .build())
        .build();
  }

  static HourEntity.HourState handle(HourEntity.HourState state, HourEntity.HourAggregated event) {
    return state; // non-state change event
  }

  static HourEntity.HourState handle(HourEntity.HourState state, HourEntity.ActiveMinuteAggregated event) {
    return state.toBuilder()
        .clearAggregateHours()
        .addAllAggregateHours(updateAggregateMinutes(state, event))
        .build();
  }

  static List<HourEntity.AggregateHour> updateAggregateMinutes(HourEntity.HourState state, HourEntity.ActiveMinuteAggregated event) {
    return state.getAggregateHoursList().stream()
        .map(aggregatedHour -> {
          if (aggregatedHour.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp())) {
            return aggregatedHour.toBuilder()
                .clearActiveMinutes()
                .addAllActiveMinutes(updateActiveMinutes(event, aggregatedHour))
                .build();
          } else {
            return aggregatedHour;
          }
        })
        .toList();
  }

  static List<HourEntity.ActiveMinute> updateActiveMinutes(HourEntity.ActiveMinuteAggregated event, HourEntity.AggregateHour aggregateHour) {
    return aggregateHour.getActiveMinutesList().stream()
        .map(activeMinute -> {
          if (activeMinute.getEpochMinute() == event.getEpochMinute()) {
            return activeMinute
                .toBuilder()
                .setTransactionTotalAmount(event.getTransactionTotalAmount())
                .setTransactionCount(event.getTransactionCount())
                .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .build();
          } else {
            return activeMinute;
          }
        })
        .toList();
  }

  static List<?> eventsFor(HourEntity.HourState state, HourApi.AddMinuteCommand command) {
    var minuteAdded = HourEntity.MinuteAdded
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochMinute(command.getEpochMinute())
        .build();

    if (state.getActiveMinutesCount() == 0) {
      var hourActivated = HourEntity.HourActivated
          .newBuilder()
          .setMerchantKey(
              TransactionMerchantKey.MerchantKey
                  .newBuilder()
                  .setMerchantId(command.getMerchantId())
                  .build())
          .setEpochHour(command.getEpochHour())
          .build();

      return List.of(hourActivated, minuteAdded);
    } else {
      return List.of(minuteAdded);
    }
  }

  static List<?> eventsFor(HourEntity.HourState state, HourApi.AggregateHourCommand command) {
    if (state.getActiveMinutesCount() == 0) {
      var timestamp = command.getAggregateRequestTimestamp();
//    TODO edit HourAggregated and add map of aggregations
      return List.of(
          HourEntity.HourAggregated
              .newBuilder()
              .setMerchantKey(
                  TransactionMerchantKey.MerchantKey
                      .newBuilder()
                      .setMerchantId(command.getMerchantId())
                      .build())
              .setEpochHour(command.getEpochHour())
              .setTransactionTotalAmount(0.0)
              .setTransactionCount(0)
              .setLastUpdateTimestamp(timestamp)
              .setAggregateRequestTimestamp(timestamp)
              .setPaymentId(command.getPaymentId())
              .build());
    } else {
      return List.of(
          HourEntity.HourAggregationRequested
              .newBuilder()
              .setMerchantKey(
                  TransactionMerchantKey.MerchantKey
                      .newBuilder()
                      .setMerchantId(command.getMerchantId())
                      .build())
              .setEpochHour(command.getEpochHour())
              .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
              .setPaymentId(command.getPaymentId())
              .addAllEpochMinutes(
                  state.getActiveMinutesList().stream()
                      .map(HourEntity.ActiveMinute::getEpochMinute)
                      .toList())
              .build());
    }
  }

  static List<?> eventsFor(HourEntity.HourState state, HourApi.MinuteAggregationCommand command) {
    var aggregateHour = state.getAggregateHoursList().stream()
        .filter(aggHr -> aggHr.getAggregateRequestTimestamp().equals(command.getAggregateRequestTimestamp()))
        .findFirst()
        .orElse(
            HourEntity.AggregateHour
                .newBuilder()
                .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
                .build());
    var aggregateRequestTimestamp = aggregateHour.getAggregateRequestTimestamp();
    var activeMinutes = aggregateHour.getActiveMinutesList();

    var alreadyInList = activeMinutes.stream()
        .anyMatch(activeMinute -> activeMinute.getEpochMinute() == command.getEpochMinute());

    if (!alreadyInList) {
      activeMinutes = new ArrayList<>(activeMinutes);
      activeMinutes.add(
          HourEntity.ActiveMinute
              .newBuilder()
              .setEpochMinute(command.getEpochMinute())
              .build());
      activeMinutes = Collections.unmodifiableList(activeMinutes);
    }

    activeMinutes = updateActiveMinutes(command, activeMinutes);

    var allMinutesAggregated = activeMinutes.stream()
        .allMatch(activeMinute -> activeMinute.getAggregateRequestTimestamp().equals(aggregateRequestTimestamp));

    if (allMinutesAggregated) {
      return List.of(toHourAggregated(command, activeMinutes), toActiveMinuteAggregated(command));
    } else {
      return List.of(toActiveMinuteAggregated(command));
    }
  }

  static List<HourEntity.ActiveMinute> updateActiveMinutes(HourApi.MinuteAggregationCommand command, List<HourEntity.ActiveMinute> activeMinutes) {
    return activeMinutes.stream()
        .map(activeMinute -> {
          if (activeMinute.getEpochMinute() == command.getEpochMinute()) {
            return activeMinute
                .toBuilder()
                .setTransactionTotalAmount(command.getTransactionTotalAmount())
                .setTransactionCount(command.getTransactionCount())
                .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
                .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
                .build();
          } else {
            return activeMinute;
          }
        })
        .toList();
  }

  static HourEntity.ActiveMinuteAggregated toActiveMinuteAggregated(HourApi.MinuteAggregationCommand command) {
    var activeMinuteAggregated = HourEntity.ActiveMinuteAggregated
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochMinute(command.getEpochMinute())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
    return activeMinuteAggregated;
  }

  static HourEntity.HourAggregated toHourAggregated(HourApi.MinuteAggregationCommand command, List<HourEntity.ActiveMinute> activeMinutes) {
    var transactionTotalAmount = activeMinutes.stream()
        .reduce(0.0, (amount, activeMinute) -> amount + activeMinute.getTransactionTotalAmount(), Double::sum);

    var transactionCount = activeMinutes.stream()
        .reduce(0, (count, activeMinute) -> count + activeMinute.getTransactionCount(), Integer::sum);

    var lastUpdateTimestamp = activeMinutes.stream()
        .map(activeMinute -> activeMinute.getLastUpdateTimestamp())
        .max(TimeTo.comparator())
        .get();

    return HourEntity.HourAggregated
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochHour(command.getEpochHour())
        .setTransactionTotalAmount(transactionTotalAmount)
        .setTransactionCount(transactionCount)
        .setLastUpdateTimestamp(lastUpdateTimestamp)
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
  }
}
