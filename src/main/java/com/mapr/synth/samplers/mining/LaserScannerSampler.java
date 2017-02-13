package com.mapr.synth.samplers.mining;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.synth.UnitParser;
import com.mapr.synth.samplers.FieldSampler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

@SuppressWarnings("unused")
public class LaserScannerSampler extends FieldSampler {
  private static Logger log = LoggerFactory.getLogger(LaserScannerSampler.class);

  //private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

  private static final int MM_PER_M = 1000;

  private final Random rand = new Random();
  private final String laserId = String.format("s-%04x", rand.nextInt());
  private double scanInterval = 0;

  private double currentTime = 0;

  private double mean;
  private double var;
  private double min;
  private double max;

  public void setArrival(String rate) {
    this.scanInterval = 1.0 / UnitParser.parseRate(rate);
  }

  public void setStats(JsonNode jsonNode) {
    // log.debug("stats: {}", jsonNode);
    mean = UnitParser.parseDistance(jsonNode.get("mean").asText());
    var = UnitParser.parseDistance(jsonNode.get("var").asText());
    min = UnitParser.parseDistance(jsonNode.get("min").asText());
    max = UnitParser.parseDistance(jsonNode.get("max").asText());
    //log.debug("mean:{}, var:{}",mean,var);
    //log.debug("[{},{}]", min, max);
  }

  @Override
  public JsonNode sample() {
    currentTime += -Math.log(rand.nextDouble()) * scanInterval;

    final double deltaSec = roundTo(currentTime, 2);
    final double laserDistance = mean + rand.nextGaussian() * var;
    final double laser = Math.max(min, Math.min(laserDistance, max)) * MM_PER_M;
    // log.debug(id + ": dist:{} dist2:{} ts:{}", laserDistance, laser);


    final ObjectNode objectNode = nodeFactory.objectNode();
    objectNode.set("scannerId", nodeFactory.textNode(laserId));
    objectNode.set("distance", nodeFactory.numberNode(Math.floor(laser)));
    objectNode.set("delaySec", nodeFactory.numberNode(deltaSec));
    return objectNode;
  }

  private double roundTo(double num, int precision) {
    double scale = Math.pow(10, precision);
    return Math.round(num * scale) / scale;
  }
}
