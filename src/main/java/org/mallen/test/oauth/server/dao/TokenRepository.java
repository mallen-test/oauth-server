package org.mallen.test.oauth.server.dao;

import org.mallen.test.oauth.server.domain.Token;
import org.springframework.data.repository.CrudRepository;

/**
 * @author mallen
 * @date 3/23/19
 */
public interface TokenRepository extends CrudRepository<Token, Long> {
}
