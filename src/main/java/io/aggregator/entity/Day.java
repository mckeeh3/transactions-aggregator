package io.aggregator.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.DayApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

/** An event sourced entity. */
public class Day extends AbstractDay {
  static final Logger log = LoggerFactory.getLogger(Day.class);

  public Day(EventSourcedEntityContext context) {
  }

  @Override
  public DayEntity.DayState emptyState() {
    return DayEntity.DayState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> addHour(DayEntity.DayState state, DayApi.AddHourCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> aggregateDay(DayEntity.DayState state, DayApi.AggregateDayCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> hourAggregation(DayEntity.DayState state, DayApi.HourAggregationCommand command) {
    return handle(state, command);
  }

  @Override
  public DayEntity.DayState dayCreated(DayEntity.DayState state, DayEntity.DayCreated event) {
    return handle(state, event);
  }

  @Override
  public DayEntity.DayState hourAdded(DayEntity.DayState state, DayEntity.HourAdded event) {
    return handle(state, event);
  }

  @Override
  public DayEntity.DayState dayAggregationRequested(DayEntity.DayState state, DayEntity.DayAggregationRequested event) {
    return handle(state, event);
  }

  @Override
  public DayEntity.DayState dayAggregated(DayEntity.DayState state, DayEntity.DayAggregated event) {
    return handle(state, event);
  }

  @Override
  public DayEntity.DayState activeHourAggregated(DayEntity.DayState state, DayEntity.ActiveHourAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(DayEntity.DayState state, DayApi.AddHourCommand command) {
    log.info("state: {}\nAddHourCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(DayEntity.DayState state, DayApi.AggregateDayCommand command) {
    log.info("state: {}\nAggregateDayCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(DayEntity.DayState state, DayApi.HourAggregationCommand command) {
    log.info("state: {}\nHourAggregationCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static DayEntity.DayState handle(DayEntity.DayState state, DayEntity.DayCreated event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantId())
        .setEpochDay(event.getEpochDay())
        .build();
  }

  static DayEntity.DayState handle(DayEntity.DayState state, DayEntity.HourAdded event) {
    var alreadyAdded = state.getActiveHoursList().stream()
        .anyMatch(activeHour -> activeHour.getEpochHour() == event.getEpochHour());

    if (alreadyAdded) {
      return state;
    } else {
      return state.toBuilder()
          .addActiveHours(
              DayEntity.ActiveHour
                  .newBuilder()
                  .setEpochHour(event.getEpochHour())
                  .build())
          .build();
    }
  }

  static DayEntity.DayState handle(DayEntity.DayState state, DayEntity.DayAggregationRequested event) {
    return state.toBuilder()
        .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
        .build();
  }

  static DayEntity.DayState handle(DayEntity.DayState state, DayEntity.DayAggregated event) {
    return state; // no state change event
  }

  static DayEntity.DayState handle(DayEntity.DayState state, DayEntity.ActiveHourAggregated event) {
    return state.toBuilder()
        .clearActiveHours()
        .addAllActiveHours(state.getActiveHoursList().stream()
            .map(activeHour -> {
              if (activeHour.getEpochHour() == event.getEpochHour()) {
                return activeHour
                    .toBuilder()
                    .setTransactionTotalAmount(event.getTransactionTotalAmount())
                    .setTransactionCount(event.getTransactionCount())
                    .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
                    .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                    .build();
              } else {
                return activeHour;
              }
            })
            .collect(Collectors.toList()))
        .build();
  }

  static List<?> eventsFor(DayEntity.DayState state, DayApi.AddHourCommand command) {
    var hourAdded = DayEntity.HourAdded
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochHour(command.getEpochHour())
        .build();

    if (state.getMerchantId().isEmpty()) {
      var dayCreated = DayEntity.DayCreated
          .newBuilder()
          .setMerchantId(command.getMerchantId())
          .setEpochDay(command.getEpochDay())
          .build();

      return List.of(dayCreated, hourAdded);
    } else {
      return List.of(hourAdded);
    }
  }

  static DayEntity.DayAggregationRequested eventFor(DayEntity.DayState state, DayApi.AggregateDayCommand command) {
    return DayEntity.DayAggregationRequested
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochDay(command.getEpochDay())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .addAllEpochHours(
            state.getActiveHoursList().stream()
                .map(activeHour -> activeHour.getEpochHour())
                .collect(Collectors.toList()))
        .build();
  }

  static List<?> eventsFor(DayEntity.DayState state, DayApi.HourAggregationCommand command) {
    var activeHours = state.getActiveHoursList();

    var alreadyInList = activeHours.stream()
        .anyMatch(activeHour -> activeHour.getEpochHour() == command.getEpochHour());

    if (!alreadyInList) {
      activeHours.add(
          DayEntity.ActiveHour.newBuilder()
              .setEpochHour(command.getEpochHour())
              .build());
    }

    activeHours = updateActiveHours(command, activeHours);

    final var aggregateRequestTimestamp = state.getAggregateRequestTimestamp();

    var allHoursAggregated = activeHours.stream()
        .allMatch(activeHour -> activeHour.getAggregateRequestTimestamp().equals(aggregateRequestTimestamp));

    if (allHoursAggregated) {
      return List.of(toDayAggregated(command, activeHours), toActiveHourAggregated(command));
    } else {
      return List.of(toActiveHourAggregated(command));
    }
  }

  static List<DayEntity.ActiveHour> updateActiveHours(DayApi.HourAggregationCommand command, List<DayEntity.ActiveHour> activeHours) {
    return activeHours.stream()
        .map(activeHour -> {
          if (activeHour.getEpochHour() == command.getEpochHour()) {
            return activeHour
                .toBuilder()
                .setTransactionTotalAmount(command.getTransactionTotalAmount())
                .setTransactionCount(command.getTransactionCount())
                .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
                .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
                .build();
          } else {
            return activeHour;
          }
        })
        .collect(Collectors.toList());
  }

  static DayEntity.ActiveHourAggregated toActiveHourAggregated(DayApi.HourAggregationCommand command) {
    var activeHourAggregated = DayEntity.ActiveHourAggregated
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochHour(command.getEpochHour())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .build();
    return activeHourAggregated;
  }

  static DayEntity.DayAggregated toDayAggregated(DayApi.HourAggregationCommand command, List<DayEntity.ActiveHour> activeHours) {
    var transactionTotalAmount = activeHours.stream()
        .reduce(0.0, (amount, activeHour) -> amount + activeHour.getTransactionTotalAmount(), Double::sum);

    var transactionCount = activeHours.stream()
        .reduce(0, (count, activeHour) -> count + activeHour.getTransactionCount(), Integer::sum);

    var lastUpdateTimestamp = activeHours.stream()
        .map(activeHour -> activeHour.getLastUpdateTimestamp())
        .max(TimeTo.comparator())
        .get();

    return DayEntity.DayAggregated
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochDay(command.getEpochDay())
        .setTransactionTotalAmount(transactionTotalAmount)
        .setTransactionCount(transactionCount)
        .setLastUpdateTimestamp(lastUpdateTimestamp)
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .build();
  }
}
