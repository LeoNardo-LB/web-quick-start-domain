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

/**
 * 文件业务关联表 实体类。
 * @author Administrator
 * @since 2026-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = "file_business", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
public class FileBusinessDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private String fileMetaId;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 文件业务名称
     */
    private String name;

    /**
     * 业务类型
     */
    private String type;

    /**
     * 使用场景
     */
    private String usage;

    /**
     * 排序序号
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

}
