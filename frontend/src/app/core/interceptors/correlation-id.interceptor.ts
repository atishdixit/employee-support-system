import { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs';

const CORRELATION_ID_HEADER = 'X-Correlation-Id';

/**
 * Forwards the correlation id the backend assigned on the previous response as a request
 * header on the next call, so an entire browser session can be traced end-to-end through the
 * backend's log file (see CorrelationIdFilter in the backend's common-lib module).
 */
let lastCorrelationId: string | null = null;

export const correlationIdInterceptor: HttpInterceptorFn = (req, next) => {
  const request = lastCorrelationId
    ? req.clone({ setHeaders: { [CORRELATION_ID_HEADER]: lastCorrelationId } })
    : req;

  return next(request).pipe(
    tap((event: any) => {
      const headerValue = event?.headers?.get?.(CORRELATION_ID_HEADER);
      if (headerValue) {
        lastCorrelationId = headerValue;
      }
    })
  );
};
