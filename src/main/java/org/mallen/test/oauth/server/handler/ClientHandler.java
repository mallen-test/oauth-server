package org.mallen.test.oauth.server.handler;

import org.mallen.test.oauth.server.domain.Client;
import org.springframework.data.domain.Page;

/**
 * @author mallen
 * @date 3/20/19
 */
public interface ClientHandler {
    /**
     * 分页查询Client信息
     *
     * @param clientId
     * @param clientSecurity
     * @param pageIndex      小于1，取1
     * @param pageSize       小于1取1，大于200，取200
     * @return
     */
    Page<Client> find(String clientId, String clientSecurity, Integer pageIndex, Integer pageSize);

    /**
     * 新增客户端
     * @param clientId
     * @param clientSecurity
     * @param remark
     */
    void add(String clientId, String clientSecurity, String remark);
}
