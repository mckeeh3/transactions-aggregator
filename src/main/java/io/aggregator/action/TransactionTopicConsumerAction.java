package io.aggregator.action;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.action.TransactionTopicConsumerService.TopicTransaction;
import io.aggregator.api.TransactionApi;
import kalix.javasdk.action.ActionCreationContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/transaction_topic_consumer_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionTopicConsumerAction extends AbstractTransactionTopicConsumerAction {
  static final Random random = new Random();
  private static final Logger log = LoggerFactory.getLogger(TransactionTopicConsumerAction.class);

  public TransactionTopicConsumerAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> transactionFromTopic(TopicTransaction topicTransaction) {
    log.debug("transactionFromTopic: {}", topicTransaction);

    var reply = components().transaction().createTransaction(
        TransactionApi.CreateTransactionCommand
            .newBuilder()
            .setTransactionId(topicTransaction.getTransactionKey().getTransactionId())
            .setMerchantId(topicTransaction.getMerchantId())
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setShopId(topicTransaction.getMerchantId())
            .setTransactionAmount(topicTransaction.getTransactionAmount())
            .setTransactionTimestamp(TimeTo.now())
            .build())
        .execute();

    return effects().asyncReply(
        reply.handle((response, ex) -> {
          if (ex != null) {
            log.warn("transactionFromTopic: {}", ex.getMessage());
            log.error("transactionFromTopic: failed", ex);
            return Empty.getDefaultInstance(); // this is where unhandled messages should be directed/dead letters
          }

          return Empty.getDefaultInstance();
        }));
  }
}
