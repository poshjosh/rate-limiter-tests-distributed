package io.github.poshjosh.ratelimiter.tests.client.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MathUtil {
    
    public static final RoundingMode roundingMode = RoundingMode.HALF_UP;
    public static final int DECIMAL_SCALE = 2;

    public static final BigDecimal ZERO = setScale(BigDecimal.ZERO);
    public static final BigDecimal ONE = setScale(BigDecimal.ONE);

    public static BigDecimal toBigDecimal(int n) {
        return n == 0 ? ZERO : n == 1 ? ONE : setScale(BigDecimal.valueOf(n));
    }

    public static BigDecimal toBigDecimal(Object body) {
        if (body == null) {
            return ZERO;
        }
        return setScale(body instanceof BigDecimal ? (BigDecimal)body : new BigDecimal(body.toString()));
    }

    public static BigDecimal add(BigDecimal lhs, BigDecimal rhs) {
        return setScale(lhs.add(rhs));
    }

    public static BigDecimal divide(BigDecimal lhs, BigDecimal rhs) {
        return lhs.divide(rhs, DECIMAL_SCALE, roundingMode);
    }

    public static BigDecimal multiply(BigDecimal lhs, BigDecimal rhs) {
        return setScale(lhs.multiply(rhs));
    }
    
    public static BigDecimal setScale(BigDecimal value) {
        return value.setScale(DECIMAL_SCALE, roundingMode);
    }
    
    public static BigDecimal subtract(BigDecimal lhs, BigDecimal rhs) {
        return setScale(lhs.subtract(rhs));
    }
}
