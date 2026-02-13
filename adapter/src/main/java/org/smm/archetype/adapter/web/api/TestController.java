package org.smm.archetype.adapter.web.api;

import lombok.extern.slf4j.Slf4j;
import org.smm.archetype.infrastructure.shared.log.BusinessLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 测试控制器，提供用于测试和演示的RESTful API接口。
 */
@Slf4j
@RestController
public class TestController {

    /**
     * Hello接口
     *
     * 提供基础的问候接口，根据传入的KVRequest参数返回相应的键值对列表。
     * 如果未传入参数，则返回默认的问候信息列表。
     * @param request 键值对请求参数
     * @return 包含键值对的二维列表
     */
    @GetMapping
    @BusinessLog
    List<List<String>> hello(KVRequest request) throws InterruptedException {
        if (request == null || request.key() == null || request.value() == null) {
            return List.of(List.of("hello", "world"), List.of("java", "spring", "mybatis", "hibernate"));
        }
        log.info(request.toString());
        Thread.sleep(1000);
        return List.of(List.of(request.key(), request.value()));
    }

    /**
     * 异常测试接口
     *
     * 用于测试全局异常处理机制，故意抛出RuntimeException异常。
     * @return 无返回值，总是抛出异常
     * @throws RuntimeException 总是抛出此异常用于测试
     */
    @GetMapping("exception")
    @BusinessLog
    List<List<String>> exception() {
        throw new RuntimeException("test");
    }

    /**
     * 键值对请求记录类
     *
     * 用于封装键值对请求参数的记录类，包含键和值两个字段。
     */
    private record KVRequest(String key, String value) {}

}
