package io.aggregator.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.DayApi;
import io.aggregator.api.DayApi.HourAggregationCommand;
import io.aggregator.entity.DayEntity.ActiveHour;

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
        .emitEvents(eventsFor(state, command))
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
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(event.getMerchantKey().getMerchantId())
                .setServiceCode(event.getMerchantKey().getServiceCode())
                .setAccountFrom(event.getMerchantKey().getAccountFrom())
                .setAccountTo(event.getMerchantKey().getAccountTo())
                .build())
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
    var activeAlreadyMoved = state.getAggregateDaysList().stream()
        .anyMatch(aggregatedDay -> aggregatedDay.getAggregateRequestTimestamp() == event.getAggregateRequestTimestamp());

    if (activeAlreadyMoved) {
      return state;
    } else {
      return moveActiveHoursToAggregateDay(state, event);
    }
  }

  static DayEntity.DayState moveActiveHoursToAggregateDay(DayEntity.DayState state, DayEntity.DayAggregationRequested event) {
    return state.toBuilder()
        .clearActiveHours()
        .addAggregateDays(
            DayEntity.AggregateDay
                .newBuilder()
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .setAggregationStartedTimestamp(event.getAggregationStartedTimestamp())
                .setPaymentId(event.getPaymentId())
                .addAllActiveHours(state.getActiveHoursList())
                .build())
        .build();
  }

  static DayEntity.DayState handle(DayEntity.DayState state, DayEntity.DayAggregated event) {
    return state; // no state change event
  }

  static DayEntity.DayState handle(DayEntity.DayState state, DayEntity.ActiveHourAggregated event) {
    return state.toBuilder()
        .clearAggregateDays()
        .addAllAggregateDays(updateAggregateDays(state, event))
        .build();
  }

  static List<DayEntity.AggregateDay> updateAggregateDays(DayEntity.DayState state, DayEntity.ActiveHourAggregated event) {
    return state.getAggregateDaysList().stream()
        .map(aggregatedDay -> {
          if (aggregatedDay.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp())) {
            return aggregatedDay.toBuilder()
                .clearActiveHours()
                .addAllActiveHours(updateActiveSecond(event, aggregatedDay))
                .build();
          } else {
            return aggregatedDay;
          }
        })
        .toList();
  }

  static List<DayEntity.ActiveHour> updateActiveSecond(DayEntity.ActiveHourAggregated event, DayEntity.AggregateDay aggregateDay) {
    return aggregateDay.getActiveHoursList().stream()
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
        .toList();
  }

  static List<?> eventsFor(DayEntity.DayState state, DayApi.AddHourCommand command) {
    var hourAdded = DayEntity.HourAdded
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setEpochHour(command.getEpochHour())
        .build();

    if (state.getMerchantKey().getMerchantId().isEmpty()) {
      var dayCreated = DayEntity.DayCreated
          .newBuilder()
          .setMerchantKey(
              TransactionMerchantKey.MerchantKey
                  .newBuilder()
                  .setMerchantId(command.getMerchantId())
                  .setServiceCode(command.getServiceCode())
                  .setAccountFrom(command.getAccountFrom())
                  .setAccountTo(command.getAccountTo())
                  .build())
          .setEpochDay(command.getEpochDay())
          .build();

      return List.of(dayCreated, hourAdded);
    } else {
      return List.of(hourAdded);
    }
  }

  static List<?> eventsFor(DayEntity.DayState state, DayApi.AggregateDayCommand command) {
    if (state.getActiveHoursCount() == 0) {
      var timestamp = command.getAggregateRequestTimestamp();
      return List.of(
          DayEntity.DayAggregated
              .newBuilder()
              .setMerchantKey(
                  TransactionMerchantKey.MerchantKey
                      .newBuilder()
                      .setMerchantId(command.getMerchantId())
                      .setServiceCode(command.getServiceCode())
                      .setAccountFrom(command.getAccountFrom())
                      .setAccountTo(command.getAccountTo())
                      .build())
              .setEpochDay(command.getEpochDay())
              .setTransactionTotalAmount(0.0)
              .setTransactionCount(0)
              .setLastUpdateTimestamp(timestamp)
              .setAggregateRequestTimestamp(timestamp)
              .setAggregationCompletedTimestamp(timestamp)
              .setPaymentId(command.getPaymentId())
              .build());
    } else {
      return List.of(
          DayEntity.DayAggregationRequested
              .newBuilder()
              .setMerchantKey(
                  TransactionMerchantKey.MerchantKey
                      .newBuilder()
                      .setMerchantId(command.getMerchantId())
                      .setServiceCode(command.getServiceCode())
                      .setAccountFrom(command.getAccountFrom())
                      .setAccountTo(command.getAccountTo())
                      .build())
              .setEpochDay(command.getEpochDay())
              .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
              .setPaymentId(command.getPaymentId())
              .setAggregationStartedTimestamp(command.getAggregateRequestTimestamp())
              .addAllEpochHours(
                  state.getActiveHoursList().stream()
                      .map(activeHour -> activeHour.getEpochHour())
                      .toList())
              .build());
    }
  }

  static List<?> eventsFor(DayEntity.DayState state, DayApi.HourAggregationCommand command) {
    var aggregateDay = state.getAggregateDaysList().stream()
        .filter(aggregatedDay -> aggregatedDay.getAggregateRequestTimestamp().equals(command.getAggregateRequestTimestamp()))
        .findFirst()
        .orElse(
            DayEntity.AggregateDay
                .newBuilder()
                .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
                .build());
    var aggregateRequestTimestamp = aggregateDay.getAggregateRequestTimestamp();
    var activeHours = aggregateDay.getActiveHoursList();

    var alreadyInList = activeHours.stream()
        .anyMatch(activeHour -> activeHour.getEpochHour() == command.getEpochHour());

    if (!alreadyInList) {
      activeHours = new ArrayList<>(activeHours);
      activeHours.add(
          DayEntity.ActiveHour
              .newBuilder()
              .setEpochHour(command.getEpochHour())
              .build());
      activeHours = Collections.unmodifiableList(activeHours);
    }

    activeHours = updateActiveHours(command, activeHours);

    var allHoursAggregated = activeHours.stream()
        .allMatch(activeHour -> activeHour.getAggregateRequestTimestamp().equals(aggregateRequestTimestamp));

    if (allHoursAggregated) {
      return List.of(toDayAggregated(state, command, activeHours), toActiveHourAggregated(command));
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
        .toList();
  }

  static DayEntity.ActiveHourAggregated toActiveHourAggregated(DayApi.HourAggregationCommand command) {
    var activeHourAggregated = DayEntity.ActiveHourAggregated
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setEpochHour(command.getEpochHour())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
    return activeHourAggregated;
  }

  static DayEntity.DayAggregated toDayAggregated(DayEntity.DayState state, HourAggregationCommand command, List<ActiveHour> activeHours) {
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
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setEpochDay(state.getEpochDay())
        .setTransactionTotalAmount(transactionTotalAmount)
        .setTransactionCount(transactionCount)
        .setLastUpdateTimestamp(lastUpdateTimestamp)
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setAggregationCompletedTimestamp(TimeTo.now())
        .setPaymentId(command.getPaymentId())
        .build();
  }
}
