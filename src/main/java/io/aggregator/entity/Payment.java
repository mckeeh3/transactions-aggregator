package io.aggregator.entity;

import java.util.List;
import java.util.stream.Stream;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.PaymentApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Event Sourced Entity Service described in your io/aggregator/api/payment_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class Payment extends AbstractPayment {
  static final Logger log = LoggerFactory.getLogger(Payment.class);

  public Payment(EventSourcedEntityContext context) {
  }

  @Override
  public PaymentEntity.PaymentState emptyState() {
    return PaymentEntity.PaymentState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> aggregationRequest(PaymentEntity.PaymentState state, PaymentApi.AggregationRequestCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> paymentRequest(PaymentEntity.PaymentState state, PaymentApi.PaymentRequestCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> dayAggregation(PaymentEntity.PaymentState state, PaymentApi.DayAggregationCommand command) {
    return handle(state, command);
  }

  @Override
  public PaymentEntity.PaymentState activeDayAggregated(PaymentEntity.PaymentState state, PaymentEntity.ActiveDayAggregated event) {
    return handle(state, event);
  }

  @Override
  public PaymentEntity.PaymentState paymentDayAggregationRequested(PaymentEntity.PaymentState state, PaymentEntity.PaymentDayAggregationRequested event) {
    return handle(state, event);
  }

  @Override
  public PaymentEntity.PaymentState paymentRequested(PaymentEntity.PaymentState state, PaymentEntity.PaymentRequested event) {
    return handle(state, event);
  }

  @Override
  public PaymentEntity.PaymentState paymentAggregated(PaymentEntity.PaymentState state, PaymentEntity.PaymentAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(PaymentEntity.PaymentState state, PaymentApi.AggregationRequestCommand command) {
    log.info("state: {}\nAggregationRequestCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(PaymentEntity.PaymentState state, PaymentApi.PaymentRequestCommand command) {
    log.info("state: {}\nPaymentRequestCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(PaymentEntity.PaymentState state, PaymentApi.DayAggregationCommand command) {
    log.info("state: {}\nDayAggregationCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static PaymentEntity.PaymentState handle(PaymentEntity.PaymentState state, PaymentEntity.ActiveDayAggregated event) {
    var aggregation = state.getAggregationsList().stream()
        .filter(agg -> agg.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp()))
        .findFirst();

    if (aggregation.isPresent()) {
      var aggregationDay = aggregation.get().getAggregationDaysList().stream()
          .filter(aggDay -> aggDay.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp()))
          .findFirst();

      if (aggregationDay.isPresent()) {
        return state.toBuilder()
            .clearAggregations()
            .addAllAggregations(
                state.getAggregationsList().stream()
                    .map(agg -> updateAggregation(event, agg))
                    .toList())
            .build();
      }
    }
    return state; // TODO: handle error
  }

  static PaymentEntity.Aggregation updateAggregation(PaymentEntity.ActiveDayAggregated event, PaymentEntity.Aggregation aggregation) {
    if (aggregation.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp())) {
      return aggregation.toBuilder()
          .clearAggregationDays()
          .addAllAggregationDays(
              aggregation.getAggregationDaysList().stream()
                  .map(aggDay -> updateAggregationDay(event, aggDay))
                  .toList())
          .build();
    } else {
      return aggregation;
    }
  }

  static PaymentEntity.AggregationDay updateAggregationDay(PaymentEntity.ActiveDayAggregated event, PaymentEntity.AggregationDay aggDay) {
    if (aggDay.getEpochDay() == event.getEpochDay() && aggDay.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp())) {
      return aggDay.toBuilder()
          .setTransactionTotalAmount(event.getTransactionTotalAmount())
          .setTransactionCount(event.getTransactionCount())
          .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
          .setAggregated(true)
          .build();
    } else {
      return aggDay;
    }
  }

  static PaymentEntity.PaymentState handle(PaymentEntity.PaymentState state, PaymentEntity.PaymentDayAggregationRequested event) {
    if (state.getMerchantKey().getMerchantId().isEmpty()) {
      state = state.toBuilder()
          .setMerchantKey(event.getMerchantKey())
          .setPaymentId(event.getPaymentId())
          .build();
    }

    var aggregation = state.getAggregationsList().stream()
        .filter(agg -> agg.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp()))
        .findFirst();

    if (aggregation.isPresent()) {
      var aggregationDayAlreadyAdded = aggregation.get().getAggregationDaysList().stream()
          .anyMatch(aggregationDay -> aggregationDay.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp())
              && aggregationDay.getEpochDay() == event.getEpochDay());

      if (aggregationDayAlreadyAdded) {
        return state; // idempotent because aggregation and aggregation day are already present
      } else {
        return state.toBuilder()
            .clearAggregations()
            .addAllAggregations(state.getAggregationsList().stream()
                .map(agg -> {
                  if (agg.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp())) {
                    return agg.toBuilder()
                        .addAggregationDays(
                            PaymentEntity.AggregationDay
                                .newBuilder()
                                .setEpochDay(event.getEpochDay())
                                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                                .build())
                        .build();
                  } else {
                    return agg;
                  }
                })
                .toList())
            .build();
      }
    } else {
      return state.toBuilder()
          .addAggregations(
              PaymentEntity.Aggregation
                  .newBuilder()
                  .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                  .addAggregationDays(
                      PaymentEntity.AggregationDay
                          .newBuilder()
                          .setEpochDay(event.getEpochDay())
                          .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                          .build())
                  .build())
          .build();
    }
  }

  static PaymentEntity.PaymentState handle(PaymentEntity.PaymentState state, PaymentEntity.PaymentRequested event) {
    return state.toBuilder()
        .setPaymentRequested(true)
        .build();
  }

  static PaymentEntity.PaymentState handle(PaymentEntity.PaymentState state, PaymentEntity.PaymentAggregated event) {
    return state.toBuilder()
        .setTransactionTotalAmount(event.getTransactionTotalAmount())
        .setTransactionCount(event.getTransactionCount())
        .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
        .setPaymentAggregated(true)
        .build();
  }

  static List<?> eventsFor(PaymentEntity.PaymentState state, PaymentApi.DayAggregationCommand command) {
    var allAggregationDays = state.getAggregationsList().stream()
        .flatMap(aggregation -> aggregation.getAggregationDaysList().stream())
        .toList();

    var currentlyAggregatedDays = allAggregationDays.stream()
        .filter(aggregationDay -> aggregationDay.getAggregated())
        .toList();

    if (state.getPaymentRequested() && allAggregationDays.size() == currentlyAggregatedDays.size()) {
      return List.of();
    }

    var activeDayAggregated = PaymentEntity.ActiveDayAggregated
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setPaymentId(command.getPaymentId())
        .setEpochDay(command.getEpochDay())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .build();

    var allDaysAggregated = false;
    var totalTransactionAmount = 0.0;
    var totalTransactionCount = 0;

    var dayAlreadyAggregated = state.getAggregationsList().stream()
        .flatMap(aggregation -> aggregation.getAggregationDaysList().stream())
        .anyMatch(aggregationDay -> aggregationDay.getAggregated()
            && aggregationDay.getAggregateRequestTimestamp().equals(command.getAggregateRequestTimestamp())
            && aggregationDay.getEpochDay() == command.getEpochDay());

    if (state.getPaymentRequested() && !dayAlreadyAggregated && allAggregationDays.size() == 1 + currentlyAggregatedDays.size()) {
      allDaysAggregated = true;
      totalTransactionAmount = command.getTransactionTotalAmount();
      totalTransactionCount = command.getTransactionCount();
    }

    if (allDaysAggregated) {
      var transactionTotalAmount = state.getAggregationsList().stream()
          .flatMap(aggregation -> aggregation.getAggregationDaysList().stream())
          .mapToDouble(aggregationDay -> aggregationDay.getTransactionTotalAmount())
          .sum();
      var transactionCount = state.getAggregationsList().stream()
          .flatMap(aggregation -> aggregation.getAggregationDaysList().stream())
          .mapToInt(aggregationDay -> aggregationDay.getTransactionCount())
          .sum();
      var lastUpdateTimestamp = state.getAggregationsList().stream()
          .flatMap(aggregation -> aggregation.getAggregationDaysList().stream())
          .map(aggregationDay -> aggregationDay.getLastUpdateTimestamp())
          .max(TimeTo.comparator())
          .get();

      var paymentAggregated = PaymentEntity.PaymentAggregated
          .newBuilder()
          .setMerchantKey(
              TransactionMerchantKey.MerchantKey
                  .newBuilder()
                  .setMerchantId(command.getMerchantId())
                  .setServiceCode(command.getServiceCode())
                  .setAccountFrom(command.getAccountFrom())
                  .setAccountTo(command.getAccountTo())
                  .build())
          .setPaymentId(command.getPaymentId())
          .setTransactionTotalAmount(transactionTotalAmount + totalTransactionAmount)
          .setTransactionCount(transactionCount + totalTransactionCount)
          .setLastUpdateTimestamp(TimeTo.max(lastUpdateTimestamp, command.getLastUpdateTimestamp()))
          .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
          .build();

      return List.of(paymentAggregated, activeDayAggregated);
    } else {
      return List.of(activeDayAggregated);
    }
  }

  static List<?> eventsFor(PaymentEntity.PaymentState state, PaymentApi.PaymentRequestCommand command) {
    if (state.getPaymentRequested()) {
      return List.of();
    }

    var event = PaymentEntity.PaymentRequested
        .newBuilder()
        .setMerchantKey(toMerchantKey(command))
        .setPaymentId(command.getPaymentId())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .build();

    var events = toPaymentDayAggregationRequestedList(
        state, command.getAggregateRequestTimestamp(), command.getEpochDaysList(), toMerchantKey(command), command.getPaymentId());

    return Stream.concat(Stream.of(event), events.stream()).toList();
  }

  static List<?> eventsFor(PaymentEntity.PaymentState state, PaymentApi.AggregationRequestCommand command) {
    return toPaymentDayAggregationRequestedList(
        state, command.getAggregateRequestTimestamp(), command.getEpochDaysList(), toMerchantKey(command), command.getPaymentId());
  }

  static List<PaymentEntity.PaymentDayAggregationRequested> toPaymentDayAggregationRequestedList(
      PaymentEntity.PaymentState state, Timestamp aggregateRequestTimestamp, List<Long> epochDays, TransactionMerchantKey.MerchantKey merchantKey, String paymentId) {
    var aggregationAlreadyRequested = state.getAggregationsList().stream()
        .anyMatch(aggregation -> aggregation.getAggregateRequestTimestamp().equals(aggregateRequestTimestamp));

    if (aggregationAlreadyRequested) {
      return List.of();
    }

    var aggregation = state.getAggregationsList().stream()
        .filter(agg -> agg.getAggregateRequestTimestamp().equals(aggregateRequestTimestamp))
        .findFirst()
        .orElse(
            PaymentEntity.Aggregation
                .newBuilder()
                .setAggregateRequestTimestamp(aggregateRequestTimestamp)
                .addAllAggregationDays(epochDays.stream()
                    .map(epochDay -> PaymentEntity.AggregationDay
                        .newBuilder()
                        .setEpochDay(epochDay)
                        .setAggregateRequestTimestamp(aggregateRequestTimestamp)
                        .build())
                    .toList())
                .build());

    return aggregation.getAggregationDaysList().stream()
        .map(activeDay -> PaymentEntity.PaymentDayAggregationRequested
            .newBuilder()
            .setMerchantKey(merchantKey)
            .setEpochDay(activeDay.getEpochDay())
            .setPaymentId(paymentId)
            .setAggregateRequestTimestamp(aggregateRequestTimestamp)
            .build())
        .toList();
  }

  static TransactionMerchantKey.MerchantKey toMerchantKey(PaymentApi.PaymentRequestCommand command) {
    return TransactionMerchantKey.MerchantKey
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setServiceCode(command.getServiceCode())
        .setAccountFrom(command.getAccountFrom())
        .setAccountTo(command.getAccountTo())
        .build();
  }

  static TransactionMerchantKey.MerchantKey toMerchantKey(PaymentApi.AggregationRequestCommand command) {
    return TransactionMerchantKey.MerchantKey
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setServiceCode(command.getServiceCode())
        .setAccountFrom(command.getAccountFrom())
        .setAccountTo(command.getAccountTo())
        .build();
  }
}