package com.mapr.synth.samplers;

import com.google.common.base.Strings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.synth.distributions.WeightedRandom;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"WeakerAccess", "unused"})
public class CdrSampler extends FieldSampler {

  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

  private final WeightedRandom<CallType> callTypeRandom;
  private final WeightedRandom<Boolean> isFromHighDiscSite;
  private final WeightedRandom<Boolean> isPossibleDisc;
  private final Random random;

  public CdrSampler() {
    this(new Random());
  }

  public CdrSampler(Random random) {
    callTypeRandom = new WeightedRandom<>(random);
    isPossibleDisc = new WeightedRandom<>(random);
    this.random = random;
    callTypeRandom.add(0.15, CallType.Voice);
    callTypeRandom.add(0.25, CallType.DATA);
    callTypeRandom.add(0.25, CallType.SMS);
    callTypeRandom.add(0.35, CallType.LOCATION_UPDATE);
    isFromHighDiscSite = new WeightedRandom<>(random);
    isFromHighDiscSite.add(0.2, Boolean.TRUE);
    isFromHighDiscSite.add(0.8, Boolean.FALSE);
    isPossibleDisc.add(0.50, Boolean.TRUE);
    isPossibleDisc.add(0.50, Boolean.FALSE);
  }

  @Override
  public JsonNode sample() {
    SimpleCallDetailRecord cdr = isFromHighDiscSite.next() ? nextHighDiscSiteCdr() :
            nextRegularCdr();
    final ObjectNode node = nodeFactory.objectNode();
    node.set("callingPartyNumber", nodeFactory.textNode(cdr.callingPartyNumber));
    node.set("calledPartyNumber", nodeFactory.textNode(cdr.calledPartyNumber));
    node.set("callType", nodeFactory.textNode(cdr.callType.name()));
    node.set("prevCalledDate", nodeFactory.numberNode(cdr.prevCalledDate.toInstant().toEpochMilli()));
    node.set("calledDate", nodeFactory.numberNode(cdr.calledDate.toInstant().toEpochMilli()));
    node.set("durationInSeconds", nodeFactory.numberNode(cdr.durationInSeconds));
    node.set("billingPhoneNumber", nodeFactory.textNode(cdr.billingPhoneNumber));
    node.set("siteId", nodeFactory.numberNode(cdr.siteId));
    node.set("used2Gdata", nodeFactory.booleanNode(cdr.used2Gdata));
    return node;
  }

  private SimpleCallDetailRecord nextRegularCdr() {
    Date calledTime = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(calledTime);
    cal.add(Calendar.HOUR, -1);
    Date prevCalledTime = cal.getTime();
    CallType callType = callTypeRandom.next();
    long siteId = random.nextInt(999) + 1L;


    return new SimpleCallDetailRecord(DataModelUtils.generatePhoneNumber(random, 13),
                                      DataModelUtils.generatePhoneNumber(random, 13),
                                      callType,
                                      prevCalledTime,
                                      calledTime,
                                      DataModelUtils.randomDurationInSeconds(random, callType),
                                      DataModelUtils.generatePhoneNumber(random, 13),
                                      DataModelUtils.getCdrSequence(),
                                      siteId,
                                      false);
  }

  private SimpleCallDetailRecord nextHighDiscSiteCdr() {
    Date calledTime = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(calledTime);
    cal.add(Calendar.HOUR, -1);
    Date prevCalledTime = cal.getTime();
    String billingPhoneNumber = Strings.repeat(Integer.toString(random.nextInt(9)), 13);
    long durationInSecs = isPossibleDisc.next() ? random.nextInt(5) + 1 :
            DataModelUtils.randomDurationInSeconds(random, CallType.Voice);
    return new SimpleCallDetailRecord(DataModelUtils.generatePhoneNumber(random, 13),
                                      DataModelUtils.generatePhoneNumber(random, 13),
                                      CallType.Voice,
                                      prevCalledTime,
                                      calledTime,
                                      durationInSecs,
                                      billingPhoneNumber,
                                      DataModelUtils.getCdrSequence(),
                                      0,
                                      false);
  }

  private enum CallType {
    Voice,
    SMS,
    DATA,
    SWITCH_ON,
    LOCATION_UPDATE;

    CallType() {
    }
  }

  @SuppressWarnings("unused")
  private class SimpleCallDetailRecord {
    private String callingPartyNumber;
    private String calledPartyNumber;
    private CallType callType;
    private Date prevCalledDate;
    private Date calledDate;
    private long durationInSeconds;
    private String billingPhoneNumber;
    private long sequenceNumber;
    private long siteId;
    private boolean used2Gdata;

    public SimpleCallDetailRecord() {
    }

    SimpleCallDetailRecord(String callingPartyNumber, String calledPartyNumber,
                           CallType callType, Date prevCalledDate,
                           Date calledDate, long durationInSeconds,
                           String billingPhoneNumber, long sequenceNumber, long siteId,
                           boolean used2Gdata) {
      this.callingPartyNumber = callingPartyNumber;
      this.calledPartyNumber = calledPartyNumber;
      this.callType = callType;
      this.prevCalledDate = prevCalledDate;
      this.calledDate = calledDate;
      this.durationInSeconds = durationInSeconds;
      this.billingPhoneNumber = billingPhoneNumber;
      this.sequenceNumber = sequenceNumber;
      this.siteId = siteId;
      this.used2Gdata = used2Gdata;
    }
  }

  @SuppressWarnings("SameParameterValue")
  private static class DataModelUtils {
    private static AtomicLong cdrSequence = new AtomicLong(0);

    /**
     * Generates random phone numbers where the leading number is never 0.
     *
     * @param random the random number generator to use
     * @param length length of the number to generate
     * @return a random phone number
     */
    static String generatePhoneNumber(Random random, int length) {
      StringBuilder builder = new StringBuilder(length);

      builder.append(String.valueOf(random.nextInt(9) + 1).charAt(0));
      for (int index = 1; index < length; index++) {
        String randomNumber = String.valueOf(random.nextInt(10));
        builder.append(randomNumber.charAt(0));
      }

      return builder.toString();
    }


    /**
     * Get a random call duration in seconds with a mean of 300 seconds
     * (5 minutes) and a standard deviation of 100.
     * <p>
     * LOCATION_UPDATES and SMS are considered to have 0 second duration.
     *
     * @param random   the random number generator to use
     * @param callType call type
     * @return a random call duration in seconds
     */
    static long randomDurationInSeconds(Random random, CallType callType) {
      return randomDurationInSeconds(random, callType, 300, 150);
    }

    static long getCdrSequence() {
      return cdrSequence.getAndIncrement();
    }

    /**
     * Get a random call duration in seconds with mean and standard deviation but
     * always at least 1 second long.
     * <p>
     * LOCATION_UPDATES and SMS are considered to have 0 second duration.
     *
     * @param random   the random number generator to use
     * @param callType call type
     * @param mean     average duration in seconds
     * @param sd       standard deviation
     * @return a random call duration in seconds
     */
    static long randomDurationInSeconds(Random random, CallType callType,
                                        int mean, int sd) {
      if (callType == CallType.LOCATION_UPDATE ||
              callType == CallType.SMS) {
        return 0;
      }
      long callDuration = (long) Math.abs(random.nextGaussian() * sd + mean);
      return callDuration < 1 ? 1 : callDuration;
    }
  }

}
