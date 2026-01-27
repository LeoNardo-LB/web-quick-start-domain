package org.smm.archetype.infrastructure.bizshared.dal.generated.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.smm.archetype.infrastructure.bizshared.dal.BaseDO;
import org.smm.archetype.infrastructure.bizshared.dal.BaseDOFillListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 文件元数据表 实体类。
 *
 * @author Administrator
 * @since 2026-01-10
 */
@Getter
@Setter
@Builder(setterPrefix = "set")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(value = "file_metadata", onInsert = BaseDOFillListener.class, onUpdate = BaseDOFillListener.class)
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

}
