package io.aggregator;

import java.time.Instant;
import java.util.Comparator;

import com.google.protobuf.Timestamp;

public class TimeTo {
  public static Timestamp zero() {
    return Timestamp
        .newBuilder()
        .setSeconds(0)
        .setNanos(0)
        .build();
  }

  public static Timestamp now() {
    var now = Instant.now();
    return Timestamp
        .newBuilder()
        .setSeconds(now.getEpochSecond())
        .setNanos(now.getNano())
        .build();
  }

  public static long epochSecondFor(Timestamp timestamp) {
    return timestamp.getSeconds();
  }

  public static long epochMinuteFor(Timestamp timestamp) {
    return timestamp.getSeconds() / 60;
  }

  public static long epochHourFor(Timestamp timestamp) {
    return timestamp.getSeconds() / 60 / 60;
  }

  public static long epochDayFor(Timestamp timestamp) {
    return timestamp.getSeconds() / 60 / 60 / 24;
  }

  public static long epochMinuteFor(Long second) {
    return second / 60;
  }

  public static long epochHourFor(Long second) {
    return second / 60 / 60;
  }

  public static long epochDayFor(Long second) {
    return second / 60 / 60 / 24;
  }

  public static int compare(Timestamp timestamp1, Timestamp timestamp2) {
    if (timestamp1.getSeconds() < timestamp2.getSeconds()) {
      return -1;
    } else if (timestamp1.getSeconds() > timestamp2.getSeconds()) {
      return 1;
    } else if (timestamp1.getNanos() < timestamp2.getNanos()) {
      return -1;
    } else if (timestamp1.getNanos() > timestamp2.getNanos()) {
      return 1;
    } else {
      return 0;
    }
  }

  public static Comparator<Timestamp> comparator() {
    return (timestamp1, timestamp2) -> compare(timestamp1, timestamp2);
  }

  public static Timestamp dayTimeStampFromDay(long epochDay) {
    return Timestamp
        .newBuilder()
        .setSeconds(epochDay * 60 * 60 * 24)
        .setNanos(0)
        .build();
  }
}
