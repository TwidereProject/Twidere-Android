package org.mariotaku.querybuilder;

public class NewColumn implements SQLLang {

	private final String name;
	private final String type;

	public NewColumn(final String name, final String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getSQL() {
		if (name == null || type == null) throw new NullPointerException("name and type must not be null!");
		return String.format("%s %s", name, type);
	}

	public String getType() {
		return type;
	}

	public static NewColumn[] createNewColumns(final String[] colNames, final String[] colTypes) {
		if (colNames == null || colTypes == null || colNames.length != colTypes.length)
			throw new IllegalArgumentException("length of columns and types not match.");
		final NewColumn[] newColumns = new NewColumn[colNames.length];
		for (int i = 0, j = colNames.length; i < j; i++) {
			newColumns[i] = new NewColumn(colNames[i], colTypes[i]);
		}
		return newColumns;
	}

}
