package support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 纯单元测试基类 - 不启动任何Spring上下文
 *
 * 特点：
 * ✅ 0毫秒启动时间
 * ✅ 完全隔离，无外部依赖
 * ✅ 可Mock任意对象
 * ❌ 不能测试Spring集成
 *
 * 使用示例：
 * <pre>
 * class OrderServiceTest extends UnitTestBase {
 *     &#64;Mock
 *     private OrderRepository orderRepository;
 *
 *     &#64;InjectMocks
 *     private OrderService orderService;
 *
 *     &#64;Test
 *     void testSomeMethod() {
 *         // 测试逻辑
 *     }
 * }
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
public abstract class UnitTestBase {

    /**
     * 在每个测试方法前初始化Mock
     */
    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
        setUpMocks();
    }

    /**
     * 子类重写此方法来设置公共的mock行为
     */
    protected void setUpMocks() {
        // 默认空实现，子类可覆盖
    }

    /**
     * 重置所有Mock对象
     * 子类可在测试方法中调用此方法重置特定的Mock对象
     *
     * 使用场景：
     * - 需要在测试中间重置Mock状态
     * - 多个测试场景需要不同的Mock行为
     *
     * 示例：
     * <pre>
     * &#64;Test
     * void testMultipleScenarios() {
     *     // 场景1
     *     when(mockRepository.findById(1)).thenReturn(entity1);
     *     service.process(1);
     *
     *     // 重置Mock
     *     resetAllMocks();
     *
     *     // 场景2
     *     when(mockRepository.findById(2)).thenReturn(entity2);
     *     service.process(2);
     * }
     * </pre>
     *
     * 注意：此方法为预留方法，默认为空实现。
     * 如果需要重置特定Mock对象，建议在子类中重写此方法：
     * <pre>
     * &#64;Override
     * protected void resetAllMocks() {
     *     Mockito.reset(mockRepository);
     *     Mockito.reset(externalService);
     * }
     * </pre>
     */
    protected void resetAllMocks() {
        // 默认空实现，子类可重写此方法来重置特定的Mock对象
    }

}