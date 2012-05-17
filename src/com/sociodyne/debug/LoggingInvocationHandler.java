// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.debug;

import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LoggingInvocationHandler implements InvocationHandler {

  private final Object delegate;
  private final PrintStream writer;

  public LoggingInvocationHandler(Object delegate, PrintStream writer) {
    this.delegate = delegate;
    this.writer = writer;
  }

  public Object invoke(Object object, Method method, Object[] args) throws Throwable {
    final Object result = method.invoke(delegate, args);
    final StringBuffer logBuffer = new StringBuffer();
    logBuffer.append(method.getReturnType()).append(' ').append(method.getName()).append('(');
    if (args != null) {
      if (args.length > 0) {
        for (final Object arg : args) {
          String value;
          if (arg != null) {
            value = arg.toString();
            if (value == null) {
              value = arg.getClass().getName();
            }
            if (arg.getClass().equals(String.class)) {
              value = "\"" + value + "\"";
            } else if (arg.getClass().isArray()
                && arg.getClass().getComponentType().equals(char.class)) {
              value = "\"" + new String((char[]) arg) + "\"";
            }
          } else {
            value = "null";
          }
          logBuffer.append(value).append(", ");
        }

        logBuffer.delete(logBuffer.length() - 2, logBuffer.length());
      }
    }
    logBuffer.append(')');
    writer.println(logBuffer.toString());
    return result;
  }
}
