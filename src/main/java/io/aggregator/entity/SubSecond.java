package io.aggregator.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import lombok.Builder;
import lombok.Value;
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
  public Effect<Empty> addLedgerItems(SubSecondEntity.SubSecondState state, SubSecondApi.AddLedgerItemsCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> aggregateSubSecond(SubSecondEntity.SubSecondState state, SubSecondApi.AggregateSubSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public SubSecondEntity.SubSecondState subSecondActivated(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondActivated event) {
    return handle(state, event);
  }

  @Override
  public SubSecondEntity.SubSecondState subSecondLedgerItemsAdded(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondLedgerItemsAdded event) {
    return handle(state, event);
  }

  @Override
  public SubSecondEntity.SubSecondState subSecondAggregated(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondAggregated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(SubSecondEntity.SubSecondState state, SubSecondApi.AddLedgerItemsCommand command) {
    log.debug(Thread.currentThread().getName() + " - state: {}\nAddLedgerItemsCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: AddLedgerItemsCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SubSecondEntity.SubSecondState state, SubSecondApi.AggregateSubSecondCommand command) {
    log.debug("state: {}\nAggregateCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: AggregateSubSecondCommand");

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static SubSecondEntity.SubSecondState handle(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondActivated event) {
    log.debug(Thread.currentThread().getName() + " - SubSecondActivated: {}", event);
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: SubSecondActivated");

    return state.toBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(event.getMerchantKey().getMerchantId())
                .build())
        .setEpochSubSecond(event.getEpochSubSecond())
        .setEpochSecond(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochSecond())
        .setEpochMinute(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochMinute())
        .setEpochHour(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochDay())
        .build();
  }

  static SubSecondEntity.SubSecondState handle(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondLedgerItemsAdded event) {
    log.debug(Thread.currentThread().getName() + " - SubSecondLedgerItemsAdded: {}", event);
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: SubSecondLedgerItemsAdded");

    var newState = state.toBuilder();
    event.getLedgerEntriesList().stream()
        .filter(ledgerEntry -> state.getLedgerEntriesList().stream()
            .noneMatch(existingLedgerEntry -> existingLedgerEntry.getTransactionKey().equals(ledgerEntry.getTransactionKey())))
        .forEach(newState::addLedgerEntries);
    return newState.build();
  }

  static SubSecondEntity.SubSecondState handle(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondAggregated event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: SubSecondAggregated");

    var ledgerEntries = state.getLedgerEntriesList().stream()
        .map(ledgerEntry -> {
          if (ledgerEntry.getAggregateRequestTimestamp().getSeconds() == 0) {
            return ledgerEntry.toBuilder()
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .build();
          } else {
            return ledgerEntry;
          }
        })
        .toList();

    return state.toBuilder()
        .clearLedgerEntries()
        .addAllLedgerEntries(ledgerEntries)
        .build();
  }

  static List<?> eventsFor(SubSecondEntity.SubSecondState state, SubSecondApi.AddLedgerItemsCommand command) {
    var subSecondLedgerItemsAdded = SubSecondEntity.SubSecondLedgerItemsAdded
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .build())
        .setEpochSubSecond(command.getEpochSubSecond())
        .setTimestamp(command.getTimestamp())
        .addAllLedgerEntries(
            command.getLedgerItemList().stream()
                .map(ledgerItem -> SubSecondEntity.LedgerEntry.newBuilder()
                    .setTransactionKey(TransactionMerchantKey.TransactionKey.newBuilder()
                        .setTransactionId(command.getTransactionId())
                        .setServiceCode(ledgerItem.getServiceCode())
                        .setAccountFrom(ledgerItem.getAccountFrom())
                        .setAccountTo(ledgerItem.getAccountTo())
                        .build())
                    .setAmount(ledgerItem.getAmount())
                    .setEpochSubSecond(command.getEpochSubSecond())
                    .setTimestamp(command.getTimestamp())
                    .build())
                .collect(Collectors.toList())
        )
        .build();

    var isInactive = state.getLedgerEntriesCount() == 0 || state.getLedgerEntriesList().stream()
        .allMatch(transaction -> transaction.getAggregateRequestTimestamp().getSeconds() > 0);

    if (isInactive) {
      var subSecondActivated = SubSecondEntity.SubSecondActivated.newBuilder()
          .setMerchantKey(
              TransactionMerchantKey.MerchantKey.newBuilder()
                  .setMerchantId(command.getMerchantId())
                  .build())
          .setEpochSubSecond(command.getEpochSubSecond())
          .build();

      return List.of(subSecondActivated, subSecondLedgerItemsAdded);
    } else {
      return List.of(subSecondLedgerItemsAdded);
    }
  }

  static List<?> eventsFor(SubSecondEntity.SubSecondState state, SubSecondApi.AggregateSubSecondCommand command) {
    var ledgerEntries = state.getLedgerEntriesList().stream()
        .filter(ledgerEntry -> ledgerEntry.getAggregateRequestTimestamp().getSeconds() == 0)
        .toList();

    if (ledgerEntries.size() == 0) {
      return List.of(SubSecondEntity.SubSecondAggregated.newBuilder()
          .setMerchantKey(state.getMerchantKey())
          .setEpochSubSecond(state.getEpochSubSecond())
          .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
          .setPaymentId(command.getPaymentId())
          .build());
    } else {
      Map<MoneyTransferKey, TransactionMerchantKey.MoneyMovement> summarisedMoneyMovementsMap = new HashMap<>();
      ledgerEntries.stream()
          .map(ledgerEntry -> TransactionMerchantKey.MoneyMovement.newBuilder()
              .setAccountFrom(ledgerEntry.getTransactionKey().getAccountFrom())
              .setAccountTo(ledgerEntry.getTransactionKey().getAccountTo())
              .setAmount(ledgerEntry.getAmount())
              .build())
          .forEach(transfer -> {
            MoneyTransferKey key = MoneyTransferKey.builder()
                .from(transfer.getAccountFrom())
                .to(transfer.getAccountTo())
                .build();
            summarisedMoneyMovementsMap.merge(key, transfer, (transfer1, transfer2) -> TransactionMerchantKey.MoneyMovement.newBuilder()
                .setAccountFrom(transfer1.getAccountFrom())
                .setAccountTo(transfer1.getAccountTo())
                .setAmount(transfer1.getAmount() + transfer2.getAmount())
                .build());
          });
      var lastUpdate = ledgerEntries.stream()
          .map(SubSecondEntity.LedgerEntry::getTimestamp)
          .max(TimeTo.comparator())
          .get();

      return List.of(SubSecondEntity.SubSecondAggregated.newBuilder()
          .setMerchantKey(state.getMerchantKey())
          .setEpochSubSecond(state.getEpochSubSecond())
          .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
          .setLastUpdateTimestamp(lastUpdate)
          .setPaymentId(command.getPaymentId())
          .addAllMoneyMovements(summarisedMoneyMovementsMap.values())
          .build());
    }
  }

  @Value
  @Builder
  static class MoneyTransferKey {
    String from;
    String to;
  }
}
