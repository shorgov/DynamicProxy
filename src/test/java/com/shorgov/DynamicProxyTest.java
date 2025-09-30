package com.shorgov;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicProxyTest {

    interface Adder {
        int add(int a, int b);
    }

    static class AdderImpl implements Adder {
        public int add(int a, int b) {
            return a + b;
        }
    }

    static class AdderNotImpl {
        public int add(int a, int b) {
            return a + b;
        }
    }

    static class Unrelated {
    }

    @Test
    void from_ShouldNotAcceptNullForDelegate() {
        // Arrange

        // Act

        // Assert
        assertThrows(IllegalArgumentException.class,
                () -> DynamicProxy.from(null),
                "Should throw a IllegalArgumentException");
    }

    @Test
    void to_whenDelegateImplementsTargetInterface_shouldReturnDelegate() {
        // Arrange
        AdderImpl implementor = new AdderImpl();

        // Act
        DynamicProxy dp = DynamicProxy.from(implementor);
        Adder result = dp.to(Adder.class);

        // Assert
        assertSame(implementor, result,
                "The original delegate should be returned when it implements the target interface.");
    }

    @Test
    void to_whenDelegateDoesNotImplementsTargetInterface_shouldReturnProxy() {
        // Arrange
        AdderNotImpl notImplementor = new AdderNotImpl();

        // Act
        DynamicProxy dp = DynamicProxy.from(notImplementor);
        Adder proxy = dp.to(Adder.class);

        // Assert
        assertTrue(Proxy.isProxyClass(proxy.getClass()),
                "The returned object should be a dynamic proxy class.");
    }

    @Test
    void to_whenDelegateImplementsTargetInterface_shouldInvokeSuccessfully() {
        // Arrange
        AdderImpl implementor = new AdderImpl();

        // Act
        DynamicProxy dp = DynamicProxy.from(implementor);
        Adder proxy = dp.to(Adder.class);

        // Assert
        assertEquals(3, proxy.add(1, 2));
    }

    @Test
    void to_whenDelegateDoesNotImplementsTargetInterface_shouldInvokeSuccessfully() {
        // Arrange
        AdderNotImpl notImplementor = new AdderNotImpl();

        // Act
        DynamicProxy dp = DynamicProxy.from(notImplementor);
        Adder proxy = dp.to(Adder.class);

        // Assert
        assertEquals(3, proxy.add(1, 2));
    }

    @Test
    void to_whenMethodIsNotFoundOnDelegate_shouldThrowNoSuchMethodException() {
        // Arrange
        Unrelated unrelated = new Unrelated();

        // Act
        DynamicProxy dp = DynamicProxy.from(unrelated);
        Adder proxy = dp.to(Adder.class);

        // Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            // The reflected call throws NoSuchMethodException, which is wrapped
            // in an UndeclaredThrowableException or similar RuntimeException by Proxy.newProxyInstance.
            proxy.add(1, 2);
        }, "Should throw a RuntimeException wrapping NoSuchMethodException.");

        // do additional assert to chek if method name is part of the exception
        assertTrue(exception.getCause().getMessage().contains("add"),
                "The exception message should indicate the missing method name (add).");
    }
}
