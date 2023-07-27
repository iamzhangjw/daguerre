package io.ooze.daguerre.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.ooze.daguerre.mapper.CredentialMapper;
import io.ooze.daguerre.pojo.entity.Credential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * CredentialService
 * </p>
 *
 * @author zhangjw
 * @since 2022-04-01
 */
@Slf4j
@Service
public class CredentialService extends ServiceImpl<CredentialMapper, Credential> {
    @Autowired
    private CredentialMapper mapper;

    public Credential getByAccessKey(String accessKey) {
        return super.getOne(Wrappers.lambdaQuery(Credential.class).eq(Credential::getAccessKey, accessKey));
    }

}
