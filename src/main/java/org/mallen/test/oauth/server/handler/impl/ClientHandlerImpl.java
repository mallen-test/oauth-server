package org.mallen.test.oauth.server.handler.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.mallen.test.oauth.server.dao.ClientRepository;
import org.mallen.test.oauth.server.domain.Client;
import org.mallen.test.oauth.server.domain.QClient;
import org.mallen.test.oauth.server.handler.ClientHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author mallen
 * @date 3/20/19
 */
@Service
public class ClientHandlerImpl implements ClientHandler {
    @Autowired
    private ClientRepository clientRepo;

    @Override
    public Page<Client> find(String clientId, String clientSecurity, Integer pageIndex, Integer pageSize) {
        pageIndex = (pageIndex == null|| pageIndex < 1) ? 1 : pageIndex;
        pageSize = (pageSize == null || pageSize < 1) ? 1 : pageSize > 200 ? 200 : pageSize;
        BooleanExpression expression = null;
        if (!StringUtils.isEmpty(clientId)) {
            expression = QClient.client.clientId.likeIgnoreCase(clientId);
        }
        if (!StringUtils.isEmpty(clientSecurity)) {
            BooleanExpression securityExpression = QClient.client.clientSecret.likeIgnoreCase(clientSecurity);
            expression = expression == null ? securityExpression : expression.and(securityExpression);
        }
        return clientRepo.findAll(expression, PageRequest.of(pageIndex - 1, pageSize, Sort.Direction.ASC, "createdTime"));
    }

    @Override
    public void add(String appName, String clientId, String clientSecurity, String redirectUris, String remark) {
        Client client = new Client(appName, clientId, clientSecurity, redirectUris, remark);
        clientRepo.save(client);
    }
}
