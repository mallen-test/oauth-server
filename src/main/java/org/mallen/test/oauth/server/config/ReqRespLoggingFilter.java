package org.mallen.test.oauth.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用于打印http请求和响应
 * 不支持multipart形式的打印
 * 支持打印日志时的敏感词过滤
 * Created by mallen on 12/08/17.
 */
public class ReqRespLoggingFilter implements Filter, org.springframework.core.Ordered {
    // 敏感词列表，多个敏感词使用英文逗号分隔
    public static final String SENSITIVE_WORDS = "sensitive-words";
    public static final String LOG_BODY = "log_body";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReqRespLoggingFilter.class);
    private static final String NOTIFICATION_PREFIX = "* ";
    private static final String REQUEST_PREFIX = "> ";
    private static final String RESPONSE_PREFIX = "< ";
    /**
     * 过滤敏感uri参数的正则表达式，最终表达式的格式如下：
     * password[^&]*&?| newPassword[^&]*&?
     */
    private static String uriSensitiveWordsRegex = "";
    /**
     * 过滤敏感body参数的正则表达式，最终表达式的格式如下：
     * "?password[^,]*,|password[^&]*&?|"?newPassword[^,]*,|newPassword[^&]*&?
     * 该正则表达式能处理json和form参数，如果为multidata，需另行实现
     */
    private static String bodySensitiveWordsRegex = "";
    private ReqRespLoggingProperties reqRespLoggingProperties;
    ThreadLocal<Long> requestId = new ThreadLocal<Long>();
    private AtomicLong id = new AtomicLong(1L);
    private boolean logHttpBody = true;

    public ReqRespLoggingFilter() {
    }

    public ReqRespLoggingFilter(String sensitiveWords) {
        parseSensitiveWords(sensitiveWords);
    }

    public ReqRespLoggingFilter(boolean logHttpBody) {
        this.logHttpBody = logHttpBody;
    }

    public ReqRespLoggingFilter(ReqRespLoggingProperties reqRespLoggingProperties) {
        this.reqRespLoggingProperties = reqRespLoggingProperties;
    }

    public ReqRespLoggingFilter(String sensitiveWords, ReqRespLoggingProperties reqRespLoggingProperties) {
        this.reqRespLoggingProperties = reqRespLoggingProperties;
        parseSensitiveWords(sensitiveWords);
    }

    public ReqRespLoggingFilter(String sensitiveWords, ReqRespLoggingProperties reqRespLoggingProperties,
                                boolean logHttpBody) {
        this.logHttpBody = logHttpBody;
        this.reqRespLoggingProperties = reqRespLoggingProperties;
        parseSensitiveWords(sensitiveWords);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (LOGGER.isInfoEnabled()) {
            requestId.set(id.incrementAndGet());

            RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
            ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
            boolean needLog = needLog(requestWrapper);
            if (needLog) logRequest(requestWrapper);

            chain.doFilter(requestWrapper, responseWrapper);

            if (needLog) logResponse(responseWrapper);
        } else {
            // 如果未开启info级别日志(即不打印http日志)，也不需要封装request和response
            chain.doFilter(request, response);
        }
    }

    private boolean needLog(RequestWrapper request) {
        String uri = request.getRequestUri();
        return uri.matches(reqRespLoggingProperties.getBlackUrlRegex()) ? false : true;
    }

    private void logRequest(RequestWrapper request) {
        try {
            StringBuilder b = new StringBuilder();
            printRequestLine(b, request);
            printRequestHeaders(b, request);
            // 打印request body
            if (logHttpBody)
                printRequestBody(b, request);

            LOGGER.info("{}\n", b.toString());
        } catch (Exception e) {
            LOGGER.error("{}", e);
        }
    }

    private void logResponse(ResponseWrapper response) {
        try {
            StringBuilder b = new StringBuilder();
            printResponseLine(b, response);
            printResponseHeaders(b, response);
            // 打印response body
            if (logHttpBody)
                printResponseBody(b, response);
            LOGGER.info("{}\n", b.toString());
        } catch (Exception e) {
            LOGGER.error("{}", e);
        }
    }

    private void printRequestLine(StringBuilder b, RequestWrapper request) {
        String requestUri = filterUriSensitiveWords(request.getRequestUri());
        prefixId(b).append(NOTIFICATION_PREFIX).append("Server in-bound request");
        prefixId(b).append(REQUEST_PREFIX).append(request.getMethod()).append(" ").append(requestUri);

    }


    private void printResponseLine(StringBuilder b, ResponseWrapper response) {
        prefixId(b).append(NOTIFICATION_PREFIX).
                append("Server out-bound response");
        prefixId(b).append(RESPONSE_PREFIX).append(Integer.toString(response.getStatus()));
    }

    private void printRequestHeaders(StringBuilder b, RequestWrapper request) {
        Enumeration<String> headerNameEnum = request.getHeaderNames();
        while (headerNameEnum.hasMoreElements()) {
            String headerName = headerNameEnum.nextElement();
            String headerValue = request.getHeader(headerName);
            prefixId(b).append(REQUEST_PREFIX).append(headerName).append(": ").
                    append(headerValue);
        }
    }

    private void printResponseHeaders(StringBuilder b, ResponseWrapper response) {
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            prefixId(b).append(RESPONSE_PREFIX).append(headerName).append(": ").
                    append(response.getHeader(headerName));
        }
    }

    private void printRequestBody(StringBuilder b, RequestWrapper request) {
        prefixId(b).append(REQUEST_PREFIX).append('\n');
        String body = new String(request.getContentAsByteArray());
        body = filterBodySensitiveWords(body);
        b.append(body);
    }

    private void printResponseBody(StringBuilder b, ResponseWrapper response) {
        prefixId(b).append(RESPONSE_PREFIX).append('\n');
        String body = new String(response.toByteArray());
        body = filterBodySensitiveWords(body);

        b.append(body);
    }

    private StringBuilder prefixId(StringBuilder b) {
        return b.append("\n").append(requestId.get()).append(" ");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 如果不配值，则默认打印
        this.logHttpBody = filterConfig.getInitParameter(LOG_BODY) == null ? true : Boolean.valueOf(filterConfig.getInitParameter(LOG_BODY));
        String words = filterConfig.getInitParameter(SENSITIVE_WORDS);
        parseSensitiveWords(words);
    }

    private void parseSensitiveWords(String words) {
        if (words == null || words.length() < 1)
            return;

        String[] ws = words.split(",");
        for (String w : ws) {
            uriSensitiveWordsRegex += w + "[^&]*&?|";
            bodySensitiveWordsRegex += "\"?" + w + "[^,]*,|" + w + "[^&]*&?|";
        }
        uriSensitiveWordsRegex = uriSensitiveWordsRegex.length() > 0 ?
                uriSensitiveWordsRegex.substring(0, uriSensitiveWordsRegex.length() - 1) : uriSensitiveWordsRegex;
        bodySensitiveWordsRegex = bodySensitiveWordsRegex.length() > 0 ?
                bodySensitiveWordsRegex.substring(0, bodySensitiveWordsRegex.length() - 1) : bodySensitiveWordsRegex;
    }

    private String filterUriSensitiveWords(String requestUri) {
        if ("".equals(uriSensitiveWordsRegex))
            return requestUri;

        return requestUri.replaceAll(uriSensitiveWordsRegex, "");
    }

    private String filterBodySensitiveWords(String body) {
        if (!"".equals(bodySensitiveWordsRegex))
            body = body.replaceAll(bodySensitiveWordsRegex, "");

        return body;
    }


    @Override
    public void destroy() {

    }
}
