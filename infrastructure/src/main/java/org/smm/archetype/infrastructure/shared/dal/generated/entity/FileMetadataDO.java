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
import java.time.Instant;

    /**
     * 文件元数据表DO实体。


     */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("file_metadata")
public class FileMetadataDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件MD5值
     */
    private String md5;

    /**
     * 文件MIME类型
     */
    private String contentType;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 文件访问URL
     */
    private String url;

    /**
     * URL过期时间
     */
    private Instant urlExpire;

    /**
     * 文件存储路径
     */
    private String path;

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
