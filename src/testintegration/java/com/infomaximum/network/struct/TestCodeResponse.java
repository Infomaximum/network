package com.infomaximum.network.struct;

import java.io.Serializable;

/**
 * Created by kris on 27.10.16.
 */
public class TestCodeResponse implements ICodeResponse {

	@Override
	public Serializable SUCCESS() {
		return "success";
	}

	@Override
	public Serializable INTERNAL_ERROR() {
		return "internal_error";
	}
}
