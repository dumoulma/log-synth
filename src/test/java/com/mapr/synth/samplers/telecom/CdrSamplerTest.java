/*
 * Licenced to Mathieu Dumoulin under one or more contributor license
 * agreements.  See the NOTICE file that may be
 * distributed with this work for additional information
 * regarding copyright ownership.  Ted Dunning licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.mapr.synth.samplers.telecom;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.mapr.synth.samplers.SchemaSampler;

import org.junit.Test;

import java.io.IOException;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CdrSamplerTest {
  private SchemaSampler s = new SchemaSampler(
          Resources.toString(Resources.getResource("schema040.json"), Charsets.UTF_8));

  public CdrSamplerTest() throws IOException {
  }

  @Test
  public void testArrivalTimes() throws IOException {
    final OptionalDouble average = IntStream.range(0, 10000).boxed()
            .map(i -> s.sample().get("cdr1"))
            .mapToLong(x -> x.get("delayMs").asLong())
            .average();

    assertTrue(average.isPresent());
    System.out.println("Avg delay: " + average.getAsDouble());
    assertEquals(600.0, average.getAsDouble(), 25);
  }

  @Test
  public void testCallDurationInNormalSite() throws IOException {
    final OptionalDouble average = IntStream.range(0, 10000).boxed()
            .map(i -> s.sample().get("cdr1"))
            .filter(x -> x.get("siteId").asLong() == 1L)
            .mapToDouble(x -> x.get("callDuration").asDouble())
            .average();

    assertTrue(average.isPresent());
    System.out.println("Avg for site 1: " + average.getAsDouble());
    assertEquals(100.0, average.getAsDouble(), 5);
  }

  @Test
  public void testCallDurationInProblemSite() throws IOException {
    final OptionalDouble average = IntStream.range(0, 10000).skip(1000).boxed()
            .map(i -> s.sample().get("cdr1"))
            .filter(x -> x.get("siteId").asLong() == 0L)
            .mapToDouble(x -> x.get("callDuration").asDouble())
            .average();

    assertTrue(average.isPresent());
    System.out.println("Avg for problem site 0: " + average.getAsDouble());
    assertEquals(3, average.getAsDouble(), 0.1);
  }


}
