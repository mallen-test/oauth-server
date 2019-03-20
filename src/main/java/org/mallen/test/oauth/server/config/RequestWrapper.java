package org.mallen.test.oauth.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by mallen on 4/26/17.
 */
public class RequestWrapper extends HttpServletRequestWrapper {
    private static final Logger logger = LoggerFactory.getLogger(RequestWrapper.class);
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private ByteArrayOutputStream outputStream;
    private volatile Map<String, String[]> parameterMap;
    private boolean isMultipart = false;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     * @throws IllegalArgumentException if the request is null
     */
    public RequestWrapper(HttpServletRequest request) {
        super(request);
        isMultipart = isMultipart(request);
        if (!isMultipart)
            // 缓存parameter map，不然后续spring获取参数时会获取不到（原因未知，与tomcat底层实现有关）
            cacheInputStream();
    }

    private boolean isMultipart(final HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
    }

    public byte[] getContentAsByteArray() {
        return this.outputStream != null ? this.outputStream.toByteArray() : new byte[]{};
    }

    /**
     * 缓存paramter map，主要处理content-type为application/x-www-form-urlencoded的情况。
     * 因为在读取完input stream之后，再调用获取paramter map相关接口，会出现获取不到body参数的情况。
     * 因此，覆盖request的相关方法，自己将参数缓存起来，来避免该问题
     */
    private void cacheParamMap() {
        if (isFormPost()) {
            // 获取url参数
            Map<String, List> params = getQueryParam();

            // 从outputstream中获取form urlencode参数
            String body = new String(this.getContentAsByteArray());
            if (null != body && !"".equals(body)) {
                String[] nameAndVals = body.split("&");
                for (String nameAndVal : nameAndVals) {
                    String[] nameVal = nameAndVal.split("=");
                    List existVal = params.get(nameVal[0]);
                    if (existVal != null) {
                        // 可能存在只传递key的情况，比如vin=123&name=&age=17，其中的name的值默认为空字符串
                        existVal.add(nameVal.length > 1 ? nameVal[1] : "");
                    } else {
                        params.put(nameVal[0], Arrays.asList(nameVal.length > 1 ? decode(nameVal[1]) : ""));
                    }
                }
            }

            this.parameterMap = new HashMap<>();
            for (String key : params.keySet()) {
                this.parameterMap.put(key, (String[]) params.get(key).toArray());
            }
        } else {
            this.parameterMap = super.getParameterMap();
        }
    }

    /**
     * 截取url参数
     *
     * @return
     */
    private Map<String, List> getQueryParam() {
        Map<String, List> result = new HashMap<String, List>();
        String queryString = super.getQueryString();
        if (null == queryString || "".equals(queryString.trim()))
            return result;
        try {
            queryString = URLDecoder.decode(queryString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return result;
        }
        String[] queryParams = queryString.split("&");
        for (String queryParam : queryParams) {
            String[] paramAndValue = queryParam.split("=");
            List<String> existVal = result.get(paramAndValue[0]);
            if (existVal != null) {
                // 可能存在只传递key的情况，比如vin=123&name=&age=17，其中的name的值默认为空字符串
                existVal.add(paramAndValue.length > 1 ? paramAndValue[1] : "");
            } else {
                result.put(paramAndValue[0], Arrays.asList(paramAndValue.length > 1 ? decode(paramAndValue[1]) : ""));
            }

        }

        return result;
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (isMultipart)
            return super.getInputStream();

        if (outputStream == null) {
            cacheInputStream();
        }
        return new CachedServletInputStream();
    }

    public String getRequestUri() {
        return new StringBuilder().append(super.getScheme()).append("://")
                .append(super.getServerName()).append(":").append(super.getServerPort())
                .append(super.getRequestURI())
                .append((super.getQueryString() != null ? "?" + super.getQueryString() : "")).toString();
    }

    private void cacheInputStream() {
        try {
            ServletInputStream in = super.getInputStream();
            outputStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = in.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
        } catch (IOException e) {
        }

    }

    @Override
    public String getParameter(String name) {
        if (isMultipart)
            return super.getParameter(name);

        if (this.parameterMap == null) {
            cacheParamMap();
        }
        String[] values = this.parameterMap.get(name);
        return values == null || values.length < 1 ? null : this.parameterMap.get(name)[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (isMultipart)
            return super.getParameterMap();

        if (this.parameterMap == null) {
            cacheParamMap();
        }
        return this.parameterMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (isMultipart)
            return super.getParameterNames();

        if (this.parameterMap == null) {
            cacheParamMap();
        }

        return Collections.enumeration(this.parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (isMultipart)
            return super.getParameterValues(name);

        if (this.parameterMap == null) {
            cacheParamMap();
        }
        return this.parameterMap.get(name);
    }

    private boolean isFormPost() {
        String contentType = getContentType();
        return (contentType != null && contentType.contains(FORM_CONTENT_TYPE) &&
                "POST".equalsIgnoreCase(getMethod()));
    }

    private String decode(String val) {
        String result = null;
        try {
            result = URLDecoder.decode(val, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("", e);
        }
        return result;
    }

    private class CachedServletInputStream extends ServletInputStream {
        private ByteArrayInputStream input;
        private boolean isFinish = false;

        public CachedServletInputStream() {
            input = new ByteArrayInputStream(outputStream.toByteArray());
        }

        @Override
        public int read() throws IOException {
            int result = input.read();
            if (-1 == result)
                isFinish = true;
            return result;
        }

        @Override
        public boolean isFinished() {
            return isFinish;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {

        }
    }
}
