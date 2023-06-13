<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->


# Druid DataSketches

This module provides Druid aggregators based on https://datasketches.apache.org/.

Credits: This module is a result of feedback and work done by following people.

- https://github.com/cheddar
- https://github.com/himanshug
- https://github.com/leerho
- https://github.com/will-lauer

This is a copy of the module from the commit [7cf761cee4](https://github.com/apache/druid/commit/7cf761cee42dae8c21e415b7ce3fd4191f199a38)
that is the code version promoted to the release 26.0.0. 

This repository contains a bugfix for the following bug: 
- https://github.com/apache/druid/issues/10364
- https://github.com/apache/druid/pull/12301/files

that is fixed in the upcomming release 27.0.0. However, the "fix" is a result of switching from the DataSketches 3.2.0 to 4.0.0 in which the
sorting algorithm for the Histogram is transformed from the inplace implementation to the view (copy) implementation.

The error occured only when the number of histogram bins exceeded 50 elements that triggered the sorting. 
The sorting is located in the DataSketches 3.2.0 in file `org/apache/datasketches/quantiles/DoublesPmfCdfImpl.java` in method: `private static double[] internalBuildHistogram(final DoublesSketch sketch, final double[] splitPoints)`.

This is culprit:
```
   long weight = 1;
    sketchAccessor.setLevel(DoublesSketchAccessor.BB_LVL_IDX); //base-buffer level index
    if (numSplitPoints < 50) { // empirically determined crossover
      // sort not worth it when few split points
      DoublesPmfCdfImpl.bilinearTimeIncrementHistogramCounters(
              sketchAccessor, weight, splitPoints, counters);
    } else {
      sketchAccessor.sort();
      // sort is worth it when many split points
      DoublesPmfCdfImpl.linearTimeIncrementHistogramCounters(
              sketchAccessor, weight, splitPoints, counters);
    }
```

Despite the backing memory being writable, the object reported itself as being read-only. This is done, when the undelrying DataSketch is desierialized and wrappend in a `Memory` instance, instead of `WritableMemory`. Thus the inplace sorting failed with following message:
```
Error: Unknown exception

MemoryImpl is read-only.

org.apache.datasketches.memory.ReadOnlyException
```  

Since the sorting is no longer an issiue in the new 4.0.0 release of the DataSketches, then there are no fix that can be made to the Druid upstream.

This repository contains a compiled jar extension with fix, that can be used for quickly swap the broken jar for Druid versions: 25.0.0 and 26.0.0.

## Docker Build

Build and push image with:
```bash
docker build . -t deepbi/druid:25.0.0-patch1
docker tag deepbi/druid:25.0.0-patch1 deepbi/druid:latest

docker push deepbi/druid:25.0.0-patch1
docker push deepbi/druid:latest
```

You need to login to docker hub first:
```bash
docker login
```