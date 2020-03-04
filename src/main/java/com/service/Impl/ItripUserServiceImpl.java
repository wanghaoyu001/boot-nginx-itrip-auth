package com.service.Impl;

import com.mapper.ItripUserMapper;
import com.po.ItripUser;
import com.service.ItripUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ItripUserServiceImpl implements ItripUserService {
    @Autowired
    private ItripUserMapper itripUserMapper;

    public ItripUserMapper getItripUserMapper() {
        return itripUserMapper;
    }

    public void setItripUserMapper(ItripUserMapper itripUserMapper) {
        this.itripUserMapper = itripUserMapper;
    }

    @Override
    public ItripUser findByUserCode(ItripUser user) {
        return itripUserMapper.findByUserCode(user);
    }

    @Override
    public int codeUsersave(ItripUser user) {
        return itripUserMapper.insert(user);
    }

    @Override
    public int updateUserActived(ItripUser user) {
        return itripUserMapper.updateUserActived(user);
    }

    @Override
    public ItripUser dologin(ItripUser user) {
        return itripUserMapper.dologin(user);
    }
}
