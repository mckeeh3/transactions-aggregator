package io;

import static org.junit.Assert.*;

import org.junit.Test;

import io.aggregator.TimeTo;

public class TimeToTest {

  @Test
  public void secondsTest() {
    var timestamp1 = TimeTo.now();
    var timestamp2 = TimeTo.fromTimestamp(timestamp1).plus().seconds(5).toTimestamp();

    assertEquals(5, timestamp2.getSeconds() - timestamp1.getSeconds());
  }

  @Test
  public void minutesTest() {
    var timestamp1 = TimeTo.now();
    var timestamp2 = TimeTo.fromTimestamp(timestamp1).plus().minutes(5).toTimestamp();

    assertEquals(5 * 60, timestamp2.getSeconds() - timestamp1.getSeconds());
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

  @Test
  public void maxTest() {
    var t0 = TimeTo.now();
    var t1 = TimeTo.fromTimestamp(t0).plus().days(1).toTimestamp();
    var t2 = TimeTo.fromTimestamp(t0).plus().days(2).toTimestamp();

    assertEquals(t1, TimeTo.max(t0, t1));
    assertEquals(t2, TimeTo.max(t1, t0, t2));
  }
}
