package org.smm.archetype.infrastructure.shared.dal.generated.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.FileMetadataDO;

/**
 * 文件元数据表 映射层。
 *


 */
public interface FileMetadataMapper extends BaseMapper<FileMetadataDO> {

    /**
     * 插入或更新文件元数据（原子操作，基于 md5 唯一键）。
     * 使用 MySQL 的 INSERT ... ON DUPLICATE KEY UPDATE 实现并发安全的 upsert。
     * @param entity 文件元数据实体
     * @return 影响行数
     */
    @Insert("""
            INSERT INTO file_metadata (id, md5, content_type, size, url, url_expire, path, 
                                       create_time, update_time, create_user, update_user, delete_time, delete_user)
            VALUES (#{entity.id}, #{entity.md5}, #{entity.contentType}, #{entity.size}, #{entity.url}, 
                    #{entity.urlExpire}, #{entity.path}, #{entity.createTime}, #{entity.updateTime},
                    #{entity.createUser}, #{entity.updateUser}, 0, NULL)
            ON DUPLICATE KEY UPDATE
                content_type = #{entity.contentType},
                size = #{entity.size},
                url = #{entity.url},
                url_expire = #{entity.urlExpire},
                path = #{entity.path},
                update_time = #{entity.updateTime},
                update_user = #{entity.updateUser}
            """)
    @Options(useGeneratedKeys = true, keyProperty = "entity.id")
    int upsertByMd5(@Param("entity") FileMetadataDO entity);

}
