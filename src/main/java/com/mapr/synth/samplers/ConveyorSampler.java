package com.mapr.synth.samplers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mapr.synth.UnitParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import processing.data.JSONArray;


/*
[
  {"name":"conveyor1", "class":"conveyor", "arrival": "4/day",
    "oretype": ["A","B","C"],
    "deflector": "10 deg", "deflectorvar": "5 deg",
    "moisture": "15 %", "moisturevar": "5 %",
    "laser": "50 mm", "laservar": "20 mm",
    "flow": "6000 tph", "flowvar": "500 tph"
  }
]
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class ConveyorSampler extends FieldSampler {
  private static final ObjectMapper mapper = new ObjectMapper();

  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

  private final Random rand = new Random();

  private double trainInterval = 0;                                // in train/day
  private List<String> oreTypes;
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

  public void setOreType(JSONArray jsonArray) {
    oreTypes = Arrays.asList(jsonArray.getStringArray());
  }

  public void setDeflector(String angle) {
    this.deflectorDeg = UnitParser.parseAngle(angle);
  }

  public void setDeflectorVar(String angleVar) {
    this.deflectorDegVar = UnitParser.parseAngle(angleVar);
  }

  public void setMoisture(String moisture) {
    this.moisturePct = 1.0 / UnitParser.parsePercentage(moisture);
  }

  public void setMoistureVar(String moistureVar) {
    this.moisturePctVar = 1.0 / UnitParser.parsePercentage(moistureVar);
  }

  public void setLaser(String distance) {
    this.laserMilli = 1.0 / UnitParser.parseDistance(distance);
  }

  public void setLaserVar(String distanceVar) {
    this.laserMilliVar = 1.0 / UnitParser.parseRate(distanceVar);
  }

  public void setFlow(String rate) {
    this.flowTph = 1.0 / UnitParser.parseConveyorFlow(rate);
  }

  public void setFlowVar(String rate) {
    this.flowTphVar = 1.0 / UnitParser.parseRate(rate);
  }

  @Override
  public JsonNode sample() {
    currentTime += -Math.log(rand.nextDouble()) * trainInterval;

    //double nominalSpeed = averageSpeed + rand.nextGaussian() * sdSpeed;

    double t = currentTime;
    double location = 0;
    Map<String, String> data = new HashMap<>();
    data.put("oreType", oreTypes.get(rand.nextInt(3)));
    data.put("prevOreType", oreTypes.get(rand.nextInt(3)));
    data.put("deflectorDeg", "");
    data.put("deflectorDegVar", "");
    data.put("moisturePct", "");
    data.put("moisturePctVar", "");
    data.put("laserMilli", "");
    data.put("laserMilliVar", "");
    data.put("flowTph", "");
    data.put("flowTphVar", "");
    try {
      return mapper.valueToTree(mapper.writeValueAsString(data));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return nodeFactory.objectNode();
  }
}
