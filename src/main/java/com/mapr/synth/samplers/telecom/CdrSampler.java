package com.mapr.synth.samplers.telecom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.synth.samplers.ArrivalSampler;
import com.mapr.synth.samplers.FieldSampler;
import com.mapr.synth.samplers.StringSampler;
import com.mapr.synth.samplers.VectorSampler;

import java.text.ParseException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CdrSampler extends FieldSampler {

  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

  @SuppressWarnings("FieldCanBeLocal")
  private final Random random;

  private CallTypeSampler callTypeSampler = new CallTypeSampler();
  private ArrivalSampler arrivalSampler = new ArrivalSampler();
  private JsonNode callDurationSamples;
  private JsonNode possibleDiscs;
  private final AtomicInteger cdIndex = new AtomicInteger();
  private final AtomicInteger pdIndex = new AtomicInteger();
  private int numSites;
  private long problemSiteId;
  private long startTs;
  private long prevCallTs;
  private int startDelayMs = 0;
  private JsonNode callDurationNode;
  private JsonNode problemCdNode;

  public CdrSampler() {
    this(new Random());
  }

  @SuppressWarnings("WeakerAccess")
  public CdrSampler(Random random) {
    this.random = random;
  }

  public void setSites(String numSites) {
    this.numSites = Integer.parseInt(numSites);
  }

  void setCallDuration(JsonNode node) {
    this.callDurationNode = node;
    VectorSampler sampler = new VectorSampler();
    sampler.setMin(node.get("min").asDouble());
    sampler.setMean(node.get("mean").asDouble());
    sampler.setSd(node.get("sd").asDouble());
    sampler.setMax(node.get("max").asDouble());
    sampler.setResolution(0.1);
    callDurationSamples = sampler.sample();
  }

  void setPossibleDisc(JsonNode node) {
    this.problemCdNode = node;
    VectorSampler sampler = new VectorSampler();
    sampler.setMin(node.get("min").asDouble());
    sampler.setMax(node.get("max").asDouble());
    sampler.setResolution(0.1);
    startDelayMs = node.get("startDelaySec").asInt() * 1000;
    possibleDiscs = sampler.sample();
  }

  public void setArrival(String arrivalRate) throws ParseException {
    arrivalSampler.setRate(arrivalRate);
    arrivalSampler.setFormat("Q");
    startTs = arrivalSampler.sample().asLong();
    prevCallTs = startTs;
  }

  public void setProblemSite(String id) {
    problemSiteId = Long.parseLong(id);
  }

  @Override
  public JsonNode sample() {
    if (cdIndex.get() >= 10000) {
      setCallDuration(callDurationNode);
      cdIndex.set(0);
    }
    if (pdIndex.get() >= 10000) {
      setPossibleDisc(problemCdNode);
      pdIndex.set(0);
    }
    final long nextCallTs = arrivalSampler.sample().asLong();
    final long delayMs = nextCallTs - startTs;
    final long siteId = random.nextInt(numSites);
    final double callDuration = siteId != problemSiteId || delayMs < startDelayMs ?
            callDurationSamples.get(cdIndex.getAndIncrement()).asDouble() :
            possibleDiscs.get(pdIndex.getAndIncrement()).asDouble();

    return getJsonNodes(delayMs, siteId, callDuration);
  }

  private ObjectNode getJsonNodes(long delayMs, long siteId, double callDuration) {
    final ObjectNode node = nodeFactory.objectNode();
    node.set("callingPartyNumber", nodeFactory.textNode(generatePhoneNumber(random)));
    node.set("calledPartyNumber", nodeFactory.textNode(generatePhoneNumber(random)));
    node.set("callType", callTypeSampler.sample());
    node.set("delayMs", nodeFactory.numberNode(delayMs));
    node.set("callDuration", nodeFactory.numberNode(Math.round(callDuration)));
    node.set("siteId", nodeFactory.textNode(String.valueOf(siteId)));
    return node;
  }

  private static String generatePhoneNumber(Random random) {
    return generatePhoneNumber(random, 10);
  }

  @SuppressWarnings("SameParameterValue")
  private static String generatePhoneNumber(Random random, int length) {
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
