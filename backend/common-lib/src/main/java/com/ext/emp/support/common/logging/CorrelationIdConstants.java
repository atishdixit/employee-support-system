package com.ext.emp.support.common.logging;

public final class CorrelationIdConstants {

    /** HTTP header carrying the correlation id, read from the caller or generated fresh. */
    public static final String HEADER_NAME = "X-Correlation-Id";

    /** Key under which the correlation id is stored in the Log4j2 ThreadContext (MDC). */
    public static final String MDC_KEY = "correlationId";

    private CorrelationIdConstants() {
    }
}
