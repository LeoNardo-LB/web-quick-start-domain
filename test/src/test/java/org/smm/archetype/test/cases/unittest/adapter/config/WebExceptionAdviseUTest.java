package org.smm.archetype.test.cases.unittest.adapter.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.smm.archetype.adapter.web.config.WebExceptionAdvise;
import org.smm.archetype.app.shared.result.BaseResult;
import org.smm.archetype.test.support.UnitTestBase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * WebExceptionAdvise 单元测试
 *
 * <p>验证全局异常处理器对各类异常的正确处理：
 * <ul>
 *   <li>MethodArgumentNotValidException - @RequestBody 参数校验失败</li>
 *   <li>BindException - 表单绑定异常</li>
 *   <li>ConstraintViolationException - @RequestParam 约束违反</li>
 * </ul>
 */
@DisplayName("WebExceptionAdvise 单元测试")
class WebExceptionAdviseUTest extends UnitTestBase {

    @InjectMocks
    private WebExceptionAdvise webExceptionAdvise;

    @Nested
    @DisplayName("MethodArgumentNotValidException 处理测试")
    class MethodArgumentNotValidExceptionTests {

        private MethodArgumentNotValidException exception;

        @BeforeEach
        void setUp() {
            exception = mock(MethodArgumentNotValidException.class);
            BindException bindException = new BindException(new Object(), "target");
            bindException.addError(new FieldError("target", "name", "名称不能为空"));
            bindException.addError(new FieldError("target", "age", "年龄必须大于0"));
            when(exception.getBindingResult()).thenReturn(bindException.getBindingResult());
        }

        @Test
        @DisplayName("应返回 HTTP 400 状态码")
        void shouldReturnHttpStatus400() {
            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleMethodArgumentNotValidException(exception);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("应返回 code=400")
        void shouldReturnCode400() {
            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleMethodArgumentNotValidException(exception);
            assertNotNull(response.getBody());
            assertEquals("400", response.getBody().getCode());
        }

        @Test
        @DisplayName("应包含所有字段的错误信息")
        void shouldIncludeAllFieldErrors() {
            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleMethodArgumentNotValidException(exception);
            assertNotNull(response.getBody());
            String message = response.getBody().getMessage();
            assertNotNull(message);
            assertEquals("参数校验失败: name: 名称不能为空, age: 年龄必须大于0", message);
        }

    }

    @Nested
    @DisplayName("BindException 处理测试")
    class BindExceptionTests {

        private BindException exception;

        @BeforeEach
        void setUp() {
            exception = new BindException(new Object(), "target");
            exception.addError(new FieldError("target", "email", "邮箱格式不正确"));
            exception.addError(new FieldError("target", "phone", "手机号格式不正确"));
        }

        @Test
        @DisplayName("应返回 HTTP 400 状态码")
        void shouldReturnHttpStatus400() {
            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleBindException(exception);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("应返回 code=400")
        void shouldReturnCode400() {
            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleBindException(exception);
            assertNotNull(response.getBody());
            assertEquals("400", response.getBody().getCode());
        }

        @Test
        @DisplayName("应包含所有绑定错误信息")
        void shouldIncludeAllBindingErrors() {
            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleBindException(exception);
            assertNotNull(response.getBody());
            String message = response.getBody().getMessage();
            assertNotNull(message);
            assertEquals("参数校验失败: email: 邮箱格式不正确, phone: 手机号格式不正确", message);
        }

    }

    @Nested
    @DisplayName("ConstraintViolationException 处理测试")
    class ConstraintViolationExceptionTests {

        private ConstraintViolationException exception;

        @BeforeEach
        void setUp() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();

            ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("用户名不能为空");

            ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
            when(violation2.getMessage()).thenReturn("密码长度必须在6-20之间");

            violations.add(violation1);
            violations.add(violation2);

            exception = new ConstraintViolationException(violations);
        }

        @Test
        @DisplayName("应返回 HTTP 400 状态码")
        void shouldReturnHttpStatus400() {
            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleConstraintViolationException(exception);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("应返回 code=400")
        void shouldReturnCode400() {
            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleConstraintViolationException(exception);
            assertNotNull(response.getBody());
            assertEquals("400", response.getBody().getCode());
        }

        @Test
        @DisplayName("应包含所有约束违反信息")
        void shouldIncludeAllConstraintViolations() {
            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleConstraintViolationException(exception);
            assertNotNull(response.getBody());
            String message = response.getBody().getMessage();
            assertNotNull(message);
            // 消息顺序可能不确定，只验证包含关键内容
            assertTrue(message.startsWith("参数校验失败:"), "应包含前缀");
            assertTrue(message.contains("用户名不能为空"), "应包含用户名错误");
            assertTrue(message.contains("密码长度必须在6-20之间"), "应包含密码错误");
        }

    }

    @Nested
    @DisplayName("单字段校验失败测试")
    class SingleFieldValidationTests {

        @Test
        @DisplayName("MethodArgumentNotValidException - 单字段失败")
        void singleFieldFailure() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindException bindException = new BindException(new Object(), "target");
            bindException.addError(new FieldError("target", "username", "用户名不能为空"));
            when(exception.getBindingResult()).thenReturn(bindException.getBindingResult());

            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleMethodArgumentNotValidException(exception);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("400", response.getBody().getCode());
            assertEquals("参数校验失败: username: 用户名不能为空", response.getBody().getMessage());
        }

        @Test
        @DisplayName("BindException - 单字段失败")
        void singleBindFieldFailure() {
            BindException exception = new BindException(new Object(), "target");
            exception.addError(new FieldError("target", "password", "密码不能为空"));

            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleBindException(exception);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("400", response.getBody().getCode());
            assertEquals("参数校验失败: password: 密码不能为空", response.getBody().getMessage());
        }

        @Test
        @DisplayName("ConstraintViolationException - 单约束失败")
        void singleConstraintViolation() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("必须为正数");
            violations.add(violation);
            ConstraintViolationException exception = new ConstraintViolationException(violations);

            ResponseEntity<BaseResult<Void>> response = webExceptionAdvise.handleConstraintViolationException(exception);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("400", response.getBody().getCode());
            assertEquals("参数校验失败: 必须为正数", response.getBody().getMessage());
        }

    }

}
