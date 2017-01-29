package com.mapr.synth.samplers.mining;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.mapr.synth.samplers.SchemaSampler;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

public class LaserScannerSamplerTest {
  private static Logger log = LoggerFactory.getLogger(LaserScannerSamplerTest.class);

  @Test
  public void testBasics_laserDistanceField() throws IOException {
    SchemaSampler s = new SchemaSampler(
            Resources.toString(Resources.getResource("schema035.json"), Charsets.UTF_8));

    double mean = 0;
    double sd = 0;

    for (int i = 0; i < 10000; i++) {
      JsonNode sample = s.sample();
      //log.debug("{}", sample);
      final JsonNode conveyor = sample.get("laser");
      final double t = conveyor.get("laserDistanceMilli").asDouble();
      double oldMean = mean;
      mean = oldMean + (t - oldMean) / (i + 1);

      double oldSd = sd;
      sd = oldSd + ((t - oldMean) * (t - mean) - oldSd) / (i + 1);
      //log.debug("mean:{} sd: {}", mean, sd);
    }

    assertThat(mean, closeTo(60.0, 2));
    assertThat(Math.sqrt(sd), closeTo(30.0, 4));

  }

  @Test
  public void testArrivalTimes() throws IOException {
    SchemaSampler s = new SchemaSampler(Resources.toString(Resources.getResource
            ("schema035.json"), Charsets.UTF_8));
    double meanDelay = 0;
    // double exponentialMean = 0;
    // double sd = 0;
    double previous = 0;
    for (int i = 0; i < 10000; i++) {
      // time to first sensor should show steady increase
      JsonNode x = s.sample();
      double t = x.get("laser").get("delaySec").asDouble();
      double delay = t - previous;
      double expt = Math.exp(-delay);
      assertTrue(expt >= 0);
      assertTrue(expt <= 1);
      // exponentialMean += expt;
      // sd += (expt - 0.5) * (expt - 0.5);
      previous = t;
      meanDelay += delay;
    }
    meanDelay /= 10000;
    assertThat(meanDelay, closeTo(20.0, 1));
  }
}