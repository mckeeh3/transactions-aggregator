package io.aggregator.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
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
  public Effect<Empty> activateSubSecond(SecondEntity.SecondState state, SecondApi.ActivateSubSecondCommand command) {
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

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.ActivateSubSecondCommand command) {
    log.debug("state: {}\nActivateSubSecondCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    log.debug("state: {}\nAggregateSecondCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.SubSecondAggregationCommand command) {
    log.debug("state: {}\nSubSecondAggregationCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SecondActivated event) {
    return state.toBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(event.getMerchantKey().getMerchantId())
                .setServiceCode(event.getMerchantKey().getServiceCode())
                .setAccountFrom(event.getMerchantKey().getAccountFrom())
                .setAccountTo(event.getMerchantKey().getAccountTo())
                .build())
        .setEpochSecond(event.getEpochSecond())
        .setEpochHour(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochDay())
        .build();
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SubSecondAdded event) {
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

  static List<?> eventsFor(SecondEntity.SecondState state, SecondApi.ActivateSubSecondCommand command) {
    var subSecondAdded = SecondEntity.SubSecondAdded
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
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
                  .setServiceCode(command.getServiceCode())
                  .setAccountFrom(command.getAccountFrom())
                  .setAccountTo(command.getAccountTo())
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
                      .setServiceCode(command.getServiceCode())
                      .setAccountFrom(command.getAccountFrom())
                      .setAccountTo(command.getAccountTo())
                      .build())
              .setEpochSecond(command.getEpochSecond())
              .setTransactionTotalAmount(0.0)
              .setTransactionCount(0)
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
                      .setServiceCode(command.getServiceCode())
                      .setAccountFrom(command.getAccountFrom())
                      .setAccountTo(command.getAccountTo())
                      .build())
              .setEpochSecond(command.getEpochSecond())
              .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
              .setPaymentId(command.getPaymentId())
              .addAllEpochSubSeconds(
                  state.getActiveSubSecondsList().stream()
                      .map(activeSubSecond -> activeSubSecond.getEpochSubSecond())
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
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setEpochSubSecond(command.getEpochSubSecond())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
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
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setEpochSecond(command.getEpochSecond())
        .setTransactionTotalAmount(transactionTotalAmount)
        .setTransactionCount(transactionCount)
        .setLastUpdateTimestamp(lastUpdateTimestamp)
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
  }
}
