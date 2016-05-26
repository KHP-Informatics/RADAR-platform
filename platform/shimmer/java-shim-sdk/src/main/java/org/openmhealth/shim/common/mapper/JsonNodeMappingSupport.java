package org.openmhealth.shim.common.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Function;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Optional.empty;


/**
 * A set of utility methods to help with mapping {@link JsonNode} objects.
 *
 * @author Emerson Farrugia
 */
public class JsonNodeMappingSupport {

    private static final Logger logger = LoggerFactory.getLogger(JsonNodeMappingSupport.class);


    /**
     * @param parentNode a parent node
     * @param path a path to a child node
     * @return the child node reached by traversing the path, where dots denote nested nodes
     * @throws MissingJsonNodeMappingException if the child node doesn't exist
     */
    public static JsonNode asRequiredNode(JsonNode parentNode, String path) {

        Iterable<String> pathSegments = Splitter.on(".").split(path);
        JsonNode node = parentNode;

        for (String pathSegment : pathSegments) {

            if (!node.hasNonNull(pathSegment)) {
                throw new MissingJsonNodeMappingException(node, pathSegment);
            }

            node = node.path(pathSegment);
        }

        return node;
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @param typeChecker the function to check if the type is compatible
     * @param converter the function to convert the node to a value
     * @param <T> the type of the value to convert to
     * @return the value of the child node
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't compatible
     */
    public static <T> T asRequiredValue(JsonNode parentNode, String path, Function<JsonNode, Boolean> typeChecker,
            Function<JsonNode, T> converter, Class<T> targetType) {

        JsonNode childNode = asRequiredNode(parentNode, path);

        if (!typeChecker.apply(childNode)) {
            throw new IncompatibleJsonNodeMappingException(parentNode, path, targetType);
        }

        return converter.apply(childNode);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a string
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't textual
     */
    public static String asRequiredString(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isTextual, JsonNode::textValue, String.class);
    }

    // TODO add tests

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a boolean
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't a boolean
     */
    public static Boolean asRequiredBoolean(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isBoolean, JsonNode::booleanValue, Boolean.class);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a long
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't an integer
     */
    public static Long asRequiredLong(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isIntegralNumber, JsonNode::longValue, Long.class);
    }

    // TODO add tests

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as an integer
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't an integer
     */
    public static Integer asRequiredInteger(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isIntegralNumber, JsonNode::intValue, Integer.class);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a double
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't numeric
     */
    public static Double asRequiredDouble(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isNumber, JsonNode::doubleValue, Double.class);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a BigDecimal
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't numeric
     */
    public static BigDecimal asRequiredBigDecimal(JsonNode parentNode, String path) {

        return asRequiredValue(parentNode, path, JsonNode::isNumber, JsonNode::decimalValue, BigDecimal.class);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a {@link LocalDate}
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't a date
     */
    // TODO overload with a DateTimeFormatter parameter
    public static LocalDate asRequiredLocalDate(JsonNode parentNode, String path) {

        String string = asRequiredString(parentNode, path);

        try {
            return LocalDate.parse(string);
        }
        catch (DateTimeParseException e) {
            throw new IncompatibleJsonNodeMappingException(parentNode, path, LocalDate.class, e);
        }
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as an {@link OffsetDateTime}
     * @throws MissingJsonNodeMappingException if the child doesn't exist
     * @throws IncompatibleJsonNodeMappingException if the value of the child node isn't a date time
     */
    public static OffsetDateTime asRequiredOffsetDateTime(JsonNode parentNode, String path) {

        String string = asRequiredString(parentNode, path);

        try {
            return OffsetDateTime.parse(string);
        }
        catch (DateTimeParseException e) {
            throw new IncompatibleJsonNodeMappingException(parentNode, path, OffsetDateTime.class, e);
        }
    }

    /**
     * @param parentNode a parent node
     * @param path a path to a child node, where dots denote nested nodes
     * @return the child node reached by traversing the path, or an empty optional if the child doesn't exist
     */
    public static Optional<JsonNode> asOptionalNode(final JsonNode parentNode, final String path) {

        Iterable<String> pathSegments = Splitter.on(".").split(path);
        JsonNode node = parentNode;

        for (String pathSegment : pathSegments) {
            JsonNode childNode = node.path(pathSegment);

            if (childNode.isMissingNode()) {
                logger.debug("A '{}' field wasn't found in node '{}'.", pathSegment, node);
                return empty();
            }

            if (childNode.isNull()) {
                logger.debug("The '{}' field is null in node '{}'.", pathSegment, node);
                return empty();
            }

            node = childNode;
        }

        return Optional.of(node);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @param typeChecker the function to check if the type is compatible
     * @param converter the function to convert the node to a value
     * @param <T> the type of the value to convert to
     * @return the value of the child node, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't compatible
     */
    public static <T> Optional<T> asOptionalValue(JsonNode parentNode, String path,
            Function<JsonNode, Boolean> typeChecker, Function<JsonNode, T> converter) {

        JsonNode childNode = asOptionalNode(parentNode, path).orElse(null);

        if (childNode == null) {
            return empty();
        }

        if (!typeChecker.apply(childNode)) {
            logger.warn("The '{}' field in node '{}' isn't compatible.", path, parentNode);
            return empty();
        }

        return Optional.of(converter.apply(childNode));
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a string, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't textual
     */
    public static Optional<String> asOptionalString(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isTextual, JsonNode::textValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a boolean, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't boolean
     */
    public static Optional<Boolean> asOptionalBoolean(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isBoolean, JsonNode::booleanValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a date time, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't a date time
     */
    public static Optional<OffsetDateTime> asOptionalOffsetDateTime(JsonNode parentNode, String path) {

        Optional<String> string = asOptionalString(parentNode, path);

        if (!string.isPresent()) {
            return empty();
        }

        OffsetDateTime dateTime = null;

        try {
            dateTime = OffsetDateTime.parse(string.get());
        }
        catch (DateTimeParseException e) {
            logger.warn("The '{}' field in node '{}' with value '{}' isn't a valid timestamp.",
                    path, parentNode, string.get(), e);
        }

        return Optional.ofNullable(dateTime);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @param formatter the formatter to use to parse the value of the child node
     * @return the value of the child node as a date time, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't a date time
     */
    public static Optional<LocalDateTime> asOptionalLocalDateTime(JsonNode parentNode, String path,
            DateTimeFormatter formatter) {

        Optional<String> string = asOptionalString(parentNode, path);

        if (!string.isPresent()) {
            return empty();
        }

        LocalDateTime dateTime = null;

        try {
            dateTime = LocalDateTime.parse(string.get(), formatter);
        }
        catch (DateTimeParseException e) {
            logger.warn("The '{}' field in node '{}' with value '{}' isn't a valid timestamp.",
                    path, parentNode, string.get(), e);
        }

        return Optional.ofNullable(dateTime);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a date time, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't a date time matching {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}
     */
    public static Optional<LocalDateTime> asOptionalLocalDateTime(JsonNode parentNode, String path) {

        return asOptionalLocalDateTime(parentNode, path, ISO_LOCAL_DATE_TIME);
    }

    // TODO refactor this by delegating to existing methods, then add tests
    public static Optional<LocalDateTime> asOptionalLocalDateTime(JsonNode parentNode, String pathToDate,
            String pathToTime) {
        Optional<String> time = asOptionalString(parentNode, pathToTime);
        Optional<String> date = asOptionalString(parentNode, pathToDate);
        if (!time.isPresent() || !date.isPresent()) {
            return empty();
        }
        LocalDateTime dateTime = null;
        try {
            dateTime = LocalDateTime.parse(date.get() + "T" + time.get(), ISO_LOCAL_DATE_TIME);
        }
        catch (DateTimeParseException e) {
            logger.warn(
                    "The '{}' and '{}' fields in node '{}' with values '{}' and '{}' do not make-up a valid timestamp.",
                    pathToDate, pathToTime, parentNode, date.get(), time.get(), e);
        }
        return Optional.ofNullable(dateTime);
    }

    // TODO add Javadoc and tests
    public static Optional<LocalDate> asOptionalLocalDate(JsonNode parentNode, String path) {

        return asOptionalLocalDate(parentNode, path, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // TODO add Javadoc and tests
    public static Optional<LocalDate> asOptionalLocalDate(JsonNode parentNode, String path,
            DateTimeFormatter dateFormat) {

        Optional<String> string = asOptionalString(parentNode, path);

        if (!string.isPresent()) {
            return empty();
        }

        LocalDate localDate = null;

        try {
            localDate = LocalDate.parse(string.get(), dateFormat);
        }
        catch (DateTimeParseException e) {
            logger.warn("The '{}' field in node '{}' with value '{}' isn't a valid date.",
                    path, parentNode, string.get(), e);
        }

        return Optional.ofNullable(localDate);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a double, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't numeric
     */
    public static Optional<Double> asOptionalDouble(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isNumber, JsonNode::doubleValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a long, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't an integer
     */
    public static Optional<Long> asOptionalLong(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isIntegralNumber, JsonNode::longValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as an integer, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't an integer
     */
    public static Optional<Integer> asOptionalInteger(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isIntegralNumber, JsonNode::intValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a Big Decimal, or an empty optional if the child doesn't exist or if the
     * value of the child node isn't numeric
     */
    public static Optional<BigDecimal> asOptionalBigDecimal(JsonNode parentNode, String path) {

        return asOptionalValue(parentNode, path, JsonNode::isNumber, JsonNode::decimalValue);
    }

    /**
     * @param parentNode a parent node
     * @param path the path to a child node
     * @return the value of the child node as a {@link ZoneId}, or an empty optional if the child doesn't exist or if
     * the value of the child node isn't a valid time zone
     */
    public static Optional<ZoneId> asOptionalZoneId(JsonNode parentNode, String path) {

        Optional<String> string = asOptionalString(parentNode, path);

        if (!string.isPresent()) {
            return empty();
        }

        ZoneId zoneId = null;

        try {
            zoneId = ZoneId.of(string.get());
        }
        catch (DateTimeException e) {
            logger.warn("The '{}' field in node '{}' with value '{}' isn't a valid time zone.",
                    path, parentNode, string.get(), e);
        }

        return Optional.ofNullable(zoneId);
    }
}
