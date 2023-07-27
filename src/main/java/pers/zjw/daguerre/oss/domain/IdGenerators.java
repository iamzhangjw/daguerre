package pers.zjw.daguerre.oss.domain;

import pers.zjw.daguerre.exception.UnsupportedClassException;
import pers.zjw.daguerre.utils.Base62;
import pers.zjw.daguerre.utils.SnowflakeId;
import pers.zjw.daguerre.utils.ULID;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IdGenerators
 *
 * @author zhangjw
 * @date 2022/11/01 0001 17:56
 */
public class IdGenerators {

    static IdGenerator of(String spec) {
        if ("random".equalsIgnoreCase(spec)) return new Random();
        if ("auto_incr".equalsIgnoreCase(spec)) return new AutoIncrement();
        if ("uuid".equalsIgnoreCase(spec)) return new Uuid();
        if ("snowflake".equalsIgnoreCase(spec)) return new Snowflake();
        throw new UnsupportedClassException("unknown type spec: " + spec);
    }

    private static class Random implements IdGenerator {
        private final static SecureRandom SR = new SecureRandom(ByteBuffer.allocate(Long.SIZE/Byte.SIZE).putLong(System.currentTimeMillis()).array());
        private final static long HIGH_WORD = (2L<<61) - (2L<<31);
        private final static long LOW_WORD = (2L<<30) - 1;

        @Override
        public boolean checkId(String id) {
            return Base62.check(id);
        }

        @Override
        public String createId() {
            return Base62.encode((SR.nextLong() & HIGH_WORD) | (System.currentTimeMillis() & LOW_WORD));
        }
    }

    private static class AutoIncrement implements IdGenerator {
        private static final AtomicLong GEN = new AtomicLong(0);

        @Override
        public boolean checkId(String id) {
            return true;
        }

        @Override
        public String createId() {
            return String.valueOf(GEN.incrementAndGet());
        }
    }

    private static class Uuid implements IdGenerator {
        private final static ULID UUID = new ULID(new SecureRandom(ByteBuffer.allocate(Long.SIZE/Byte.SIZE).putLong(System.currentTimeMillis()).array()));

        @Override
        public boolean checkId(String id) {
            return ULID.isValid(id);
        }

        @Override
        public String createId() {
            return UUID.nextULID();
        }
    }

    private static class Snowflake implements IdGenerator {
        private final static SnowflakeId GEN = new SnowflakeId();

        @Override
        public boolean checkId(String id) {
            return Base62.check(id);
        }

        @Override
        public String createId() {
            return String.valueOf(GEN.nextId());
        }
    }
}
