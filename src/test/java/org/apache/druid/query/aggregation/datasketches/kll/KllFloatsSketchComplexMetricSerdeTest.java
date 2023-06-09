/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

package org.apache.druid.query.aggregation.datasketches.kll;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.datasketches.kll.KllFloatsSketch;
import org.apache.druid.data.input.MapBasedInputRow;
import org.apache.druid.segment.serde.ComplexMetricExtractor;
import org.junit.Assert;
import org.junit.Test;

public class KllFloatsSketchComplexMetricSerdeTest
{
  @Test
  public void testExtractorOnEmptyString()
  {
    final KllFloatsSketchComplexMetricSerde serde = new KllFloatsSketchComplexMetricSerde();
    final ComplexMetricExtractor extractor = serde.getExtractor();
    final KllFloatsSketch sketch = (KllFloatsSketch) extractor.extractValue(
        new MapBasedInputRow(0L, ImmutableList.of(), ImmutableMap.of("foo", "")),
        "foo"
    );
    Assert.assertEquals(0, sketch.getNumRetained());
  }

  @Test
  public void testExtractorOnPositiveNumber()
  {
    final KllFloatsSketchComplexMetricSerde serde = new KllFloatsSketchComplexMetricSerde();
    final ComplexMetricExtractor extractor = serde.getExtractor();
    final KllFloatsSketch sketch = (KllFloatsSketch) extractor.extractValue(
        new MapBasedInputRow(0L, ImmutableList.of(), ImmutableMap.of("foo", "777")),
        "foo"
    );
    Assert.assertEquals(1, sketch.getNumRetained());
    Assert.assertEquals(777d, sketch.getMaxValue(), 0.01d);
  }

  @Test
  public void testExtractorOnNegativeNumber()
  {
    final KllFloatsSketchComplexMetricSerde serde = new KllFloatsSketchComplexMetricSerde();
    final ComplexMetricExtractor extractor = serde.getExtractor();
    final KllFloatsSketch sketch = (KllFloatsSketch) extractor.extractValue(
        new MapBasedInputRow(0L, ImmutableList.of(), ImmutableMap.of("foo", "-133")),
        "foo"
    );
    Assert.assertEquals(1, sketch.getNumRetained());
    Assert.assertEquals(-133d, sketch.getMaxValue(), 0.01d);
  }

  @Test
  public void testExtractorOnDecimalNumber()
  {
    final KllFloatsSketchComplexMetricSerde serde = new KllFloatsSketchComplexMetricSerde();
    final ComplexMetricExtractor extractor = serde.getExtractor();
    final KllFloatsSketch sketch = (KllFloatsSketch) extractor.extractValue(
        new MapBasedInputRow(0L, ImmutableList.of(), ImmutableMap.of("foo", "3.1")),
        "foo"
    );
    Assert.assertEquals(1, sketch.getNumRetained());
    Assert.assertEquals(3.1d, sketch.getMaxValue(), 0.01d);
  }

  @Test
  public void testExtractorOnLeadingDecimalNumber()
  {
    final KllFloatsSketchComplexMetricSerde serde = new KllFloatsSketchComplexMetricSerde();
    final ComplexMetricExtractor extractor = serde.getExtractor();
    final KllFloatsSketch sketch = (KllFloatsSketch) extractor.extractValue(
        new MapBasedInputRow(0L, ImmutableList.of(), ImmutableMap.of("foo", ".1")),
        "foo"
    );
    Assert.assertEquals(1, sketch.getNumRetained());
    Assert.assertEquals(0.1d, sketch.getMaxValue(), 0.01d);
  }
}
