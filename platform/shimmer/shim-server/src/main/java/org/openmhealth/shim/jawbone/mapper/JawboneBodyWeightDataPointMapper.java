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

package org.openmhealth.shim.jawbone.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.BodyWeight;
import org.openmhealth.schema.domain.omh.MassUnitValue;
import org.openmhealth.schema.domain.omh.Measure;

import java.util.Optional;

import static org.openmhealth.schema.domain.omh.MassUnit.KILOGRAM;
import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.asOptionalDouble;
import static org.openmhealth.shim.jawbone.mapper.JawboneBodyEventType.BODY_WEIGHT;


/**
 * @author Chris Schaefbauer
 * @see <a href="https://jawbone.com/up/developer/endpoints/body">API documentation</a>
 */
public class JawboneBodyWeightDataPointMapper extends JawboneBodyEventsDataPointMapper<BodyWeight> {

    @Override
    protected Optional<Measure.Builder<BodyWeight, ?>> newMeasureBuilder(JsonNode listEntryNode) {

        Optional<Double> optionalWeight = asOptionalDouble(listEntryNode, "weight");

        if (!optionalWeight.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new BodyWeight.Builder(new MassUnitValue(KILOGRAM, optionalWeight.get())));
    }

    @Override
    protected JawboneBodyEventType getBodyEventType() {
        return BODY_WEIGHT;
    }
}
