package io.ooze.daguerre.enhance;

import io.ooze.daguerre.constant.Algorithm;
import io.ooze.daguerre.exception.BizException;
import io.ooze.daguerre.exception.ErrorCode;
import io.ooze.daguerre.oss.OssHolder;
import io.ooze.daguerre.pojo.entity.Credential;
import io.ooze.daguerre.service.CredentialService;
import io.ooze.daguerre.utils.NewDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 服务是否可访问 filter
 *
 * @date 2022/04/04 0004 11:38
 * @author zhangjw
 */
@Slf4j
public class ApiAccessibleFilter implements Filter {
    private final boolean enableSign;
    private final CredentialService credentialService;

    public ApiAccessibleFilter(boolean enableSign, CredentialService credentialService) {
        this.enableSign = enableSign;
        this.credentialService = credentialService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        Map<String, String[]> params = req.getParameterMap();
        String[] keyId = params.get("accessKey");
        if (null == keyId || !StringUtils.hasText(keyId[0])) {
            throw new BizException(ErrorCode.MISSING_KEY);
        }
        Credential credential = credentialService.getByAccessKey(keyId[0]);
        if (Objects.isNull(credential) || !Credential.validateAccessKey(credential.getAccessKey())) {
            throw new BizException(ErrorCode.ACCESS_KEY_ABNORMAL);
        }
        OssHolder.put(credential);
        // 签名验证
        if (!validateSign(credential, params)) {
            throw new BizException(ErrorCode.INVALID_SIGN);
        }
        log.debug("validate api sign success");
        chain.doFilter(request, response);
    }

    private boolean validateSign(Credential credential, Map<String, String[]> params) {
        if (!enableSign) return true;
        log.debug("");
        String[] nonce = params.get("nonce");
        if (null == nonce || !StringUtils.hasText(nonce[0])) return false;
        String[] timestamp = params.get("timestamp");
        if (null == timestamp || !StringUtils.hasText(timestamp[0])) return false;
        // 时间戳和当前超过10分钟
        if (Math.abs(NewDateTimeUtils.getCurrentSeconds() - Long.parseLong(timestamp[0]))
                > 600L) {
            return false;
        }
        String[] sign = params.get("sign");
        if (null == sign || !StringUtils.hasText(sign[0])) return false;

        StringBuilder source = params.entrySet().stream()
                .filter(e -> null != e.getValue() && StringUtils.hasText(e.getValue()[0]))
                .filter(e -> !e.getKey().equals("sign"))
                .sorted(Map.Entry.comparingByKey())
                .collect(StringBuilder::new,
                        (buffer, e) -> buffer.append("&")
                                .append(e.getKey())
                                .append("=")
                                .append(e.getValue()[0]),
                        StringBuilder::append);
        Optional<Algorithm> algorithm = Algorithm.parse(credential.getAlgorithm());
        return algorithm.filter(value ->
                sign[0].equalsIgnoreCase(value.digest(credential.getAccessSecret(), source.toString())))
                .isPresent();
    }
}
