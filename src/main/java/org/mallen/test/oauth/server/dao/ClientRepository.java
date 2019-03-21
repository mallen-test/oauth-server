package org.mallen.test.oauth.server.dao;

import org.mallen.test.oauth.server.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * @author mallen
 * @date 3/20/19
 */
public interface ClientRepository extends QuerydslPredicateExecutor<Client>, JpaRepository<Client, Long> {
}
