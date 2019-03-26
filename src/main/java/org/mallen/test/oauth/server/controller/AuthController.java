package org.mallen.test.oauth.server.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mallen.test.common.utils.BeanCopier;
import org.mallen.test.common.utils.JSONUtil;
import org.mallen.test.oauth.server.dao.ClientRepository;
import org.mallen.test.oauth.server.dao.RefreshTokenRepository;
import org.mallen.test.oauth.server.dao.TokenRepository;
import org.mallen.test.oauth.server.domain.Client;
import org.mallen.test.oauth.server.domain.RefreshToken;
import org.mallen.test.oauth.server.domain.Token;
import org.mallen.test.oauth.server.handler.AuthHandler;
import org.mallen.test.oauth.server.utils.CodeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * @author mallen
 * @date 3/15/19
 */
@Controller
public class AuthController {
    @Autowired
    private ClientRepository clientRepo;
    @Autowired
    private TokenRepository tokenRepo;
    @Autowired
    private RefreshTokenRepository refreshTokenRepo;

    @GetMapping("authorize")
    public String authorize(
            @RequestParam("clientId") String clientId,
            @RequestParam("responseType") String responseType,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam("redirectUri") String redirectUri,
            HttpSession session, HttpServletResponse response) {
        // TODO mallen 如果校验不通过，怎么处理
//        authHandler.authorize(clientId, redirectUri);
        Client client = clientRepo.findByClientId(clientId);
        if (client == null) {
            return "clientIdError";
        }
        if (!client.checkRedirectUri(redirectUri)) {
            return "redirectUriError";
        }
        // 如果校验通过，重定向到登录页面
        session.setAttribute("clientId", clientId);
        session.setAttribute("appName", client.getAppName());
        session.setAttribute("state", state);
        session.setAttribute("redirectUri", redirectUri);
        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader(HttpHeaders.LOCATION, buildLoginRedirectUrl(state, redirectUri));

        return null;
    }

    @PostMapping("login")
    public void login(@RequestParam("username") String username, @RequestParam("password") String password,
                      @RequestParam(value = "state", required = false) String state,
                      @RequestParam("redirectUri") String redirectUri, HttpServletResponse response, HttpSession session) {
        // 校验username和password
        // 生成code
        String code = generateCode((String) session.getAttribute("clientId"), 6);
        // 回调
        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader(HttpHeaders.LOCATION, buildRedirectUri(redirectUri, state, code));
    }

    @GetMapping("loginPage")
    public ModelAndView loginPage(
            @RequestParam(value = "state", required = false) String state,
            @RequestParam("redirectUri") String redirectUri,
            HttpSession session) {
        // 校验redirectUri防止跨域请求伪造（CSRF）
        if (!session.getAttribute("redirectUri").equals(redirectUri)) {
            return new ModelAndView("authError");
        }
        ModelAndView result = new ModelAndView();
        result.setViewName("login");
        result.addObject("state", state);
        result.addObject("redirectUri", redirectUri);
        result.addObject("appName", session.getAttribute("appName"));
        return result;
    }

    /**
     * 获取token接口
     *
     * @param grantType    授权方式，可取值：authorization_code和refresh_token
     * @param code         当授权方式为authorization_code时，必填
     * @param refreshToken 当授权方式为refresh_token时，必填
     * @return
     */
    @PostMapping(value = "token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String token(
            @RequestParam(value = "clientId", required = false) String clientId,
            @RequestParam(value = "clientSecret", required = false) String clientSecret,
            @RequestParam(value = "grantType") String grantType,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "refreshToken", required = false) String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        // 优先从header中获取Basic Authorization信息，如果没有，则获取form参数中的信息
        final String authorization = request.getHeader("authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            clientId = values[0];
            clientSecret = values[1];
        }
        // 校验clientId和clientSecret
        Client client = clientRepo.findByClientId(clientId);
        if (null == client || !client.getClientSecret().equals(clientSecret)) {
            // 返回错误
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return "{\"error\": \"invalid_client\"}";
        }
        if (grantType.equals("authorization_code")) {
            String cachedClientId = CodeCache.get(code);
            // 校验code
            if (StringUtils.isEmpty(cachedClientId) || !cachedClientId.equals(clientId)) {
                // 返回错误
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return "{\"error\": \"invalid_code\"}";
            }
            // 移除缓存
            CodeCache.remove(code);
        } else if (grantType.equals("refresh_token")) {
            if (StringUtils.isEmpty(refreshToken)) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return "{\"error\": \"invalid_token\"}";
            }
            // 校验refresh token
            RefreshToken refreshTokenDomain = refreshTokenRepo.findByToken(refreshToken);
            if (null == refreshTokenDomain && !refreshTokenDomain.getClientId().equals(clientId)) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return "{\"error\": \"invalid_token\"}";
            }
            // 删除refreshToken
            refreshTokenRepo.delete(refreshTokenDomain);
            // 直接执行到：颁发token
        } else {
            // 返回错误
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "{\"error\": \"invalid_grant\"}";
        }

        // 颁发token
        Token token = generateToken(clientId);
        return JSONUtil.writeValueAsString(token);
    }

    private String buildLoginRedirectUrl(String state, String redirectUri) {
        StringBuilder sb = new StringBuilder("loginPage?state=")
                .append(state)
                .append("&redirectUri=").append(redirectUri);
        return sb.toString();
    }

    private String buildRedirectUri(String redirectUri, String state, String code) {
        return new StringBuilder(redirectUri)
                .append("?code=").append(code)
                .append("&state=").append(state).toString();
    }

    private String generateCode(String clientId, int length) {
        if (StringUtils.isEmpty(clientId)) {
            return "error";
        }
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt());
        }
        String code = sb.toString();
        // 保存
        CodeCache.set(clientId, code);

        return code;
    }

    private Token generateToken(String clientId) {
        String accessToken = DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes());
        String refreshToken = DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes());
        // 保存到数据库时，token可以加密
        org.mallen.test.oauth.server.domain.Token token = new org.mallen.test.oauth.server.domain.Token(accessToken, clientId, "Bearer");
        // 暂时不存aid信息（如果接入真实帐号系统，需要保存帐号信息，以便在通过refreshToken获取token时能知道是哪个帐号）
        RefreshToken refreshTokenDomain = new RefreshToken(refreshToken, clientId, null);
        long now = System.currentTimeMillis();
        // 24小时后过期24 * 60 * 60 * 1000
        token.setExpireAt(now + 86400000L);
        // 一周后过期：7 * 24 * 60 * 60 * 1000
        refreshTokenDomain.setExpireAt(now + 604800000L);
        Token result = new Token();
        BeanCopier.copyProperties(token, result);
        result.setRefreshToken(refreshToken);

        tokenRepo.save(token);
        refreshTokenRepo.save(refreshTokenDomain);

        return result;
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
        private Long expireAt;
    }
}
