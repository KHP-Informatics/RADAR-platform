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

package org.openmhealth.shim.misfit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.DurationUnitValue;
import org.openmhealth.schema.domain.omh.SleepDuration;
import org.openmhealth.shim.common.mapper.JsonNodeMappingException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.lang.String.format;
import static org.openmhealth.schema.domain.omh.DurationUnit.SECOND;
import static org.openmhealth.schema.domain.omh.TimeInterval.ofStartDateTimeAndEndDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;


/**
 * A mapper from Misfit Resource API /activity/sleeps responses to {@link SleepDuration} objects. This mapper
 * currently creates a single data point per sleep node in the response, subtracting the duration of awake segments
 * from the sleep duration. It's also possible to create a single data point per sleep segment, which would help
 * preserve the granularity of the original data. This mapper may be updated to return a data point per segment in the
 * future.
 *
 * @author Emerson Farrugia
 * @see <a href="https://build.misfit.com/docs/references#APIReferences-Sleep">API documentation</a>
 */
public class MisfitSleepDurationDataPointMapper extends MisfitDataPointMapper<SleepDuration> {

    public static final int AWAKE_SEGMENT_TYPE = 1;

    @Override
    protected String getListNodeName() {
        return "sleeps";
    }

    @Override
    public Optional<DataPoint<SleepDuration>> asDataPoint(JsonNode sleepNode) {

        // The sleep details array contains segments corresponding to whether the user was awake, sleeping lightly,
        // or sleeping restfully for the duration of that segment. To discount the awake segments, we have to deduct
        // their duration from the total sleep duration.
        JsonNode sleepDetailsNode = asRequiredNode(sleepNode, "sleepDetails");

        long awakeDurationInSec = 0;

        OffsetDateTime previousSegmentStartDateTime = null;
        Long previousSegmentType = null;

        for (JsonNode sleepDetailSegmentNode : sleepDetailsNode) {

            OffsetDateTime startDateTime = asRequiredOffsetDateTime(sleepDetailSegmentNode, "datetime");
            Long value = asRequiredLong(sleepDetailSegmentNode, "value");

            // if the user was awake, add it to the awake tally
            if (previousSegmentType != null && previousSegmentType == AWAKE_SEGMENT_TYPE) {
                awakeDurationInSec += Duration.between(previousSegmentStartDateTime, startDateTime).getSeconds();
            }

            previousSegmentStartDateTime = startDateTime;
            previousSegmentType = value;
        }

        // checking if the segment array is empty this way avoids compiler confusion later
        if (previousSegmentType == null) {
            throw new JsonNodeMappingException(format("The Misfit sleep node '%s' has no sleep details.", sleepNode));
        }

        // to calculate the duration of last segment, first determine the overall end time
        OffsetDateTime startDateTime = asRequiredOffsetDateTime(sleepNode, "startTime");
        Long totalDurationInSec = asRequiredLong(sleepNode, "duration");
        OffsetDateTime endDateTime = startDateTime.plusSeconds(totalDurationInSec);

        if (previousSegmentType == AWAKE_SEGMENT_TYPE) {
            awakeDurationInSec += Duration.between(previousSegmentStartDateTime, endDateTime).getSeconds();
        }

        Long sleepDurationInSec = totalDurationInSec - awakeDurationInSec;

        if (sleepDurationInSec == 0) {
            return Optional.empty();
        }

        SleepDuration measure = new SleepDuration.Builder(new DurationUnitValue(SECOND, sleepDurationInSec))
                .setEffectiveTimeFrame(ofStartDateTimeAndEndDateTime(startDateTime, endDateTime))
                .build();

        String externalId = asOptionalString(sleepNode, "id").orElse(null);
        Boolean sensed = asOptionalBoolean(sleepNode, "autoDetected").orElse(null);

        return Optional.of(newDataPoint(measure, RESOURCE_API_SOURCE_NAME, externalId, sensed));
    }
}
