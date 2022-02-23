package io.aggregator;

import com.akkaserverless.javasdk.AkkaServerless;
import io.aggregator.action.DayToHourAction;
import io.aggregator.action.FrontendAction;
import io.aggregator.action.HourToDayAction;
import io.aggregator.action.HourToMinuteAction;
import io.aggregator.action.MinuteToHourAction;
import io.aggregator.action.MinuteToSecondAction;
import io.aggregator.action.SecondToMinuteAction;
import io.aggregator.action.SecondToSubSecondAction;
import io.aggregator.action.SubSecondToSecondAction;
import io.aggregator.action.TransactionToSubSecondAction;
import io.aggregator.entity.Day;
import io.aggregator.entity.Hour;
import io.aggregator.entity.Minute;
import io.aggregator.entity.Second;
import io.aggregator.entity.SubSecond;
import io.aggregator.entity.Transaction;
import io.aggregator.view.DailyTotalsByDateView;
import io.aggregator.view.DailyTotalsByMerchantView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public final class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static AkkaServerless createAkkaServerless() {
    // The AkkaServerlessFactory automatically registers any generated Actions, Views or Entities,
    // and is kept up-to-date with any changes in your protobuf definitions.
    // If you prefer, you may remove this and manually register these components in a
    // `new AkkaServerless()` instance.
    return AkkaServerlessFactory.withComponents(
      Day::new,
      Hour::new,
      Minute::new,
      Second::new,
      SubSecond::new,
      Transaction::new,
      DailyTotalsByDateView::new,
      DailyTotalsByMerchantView::new,
      DayToHourAction::new,
      FrontendAction::new,
      HourToDayAction::new,
      HourToMinuteAction::new,
      MinuteToHourAction::new,
      MinuteToSecondAction::new,
      SecondToMinuteAction::new,
      SecondToSubSecondAction::new,
      SubSecondToSecondAction::new,
      TransactionToSubSecondAction::new);
  }

  public static void main(String[] args) throws Exception {
    LOG.info("starting the Akka Serverless service");
    createAkkaServerless().start();
  }
}
