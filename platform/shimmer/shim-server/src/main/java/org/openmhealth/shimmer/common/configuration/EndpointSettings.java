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

package org.openmhealth.shimmer.common.configuration;

import org.springframework.web.util.UriTemplate;

import java.util.Optional;


/**
 * @author Emerson Farrugia
 */
public interface EndpointSettings {

    /**
     * @return the identifier of this endpoint
     */
    String getId();

    /**
     * @return the URI template used to send HTTP requests to this endpoint
     */
    UriTemplate getUriTemplate();

    /**
     * @return the settings used by this endpoint to handle creation date time range queries
     */
    Optional<DateTimeQuerySettings> getCreationDateTimeQuerySettings();

    /**
     * @return true if this endpoint supports creation data time range queries
     */
    default boolean supportsCreationDateTimeQueries() {
        return getCreationDateTimeQuerySettings().isPresent();
    }

    /**
     * @return the settings used by this endpoint to handle effective date time range queries
     */
    Optional<DateTimeQuerySettings> getEffectiveDateTimeQuerySettings();

    /**
     * @return true if this endpoint supports effective data time range queries
     */
    default boolean supportsEffectiveDateTimeQueries() {
        return getEffectiveDateTimeQuerySettings().isPresent();
    }

    /**
     * @return the settings used by this endpoint to handle modification date time range queries
     */
    Optional<DateTimeQuerySettings> getModificationDateTimeQuerySettings();

    /**
     * @return true if this endpoint supports modification data time range queries
     */
    default boolean supportsModificationDateTimeQueries() {
        return getModificationDateTimeQuerySettings().isPresent();
    }
}
