package org.smm.archetype.domain._shared.base;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 *
 * @author Leonardo
 * @since 2026/1/6
 */
@Getter
@Setter
@Builder
public class PageModel<T extends BaseModel> implements BasePage {

    private Long pageNumber;

    private Long pageSize;

    private List<T> records;

    private Long totalRaw;

}
