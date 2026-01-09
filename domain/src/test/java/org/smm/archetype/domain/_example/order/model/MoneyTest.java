package org.smm.archetype.domain._example.order.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 金额值对象单元测试
 * @author Leonardo
 * @since 2025/12/30
 */
@DisplayName("金额值对象测试")
class MoneyTest {

    @Test
    @DisplayName("应该成功创建金额")
    void should_create_money_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("100.50");

        // When
        Money money = Money.of(amount);

        // Then
        assertNotNull(money);
        assertEquals(0, money.getAmount().compareTo(new BigDecimal("100.50")));
        assertEquals("CNY", money.getCurrency());
    }

    @Test
    @DisplayName("不应该创建负数金额")
    void should_not_create_negative_money() {
        // Given
        BigDecimal amount = new BigDecimal("-100");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> Money.of(amount));
    }

    @Test
    @DisplayName("应该成功相加两个金额")
    void should_add_two_money_successfully() {
        // Given
        Money money1 = Money.of(new BigDecimal("100"));
        Money money2 = Money.of(new BigDecimal("50"));

        // When
        Money result = money1.add(money2);

        // Then
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("150")));
    }

    @Test
    @DisplayName("不应该相加不同币种的金额")
    void should_not_add_different_currency_money() {
        // Given
        Money money1 = Money.of(new BigDecimal("100"), "CNY");
        Money money2 = Money.of(new BigDecimal("50"), "USD");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> money1.add(money2));
    }

    @Test
    @DisplayName("应该成功相乘金额")
    void should_multiply_money_successfully() {
        // Given
        Money money = Money.of(new BigDecimal("100"));

        // When
        Money result = money.multiply(new BigDecimal("2"));

        // Then
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("200")));
    }

    @Test
    @DisplayName("应该正确比较金额大小")
    void should_compare_money_correctly() {
        // Given
        Money money1 = Money.of(new BigDecimal("100"));
        Money money2 = Money.of(new BigDecimal("50"));

        // Then
        assertTrue(money1.greaterThan(money2));
        assertFalse(money2.greaterThan(money1));
    }

    @Test
    @DisplayName("应该创建零金额")
    void should_create_zero_money() {
        // When
        Money zero = Money.zero();

        // Then
        assertEquals(0, zero.getAmount().compareTo(BigDecimal.ZERO));
        assertEquals("CNY", zero.getCurrency());
    }

    @Test
    @DisplayName("相同金额应该相等")
    void should_equal_same_money() {
        // Given
        Money money1 = Money.of(new BigDecimal("100"));
        Money money2 = Money.of(new BigDecimal("100"));

        // Then
        assertEquals(money1, money2);
    }

    @Test
    @DisplayName("不同金额应该不相等")
    void should_not_equal_different_money() {
        // Given
        Money money1 = Money.of(new BigDecimal("100"));
        Money money2 = Money.of(new BigDecimal("50"));

        // Then
        assertNotEquals(money1, money2);
    }

    @Test
    @DisplayName("应该正确四舍五入")
    void should_round_correctly() {
        // Given
        BigDecimal amount = new BigDecimal("100.555");

        // When
        Money money = Money.of(amount);

        // Then
        assertEquals(0, money.getAmount().compareTo(new BigDecimal("100.56")));
    }

}
