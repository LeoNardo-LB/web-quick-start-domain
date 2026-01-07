package org.smm.archetype.domain.common.file;

import ch.qos.logback.core.model.Model;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

/**
 *
 * @author Leonardo
 * @since 2025/12/28
 */
@Getter
@Setter
public class File extends Model {

    protected String md5;

    protected String contentType;

    protected Integer size;

    protected InputStream inputStream;

    protected String url;

    protected FileBusiness fileBusiness;

    /**
     * 业务关联
     */
    @Setter
    public static class FileBusiness extends Model {

        protected String name;

        protected Type Type;

        protected String businessId;

        protected Usage usage;

        protected String remark;

        protected Integer order = 0;

        /**
         * 业务类型
         */
        @Getter
        public enum Type {

            ;

            private final String desc;

            Type(String desc) {
                this.desc = desc;
            }
        }

        /**
         * 使用场景
         */
        @Getter
        public enum Usage {

            ;

            private final String desc;

            private final Type type;

            Usage(String desc, Type type) {
                this.desc = desc;
                this.type = type;
            }
        }

    }

}
