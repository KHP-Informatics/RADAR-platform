/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DataPointAcquisitionProvenance;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openmhealth.schema.domain.omh.DataPointModality.SENSED;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.SleepDuration.*;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;


/**
 * @author Chris Schaefbauer
 */
public class WithingsSleepDurationDataPointMapperUnitTests extends DataPointMapperUnitTests {

    private JsonNode responseNode;
    private WithingsSleepDurationDataPointMapper mapper = new WithingsSleepDurationDataPointMapper();

    @BeforeTest
    public void initializeResponseNode() throws IOException {

        responseNode = asJsonNode("org/openmhealth/shim/withings/mapper/withings-sleep-summary.json");
    }

    @Test
    public void asDataPointsShouldReturnCorrectNumberOfDataPoints() {

        assertThat(mapper.asDataPoints(responseNode).size(), equalTo(1));
    }

    @Test
    public void asDataPointsShouldReturnCorrectDataPoints() {

        List<DataPoint<SleepDuration>> dataPoints = mapper.asDataPoints(responseNode);

        SleepDuration expectedSleepDuration = new SleepDuration.Builder(new DurationUnitValue(SECOND, 37460))
                .setEffectiveTimeFrame(ofStartDateTimeAndEndDateTime(OffsetDateTime.parse("2014-09-12T11:34:19Z"),
                        OffsetDateTime.parse("2014-09-12T17:22:57Z")))
                .build();

        expectedSleepDuration.setAdditionalProperty("wakeup_count", 3);
        expectedSleepDuration.setAdditionalProperty("light_sleep_duration", new DurationUnitValue(SECOND, 18540));
        expectedSleepDuration.setAdditionalProperty("deep_sleep_duration", new DurationUnitValue(SECOND, 8460));
        expectedSleepDuration.setAdditionalProperty("rem_sleep_duration", new DurationUnitValue(SECOND, 10460));
        expectedSleepDuration.setAdditionalProperty("duration_to_sleep", new DurationUnitValue(SECOND, 420));

        assertThat(dataPoints.get(0).getBody(), equalTo(expectedSleepDuration));

        assertThat(dataPoints.get(0).getBody().getAdditionalProperties(),
                equalTo(expectedSleepDuration.getAdditionalProperties()));

        DataPointAcquisitionProvenance acquisitionProvenance = dataPoints.get(0).getHeader().getAcquisitionProvenance();

        assertThat(acquisitionProvenance.getSourceName(), equalTo(WithingsDataPointMapper.RESOURCE_API_SOURCE_NAME));
        assertThat(acquisitionProvenance.getModality(), equalTo(SENSED));
        assertThat(acquisitionProvenance.getAdditionalProperties().get("external_id"), equalTo(16616514L));
        assertThat(acquisitionProvenance.getAdditionalProperties().get("device_name"), equalTo("Aura"));
        assertThat(dataPoints.get(0).getHeader().getBodySchemaId(), equalTo(SCHEMA_ID));
    }
}
