package org.mallen.test.oauth.server.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mallen
 * @date 3/15/19
 */
@Controller
public class AuthController {
    @GetMapping("authorize")
    public void authorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("response_type") String responseType,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam("redirect_uri") String redirectUri,
            HttpSession session, HttpServletResponse response) {
        session.setAttribute("state", state);
        session.setAttribute("redirectUri", redirectUri);
        response.setStatus(HttpStatus.FOUND.value());

        response.setHeader(HttpHeaders.LOCATION, buildLoginRedirectUrl(state, redirectUri));
    }

    @PostMapping("login")
    public void login(@RequestParam("username") String username, @RequestParam("password") String password,
                      @RequestParam(value = "state", required = false) String state,
                      @RequestParam("redirectCallback") String redirectUri, HttpServletResponse response) {
        // 校验username和password
        // 回调
        String code = "oauth-server-code";
        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader(HttpHeaders.LOCATION, buildCallbackUrl(redirectUri, state, code));
    }

    @GetMapping("loginPage")
    public ModelAndView loginPage(
            @RequestParam("state") String state,
            @RequestParam("redirectCallback") String redirectCallback) {
        ModelAndView result = new ModelAndView();
        result.setViewName("login");
        result.addObject("state", state);
        result.getModel().put("redirectCallback", redirectCallback);
        return result;
    }

    /**
     * 获取token接口
     *
     * @param grant_type   授权方式，可取值：authorization_code和refresh_token
     * @param code         当授权方式为authorization_code时，必填
     * @param refreshToken 当授权方式为refresh_token时，必填
     * @return
     */
    @PostMapping(value = "token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Token token(
            @RequestParam(value = "grant_type") String grant_type,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "refreshToken", required = false) String refreshToken) {
        Token token = new Token();
        // 生成token
        token.setAccessToken("accessToken");
        token.setRefreshToken("refreshToken");
        token.setTokenType("Bearer");

        return token;
    }

    private String buildLoginRedirectUrl(String state, String redirectUri) {
        StringBuilder sb = new StringBuilder("loginPage?state=")
                .append(state)
                .append("&redirectCallback=").append(redirectUri);
        return sb.toString();
    }

    private String buildCallbackUrl(String redirectUri, String state, String code) {
        return new StringBuilder(redirectUri)
                .append("?code=").append(code)
                .append("&state=").append(state).toString();
    }

    @Getter
    @Setter
    @ToString
    public class Token {
        private String accessToken;
        private String tokenType;
        private String refreshToken;
        /**
         * 默认过期时间24小时
         */
        private Integer expireIn = 24 * 60 * 60;
    }
}
