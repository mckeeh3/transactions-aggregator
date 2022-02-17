package io.aggregator.entity;

import java.util.List;
import java.util.stream.Collectors;

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
  public HourEntity.HourState hourCreated(HourEntity.HourState state, HourEntity.HourCreated event) {
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
    log.info("state: {}\nAddMinuteCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(HourEntity.HourState state, HourApi.AggregateHourCommand command) {
    log.info("state: {}\nAggregateHourCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(HourEntity.HourState state, HourApi.MinuteAggregationCommand command) {
    log.info("state: {}\nMinuteAggregationCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static HourEntity.HourState handle(HourEntity.HourState state, HourEntity.HourCreated event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantId())
        .setEpochHour(event.getEpochHour())
        .setEpochDay(TimeTo.fromEpochHour(event.getEpochHour()).toEpochDay())
        .build();
  }

  static HourEntity.HourState handle(HourEntity.HourState state, HourEntity.MinuteAdded event) {
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
    return state.toBuilder()
        .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
        .build();
  }

  static HourEntity.HourState handle(HourEntity.HourState state, HourEntity.HourAggregated event) {
    return state; // no state change event
  }

  static HourEntity.HourState handle(HourEntity.HourState state, HourEntity.ActiveMinuteAggregated event) {
    return state.toBuilder()
        .clearActiveMinutes()
        .addAllActiveMinutes(state.getActiveMinutesList().stream()
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
            .collect(Collectors.toList()))
        .build();
  }

  static List<?> eventsFor(HourEntity.HourState state, HourApi.AddMinuteCommand command) {
    var minuteAdded = HourEntity.MinuteAdded
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochMinute(command.getEpochMinute())
        .build();

    if (state.getMerchantId().isEmpty()) {
      var hourCreated = HourEntity.HourCreated
          .newBuilder()
          .setMerchantId(command.getMerchantId())
          .setEpochHour(command.getEpochHour())
          .build();

      return List.of(hourCreated, minuteAdded);
    } else {
      return List.of(minuteAdded);
    }
  }

  static HourEntity.HourAggregationRequested eventFor(HourEntity.HourState state, HourApi.AggregateHourCommand command) {
    return HourEntity.HourAggregationRequested
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochHour(command.getEpochHour())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .addAllEpochMinutes(
            state.getActiveMinutesList().stream()
                .map(activeMinute -> activeMinute.getEpochMinute())
                .collect(Collectors.toList()))
        .build();
  }

  static List<?> eventsFor(HourEntity.HourState state, HourApi.MinuteAggregationCommand command) {
    var activeMinutes = state.getActiveMinutesList();

    var alreadyInList = activeMinutes.stream()
        .anyMatch(activeMinute -> activeMinute.getEpochMinute() == command.getEpochMinute());

    if (!alreadyInList) {
      activeMinutes.add(
          HourEntity.ActiveMinute
              .newBuilder()
              .setEpochMinute(command.getEpochMinute())
              .build());
    }

    activeMinutes = updateActiveMinutes(command, activeMinutes);

    final var aggregateRequestTimestamp = state.getAggregateRequestTimestamp();

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
        .collect(Collectors.toList());
  }

  static HourEntity.ActiveMinuteAggregated toActiveMinuteAggregated(HourApi.MinuteAggregationCommand command) {
    var activeMinuteAggregated = HourEntity.ActiveMinuteAggregated
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochMinute(command.getEpochMinute())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
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
        .setMerchantId(command.getMerchantId())
        .setEpochHour(command.getEpochHour())
        .setTransactionTotalAmount(transactionTotalAmount)
        .setTransactionCount(transactionCount)
        .setLastUpdateTimestamp(lastUpdateTimestamp)
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .build();
  }
}
