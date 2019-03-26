package org.mallen.test.oauth.server.dao;

import org.mallen.test.oauth.server.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

/**
 * @author mallen
 * @date 3/23/19
 */
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    /**
     * 根据token获取
     *
     * @param token
     * @return
     */
    RefreshToken findByToken(String token);
}
