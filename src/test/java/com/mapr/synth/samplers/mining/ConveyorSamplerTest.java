package com.mapr.synth.samplers.mining;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.mapr.synth.samplers.SchemaSampler;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;


public class ConveyorSamplerTest {
  @Test
  public void testBasics() throws IOException {
    SchemaSampler s = new SchemaSampler(
            Resources.toString(Resources.getResource("schema034.json"), Charsets.UTF_8));

    Map<String, Double> mean = new HashMap<>();
    Map<String, Double> sd = new HashMap<>();

    for (int i = 0; i < 10000; i++) {
      JsonNode x = s.sample();
      final JsonNode conveyor = x.get("conveyor");
      final double t = conveyor.get("moisturePct").asDouble();
      double oldMean = mean.getOrDefault("moisturePct", 0.0);
      mean.put("moisturePct", oldMean + (t - oldMean) / (i + 1));
      double newMean = mean.get("moisturePct");
      double oldSd = sd.getOrDefault("moisturePct", 0.0);
      sd.put("moisturePct", oldSd + ((t - oldMean) * (t - newMean) - oldSd) / (i + 1));
    }

    assertThat(mean.get("moisturePct"), closeTo(15.0, 0.1));
    assertThat(Math.sqrt(sd.get("moisturePct")), closeTo(5.0, 0.1));

  }

  @Test
  public void testArrivalTimes() throws IOException {
    SchemaSampler s = new SchemaSampler(Resources.toString(Resources.getResource
            ("schema034.json"), Charsets.UTF_8));
    double meanDelay = 0;
    // double exponentialMean = 0;
    // double sd = 0;
    double previous = 0;
    for (int i = 0; i < 10000; i++) {
      // time to first sensor should show steady increase
      JsonNode x = s.sample();
      double t = x.get("conveyor").get("delayHr").asDouble();
      double delay = t - previous;
      double expt = Math.exp(-delay / 3600);
      assertTrue(expt >= 0);
      assertTrue(expt <= 1);
      // exponentialMean += expt;
      // sd += (expt - 0.5) * (expt - 0.5);
      previous = t;
      meanDelay += delay;
    }
    meanDelay /= 10000;
    assertThat(meanDelay, closeTo(6.0, 0.6));
  }

}