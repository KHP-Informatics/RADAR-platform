package org.openmhealth.shim.googlefit.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.*;
import org.openmhealth.shim.common.mapper.DataPointMapperUnitTests;
import org.openmhealth.shim.googlefit.common.GoogleFitTestProperties;

import java.io.IOException;
import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.openmhealth.schema.domain.omh.DataPointModality.SELF_REPORTED;
import static org.openmhealth.shim.googlefit.mapper.GoogleFitDataPointMapper.RESOURCE_API_SOURCE_NAME;


/**
 * Base class for unit tests that evaluate individual data point mappers, used to build the measure specific unit
 * tests.
 *
 * @author Chris Schaefbauer
 */
public abstract class GoogleFitDataPointMapperUnitTests<T extends Measure> extends DataPointMapperUnitTests {

    protected JsonNode responseNode;

    public abstract void initializeResponseNode() throws IOException;

    /**
     * Implemented by measure specific test classes in order to test the {@link Measure} contained within the mapper
     * created {@link DataPoint}. Should contain the assertions needed to test the individual values in the measure.
     */
    public abstract void assertThatMeasureMatches(T testMeasure, GoogleFitTestProperties testProperties);

    /**
     * Used to test data points created through measure specific Google Fit mappers.
     *
     * @param dataPoint data point created by the mapper
     * @param testProperties properties to test against the mapper generated data point
     */
    public void assertThatDataPointMatches(DataPoint<T> dataPoint, GoogleFitTestProperties testProperties) {

        assertThatMeasureMatches(dataPoint.getBody(), testProperties);
        DataPointHeader dataPointHeader = dataPoint.getHeader();

        assertThat(dataPointHeader.getAcquisitionProvenance().getSourceName(), equalTo(RESOURCE_API_SOURCE_NAME));

        assertThat(dataPointHeader.getAcquisitionProvenance().getAdditionalProperties().get(
                "source_origin_id"), equalTo(testProperties.getSourceOriginId()));

        assertThat(dataPointHeader.getBodySchemaId(), equalTo(testProperties.getBodySchemaId()));

        if (testProperties.getModality().isPresent()) {

            assertThat(dataPointHeader.getAcquisitionProvenance().getModality(),
                    equalTo(testProperties.getModality().get()));
        }
        else {

            assertThat(dataPointHeader.getAcquisitionProvenance().getModality(), nullValue());
        }
    }

    /**
     * Creates a test properties object used to generate an expected value data point to test google fit data points
     * that use floating point values in their response.
     */
    public GoogleFitTestProperties createFloatingPointTestProperties(double fpValue, String startDateTime,
            String endDateTime, String sourceOriginId, SchemaId schemaId) {

        GoogleFitTestProperties testProperties =
                createTestProperties(startDateTime, endDateTime, sourceOriginId, schemaId);

        testProperties.addFloatingPointProperty(fpValue);

        return testProperties;
    }

    /**
     * Creates a test properties object used to generate an expected value data point to test google fit data points
     * that use integer values in their response.
     */
    public GoogleFitTestProperties createIntegerTestProperties(long intValue, String startDateTime, String endDateTime,
            String sourceOriginId, SchemaId schemaId) {

        GoogleFitTestProperties testProperties =
                createTestProperties(startDateTime, endDateTime, sourceOriginId, schemaId);

        testProperties.setIntValue(intValue);

        return testProperties;
    }

    /**
     * Creates a test properties object used to generate an expected value data point to test google fit data points
     * that use strings to represent values.
     */
    public GoogleFitTestProperties createStringTestProperties(String stringValue, String startDateTime,
            String endDateTime,
            String sourceOriginId,
            SchemaId schemaId) {

        GoogleFitTestProperties testProperties =
                createTestProperties(startDateTime, endDateTime, sourceOriginId, schemaId);

        testProperties.setStringValue(stringValue);

        return testProperties;
    }

    private GoogleFitTestProperties createTestProperties(String startDateTimeString, String endDateTimeString,
            String sourceOriginId, SchemaId schemaId) {

        GoogleFitTestProperties testProperties = new GoogleFitTestProperties();

        if (startDateTimeString != null) {
            testProperties.setStartDateTime(startDateTimeString);
        }

        if (endDateTimeString != null) {
            testProperties.setEndDateTime(endDateTimeString);
        }

        if (sourceOriginId != null) {

            testProperties.setSourceOriginId(sourceOriginId);

            if (sourceOriginId.endsWith("user_input")) {
                testProperties.setModality(SELF_REPORTED);
            }
        }

        testProperties.setBodySchemaId(schemaId);

        return testProperties;
    }

    /**
     * Sets the effective time frame for a data point builder given a {@link GoogleFitTestProperties} object.
     */
    public void setExpectedEffectiveTimeFrame(T.Builder builder, GoogleFitTestProperties testProperties) {

        if (testProperties.getEndDateTime().isPresent()) {

            builder.setEffectiveTimeFrame(TimeInterval.ofStartDateTimeAndEndDateTime(
                    OffsetDateTime.parse(testProperties.getStartDateTime().get()),
                    OffsetDateTime.parse(testProperties.getEndDateTime().get())));
        }
        else {
            builder.setEffectiveTimeFrame(OffsetDateTime.parse(testProperties.getStartDateTime().get()));
        }
    }
}
