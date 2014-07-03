/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import twitter4j.http.HttpParameter;
import twitter4j.internal.util.InternalStringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 3.0.2
 */
public final class OEmbedRequest implements Serializable {
	private static final long serialVersionUID = -4330607167106242987L;
	private final long statusId;
	private final String url;
	private int maxWidth;
	private boolean hideMedia = true;
	private boolean hideThread = true;
	private boolean omitScript = false;
	private Align align = Align.NONE;
	private String[] related = {};
	private String lang;

	OEmbedRequest(final long statusId, final String url) {
		this.statusId = statusId;
		this.url = url;
	}

	public OEmbedRequest align(final Align align) {
		this.align = align;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final OEmbedRequest that = (OEmbedRequest) o;

		if (hideMedia != that.hideMedia) return false;
		if (hideThread != that.hideThread) return false;
		if (maxWidth != that.maxWidth) return false;
		if (omitScript != that.omitScript) return false;
		if (statusId != that.statusId) return false;
		if (align != that.align) return false;
		if (lang != null ? !lang.equals(that.lang) : that.lang != null) return false;
		if (!Arrays.equals(related, that.related)) return false;
		if (url != null ? !url.equals(that.url) : that.url != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (statusId ^ statusId >>> 32);
		result = 31 * result + (url != null ? url.hashCode() : 0);
		result = 31 * result + maxWidth;
		result = 31 * result + (hideMedia ? 1 : 0);
		result = 31 * result + (hideThread ? 1 : 0);
		result = 31 * result + (omitScript ? 1 : 0);
		result = 31 * result + (align != null ? align.hashCode() : 0);
		result = 31 * result + (related != null ? Arrays.hashCode(related) : 0);
		result = 31 * result + (lang != null ? lang.hashCode() : 0);
		return result;
	}

	public OEmbedRequest HideMedia(final boolean hideMedia) {
		this.hideMedia = hideMedia;
		return this;
	}

	public OEmbedRequest HideThread(final boolean hideThread) {
		this.hideThread = hideThread;
		return this;
	}

	public OEmbedRequest lang(final String lang) {
		this.lang = lang;
		return this;
	}

	public OEmbedRequest MaxWidth(final int maxWidth) {
		this.maxWidth = maxWidth;
		return this;
	}

	public OEmbedRequest omitScript(final boolean omitScript) {
		this.omitScript = omitScript;
		return this;
	}

	public OEmbedRequest related(final String[] related) {
		this.related = related;
		return this;
	}

	public void setAlign(final Align align) {
		this.align = align;
	}

	public void setHideMedia(final boolean hideMedia) {
		this.hideMedia = hideMedia;
	}

	public void setHideThread(final boolean hideThread) {
		this.hideThread = hideThread;
	}

	public void setLang(final String lang) {
		this.lang = lang;
	}

	public void setMaxWidth(final int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void setOmitScript(final boolean omitScript) {
		this.omitScript = omitScript;
	}

	public void setRelated(final String[] related) {
		this.related = related;
	}

	@Override
	public String toString() {
		return "OEmbedRequest{" + "statusId=" + statusId + ", url='" + url + '\'' + ", maxWidth=" + maxWidth
				+ ", hideMedia=" + hideMedia + ", hideThread=" + hideThread + ", omitScript=" + omitScript + ", align="
				+ align + ", related=" + (related == null ? null : Arrays.asList(related)) + ", lang='" + lang + '\''
				+ '}';
	}

	private void appendParameter(final String name, final long value, final List<HttpParameter> params) {
		if (0 <= value) {
			params.add(new HttpParameter(name, String.valueOf(value)));
		}
	}

	private void appendParameter(final String name, final String value, final List<HttpParameter> params) {
		if (value != null) {
			params.add(new HttpParameter(name, value));
		}
	}

	/* package */HttpParameter[] asHttpParameterArray() {
		final ArrayList<HttpParameter> params = new ArrayList<HttpParameter>(12);
		appendParameter("id", statusId, params);
		appendParameter("url", url, params);
		appendParameter("maxwidth", maxWidth, params);
		params.add(new HttpParameter("hide_media", hideMedia));
		params.add(new HttpParameter("hide_thread", hideThread));
		params.add(new HttpParameter("omit_script", omitScript));
		params.add(new HttpParameter("align", align.name().toLowerCase(Locale.US)));
		if (related.length > 0) {
			appendParameter("related", InternalStringUtil.join(related), params);
		}
		appendParameter("lang", lang, params);
		final HttpParameter[] paramArray = new HttpParameter[params.size()];
		return params.toArray(paramArray);
	}

	public enum Align {
		LEFT, CENTER, RIGHT, NONE
	}
}
