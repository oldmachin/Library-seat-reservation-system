package com.anonymous.common.util;

import com.anonymous.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ResultResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ResultResponseWriter() {
    }

    public static void write(HttpServletResponse response, int httpStatus, int code, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                OBJECT_MAPPER.writeValueAsString(Result.fail(code, message))
        );
    }
}
