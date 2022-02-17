package io;

import static org.junit.Assert.*;

import org.junit.Test;

import io.aggregator.TimeTo;

public class TimeToTest {

  @Test
  public void subSecondTest() {
    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();
    var timestamp = TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp();

    assertEquals(epochSubSecond, TimeTo.fromTimestamp(timestamp).toEpochSubSecond());
    assertEquals(timestamp, TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp());
  }
}
