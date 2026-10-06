package vn.machtrip.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/** The single error body returned by every endpoint. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(Instant timestamp, int status, String code, String message, String path,
                       List<Violation> details) {

    public record Violation(String field, String message) {
    }
}
