package generated.repository.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.smm.archetype.infrastructure._shared.dal.BaseDO;
import org.smm.archetype.infrastructure._shared.dal.BaseDOFillListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 日志表 实体类。
 * @author Administrator
 * @since 2026-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "log", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class LogDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 删除时间
     */
    private Instant deleteTime;

    /**
     * 删除人ID
     */
    private String deleteUser;

    /**
     * 版本号（乐观锁）
     */
    private Long version;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 方法名
     */
    private String method;

    /**
     * 参数
     */
    private String argString;

    /**
     * 结果
     */
    private String resultString;

    /**
     * 线程名
     */
    private String threadName;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 开始时间
     */
    private Instant startTime;

    /**
     * 结束时间
     */
    private Instant endTime;

}
