package com.controller;

import com.alibaba.fastjson.JSON;
import com.po.Dto;
import com.po.ItripUser;
import com.service.ItripUserService;
import com.util.*;
import com.util.vo.ItripTokenVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

@RestController
@RequestMapping(value = "/api")
public class ItripLoginController {
    @Autowired
    private ItripUserService itripUserService;/*jedis启动*/
    private Jedis jedis = new Jedis("127.0.0.1", 6379);

    public ItripUserService getItripUserService() {
        return itripUserService;
    }

    public void setItripUserService(ItripUserService itripUserService) {
        this.itripUserService = itripUserService;
    }

    @RequestMapping(value = "/dologin")
    public Dto dologin(HttpServletRequest request, HttpServletResponse response, String name, String password) {
        System.out.println("登录验证模块");/*判断账号密码不为空*/
        if (!EmptyUtil.isEmpty(name) && !EmptyUtil.isEmpty(password)) {/*账号密码不为空*/
            ItripUser user = new ItripUser();
            user.setUsercode(name.trim());
            user.setUserpassword(MD5Util.getMd5(password.trim(), 32));
            user = itripUserService.dologin(user);/*判断*/
            if (user != null) {
                //登录成功
                //生成token
                String token = TokenUtil.getTokenGenerator(request.getHeader("user-agent"), user);
                System.out.println("浏览器类型" + request.getHeader("user-agent"));
                //缓存token
                if (token.startsWith("token:PC-")) {
                    //将token作为key，将user对象作为value，存入redis(因为将来取值需要返回对象所以value转成json)
                    String StrJSON = JSON.toJSONString(user);
                    jedis.setex(token, 7200, StrJSON);

                    System.out.println("登录token：" + jedis.get(token));
                }
                ItripTokenVO tokenVO = new ItripTokenVO(token,
                        Calendar.getInstance().getTimeInMillis() + 60 *60 * 1000,
                        Calendar.getInstance().getTimeInMillis());
                System.out.println("登录成功返回参数：" + tokenVO.toString());
                return DtoUtil.returnDataSuccess(tokenVO);
            } else {
                System.out.println("账号密码错误登录失败");
                return DtoUtil.returnFail("用户名密码错误", ErrorCode.AUTH_AUTHENTICATION_FAILED);
            }

        } else {
            System.out.println("账号密码有空登录失败");
            return DtoUtil.returnFail("用户名密码错误", ErrorCode.AUTH_AUTHENTICATION_FAILED);

        }
    }

    /*退出登录*/
    @RequestMapping(value = "/logout")
    public Dto logout(HttpServletRequest request) {
        System.out.println("注销用户:" + request.getHeader("token"));
        String token = request.getHeader("token");
        System.out.println(token);
        if (!TokenUtil.validate(request.getHeader("user-agent"), token)) {
            System.out.println("时间异常");
            return DtoUtil.returnFail("token无效", ErrorCode.AUTH_TOKEN_INVALID);
        }
        try {
            //删除token信息
            TokenUtil.delete(token);
            return DtoUtil.returnSuccess("注销成功");
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("注销失败", ErrorCode.AUTH_UNKNOWN);
        }

    }
    @RequestMapping(value = "/userinfo/adduserlinkuser")
    private Dto adduserlinkuser(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("添加用户信息模块。。。。。");
        return DtoUtil.returnSuccess("注销成功");
    }
}
