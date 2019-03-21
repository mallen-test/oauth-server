package org.mallen.test.oauth.server.controller;

import org.mallen.test.common.response.Response;
import org.mallen.test.oauth.server.domain.Client;
import org.mallen.test.oauth.server.handler.ClientHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @author mallen
 * @date 3/20/19
 */
@Controller
@RequestMapping("client")
public class ClientController {
    @Autowired
    private ClientHandler clientHandler;

    @GetMapping("page")
    public ModelAndView page(
            @RequestParam(value = "clientId", required = false)String clientId,
            @RequestParam(value = "clientSecurity", required = false)String clientSecurity,
            @RequestParam(value = "pageIndex", required = false)Integer pageIndex,
            @RequestParam(value = "pageSize", required = false)Integer pageSize) {
        Page<Client> clientPage = clientHandler.find(clientId, clientSecurity, pageIndex, pageSize);
        ModelAndView result = new ModelAndView("clientPage");
        result.addObject("data", clientPage.getContent());
        result.addObject("total", clientPage.getTotalElements());
        return result;
    }

    @GetMapping("addPage")
    public ModelAndView addPage() {
        ModelAndView result = new ModelAndView("addClient");
        String clientId = generateClientId();
        String clientSecurity = generateSecurity();
        result.addObject("clientId", clientId);
        result.addObject("clientSecurity", clientSecurity);

        return result;
    }

    @PostMapping("add")
    public String add(@RequestParam("clientId")String clientId,
                    @RequestParam("clientSecurity")String clientSecurity,
                    @RequestParam(value = "remark", required = false)String remark) {
        clientHandler.add(clientId, clientSecurity, remark);
        return "redirect:page?pageSize=100";
    }

    private String generateSecurity() {
        return DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes());
    }

    private String generateClientId() {
        return DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes());
    }
}
