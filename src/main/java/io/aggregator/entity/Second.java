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
  public Effect<Empty> activateStripedSecond(SecondEntity.SecondState state, SecondApi.ActivateStripedSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> aggregateSecond(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> stripedSecondAggregation(SecondEntity.SecondState state, SecondApi.StripedSecondAggregationCommand command) {
    return handle(state, command);
  }

  @Override
  public SecondEntity.SecondState secondActivated(SecondEntity.SecondState state, SecondEntity.SecondActivated event) {
    return handle(state, event);
  }

  @Override
  public SecondEntity.SecondState stripedSecondAdded(SecondEntity.SecondState state, SecondEntity.StripedSecondAdded event) {
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
  public SecondEntity.SecondState activeStripedSecondAggregated(SecondEntity.SecondState state, SecondEntity.ActiveStripedSecondAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.ActivateStripedSecondCommand command) {
    log.debug("state: {}\nActivateStripedSecondCommand: {}", state, command);

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

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.StripedSecondAggregationCommand command) {
    log.debug("state: {}\nStripedSecondAggregationCommand: {}", state, command);

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

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.StripedSecondAdded event) {
    var alreadyActivated = state.getActiveStripedSecondsList().stream()
        .anyMatch(activeSecond -> activeSecond.getEpochSecond() == event.getEpochSecond() && activeSecond.getStripe() == event.getStripe());

    if (alreadyActivated) {
      return state;
    } else {
      return state.toBuilder()
          .addActiveStripedSeconds(
              SecondEntity.ActiveStripedSecond
                  .newBuilder()
                  .setEpochSecond(event.getEpochSecond())
                  .setStripe(event.getStripe())
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
      return moveActiveStripedSecondsToAggregateSecond(state, event);
    }
  }

  static SecondEntity.SecondState moveActiveStripedSecondsToAggregateSecond(SecondEntity.SecondState state, SecondEntity.SecondAggregationRequested event) {
    return state.toBuilder()
        .clearActiveStripedSeconds()
        .addAggregateSeconds(
            SecondEntity.AggregateSecond
                .newBuilder()
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .setPaymentId(event.getPaymentId())
                .addAllActiveStripedSeconds(state.getActiveStripedSecondsList())
                .build())
        .build();
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SecondAggregated event) {
    return state; // non-state change event
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.ActiveStripedSecondAggregated event) {
    return state.toBuilder()
        .clearAggregateSeconds()
        .addAllAggregateSeconds(updateAggregateSeconds(state, event))
        .build();
  }

  static List<SecondEntity.AggregateSecond> updateAggregateSeconds(SecondEntity.SecondState state, SecondEntity.ActiveStripedSecondAggregated event) {
    return state.getAggregateSecondsList().stream()
        .map(aggregatedSecond -> {
          if (aggregatedSecond.getAggregateRequestTimestamp().equals(event.getAggregateRequestTimestamp())) {
            return aggregatedSecond.toBuilder()
                .clearActiveStripedSeconds()
                .addAllActiveStripedSeconds(updateActiveStripedSeconds(event, aggregatedSecond))
                .build();
          } else {
            return aggregatedSecond;
          }
        })
        .toList();
  }

  static List<SecondEntity.ActiveStripedSecond> updateActiveStripedSeconds(SecondEntity.ActiveStripedSecondAggregated event, SecondEntity.AggregateSecond aggregateStripedSecond) {
    return aggregateStripedSecond.getActiveStripedSecondsList().stream()
        .map(activeStripedSecond -> {
          if (activeStripedSecond.getEpochSecond() == event.getEpochSecond() && activeStripedSecond.getStripe() == event.getStripe()) {
            return activeStripedSecond
                .toBuilder()
                .setTransactionTotalAmount(event.getTransactionTotalAmount())
                .setTransactionCount(event.getTransactionCount())
                .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .build();
          } else {
            return activeStripedSecond;
          }
        })
        .toList();
  }

  static List<?> eventsFor(SecondEntity.SecondState state, SecondApi.ActivateStripedSecondCommand command) {
    var alreadyActivated = state.getActiveStripedSecondsList().stream()
        .anyMatch(activeSecond -> activeSecond.getEpochSecond() == command.getEpochSecond() && activeSecond.getStripe() == command.getStripe());
    if (alreadyActivated)
      return Collections.emptyList();

    var stripedSecondAdded = SecondEntity.StripedSecondAdded
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
        .setStripe(command.getStripe())
        .build();

    if (state.getActiveStripedSecondsCount() == 0) {
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

      return List.of(secondActivated, stripedSecondAdded);
    } else {
      return List.of(stripedSecondAdded);
    }
  }

  static List<?> eventsFor(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    if (state.getActiveStripedSecondsCount() == 0) {
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
              .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
              .setPaymentId(command.getPaymentId())
              .setEpochSecond(command.getEpochSecond())
              .addAllStripes(
                  state.getActiveStripedSecondsList().stream()
                      .map(SecondEntity.ActiveStripedSecond::getStripe)
                      .toList())
              .build());
    }
  }

  static List<?> eventsFor(SecondEntity.SecondState state, SecondApi.StripedSecondAggregationCommand command) {
    var aggregateSecond = state.getAggregateSecondsList().stream()
        .filter(aggSec -> aggSec.getAggregateRequestTimestamp().equals(command.getAggregateRequestTimestamp()))
        .findFirst()
        .orElse(
            SecondEntity.AggregateSecond
                .newBuilder()
                .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
                .addAllActiveStripedSeconds(state.getActiveStripedSecondsList())
                .build());
    var aggregateRequestTimestamp = aggregateSecond.getAggregateRequestTimestamp();
    var activeStripedSeconds = aggregateSecond.getActiveStripedSecondsList();

    var alreadyInList = activeStripedSeconds.stream()
        .anyMatch(activeStripedSecond -> activeStripedSecond.getEpochSecond() == command.getEpochSecond() && activeStripedSecond.getStripe() == command.getStripe());

    if (!alreadyInList) {
      activeStripedSeconds = new ArrayList<>(activeStripedSeconds);
      activeStripedSeconds.add(
          SecondEntity.ActiveStripedSecond
              .newBuilder()
              .setEpochSecond(command.getEpochSecond())
              .setStripe(command.getStripe())
              .build());
      activeStripedSeconds = Collections.unmodifiableList(activeStripedSeconds);
    }

    activeStripedSeconds = updateActiveStripedSeconds(command, activeStripedSeconds);

    var allSecondsAggregated = activeStripedSeconds.stream()
        .allMatch(activeSecond -> activeSecond.getAggregateRequestTimestamp().equals(aggregateRequestTimestamp));

    if (allSecondsAggregated) {
      return List.of(toSecondAggregated(command, activeStripedSeconds), toActiveStripedSecondAggregated(command));
    } else {
      return List.of(toActiveStripedSecondAggregated(command));
    }
  }

  static List<SecondEntity.ActiveStripedSecond> updateActiveStripedSeconds(SecondApi.StripedSecondAggregationCommand command, List<SecondEntity.ActiveStripedSecond> activeSeconds) {
    return activeSeconds.stream()
        .map(activeSecond -> {
          if (activeSecond.getEpochSecond() == command.getEpochSecond() && activeSecond.getStripe() == command.getStripe()) {
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

  static SecondEntity.ActiveStripedSecondAggregated toActiveStripedSecondAggregated(SecondApi.StripedSecondAggregationCommand command) {
    return SecondEntity.ActiveStripedSecondAggregated
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
        .setStripe(command.getStripe())
        .setTransactionTotalAmount(command.getTransactionTotalAmount())
        .setTransactionCount(command.getTransactionCount())
        .setLastUpdateTimestamp(command.getLastUpdateTimestamp())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setPaymentId(command.getPaymentId())
        .build();
  }

  static SecondEntity.SecondAggregated toSecondAggregated(SecondApi.StripedSecondAggregationCommand command, List<SecondEntity.ActiveStripedSecond> activeSeconds) {
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
