package com.controller;

import com.po.Dto;
import com.po.ItripUser;
import com.service.ItripUserService;
import com.util.*;
import com.util.vo.ItripUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/api")
public class ItripUserController {
    @Autowired
    private ItripUserService itripUserService;/*jedis启动*/
    private Jedis jedis = new Jedis("127.0.0.1", 6379);

    public ItripUserService getItripUserService() {
        return itripUserService;
    }

    public void setItripUserService(ItripUserService itripUserService) {
        this.itripUserService = itripUserService;
    } /*手机注册*/

    @RequestMapping(value = "/registerbyphone")
    public Dto codesave(HttpServletRequest request, HttpServletResponse response, @RequestBody ItripUserVO userVO) {
        System.out.println("手机注册模块方法。。。。。");
        try {
            System.out.println("手机注册参数：" + userVO.toString());/*将获取到的参数封装到用户对象*/
            ItripUser user = new ItripUser();
            user.setUsercode(userVO.getUserCode());/*手机号*/
            user.setUserpassword(userVO.getUserPassword());/*密码*/
            user.setUsertype(0);/*注册账号类型（区分第三方，只有一种自己注册的）*/
            user.setUsername(userVO.getUserName());/*用户姓名*/
            user.setActivated(0);/*默认没有激活*//*注册（完成数据库存值） 判断数据库有没有相同的（在数据库中有没有该手机号）*/
            ItripUser oldUser = itripUserService.findByUserCode(user);
            if (oldUser == null) {/*没有该用户可以注册 处理密码进行加密（md5加密）*/
                user.setUserpassword(MD5Util.getMd5(user.getUserpassword(), 32));
                System.out.println("加密后的user对象:" + user.toString());/*注册用户*/
                int code = itripUserService.codeUsersave(user);
                if (code > 0) {/*注册成功，发送SMS*/
                    String codecheck = SMSUtil.testcheck(user.getUsercode());/*将验证码存入Redis缓存*/
                    jedis.setex(user.getUsercode(), 60, codecheck);
                    return DtoUtil.returnSuccess("注册成功，请完成激活");
                } else return DtoUtil.returnFail("注册失败", ErrorCode.AUTH_UNKNOWN);
            } else if (oldUser.getActivated() == 0 && jedis.get(oldUser.getUsercode()) == null) {/*如果用户存在但没有激活，缓存中也没数据，则再次发送验证码*/
                String codecheck = SMSUtil.testcheck(oldUser.getUsercode());/*将验证码存入Redis缓存*/
                jedis.setex(oldUser.getUsercode(), 60, codecheck);
                return DtoUtil.returnFail("用户已存在，但未激活，请根据短信重新激活", ErrorCode.AUTH_UNKNOWN);
            } else return DtoUtil.returnFail("用户已存在，请登录或找回密码", ErrorCode.AUTH_USER_ALREADY_EXISTS);
        } catch (Exception e) {
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
        }
    } /*邮箱注册*/

    @RequestMapping(value = "/doregister")
    public Dto doregister(HttpServletRequest request, HttpServletResponse response, @RequestBody ItripUserVO userVO) {/*将获取数据封装到指定对象*/
        System.out.println("邮箱注册模块方法。。。。。");
        try {
            ItripUser user = new ItripUser();
            user.setUsercode(userVO.getUserCode());/*邮箱号*/
            user.setUserpassword(userVO.getUserPassword());/*密码*/
            user.setUsertype(0);/*注册账号类型（区分第三方，只有一种自己注册的）*/
            user.setUsername(userVO.getUserName());/*用户姓名*/
            user.setActivated(0);/*默认没有激活*//*注册（完成数据库存值） 判断数据库有没有相同的（在数据库中有没有该手机号）*/
            ItripUser oldUser = itripUserService.findByUserCode(user);
            if (oldUser == null) {/*没有该用户可以注册 处理密码进行加密（md5加密）*/
                user.setUserpassword(MD5Util.getMd5(user.getUserpassword(), 32));
                System.out.println("加密后的user对象:" + user.toString());/*注册用户*/
                int code = itripUserService.codeUsersave(user);
                if (code > 0) {/*注册成功，发送SMS*/
                    String codecheck = EmailUtil.check(user);/*将验证码存入Redis缓存*/
                    jedis.setex(user.getUsercode(), 60, codecheck);
                    return DtoUtil.returnSuccess("注册成功，请完成激活");
                } else return DtoUtil.returnFail("注册失败", ErrorCode.AUTH_UNKNOWN);
            } else if (oldUser.getActivated() == 0 && jedis.get(oldUser.getUsercode()) == null) {/*如果用户存在但没有激活，缓存中也没数据，则再次发送验证码*/
                String codecheck = EmailUtil.check(oldUser);/*将验证码存入Redis缓存*/
                jedis.setex(oldUser.getUsercode(), 60, codecheck);
                return DtoUtil.returnFail("用户已存在，但未激活，请根据短信重新激活", ErrorCode.AUTH_UNKNOWN);
            } else return DtoUtil.returnFail("用户已存在，请登录或找回密码", ErrorCode.AUTH_USER_ALREADY_EXISTS);
        } catch (Exception e) {
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
        }
    }
    /*邮箱注册用户名重复验证*/

    @RequestMapping(value = "/ckusr")
    public Dto checkUser(String name) {
        try {
            ItripUser user = new ItripUser();
            user.setUsercode(name);
            if (itripUserService.findByUserCode(user) == null) return DtoUtil.returnSuccess("用户名可用");
            else return DtoUtil.returnFail("用户名已存在", ErrorCode.AUTH_USER_ALREADY_EXISTS);
        } catch (Exception e) {
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
        }
    }

    /*手机验证激活*/
    @RequestMapping(value = "/validatephone")
    public Dto validatephone(HttpServletRequest request, HttpServletResponse response, String user, String code) {
        System.out.println("手机短信验证模块");
        return checkuser(user, code);
    }

    /*邮箱验证激活*/
    @RequestMapping(value = "/activate")
    public Dto activate(HttpServletRequest request, HttpServletResponse response, String user, String code) {
        System.out.println("邮箱验证模块");
        return checkuser(user, code);
    }

    /*  通用验证*/
    public Dto checkuser(String user, String code) {
        try {/*获取该账号是否在Redis中有数据*/
            if (jedis.get(user) != null) {/*有缓存,可以正常验证*/
                if (jedis.get(user).equals(code)) {
                    ItripUser users = new ItripUser();
                    users.setUsercode(user);/*设置邮箱*/
                    itripUserService.updateUserActived(users);/*完成状态修改*/
                    return DtoUtil.returnSuccess("激活成功");
                } else return DtoUtil.returnFail("激活失败，验证码错误", ErrorCode.AUTH_UNKNOWN);
            } else {/*没有缓存，验证失败*/
                return DtoUtil.returnFail("激活失败", ErrorCode.AUTH_UNKNOWN);
            }
        } catch (Exception e) {
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
        }
    }

    /**
     * 描述：是否是邮箱. @param str 指定的字符串 @return 是否是邮箱:是为true，否则false
     */
    public static Boolean isEmail(String str) {
        Boolean isEmail = false;
        String expr = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})$";
        if (str.matches(expr)) isEmail = true;
        return isEmail;
    }

    /**
     * 判断是否是手机号 @param phone @return
     */
    public static boolean checkPhone(String phone) {
        Pattern pattern = Pattern.compile("^(13[0-9]|15[0-9]|153|15[6-9]|180|18[23]|18[5-9])\\d{8}$");
        Matcher matcher = pattern.matcher(phone);
        if (matcher.matches()) return true;
        return false;
    }
}

