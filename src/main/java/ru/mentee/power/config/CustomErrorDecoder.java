/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error(
                "Feign client error: method={}, status={}, reason={}",
                methodKey,
                response.status(),
                response.reason());

        return switch (response.status()) {
            case 400 -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Bad request to external API");
            case 404 -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Resource not found in external API");
            case 503 -> new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "External API is unavailable");
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}
