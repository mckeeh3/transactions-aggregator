package io.aggregator.entity;

import java.util.List;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.SubSecondApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SubSecond extends AbstractSubSecond {
  static final Logger log = LoggerFactory.getLogger(SubSecond.class);

  public SubSecond(EventSourcedEntityContext context) {
  }

  @Override
  public SubSecondEntity.SubSecondState emptyState() {
    return SubSecondEntity.SubSecondState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> addTransaction(SubSecondEntity.SubSecondState state, SubSecondApi.AddTransactionCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> aggregateSubSecond(SubSecondEntity.SubSecondState state, SubSecondApi.AggregateSubSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public SubSecondEntity.SubSecondState subSecondCreated(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondCreated event) {
    return handle(state, event);
  }

  @Override
  public SubSecondEntity.SubSecondState subSecondTransactionAdded(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondTransactionAdded event) {
    return handle(state, event);
  }

  @Override
  public SubSecondEntity.SubSecondState subSecondAggregated(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(SubSecondEntity.SubSecondState state, SubSecondApi.AddTransactionCommand command) {
    log.info("state: {}\nAddTransactionCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SubSecondEntity.SubSecondState state, SubSecondApi.AggregateSubSecondCommand command) {
    log.info("state: {}\nAggregateCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static SubSecondEntity.SubSecondState handle(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondCreated event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantId())
        .setEpochSubSecond(event.getEpochSubSecond())
        .setEpochSecond(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochSecond())
        .setEpochMinute(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochMinute())
        .setEpochHour(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochDay())
        .build();
  }

  static SubSecondEntity.SubSecondState handle(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondTransactionAdded event) {
    var transactionAlreadyAdded = state.getTransactionsList().stream()
        .anyMatch(transaction -> transaction.getTransactionId().equals(event.getTransactionId()));

    if (transactionAlreadyAdded) {
      return state; // idempotent - no need to re-add the same transaction
    } else {
      return state.toBuilder()
          .addTransactions(
              SubSecondEntity.Transaction
                  .newBuilder()
                  .setMerchantId(event.getMerchantId())
                  .setEpochSubSecond(event.getEpochSubSecond())
                  .setTransactionId(event.getTransactionId())
                  .setAmount(event.getAmount())
                  .setTimestamp(event.getTimestamp())
                  .build())
          .build();
    }
  }

  static SubSecondEntity.SubSecondState handle(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondAggregated event) {
    return state; // this is a non-state changing event
  }

  static List<?> eventsFor(SubSecondEntity.SubSecondState state, SubSecondApi.AddTransactionCommand command) {
    var transactionAdded = SubSecondEntity.SubSecondTransactionAdded
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setEpochSubSecond(command.getEpochSubSecond())
        .setTransactionId(command.getTransactionId())
        .setAmount(command.getAmount())
        .setTimestamp(command.getTimestamp())
        .build();

    if (state.getMerchantId().isEmpty()) {
      var secondCreated = SubSecondEntity.SubSecondCreated
          .newBuilder()
          .setMerchantId(command.getMerchantId())
          .setEpochSubSecond(command.getEpochSubSecond())
          .build();

      return List.of(secondCreated, transactionAdded);
    } else {
      return List.of(transactionAdded);
    }
  }

  static SubSecondEntity.SubSecondAggregated eventFor(SubSecondEntity.SubSecondState state, SubSecondApi.AggregateSubSecondCommand command) {
    var total = state.getTransactionsList().stream().reduce(0.0, (a, b) -> a + b.getAmount(), (a, b) -> a + b);
    var lastUpdate = state.getTransactionsList().stream()
        .map(transaction -> transaction.getTimestamp())
        .max(TimeTo.comparator())
        .get();

    return SubSecondEntity.SubSecondAggregated
        .newBuilder()
        .setMerchantId(state.getMerchantId())
        .setEpochSubSecond(state.getEpochSubSecond())
        .setTransactionTotalAmount(total)
        .setTransactionCount(state.getTransactionsCount())
        .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
        .setLastUpdateTimestamp(lastUpdate)
        .build();
  }
}
