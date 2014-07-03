package org.mariotaku.twidere.util.net;

import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntityHC4;

import twitter4j.http.HttpParameter;

import java.io.UnsupportedEncodingException;

public class HttpParameterFormEntity extends StringEntityHC4 {

	public static final ContentType CONTENT_TYPE = ContentType.APPLICATION_FORM_URLENCODED.withCharset(Consts.UTF_8);

	public HttpParameterFormEntity(final HttpParameter[] params) throws UnsupportedEncodingException {
		super(HttpParameter.encodeParameters(params), CONTENT_TYPE);
	}

}