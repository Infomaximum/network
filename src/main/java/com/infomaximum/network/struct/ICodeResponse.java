package com.infomaximum.network.struct;

import java.io.Serializable;

/**
 * Created by kris on 21.09.16.
 */
public interface ICodeResponse {

	public abstract Serializable SUCCESS();

	public abstract Serializable INTERNAL_ERROR();
}
