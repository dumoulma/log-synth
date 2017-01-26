/*
 * Licensed to the Ted Dunning under one or more contributor license
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

package com.mapr.synth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UnitParserTest {
  @Test
  public void testParseSpeed() throws Exception {
    assertEquals(1.0, UnitParser.parseSpeed("1 m/s"), 0);
    assertEquals(1000.0 / 3600.0, UnitParser.parseSpeed("1 kph"), 0);
    assertEquals(2.54e-2 * 12 * 5280 / 3600.0, UnitParser.parseSpeed(" 1mph"), 0);
    try {
      UnitParser.parseSpeed(" 1mile");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid speed"));
    }
  }

  @Test
  public void testParseDistance() throws Exception {
    assertEquals(1.0, UnitParser.parseDistance("1 m  "), 0);
    assertEquals(1000.0, UnitParser.parseDistance("1   km"), 0);
    assertEquals(2.54e-2 * 12 * 5280, UnitParser.parseDistance(" 1mile"), 0);
    try {
      UnitParser.parseDistance(" 1 mph");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(), e.getMessage().contains("Invalid location"));
    }
  }

  @Test
  public void testParseTime() throws Exception {
    assertEquals(1.0, UnitParser.parseTime("1 s  "), 0);
    assertEquals(60.0, UnitParser.parseTime("1 m  "), 0);
    assertEquals(600.0, UnitParser.parseTime("10 minute"), 0);
    assertEquals(3600.0, UnitParser.parseTime("1   hour"), 0);
    assertEquals(24 * 3600.0, UnitParser.parseTime("1   day"), 0);
    try {
      UnitParser.parseTime(" 1 mph");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(), e.getMessage().contains("Invalid time"));
    }
  }

  @Test
  public void testParseRate() throws Exception {
    assertEquals(1.0, UnitParser.parseRate("1 /s  "), 0);
    assertEquals(1 / 60.0, UnitParser.parseRate("1 /m  "), 0);
    assertEquals(10 / 60.0, UnitParser.parseRate("10/ minute"), 0);
    assertEquals(23 / 3600.0, UnitParser.parseRate("23  / hour"), 0);
    assertEquals(1.0 / 24.0 / 3600.0, UnitParser.parseRate("1/day "), 0);
    try {
      UnitParser.parseRate(" 1 mph");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(), e.getMessage().contains("Invalid rate"));
    }
    try {
      UnitParser.parseRate(" 1 / xyz");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(), e.getMessage().contains("Invalid rate"));
    }
  }

  @Test
  public void testParseDistanceUnit() throws Exception {
    assertEquals(1, UnitParser.parseDistanceUnit("m"), 0);
    assertEquals(1000, UnitParser.parseDistanceUnit(" km"), 0);
    assertEquals(2.54e-2 * 12 * 5280, UnitParser.parseDistanceUnit(" miles "), 0);
    try {
      assertEquals(2.54e-2 * 12 * 5280, UnitParser.parseDistanceUnit(" hours"), 0);
      fail("Should have caught bad unit");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad message: ", e.getMessage().contains("Bad unit"));
    }
  }

  @Test
  public void testParseAngle() throws Exception {
    assertEquals(1.0, UnitParser.parseAngle("1 deg  "), 0);
    assertEquals(10.0, UnitParser.parseAngle("10 deg  "), 0);
    assertEquals(0.5, UnitParser.parseAngle("0.5 deg  "), 0);
    try {
      UnitParser.parseAngle(" 1 mph");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(), e.getMessage().contains("Invalid angle"));
    }
    try {
      UnitParser.parseAngle(" 1 / xyz");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(), e.getMessage().contains("Invalid angle"));
    }
  }

  @Test
  public void testParsePercentage() throws Exception {
    assertEquals(1.0, UnitParser.parsePercentage("1 %  "), 0);
    assertEquals(10.0, UnitParser.parsePercentage("10.0 %  "), 0);
    assertEquals(100.5, UnitParser.parsePercentage("100.5 %  "), 0);
    try {
      UnitParser.parsePercentage(" 1 mph");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(), e.getMessage().contains("Invalid percentage"));
    }
    try {
      UnitParser.parsePercentage(" 1 / xyz");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(), e.getMessage().contains("Invalid percentage"));
    }
  }

  @Test
  public void testParseConveyorFlow() throws Exception {
    assertEquals(1.0, UnitParser.parseConveyorFlow("1 tph  "), 0.001);
    assertEquals(10.0, UnitParser.parseConveyorFlow("10.0 tph  "), 0.001);
    assertEquals(5.5, UnitParser.parseConveyorFlow("5.5 tph  "), 0.001);
    try {
      UnitParser.parseConveyorFlow(" 1 mph");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(),
                 e.getMessage().contains("Invalid conveyor flow"));
    }
    try {
      UnitParser.parseConveyorFlow(" 1 / xyz");
      fail("Should have caught bad expression");
    } catch (IllegalArgumentException e) {
      assertTrue("Bad exception: " + e.getMessage(),
                 e.getMessage().contains("Invalid conveyor flow"));
    }
  }
}