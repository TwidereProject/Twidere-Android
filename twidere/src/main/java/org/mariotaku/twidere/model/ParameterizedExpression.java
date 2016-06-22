package org.mariotaku.twidere.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.sqliteqb.library.Expression;

/**
 * Created by mariotaku on 16/6/22.
 */
public class ParameterizedExpression {
    Expression expression;
    String[] parameters;

    public ParameterizedExpression(@NonNull Expression expression, @Nullable String[] parameters) {
        this.expression = expression;
        this.parameters = parameters;
    }

    public String[] getParameters() {
        return parameters;
    }

    public String getSQL() {
        return expression.getSQL();
    }
}
