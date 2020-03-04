package com.mapper;

import com.po.ItripUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItripUserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ItripUser record);

    int insertSelective(ItripUser record);

    ItripUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ItripUser record);

    int updateByPrimaryKey(ItripUser record);

    //通过手机号查询该用户是否存在//邮箱注册用户名重复验证
    public ItripUser findByUserCode(ItripUser user);

    //通过手机号修改用户激活状态
    public int updateUserActived(ItripUser user);

    //登录验证
    public ItripUser dologin(ItripUser user);

}