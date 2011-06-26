// Copyright 2011 Sociodyne LLC. All rights reserved.

package com.sociodyne.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Mock {
	public enum Type {
		STRICT,
		NICE,
		DEFAULT
	}

	Type value() default(Type.DEFAULT);
}
