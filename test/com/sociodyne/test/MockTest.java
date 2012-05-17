// Copyright 2011, Sociodyne LLC. All rights reserved.
// Copyright 2011 Sociodyne LLC. All rights reserved.

package com.sociodyne.test;

import static org.easymock.EasyMock.verify;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.easymock.IMocksControl;

public class MockTest extends TestCase {

  private final List<Object> objectsToVerify = new ArrayList<Object>();

  @Override
  public void setUp() throws Exception {
    Class<?> clazz = this.getClass();
    while (clazz != null && !MockTest.class.equals(clazz)) {
      for (final Field field : clazz.getDeclaredFields()) {
        final Mock mockAnnotation = field.getAnnotation(Mock.class);
        if (mockAnnotation != null) {
          Object mock;
          Class<?> fieldType = field.getType();
          if (fieldType.isArray()) {
            // Create an array of mocks
            field.setAccessible(true);
            Object[] mockArray = (Object[]) field.get(this);
            for (int i = 0; i < mockArray.length; i++) {
              mock = createMockUsingAnnotation(fieldType.getComponentType(), mockAnnotation);
              objectsToVerify.add(mock);
              mockArray[i] = mock;
            }
            field.setAccessible(false);
          } else {
            mock = createMockUsingAnnotation(fieldType, mockAnnotation);

            field.setAccessible(true);
            field.set(this, mock);
            field.setAccessible(false);

            objectsToVerify.add(mock);
          }

        }
      }

      clazz = clazz.getSuperclass();
    }
  }

  private Object createMockUsingAnnotation(Class<?> fieldType, final Mock mockAnnotation) {
    Object mock;
    if (mockAnnotation.value() != null) {
      switch (mockAnnotation.value()) {
      case STRICT:
        mock = EasyMock.createStrictMock(fieldType);
        break;
      case NICE:
        mock = EasyMock.createNiceMock(fieldType);
        break;
      case DEFAULT:
      default:
        mock = EasyMock.createMock(fieldType);
      }
    } else {
      mock = EasyMock.createMock(fieldType);
    }
    return mock;
  }

  @Override
  public void tearDown() throws Exception {
    for (final Object o : objectsToVerify) {
      try {
        verify(o);
      } catch (final Error e) {
        throw new Error("Verification of " + o + " failed", e);
      }
    }
  }

  protected <T> T createMock(Class<T> clazz) {
    final T object = EasyMock.createMock(clazz);
    objectsToVerify.add(object);
    return object;
  }

  protected <T> T createStrictMock(Class<T> clazz) {
    final T object = EasyMock.createStrictMock(clazz);
    objectsToVerify.add(object);
    return object;
  }

  protected <T> T createNiceMock(Class<T> clazz) {
    final T object = EasyMock.createNiceMock(clazz);
    objectsToVerify.add(object);
    return object;
  }

  protected <T> IMockBuilder<T> createMockBuilder(Class<T> clazz) {
    return new DelegatingIMockBuilder<T>(EasyMock.createMockBuilder(clazz));
  }

  protected void replay() {
    for (final Object object : objectsToVerify) {
      EasyMock.replay(object);
    }
  }

  private class DelegatingIMockBuilder<T> implements IMockBuilder<T> {

    IMockBuilder<T> delegate;

    public DelegatingIMockBuilder(IMockBuilder<T> delegate) {
      this.delegate = delegate;
    }

    public IMockBuilder<T> addMockedMethod(Method arg0) {
      return delegate.addMockedMethod(arg0);
    }

    public IMockBuilder<T> addMockedMethod(String arg0) {
      return delegate.addMockedMethod(arg0);
    }

    public IMockBuilder<T> addMockedMethod(String arg0, Class<?>... arg1) {
      return delegate.addMockedMethod(arg0, arg1);
    }

    public IMockBuilder<T> addMockedMethods(String... arg0) {
      return delegate.addMockedMethods(arg0);
    }

    public IMockBuilder<T> addMockedMethods(Method... arg0) {
      return delegate.addMockedMethods(arg0);
    }

    public T createMock() {
      final T mock = delegate.createMock();
      objectsToVerify.add(mock);
      return mock;
    }

    public T createMock(IMocksControl arg0) {
      final T mock = delegate.createMock(arg0);
      objectsToVerify.add(mock);
      return mock;
    }

    public T createMock(String arg0) {
      final T mock = delegate.createMock(arg0);
      objectsToVerify.add(mock);
      return mock;
    }

    public T createMock(String arg0, IMocksControl arg1) {
      final T mock = delegate.createMock(arg0, arg1);
      objectsToVerify.add(mock);
      return mock;
    }

    public T createNiceMock() {
      final T mock = delegate.createNiceMock();
      objectsToVerify.add(mock);
      return mock;
    }

    public T createNiceMock(String arg0) {
      final T mock = delegate.createNiceMock(arg0);
      objectsToVerify.add(mock);
      return mock;
    }

    public T createStrictMock() {
      final T mock = delegate.createStrictMock();
      objectsToVerify.add(mock);
      return mock;
    }

    public T createStrictMock(String arg0) {
      final T mock = delegate.createStrictMock(arg0);
      objectsToVerify.add(mock);
      return mock;
    }

    public IMockBuilder<T> withArgs(Object... arg0) {
      return delegate.withArgs(arg0);
    }

    public IMockBuilder<T> withConstructor() {
      return delegate.withConstructor();
    }

    public IMockBuilder<T> withConstructor(Constructor<?> arg0) {
      return delegate.withConstructor(arg0);
    }

    public IMockBuilder<T> withConstructor(Object... arg0) {
      return delegate.withConstructor(arg0);
    }

    public IMockBuilder<T> withConstructor(Class<?>... arg0) {
      return delegate.withConstructor(arg0);
    }
  }
}
