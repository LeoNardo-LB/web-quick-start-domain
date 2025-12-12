package org.smm.archetype.web.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author Leonardo
 * @since 2025/12/13
 */
@Slf4j
@RestController
public class TestController {

    @GetMapping("hello")
    List<List<String>> hello(KVRequest request) {
        if (request == null) {
            return List.of(List.of("hello", "world"), List.of("java", "spring", "mybatis", "hibernate"));
        }
        log.info(request.toString());
        return List.of(List.of(request.key(), request.value()));
    }

    @GetMapping("exception")
    List<List<String>> exception() {
        throw new RuntimeException("test");
    }

    /**
     * 测试的kv请求类
     */
    private record KVRequest(String key, String value) {}

}
