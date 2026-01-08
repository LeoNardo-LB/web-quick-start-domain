package org.smm.archetype.infrastructure._shared.dal;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Getter;
import lombok.Setter;
import org.smm.archetype.domain._shared.base.Identifier;

import java.time.Instant;

/**
 * 数据对象（context object）基类
 * 自定进行设置操作，无需手动干预，只有get方法
 * @author Leonardo
 * @since 2025/12/29
 */
@Getter
@Setter
public abstract class BaseDO implements Identifier {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    private Instant createTime;

    private Instant updateTime;

    private String createUser;

    private String updateUser;

}
