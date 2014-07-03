package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.SQLLang;

public interface IBuilder<T extends SQLLang> {

	public T build();

	/**
	 * Equivalent to {@link #build()}.{@link #SQLLang.getSQL()}
	 * 
	 * @return
	 */
	public String buildSQL();

}
