package org.smm.archetype.infrastructure.shared.dal.generated.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.smm.archetype.infrastructure.shared.dal.generated.entity.FileBusinessDO;

/**
 * 文件业务关联表 映射层。


 */
public interface FileBusinessMapper extends BaseMapper<FileBusinessDO> {

    /**
     * 插入或更新文件业务关联（原子操作，基于 id 主键）。
     * 使用 MySQL 的 INSERT ... ON DUPLICATE KEY UPDATE 实现并发安全的 upsert。
     * @param entity 文件业务关联实体
     * @return 影响行数
     */
    @Insert("""
            INSERT INTO file_business (id, file_meta_id, business_id, name, type, usage, sort, remark,
                                       create_time, update_time, create_user, update_user, delete_time, delete_user)
            VALUES (#{entity.id}, #{entity.fileMetaId}, #{entity.businessId}, #{entity.name}, 
                    #{entity.type}, #{entity.usage}, #{entity.sort}, #{entity.remark},
                    #{entity.createTime}, #{entity.updateTime}, #{entity.createUser}, #{entity.updateUser}, 0, NULL)
            ON DUPLICATE KEY UPDATE
                file_meta_id = #{entity.fileMetaId},
                business_id = #{entity.businessId},
                name = #{entity.name},
                type = #{entity.type},
                usage = #{entity.usage},
                sort = #{entity.sort},
                remark = #{entity.remark},
                update_time = #{entity.updateTime},
                update_user = #{entity.updateUser}
            """)
    @Options(useGeneratedKeys = true, keyProperty = "entity.id")
    int upsertById(@Param("entity") FileBusinessDO entity);

}
