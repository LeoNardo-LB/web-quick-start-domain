package org.smm.archetype.domain._shared.base;

/**
 *
 *
 * @author Leonardo
 * @since 2026/1/6
 */
public interface BasePage {

    Long getPageNumber();

    Long getPageSize();

    default Long getOffset() {
        return (getPageNumber() - 1) * getPageSize();
    }

}
