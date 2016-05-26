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

package org.openmhealth.shim.fitbit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.JsonNodeDataPointMapper;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openmhealth.schema.domain.omh.DataPointHeader.Builder;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalLocalDateTime;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asRequiredNode;


/**
 * The base class for mappers that translate Fitbit API responses to data points.
 * <p>
 * Fitbit does not include time zone information in its responses. These mappers therefore set the time zone of
 * effective time frame of the measures to UTC. There is currently no way to determine the correct time zone for a data
 * point returned by the the Fitbit API.
 *
 * @author Chris Schaefbauer
 * @author Emerson Farrugia
 */
public abstract class FitbitDataPointMapper<T extends SchemaSupport> implements JsonNodeDataPointMapper<T> {

    public static final String RESOURCE_API_SOURCE_NAME = "Fitbit Resource API";

    @Override
    public List<DataPoint<T>> asDataPoints(List<JsonNode> responseNodes) {

        checkNotNull(responseNodes);
        checkArgument(responseNodes.size() == 1, "A single response node is allowed per call.");

        JsonNode listNode = asRequiredNode(responseNodes.get(0), getListNodeName());

        List<DataPoint<T>> dataPoints = Lists.newArrayList();

        for (JsonNode listEntryNode : listNode) {
            asDataPoint(listEntryNode).ifPresent(dataPoints::add);
        }

        return dataPoints;
    }

    /**
     * @return the name of the list node used by this mapper
     */
    protected abstract String getListNodeName();

    /**
     * Creates a data point.
     *
     * @param measure the measure to set as the body of the data point
     * @param externalId the identifier of the measure as recorded by the data provider
     * @param <T> the measure type
     * @return a data point
     */
    protected <T extends Measure> DataPoint<T> newDataPoint(T measure, @Nullable Long externalId) {

        DataPointAcquisitionProvenance acquisitionProvenance =
                new DataPointAcquisitionProvenance.Builder(RESOURCE_API_SOURCE_NAME).build();

        if (externalId != null) {
            acquisitionProvenance.setAdditionalProperty("external_id", externalId);
        }

        DataPointHeader header = new Builder(UUID.randomUUID().toString(), measure.getSchemaId())
                .setAcquisitionProvenance(acquisitionProvenance).build();

        return new DataPoint<>(header, measure);
    }

    /**
     * TODO rewrite this, the names don't make sense
     * @param node a JSON node containing <code>date</code> and <code>time</code> properties
     * @return the equivalent OffsetDateTime
     */
    protected Optional<OffsetDateTime> combineDateTimeAndTimezone(JsonNode node) {

        Optional<LocalDateTime> dateTime = asOptionalLocalDateTime(node, "date", "time");
        Optional<OffsetDateTime> offsetDateTime = null;

        if (dateTime.isPresent()) {
            // FIXME fix the time zone offset to use the correct offset for the data point once it is fixed by Fitbit
            offsetDateTime = Optional.of(OffsetDateTime.of(dateTime.get(), ZoneOffset.UTC));
        }

        return offsetDateTime;
    }

    /**
     * Transforms a {@link LocalDateTime} object into an {@link OffsetDateTime} object with a UTC time zone
     *
     * @param dateTime local date and time for the Fitbit response JSON node
     * @return the date and time based on the input dateTime parameter
     */
    protected OffsetDateTime combineDateTimeAndTimezone(LocalDateTime dateTime) {

        // FIXME fix the time zone offset to use the appropriate offset for the data point once it is fixed by Fitbit
        return OffsetDateTime.of(dateTime, ZoneOffset.UTC);
    }

    /**
     * Maps a JSON response node from the Fitbit API into a data point.
     *
     * @return the data point
     */
    protected abstract Optional<DataPoint<T>> asDataPoint(JsonNode node);
}
