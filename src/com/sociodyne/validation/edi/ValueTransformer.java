package com.sociodyne.validation.edi;

public interface ValueTransformer<I, O> {
	O transform(I input);
}
