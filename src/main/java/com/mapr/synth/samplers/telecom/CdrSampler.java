package com.mapr.synth.samplers.telecom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.synth.samplers.ArrivalSampler;
import com.mapr.synth.samplers.FieldSampler;
import com.mapr.synth.samplers.StringSampler;
import com.mapr.synth.samplers.VectorSampler;

import java.util.Random;

@SuppressWarnings("unused")
public class CdrSampler extends FieldSampler {

  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

  @SuppressWarnings("FieldCanBeLocal")
  private final Random random;

  private CallTypeSampler callTypeSampler = new CallTypeSampler();
  private VectorSampler callDurationSample = new VectorSampler();
  private ArrivalSampler arrivalSampler = new ArrivalSampler();

  private long numSites;


  public CdrSampler() {
    this(new Random());
  }

  @SuppressWarnings("WeakerAccess")
  public CdrSampler(Random random) {
    this.random = random;
  }

  public void setSites(long numSites) {
    this.numSites = numSites;
  }

  public void setCallDuration(JsonNode node) {
    callDurationSample.setMin(node.get("min").asDouble());
    callDurationSample.setMean(node.get("mean").asDouble());
    callDurationSample.setSd(node.get("sd").asDouble());
    callDurationSample.setMax(node.get("max").asDouble());
  }

  public void setArrival(String arrivalRate) {
    arrivalSampler.setRate(arrivalRate);
  }

  @Override
  public JsonNode sample() {
    final ObjectNode node = nodeFactory.objectNode();
    node.set("callingPartyNumber", nodeFactory.textNode(generatePhoneNumber(random, 10)));
    node.set("calledPartyNumber", nodeFactory.textNode(generatePhoneNumber(random, 10)));
    node.set("callType", callTypeSampler.sample());

    node.set("calledDate", arrivalSampler.sample());
    node.set("durationInSeconds", callDurationSample.sample());

    node.set("billingPhoneNumber", nodeFactory.textNode(generatePhoneNumber(random, 10)));

    // 10 sites
    node.set("siteId", nodeFactory.numberNode(random.nextLong() % numSites));

    // about 21%
    node.set("used2Gdata", nodeFactory.booleanNode(random.nextGaussian() < -2.0));
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
