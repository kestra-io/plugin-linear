package io.kestra.plugin.linear.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A Linear GraphQL response that may carry an {@code errors} array alongside (or instead of) {@code data}.
 */
public interface GraphQLResponse {

    List<Object> getErrors();

    default List<String> errorMessages() {
        var errors = getErrors();
        if (errors == null || errors.isEmpty()) {
            return Collections.emptyList();
        }
        return errors.stream()
            .map(error -> error instanceof Map<?, ?> map && map.get("message") != null ? String.valueOf(map.get("message")) : String.valueOf(error))
            .toList();
    }

}
