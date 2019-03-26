package org.mallen.test.oauth.server.handler;

/**
 * @author mallen
 * @date 3/21/19
 */
public interface AuthHandler {
    /**
     * 校验认证信息
     *
     * @param clientId
     * @param redirectUri
     * @return
     */
    Boolean authorize(String clientId, String redirectUri);
}
