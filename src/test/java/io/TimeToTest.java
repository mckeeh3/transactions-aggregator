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

  @Test
  public void milliSecondsTest() {
    var epochMilliSeconds = System.currentTimeMillis();
    var timestamp = TimeTo.fromEpochMilliSeconds(epochMilliSeconds).toTimestamp();

    assertEquals(epochMilliSeconds, TimeTo.fromTimestamp(timestamp).toEpochMilliSeconds());
    assertEquals(timestamp, TimeTo.fromEpochMilliSeconds(epochMilliSeconds).toTimestamp());

    assertEquals(epochMilliSeconds + 10, TimeTo.fromTimestamp(timestamp).plus().milliSeconds(10).toEpochMilliSeconds());
    assertEquals(epochMilliSeconds - 10, TimeTo.fromTimestamp(timestamp).minus().milliSeconds(10).toEpochMilliSeconds());

    assertEquals(epochMilliSeconds + 123456, TimeTo.fromTimestamp(timestamp).plus().milliSeconds(123456).toEpochMilliSeconds());
    assertEquals(epochMilliSeconds - 654321, TimeTo.fromTimestamp(timestamp).minus().milliSeconds(654321).toEpochMilliSeconds());

    var timeNow = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(timeNow).toEpochSecond();
    var epochSubSecond = TimeTo.fromTimestamp(timeNow).toEpochSubSecond();
    var subSecondMs = TimeTo.fromEpochSubSecond(epochSubSecond).plus().subSeconds(1).toEpochMilliSeconds() - TimeTo.fromEpochSubSecond(epochSubSecond).toEpochMilliSeconds();

    assertEquals(epochSubSecond + 90, TimeTo.fromEpochSubSecond(epochSubSecond).plus().milliSeconds(90 * subSecondMs).toEpochSubSecond());
    assertEquals(epochSecond + 1, TimeTo.fromEpochSubSecond(epochSubSecond).plus().milliSeconds(1_000).toEpochSecond());
  }

  @Test
  public void daysTest() {
    var epochDay = TimeTo.fromNow().toEpochDay();
    var timestamp = TimeTo.fromEpochDay(epochDay).toTimestamp();

    assertEquals(epochDay, TimeTo.fromTimestamp(timestamp).toEpochDay());
    assertEquals(timestamp, TimeTo.fromEpochDay(epochDay).toTimestamp());

    assertEquals(epochDay + 1, TimeTo.fromTimestamp(timestamp).plus().days(1).toEpochDay());
    assertEquals(epochDay - 1, TimeTo.fromTimestamp(timestamp).minus().days(1).toEpochDay());

    assertEquals(epochDay + 123456, TimeTo.fromTimestamp(timestamp).plus().days(123456).toEpochDay());
    assertEquals(epochDay - 654321, TimeTo.fromTimestamp(timestamp).minus().days(654321).toEpochDay());
  }
}
