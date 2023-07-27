package io.ooze.daguerre.oss.domain;

/**
 * object name id generator
 *
 * @author zhangjw
 * @date 2022/11/01 0001 17:53
 */
public interface IdGenerator {
    boolean checkId(String id);

    String createId();
}
