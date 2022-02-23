package io.aggregator.action;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.DayApi;
import io.aggregator.api.TransactionApi;
import io.aggregator.view.DailyTotalsByDateModel.DailyTotalsByDateRequest;
import io.aggregator.view.DailyTotalsByDateModel.DailyTotalsByDateResponse;
import io.aggregator.view.DailyTotalsModel.DailyTotal;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class FrontendAction extends AbstractFrontendAction {
  static final Random random = new Random();
  static final Logger log = LoggerFactory.getLogger(FrontendAction.class);

  public FrontendAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> generateTransactions(FrontendService.GenerateTransactionsRequest generateTransactionsRequest) {
    log.info("generateTransactions: {}", generateTransactionsRequest);

    var transactionIntervalMs = generateTransactionsRequest.getTransactionIntervalMs();
    var intervalMs = transactionIntervalMs == 0 ? 10 : transactionIntervalMs;

    var results = IntStream.range(0, generateTransactionsRequest.getTransactionCount())
        .mapToObj(i -> {
          var timestamp = TimeTo.fromTimestamp(generateTransactionsRequest.getDay()).plus().milliSeconds(i * intervalMs).toTimestamp();

          return TransactionApi.CreateTransactionCommand
              .newBuilder()
              .setTransactionId(UUID.randomUUID().toString())
              .setService("service-" + random.nextInt(10) + 1)
              .setAccount("account-" + random.nextInt(10) + 1)
              .setMerchantId("merchant-" + random.nextInt(generateTransactionsRequest.getMerchantIdRange()))
              .setTransactionAmount(random.nextInt(100) / 10.0)
              .setTransactionTimestamp(timestamp)
              .build();
        })
        .map(command -> components().transaction().createTransaction(command).execute())
        .collect(Collectors.toList());

    var result = CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()]))
        .thenApply(reply -> effects().reply(Empty.getDefaultInstance()));

    return effects().asyncEffect(result);
  }

  @Override
  public Effect<Empty> collectMerchantTotals(FrontendService.CollectMerchantTotalsRequest collectMerchantTotalsRequest) {
    log.info("collectMerchantTotals: {}", collectMerchantTotalsRequest);

    return effects().asyncReply(queryMerchants(collectMerchantTotalsRequest.getDay()));
  }

  private CompletionStage<Empty> queryMerchants(Timestamp day) {
    var epochDay = TimeTo.fromTimestamp(day).toEpochDay();
    var fromDate = TimeTo.fromEpochDay(epochDay).toTimestamp();
    var toDate = TimeTo.fromEpochDay(epochDay).plus().days(1).toTimestamp();

    log.info("epochDay: {}, fromDate: {}, toDate: {}",
        TimeTo.fromEpochDay(epochDay).format(), TimeTo.fromTimestamp(fromDate).format(), TimeTo.fromTimestamp(toDate).format());

    return components().dailyTotalsByDateView().getDailyTotalsByDate(
        DailyTotalsByDateRequest
            .newBuilder()
            .setFromDate(fromDate)
            .setToDate(toDate)
            .build())
        .execute()
        .thenCompose(response -> aggregateMerchants(response, day));
  }

  private CompletionStage<Empty> aggregateMerchants(DailyTotalsByDateResponse response, Timestamp day) {
    log.info("aggregateMerchants: ({}) {}", response.getDailyTotalsCount(), response);

    var results = response.getDailyTotalsList().stream()
        .map(dailyTotal -> toAggregateDayCommand(dailyTotal, day))
        .collect(Collectors.toList());

    return CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()]))
        .thenApply(reply -> Empty.getDefaultInstance());
  }

  private CompletionStage<Empty> toAggregateDayCommand(DailyTotal dailyTotal, Timestamp day) {
    return components().day().aggregateDay(
        DayApi.AggregateDayCommand
            .newBuilder()
            .setMerchantId(dailyTotal.getMerchantId())
            .setEpochDay(dailyTotal.getEpochDay())
            .setAggregateRequestTimestamp(day)
            .build())
        .execute();
  }
}
