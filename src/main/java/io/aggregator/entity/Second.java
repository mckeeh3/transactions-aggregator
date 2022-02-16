package io.aggregator.entity;

import java.util.List;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
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
  public Effect<Empty> addTransaction(SecondEntity.SecondState state, SecondApi.AddTransactionCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> aggregate(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public SecondEntity.SecondState secondCreated(SecondEntity.SecondState state, SecondEntity.SecondCreated event) {
    return handle(state, event);
  }

  @Override
  public SecondEntity.SecondState secondTransactionAdded(SecondEntity.SecondState state, SecondEntity.SecondTransactionAdded event) {
    return handle(state, event);
  }

  @Override
  public SecondEntity.SecondState secondAggregated(SecondEntity.SecondState state, SecondEntity.SecondAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.AddTransactionCommand command) {
    log.info("state: {}\nAddTransactionCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    log.info("state: {}\nAggregateCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SecondCreated event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantId())
        .setEpochSecond(event.getEpochSecond())
        .setEpochMinute(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochMinute())
        .setEpochHour(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochDay())
        .build();
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SecondTransactionAdded event) {
    var transactionAlreadyAdded = state.getTransactionsList().stream()
        .anyMatch(transaction -> transaction.getTransactionId().equals(event.getTransactionId()));

    if (transactionAlreadyAdded) {
      return state; // idempotent - no need to re-add the same transaction
    } else {
      return state.toBuilder()
          .addTransactions(
              SecondEntity.Transaction.newBuilder()
                  .setMerchantId(event.getMerchantId())
                  .setEpochSecond(event.getEpochSecond())
                  .setTransactionId(event.getTransactionId())
                  .setAmount(event.getAmount())
                  .setTimestamp(event.getTimestamp())
                  .build())
          .build();
    }
  }

  static SecondEntity.SecondState handle(SecondEntity.SecondState state, SecondEntity.SecondAggregated event) {
    return state; // this is a non-state changing event
  }

  static List<?> eventsFor(SecondEntity.SecondState state, SecondApi.AddTransactionCommand command) {
    var transactionAdded = SecondEntity.SecondTransactionAdded.newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochSecond(command.getEpochSecond())
        .setTransactionId(command.getTransactionId())
        .setAmount(command.getAmount())
        .setTimestamp(command.getTimestamp())
        .build();

    if (state.getMerchantId().isEmpty()) {
      var secondCreated = SecondEntity.SecondCreated.newBuilder()
          .setMerchantId(command.getMerchantId())
          .setEpochSecond(command.getEpochSecond())
          .build();

      return List.of(secondCreated, transactionAdded);
    } else {
      return List.of(transactionAdded);
    }
  }

  static SecondEntity.SecondAggregated eventFor(SecondEntity.SecondState state, SecondApi.AggregateSecondCommand command) {
    var total = state.getTransactionsList().stream().reduce(0.0, (a, b) -> a + b.getAmount(), (a, b) -> a + b);
    var lastUpdate = state.getTransactionsList().stream().max((a, b) -> TimeTo.compare(a.getTimestamp(), b.getTimestamp())).get();

    return SecondEntity.SecondAggregated
        .newBuilder()
        .setMerchantId(state.getMerchantId())
        .setEpochSecond(state.getEpochSecond())
        .setTransactionTotalAmount(total)
        .setTransactionCount(state.getTransactionsCount())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setLastUpdateTimestamp(lastUpdate.getTimestamp())
        .build();
  }
}
