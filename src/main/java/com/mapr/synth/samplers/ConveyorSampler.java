package com.mapr.synth.samplers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.synth.UnitParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class ConveyorSampler extends FieldSampler {
  private static final ObjectMapper mapper = new ObjectMapper();

  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

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

    final ObjectNode objectNode = mapper.createObjectNode();
    objectNode.set("oreType", nodeFactory.textNode(oreTypes.get(rand.nextInt(3))));
    objectNode.set("prevOreType", nodeFactory.textNode(oreTypes.get(rand.nextInt(3))));
    objectNode.set("deflectorDeg",
                   nodeFactory.numberNode(deflectorDeg + rand.nextGaussian() * deflectorDegVar));
    objectNode.set("moisturePct",
                   nodeFactory.numberNode(moisturePct + rand.nextGaussian() * moisturePctVar));
    objectNode.set("laserMilli",
                   nodeFactory.numberNode(laserMilli + rand.nextGaussian() * laserMilliVar));
    objectNode.set("flowTph", nodeFactory.numberNode(flowTph + rand.nextGaussian() * flowTphVar));
    objectNode.set("delayHr", nodeFactory.numberNode(currentTime*0.0002777));
    return objectNode;
  }

  @Override
  public String toString() {
    return "ConveyorSampler{" +
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
