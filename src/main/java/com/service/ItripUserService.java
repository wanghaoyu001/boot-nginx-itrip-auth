package com.service;

import com.po.ItripUser;

public interface ItripUserService {
    //通过手机号查询该用户是否存在//邮箱注册用户名重复验证
    public ItripUser findByUserCode(ItripUser user);

    //注册用户
    public int codeUsersave(ItripUser user);

    //更新注册状态
    public int updateUserActived(ItripUser user);

    //登录验证
    public ItripUser dologin(ItripUser user);
}
