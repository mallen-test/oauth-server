package org.mallen.test.oauth.server.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mallen.test.common.domain.BaseDomain;

import javax.persistence.*;

/**
 * @author mallen
 * @date 3/23/19
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TOKEN")
public class Token extends BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accessToken;
    private String clientId;
    private String tokenType;
    /**
     * 过期时间点，毫秒级别时间戳，在时间之后不可用
     */
    private Long expireAt;

    public Token() {
    }

    public Token(String accessToken, String clientId, String tokenType) {
        this.accessToken = accessToken;
        this.clientId = clientId;
        this.tokenType = tokenType;
    }
}
