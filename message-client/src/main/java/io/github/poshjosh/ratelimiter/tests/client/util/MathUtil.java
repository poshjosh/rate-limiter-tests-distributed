package io.github.poshjosh.ratelimiter.tests.client.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MathUtil {
    
    public static final RoundingMode roundingMode = RoundingMode.HALF_UP;
    public static final int DECIMAL_SCALE = 2;

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(DECIMAL_SCALE, roundingMode);
    public static final BigDecimal ONE = BigDecimal.ONE.setScale(DECIMAL_SCALE, roundingMode);

    public static BigDecimal toBigDecimal(int n) {
        return n == 0 ? ZERO : n == 1 ? ONE : BigDecimal.valueOf(n).setScale(DECIMAL_SCALE, roundingMode);
    }

    public static BigDecimal toBigDecimal(Object body) {
        if (body == null) {
            return ZERO;
        }
        return MathUtil.setScale(body instanceof BigDecimal ? (BigDecimal)body : new BigDecimal(body.toString()));
    }

    public static BigDecimal add(BigDecimal lhs, BigDecimal rhs) {
        return lhs.add(rhs).setScale(DECIMAL_SCALE, roundingMode);
    }

    public static BigDecimal divide(BigDecimal lhs, BigDecimal rhs) {
        return lhs.divide(rhs, DECIMAL_SCALE, roundingMode);
    }

    public static BigDecimal multiply(BigDecimal lhs, BigDecimal rhs) {
        return lhs.multiply(rhs).setScale(DECIMAL_SCALE, roundingMode);
    }
    
    public static BigDecimal setScale(BigDecimal value) {
        return value.setScale(DECIMAL_SCALE, roundingMode);
    }
    
    public static BigDecimal subtract(BigDecimal lhs, BigDecimal rhs) {
        return lhs.subtract(rhs).setScale(DECIMAL_SCALE, roundingMode);
    }
}
