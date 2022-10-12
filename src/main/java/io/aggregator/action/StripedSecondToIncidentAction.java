package io.aggregator.action;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.api.IncidentApi;
import io.aggregator.entity.StripedSecondEntity;
import kalix.javasdk.action.ActionCreationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/striped_second_to_incident_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class StripedSecondToIncidentAction extends AbstractStripedSecondToIncidentAction {
  static final Logger log = LoggerFactory.getLogger(StripedSecondToIncidentAction.class);

  public StripedSecondToIncidentAction(ActionCreationContext creationContext) {}


  @Override
  public Effect<Empty> onStripedSecondLedgerItemsAdded(StripedSecondEntity.StripedSecondLedgerItemsAdded event) {
    log.debug(Thread.currentThread().getName() + " - StripedSecondLedgerItemsAdded: {}", event);
    log.info(Thread.currentThread().getName() + " - ON EVENT: StripedSecondLedgerItemsAdded");

    List<CompletableFuture<Empty>> executes =
    event.getLedgerEntriesList().stream().map(ledgerEntry ->
      components().incident().createIncident(
              IncidentApi.CreateIncidentCommand.newBuilder()
                      .setTransactionId(ledgerEntry.getTransactionKey().getTransactionId())
                      .setMerchantId(event.getMerchantKey().getMerchantId())
                      .setShopId(event.getShopId())
                      .setServiceCode(ledgerEntry.getTransactionKey().getServiceCode())
                      .setAccountFrom(ledgerEntry.getTransactionKey().getAccountFrom())
                      .setAccountTo(ledgerEntry.getTransactionKey().getAccountTo())
                      .setIncidentAmount(ledgerEntry.getAmount())
                      .setTimestamp(event.getTimestamp())
                      .build()
      ).execute().toCompletableFuture()
    ).collect(Collectors.toList());
    CompletionStage<Empty> executesAggregation = allOf(executes).thenApply(list-> Empty.getDefaultInstance());
    return effects().asyncReply(executesAggregation);
  }

  @Override
  public Effect<Empty> onStripedSecondAggregated(StripedSecondEntity.StripedSecondAggregated event) {
    log.debug(Thread.currentThread().getName() + " - StripedSecondAggregated: {}", event);
    log.info(Thread.currentThread().getName() + " - ON EVENT: StripedSecondAggregated");

    List<CompletableFuture<Empty>> executes =
            event.getMoneyMovementsList().stream().map(moneyMovement ->
                    components().incident().addPayment(
                            IncidentApi.AddPaymentCommand.newBuilder()
                                    .setTransactionId(moneyMovement.getTransactionId())
                                    .setServiceCode(moneyMovement.getServiceCode())
                                    .setAccountFrom(moneyMovement.getAccountFrom())
                                    .setAccountTo(moneyMovement.getAccountTo())
                                    .setPaymentId(event.getPaymentId())
                                    .setTimestamp(event.getLastUpdateTimestamp())
                                    .build()
                    ).execute().toCompletableFuture()
            ).collect(Collectors.toList());
    CompletionStage<Empty> executesAggregation = allOf(executes).thenApply(list-> Empty.getDefaultInstance());
    return effects().asyncReply(executesAggregation);
  }
  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }

  public <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
    CompletableFuture<Void> allFuturesResult =
            CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
    return allFuturesResult.thenApply(v ->
            futuresList.stream().
                    map(future -> future.join()).
                    collect(Collectors.<T>toList())
    );
  }
}
