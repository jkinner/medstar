// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.common;

import java.lang.reflect.Constructor;

/**
 * Utility methods for dealing with {@link Exception Exceptions}.
 * 
 * @author jkinner
 */
public class Exceptions {

  private Exceptions() {
  }

  public static <T extends Throwable> T wrap(T throwable, String prefix) {
    T wrapped = null;

    try {
      // Type-safe multivariate cast
      @SuppressWarnings("unchecked")
      final Constructor<T> c = (Constructor<T>) throwable.getClass().getConstructor(
          new Class<?>[] { String.class, Throwable.class });
      wrapped = c.newInstance(prefix + ": " + throwable.getMessage(), throwable);
    } catch (final NoSuchMethodException e) {
      // Try the Exception ctor
      if (Exception.class.isAssignableFrom(throwable.getClass())) {
        try {
          // Type-safe multivariate cast
          @SuppressWarnings("unchecked")
          final Constructor<T> c = (Constructor<T>) throwable.getClass().getConstructor(
              new Class<?>[] { String.class, Exception.class });
          wrapped = c.newInstance(prefix + ": " + throwable.getMessage(), throwable);
        } catch (final Exception e2) {
          // Look for a String ctor and at least create the new
          // message
        }
      }
    } catch (final Exception e) {
      // Do nothing; there are bigger issues than not being able to wrap
      // the exception
    }
    if (wrapped == null) {
      try {
        @SuppressWarnings("unchecked")
        final Constructor<T> c = (Constructor<T>) throwable.getClass().getConstructor(
            new Class<?>[] { String.class });
        wrapped = c.newInstance(prefix + ": " + throwable.getMessage());
      } catch (final Exception e3) {
        // Maybe it will accept and Object (Like AssertionError)
        try {
          @SuppressWarnings("unchecked")
          final Constructor<T> c = (Constructor<T>) throwable.getClass().getConstructor(
              new Class<?>[] { Object.class });
          wrapped = c.newInstance(prefix + ": " + throwable.getMessage());
        } catch (final Exception e) {
          // Give up.
        }
      }
    }

    return wrapped == null ? throwable : wrapped;
  }
}
