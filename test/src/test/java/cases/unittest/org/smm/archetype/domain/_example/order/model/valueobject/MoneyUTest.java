package cases.unittest.org.smm.archetype.domain._example.order.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain.example.model.valueobject.Money;
import support.UnitTestBase;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Money值对象单元测试。
 */
@DisplayName("Money值对象单元测试")
class MoneyUTest extends UnitTestBase {

    @Test
    @DisplayName("创建金额 - 使用BigDecimal")
    void of_BigDecimal_ReturnsMoney() {
        // Act
        Money money = Money.of(new BigDecimal("100.50"));

        // Assert
        assertThat(money.getAmount()).isEqualByComparingTo("100.50");
        assertThat(money.getCurrency()).isEqualTo("CNY");
    }

    @Test
    @DisplayName("加法运算")
    void add_ReturnsNewMoney() {
        // Arrange
        Money money1 = Money.of(new BigDecimal("100.50"));
        Money money2 = Money.of(new BigDecimal("50.25"));

        // Act
        Money result = money1.add(money2);

        // Assert
        assertThat(result.getAmount()).isEqualByComparingTo("150.75");
        assertThat(result.getCurrency()).isEqualTo("CNY");
    }

    @Test
    @DisplayName("减法运算")
    void subtract_ReturnsNewMoney() {
        // Arrange
        Money money1 = Money.of(new BigDecimal("100.50"));
        Money money2 = Money.of(new BigDecimal("50.25"));

        // Act
        Money result = money1.subtract(money2);

        // Assert
        assertThat(result.getAmount()).isEqualByComparingTo("50.25");
    }

    @Test
    @DisplayName("比较 - 相同金额")
    void compareTo_SameAmount_ReturnsZero() {
        // Arrange
        Money money1 = Money.of(new BigDecimal("100.00"));
        Money money2 = Money.of(new BigDecimal("100.00"));

        // Act
        int result = money1.compareTo(money2);

        // Assert
        assertThat(result).isZero();
    }

    @Test
    @DisplayName("isZero - 零金额")
    void isZero_ZeroAmount_ReturnsTrue() {
        // Arrange
        Money money = Money.zero();

        // Act & Assert
        assertThat(money.isZero()).isTrue();
    }

    @Test
    @DisplayName("相等性 - 金额和货币相同")
    void equals_SameAmountAndCurrency_ReturnsTrue() {
        // Arrange
        Money money1 = Money.of(new BigDecimal("100.00"), "CNY");
        Money money2 = Money.of(new BigDecimal("100.00"), "CNY");

        // Act & Assert
        assertThat(money1).isEqualTo(money2);
    }

}
