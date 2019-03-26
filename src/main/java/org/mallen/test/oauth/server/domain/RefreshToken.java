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
@Table(name = "REFRESH_TOKEN")
public class RefreshToken extends BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private String clientId;
    private String aid;
    /**
     * 过期时间点，毫秒级别时间戳，在时间之后不可用
     */
    private Long expireAt;

    public RefreshToken() {
    }

    public RefreshToken(String token, String clientId, String aid) {
        this.token = token;
        this.clientId = clientId;
        this.aid = aid;
    }
}
