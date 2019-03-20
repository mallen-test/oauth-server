package org.mallen.test.oauth.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 用于定于请求响应filter配置项
 * Created by mallen on 6/28/18.
 */
@ConfigurationProperties("tima.logging.reqresp")
public class ReqRespLoggingProperties {
    private static final String DEFAULT_BLACK_URL_REGEX = "^https?://.*/health/?$|^https?://.*/info/?$";
    private String appendBlackUrlRegex;
    private String overrideBlackUrlRegex;

    public String getAppendBlackUrlRegex() {
        return appendBlackUrlRegex;
    }

    public void setAppendBlackUrlRegex(String appendBlackUrlRegex) {
        this.appendBlackUrlRegex = appendBlackUrlRegex;
    }

    public String getOverrideBlackUrlRegex() {
        return overrideBlackUrlRegex;
    }

    public void setOverrideBlackUrlRegex(String overrideBlackUrlRegex) {
        this.overrideBlackUrlRegex = overrideBlackUrlRegex;
    }

    public String getBlackUrlRegex() {
        if (!StringUtils.isEmpty(overrideBlackUrlRegex))
            return overrideBlackUrlRegex;
        if (StringUtils.isEmpty(appendBlackUrlRegex))
            return DEFAULT_BLACK_URL_REGEX;
        else {
            return DEFAULT_BLACK_URL_REGEX + "|" + appendBlackUrlRegex;
        }
    }
}
