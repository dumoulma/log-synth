package com.mapr.synth.samplers.telecom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.synth.samplers.FieldSampler;
import com.mapr.synth.samplers.StringSampler;

import java.util.Random;

@SuppressWarnings("unused")
public class CdrSampler extends FieldSampler {

  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

  @SuppressWarnings("FieldCanBeLocal")
  private final Random random;

  private CallTypeSampler callTypeSampler = new CallTypeSampler();

  private String callingPartyNumber;
  private String calledPartyNumber;
  private String callType;
  private java.util.Date prevCalledDate;
  private java.util.Date calledDate;
  private long durationInSeconds;
  private String billingPhoneNumber;
  private long siteId;
  private boolean used2Gdata;

  public CdrSampler() {
    this(new Random());
  }

  @SuppressWarnings("WeakerAccess")
  public CdrSampler(Random random) {
    this.random = random;
  }

  @Override
  public JsonNode sample() {
    final ObjectNode node = nodeFactory.objectNode();
    node.set("callingPartyNumber", nodeFactory.textNode(generatePhoneNumber(random, 10)));
    node.set("calledPartyNumber", nodeFactory.textNode(generatePhoneNumber(random, 10)));
    node.set("callType", callTypeSampler.sample());
    node.set("calledDate", nodeFactory.numberNode(calledDate.getTime()));
    node.set("durationInSeconds", nodeFactory.numberNode(durationInSeconds));

    node.set("billingPhoneNumber", nodeFactory.textNode(generatePhoneNumber(random, 10)));

    // 10 sites
    node.set("siteId", nodeFactory.numberNode(random.nextLong() % 10L));

    // about 21%
    node.set("used2Gdata", nodeFactory.booleanNode(random.nextGaussian() < -2.0 ? true : false));
    return node;
  }

  private String generatePhoneNumber(Random random, int length) {
    StringBuilder builder = new StringBuilder(length);

    builder.append(String.valueOf(random.nextInt(9) + 1).charAt(0));
    for (int index = 1; index < length; index++) {
      String randomNumber = String.valueOf(random.nextInt(10));
      builder.append(randomNumber.charAt(0));
    }

    return builder.toString();
  }

  private class CallTypeSampler extends StringSampler {
    CallTypeSampler() {
      readDistribution("telecom/dist.calltype");
    }
  }
}
