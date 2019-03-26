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
    private String clientSecret;
    private String appName;
    /**
     * 客户端重定向uri集合，在调用authorize接口时，传递的redirectUri必须被包含在redirectUris中。该字段中每个地址采用英文分号分隔。
     */
    private String redirectUris;
    /**
     * 客户端描述
     */
    private String remark;

    public Client() {
    }

    public Client(String appName, String clientId, String clientSecret, String redirectUris, String remark) {
        this.appName = appName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUris = redirectUris;
        this.remark = remark;
    }

    /**
     * 校验redirectUri是否合法
     *
     * @param redirectUri
     * @return 如果redirectUri合法，返回true；否则返回false
     */
    public Boolean checkRedirectUri(String redirectUri) {
        String[] uris = this.redirectUris.split(";");
        for (String uri : uris) {
            if (uri.equals(redirectUri)) {
                return true;
            }
        }

        return false;
    }
}
