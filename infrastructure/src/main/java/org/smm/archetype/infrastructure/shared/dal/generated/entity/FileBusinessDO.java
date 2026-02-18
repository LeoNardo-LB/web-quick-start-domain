package org.smm.archetype.infrastructure.shared.dal.generated.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.smm.archetype.infrastructure.shared.dal.BaseDO;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件业务关联表DO实体。


 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("file_business")
public class FileBusinessDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID，关联file_metadata.id（逻辑外键，无物理外键约束）
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

    /**
     * 逻辑删除标记：0=未删除，非0=删除时间戳（毫秒）
     */
    @TableLogic(value = "0", delval = "UNIX_TIMESTAMP(NOW()) * 1000")
    private Long deleteTime;

    /**
     * 删除人ID
     */
    private String deleteUser;

}
