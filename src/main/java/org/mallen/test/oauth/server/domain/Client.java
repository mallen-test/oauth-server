package org.mallen.test.oauth.server.domain;

import lombok.*;
import org.mallen.test.common.domain.BaseDomain;

import javax.persistence.*;

/**
 * @author mallen
 * @date 3/20/19
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "CLIENT")
public class Client extends BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String clientId;
    private String clientSecurity;
    /**
     * 客户端描述
     */
    private String remark;

    public Client() {
    }

    public Client(String clientId, String clientSecurity, String remark) {
        this.clientId = clientId;
        this.clientSecurity = clientSecurity;
        this.remark = remark;
    }
}
