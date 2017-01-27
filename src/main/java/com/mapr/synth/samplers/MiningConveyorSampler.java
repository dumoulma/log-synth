package com.mapr.synth.samplers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.synth.UnitParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class MiningConveyorSampler extends FieldSampler {
  private static Logger log = LoggerFactory.getLogger(MiningConveyorSampler.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);
  private static final int MM_PER_M = 1000;
  private static final double HR_PER_SEC = 1.0 / 3600;

  private final Random rand = new Random();

  private double trainInterval = 0;                                // in train/day
  private List<String> oreTypes = new ArrayList<>();
  private String prevOreType;
  private double deflectorDeg;
  private double deflectorDegVar;
  private double moisturePct;
  private double moisturePctVar;
  private double laserMilli;
  private double laserMilliVar;
  private double flowTph;
  private double flowTphVar;
  private double currentTime = 0;

  public void setArrival(String rate) {
    this.trainInterval = 1.0 / UnitParser.parseRate(rate);
  }

  public void setOreTypes(String[] ores) {
    oreTypes.addAll(Arrays.asList(ores));
  }

  public void setDeflector(String angle) {
    this.deflectorDeg = UnitParser.parseAngle(angle);
  }

  public void setDeflectorVar(String angleVar) {
    this.deflectorDegVar = UnitParser.parseAngle(angleVar);
  }

  public void setMoisture(String moisture) {
    this.moisturePct = UnitParser.parsePercentage(moisture);
  }

  public void setMoistureVar(String moistureVar) {
    this.moisturePctVar = UnitParser.parsePercentage(moistureVar);
  }

  public void setLaser(String distance) {
    this.laserMilli = UnitParser.parseDistance(distance);
  }

  public void setLaserVar(String distanceVar) {
    this.laserMilliVar = UnitParser.parseDistance(distanceVar);
  }

  public void setFlow(String flow) {
    this.flowTph = UnitParser.parseConveyorFlow(flow);
  }

  public void setFlowVar(String flowVar) {
    this.flowTphVar = UnitParser.parseConveyorFlow(flowVar);
  }

  @Override
  public JsonNode sample() {
    currentTime += -Math.log(rand.nextDouble()) * trainInterval;

    final double deflector = Math.max(0, deflectorDeg + rand.nextGaussian() * deflectorDegVar);
    double moisture = Math.max(0, moisturePct + rand.nextGaussian() * moisturePctVar);
    final double laser =
            Math.max(0, (laserMilli + rand.nextGaussian() * laserMilliVar) * MM_PER_M);
    final double flow = Math.max(0, flowTph + rand.nextGaussian() * flowTphVar);
    final double delayHr = currentTime * HR_PER_SEC;

    final ObjectNode objectNode = mapper.createObjectNode();
    objectNode.set("oreType", nodeFactory.textNode(oreTypes.get(rand.nextInt(3))));
    objectNode.set("prevOreType", nodeFactory.textNode(oreTypes.get(rand.nextInt(3))));
    objectNode.set("deflectorAngleDeg", nodeFactory.numberNode(roundTo(deflector, 1)));
    objectNode.set("moisturePct", nodeFactory.numberNode(roundTo(moisture, 1)));
    objectNode.set("laserDistanceMilli", nodeFactory.numberNode(Math.floor(laser)));
    objectNode.set("flowRateTph", nodeFactory.numberNode(roundTo(flow, 2)));
    objectNode.set("delayHr", nodeFactory.numberNode(roundTo(delayHr, 2)));
    return objectNode;
  }

  private double roundTo(double num, int precision) {
    double scale = Math.pow(10, precision);
    return Math.round(num * scale) / scale;
  }

  @Override
  public String toString() {
    return "MiningConveyorSampler{" +
            "rand=" + rand +
            ", trainInterval=" + trainInterval +
            ", oreTypes=" + oreTypes +
            ", prevOreType='" + prevOreType + '\'' +
            ", deflectorDeg=" + deflectorDeg +
            ", deflectorDegVar=" + deflectorDegVar +
            ", moisturePct=" + moisturePct +
            ", moisturePctVar=" + moisturePctVar +
            ", laserMilli=" + laserMilli +
            ", laserMilliVar=" + laserMilliVar +
            ", flowTph=" + flowTph +
            ", flowTphVar=" + flowTphVar +
            ", currentTime=" + currentTime +
            '}';
  }
}
