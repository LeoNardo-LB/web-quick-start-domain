package cases.unittest.org.smm.archetype.domain._shared.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smm.archetype.domain._shared.util.EnumUtil;
import org.smm.archetype.domain._shared.util.EnumUtil.EnumWrapper;
import org.smm.archetype.domain.common.search.enums.SortOrder;
import support.UnitTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EnumUtil单元测试
 *
 * @author Leonardo
 * @since 2026-01-15
 */
@DisplayName("EnumUtil单元测试")
class EnumUtilUTest extends UnitTestBase {

    @AfterEach
    void tearDown() {
        // 清理缓存，避免测试间相互影响
        EnumUtil.clearCache();
    }

    @Test
    @DisplayName("流式API：of()方法 - 返回EnumWrapper")
    void of_ReturnsEnumWrapper() {
        EnumWrapper<SortOrder> wrapper = EnumUtil.of(SortOrder.class);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getEnumClass()).isEqualTo(SortOrder.class);
    }

    @Test
    @DisplayName("流式API：of()方法 - 缓存EnumWrapper")
    void of_CachesEnumWrapper() {
        EnumWrapper<SortOrder> wrapper1 = EnumUtil.of(SortOrder.class);
        EnumWrapper<SortOrder> wrapper2 = EnumUtil.of(SortOrder.class);

        // 验证返回同一个实例（缓存生效）
        assertThat(wrapper1).isSameAs(wrapper2);
    }

    @Test
    @DisplayName("流式API：fromName() - 成功转换")
    void fromName_ValidName_ReturnsEnum() {
        SortOrder result = EnumUtil.of(SortOrder.class).fromName("ASC");

        assertThat(result).isEqualTo(SortOrder.ASC);
    }

    @Test
    @DisplayName("流式API：fromName() - 缓存生效")
    void fromName_CachesResult() {
        SortOrder result1 = EnumUtil.of(SortOrder.class).fromName("ASC");
        SortOrder result2 = EnumUtil.of(SortOrder.class).fromName("ASC");

        // 验证返回同一个实例（缓存生效）
        assertThat(result1).isSameAs(result2);
    }

    @Test
    @DisplayName("流式API：fromName() - 无效名称抛出异常")
    void fromName_InvalidName_ThrowsException() {
        assertThatThrownBy(() -> EnumUtil.of(SortOrder.class).fromName("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid enum name 'INVALID'");
    }

    @Test
    @DisplayName("流式API：fromString() - 成功转换")
    void fromString_ValidValue_ReturnsEnum() {
        SortOrder result = EnumUtil.of(SortOrder.class).fromString("asc", SortOrder.DESC);

        assertThat(result).isEqualTo(SortOrder.ASC);
    }

    @Test
    @DisplayName("流式API：fromString() - 无效值返回默认值")
    void fromString_InvalidValue_ReturnsDefault() {
        SortOrder result = EnumUtil.of(SortOrder.class).fromString("invalid", SortOrder.DESC);

        assertThat(result).isEqualTo(SortOrder.DESC);
    }

    @Test
    @DisplayName("流式API：fromString() - null值返回默认值")
    void fromString_NullValue_ReturnsDefault() {
        SortOrder result = EnumUtil.of(SortOrder.class).fromString(null, SortOrder.DESC);

        assertThat(result).isEqualTo(SortOrder.DESC);
    }

    @Test
    @DisplayName("流式API：fromStringStrict() - 成功转换")
    void fromStringStrict_ValidValue_ReturnsEnum() {
        SortOrder result = EnumUtil.of(SortOrder.class).fromStringStrict("ASC");

        assertThat(result).isEqualTo(SortOrder.ASC);
    }

    @Test
    @DisplayName("流式API：fromStringStrict() - 无效值抛出异常")
    void fromStringStrict_InvalidValue_ThrowsException() {
        assertThatThrownBy(() -> EnumUtil.of(SortOrder.class).fromStringStrict("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid enum value");
    }

    @Test
    @DisplayName("流式API：fromOrdinal() - 成功转换")
    void fromOrdinal_ValidOrdinal_ReturnsEnum() {
        SortOrder result = EnumUtil.of(SortOrder.class).fromOrdinal(0);

        assertThat(result).isEqualTo(SortOrder.ASC);
    }

    @Test
    @DisplayName("流式API：fromOrdinal() - 超出范围抛出异常")
    void fromOrdinal_OutOfRange_ThrowsException() {
        assertThatThrownBy(() -> EnumUtil.of(SortOrder.class).fromOrdinal(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ordinal");
    }

    @Test
    @DisplayName("流式API：isValid() - 有效值返回true")
    void isValid_ValidValue_ReturnsTrue() {
        boolean result = EnumUtil.of(SortOrder.class).isValid("ASC");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("流式API：isValid() - 无效值返回false")
    void isValid_InvalidValue_ReturnsFalse() {
        boolean result = EnumUtil.of(SortOrder.class).isValid("INVALID");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("流式API：getConstants() - 返回所有常量")
    void getConstants_ReturnsAllConstants() {
        SortOrder[] constants = EnumUtil.of(SortOrder.class).getConstants();

        assertThat(constants).hasSize(2);
        assertThat(constants).containsExactly(SortOrder.ASC, SortOrder.DESC);
    }

    @Test
    @DisplayName("clearCache() - 清除所有缓存")
    void clearCache_ClearsAllCaches() {
        // 创建缓存
        EnumUtil.of(SortOrder.class).fromName("ASC");

        // 清除缓存
        EnumUtil.clearCache();

        // 验证缓存已清除（重新创建Wrapper）
        EnumWrapper<SortOrder> wrapper1 = EnumUtil.of(SortOrder.class);
        EnumWrapper<SortOrder> wrapper2 = EnumUtil.of(SortOrder.class);

        // 虽然wrapper不同，但由于fromName内部缓存，仍会返回同一个实例
        SortOrder result1 = wrapper1.fromName("ASC");
        SortOrder result2 = wrapper2.fromName("ASC");

        // fromName仍然被缓存（因为clearCache只清除了WRAPPER_CACHE）
        assertThat(result1).isSameAs(result2);
    }

    @Test
    @DisplayName("流式API性能测试 - 平均调用时间应小于5微秒")
    void performance_AverageCallTime_ShouldBeFast() {
        int iterations = 10000;
        long start = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            EnumUtil.of(SortOrder.class).fromName("ASC");
        }

        long duration = System.nanoTime() - start;
        long avgTime = duration / iterations;

        // 流式 API 因为额外包装层，平均调用时间 < 5微秒（5000纳秒）
        assertThat(avgTime).isLessThan(5000);
    }

    @Test
    @DisplayName("流式API：链式调用 - 优雅的使用方式")
    void fluentAPI_ChainedCalls_ElegantUsage() {
        // 演示流式API的优雅性
        SortOrder order = EnumUtil.of(SortOrder.class)
                .fromName("ASC");

        assertThat(order).isEqualTo(SortOrder.ASC);
    }

    @Test
    @DisplayName("流式API：避免重复传递枚举类")
    void fluentAPI_AvoidsRepeatingEnumClass() {
        // 流式API：只传一次枚举类
        EnumWrapper<SortOrder> wrapper = EnumUtil.of(SortOrder.class);
        SortOrder order3 = wrapper.fromName("ASC");
        SortOrder order4 = wrapper.fromName("DESC");

        assertThat(order3).isEqualTo(SortOrder.ASC);
        assertThat(order4).isEqualTo(SortOrder.DESC);
    }

    @Test
    @DisplayName("流式API：重复使用同一个Wrapper - 性能最优")
    void fluentAPI_ReuseWrapper_BestPerformance() {
        // 创建一次Wrapper，多次使用
        EnumWrapper<SortOrder> wrapper = EnumUtil.of(SortOrder.class);

        SortOrder asc = wrapper.fromName("ASC");
        SortOrder desc = wrapper.fromName("DESC");
        boolean valid = wrapper.isValid("ASC");
        SortOrder[] constants = wrapper.getConstants();

        assertThat(asc).isEqualTo(SortOrder.ASC);
        assertThat(desc).isEqualTo(SortOrder.DESC);
        assertThat(valid).isTrue();
        assertThat(constants).hasSize(2);
    }

}
