package org.mallen.test.oauth.server.handler.impl;

import org.mallen.test.oauth.server.dao.ClientRepository;
import org.mallen.test.oauth.server.domain.Client;
import org.mallen.test.oauth.server.handler.AuthHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mallen
 * @date 3/21/19
 */
@Service
public class AuthHandlerImpl implements AuthHandler {
    @Autowired
    private ClientRepository clientRepo;

    @Override
    public Boolean authorize(String clientId, String redirectUri) {
        Client client = clientRepo.findByClientId(clientId);
        if (client == null) {
            return false;
        }
        return client.checkRedirectUri(redirectUri);
    }
}
