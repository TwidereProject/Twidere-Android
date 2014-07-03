package org.mariotaku.querybuilder;

public class RawItemArray implements Selectable {

	private final Object[] array;

	public RawItemArray(final long[] array) {
		final Long[] converted = new Long[array.length];
		for (int i = 0, j = array.length; i < j; i++) {
			converted[i] = array[i];
		}
		this.array = converted;
	}

	public RawItemArray(final Object[] array) {
		this.array = array;
	}

	@Override
	public String getSQL() {
		return Utils.toString(array, ',', false);
	}

}
