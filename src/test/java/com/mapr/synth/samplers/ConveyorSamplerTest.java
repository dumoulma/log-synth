package com.mapr.synth.samplers;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertEquals;


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

    assertThat(mean.get("moisturePct"), closeTo(15.0, 0.05));
    assertThat(Math.sqrt(sd.get("moisturePct")), closeTo(5.0, 0.05));

  }

  @Test
  public void testArrivalTimes() throws IOException {
    SchemaSampler s = new SchemaSampler(Resources.toString(Resources.getResource
            ("schema034.json"), Charsets.UTF_8));
    double meanDelay = 0;
    double exponentialMean = 0;
    double sd = 0;
    double previous = 0;
    for (int i = 0; i < 10000; i++) {
      // time to first sensor should show steady increase
      JsonNode x = s.sample();
      double t = x.get("delayed").get(0).get("time").asDouble();
      double delay = t - previous;
      double expt = Math.exp(-delay / 60);
      assertTrue(expt >= 0);
      assertTrue(expt <= 1);
      exponentialMean += expt;
      sd += (expt - 0.5) * (expt - 0.5);
      previous = t;
      meanDelay += delay;
    }
    meanDelay /= 10000;
    exponentialMean /= 10000;
    sd /= 10000;
    // exponential distribution should have correct mean
    assertEquals(60, meanDelay, 3);
    // and exp(delay) should be uniformly distributed
    assertEquals(0.5, exponentialMean, 0.02);
    assertEquals(1 / 12.0, sd, 0.002);
  }

}