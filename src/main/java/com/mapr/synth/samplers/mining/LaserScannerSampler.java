package com.mapr.synth.samplers.mining;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

  private static final int MM_PER_M = 1000;

  private final Random rand = new Random();

  private double scanInterval = 0;

  private double laserMilli;
  private double laserMilliVar;
  private double currentTime = 0;

  public void setArrival(String rate) {
    this.scanInterval = 1.0 / UnitParser.parseRate(rate);
  }

  public void setStats(JsonNode jsonObject) {

  }

  public void setLaser(String distance) {
    this.laserMilli = UnitParser.parseDistance(distance);
  }

  public void setLaserVar(String distanceVar) {
    this.laserMilliVar = UnitParser.parseDistance(distanceVar);
  }

  @Override
  public JsonNode sample() {
    currentTime += -Math.log(rand.nextDouble()) * scanInterval;

    final double laser =
            Math.max(0, (laserMilli + rand.nextGaussian() * laserMilliVar) * MM_PER_M);

    final ObjectNode objectNode = mapper.createObjectNode();
    objectNode.set("laserDistanceMilli", nodeFactory.numberNode(Math.floor(laser)));
    objectNode.set("delaySec", nodeFactory.numberNode(currentTime));
    return objectNode;
  }
}
