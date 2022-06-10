package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.google.protobuf.Empty;
import io.aggregator.action.TransactionTopicConsumerAction;
import io.aggregator.action.TransactionTopicConsumerActionTestKit;
import io.aggregator.action.TransactionTopicConsumerService;
import kalix.javasdk.testkit.ActionResult;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionTopicConsumerActionTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    TransactionTopicConsumerActionTestKit service = TransactionTopicConsumerActionTestKit.of(TransactionTopicConsumerAction::new);
    // // use the testkit to execute a command
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // ActionResult<SomeResponse> result = service.someOperation(command);
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  @Ignore("to be implemented")
  public void generateTransactionTest() {
    TransactionTopicConsumerActionTestKit testKit = TransactionTopicConsumerActionTestKit.of(TransactionTopicConsumerAction::new);
    // ActionResult<Empty> result = testKit.generateTransaction(TransactionTopicConsumerService.TopicTransaction.newBuilder()...build());
  }

}
