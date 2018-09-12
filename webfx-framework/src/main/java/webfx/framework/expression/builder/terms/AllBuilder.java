package webfx.framework.expression.builder.terms;

import webfx.framework.expression.Expression;
import webfx.framework.expression.terms.All;

/**
 * @author Bruno Salmon
 */
public class AllBuilder extends BinaryBooleanExpressionBuilder {
    String operator;

    public AllBuilder(ExpressionBuilder left, String operator, ExpressionBuilder right) {
        super(left, right);
        this.operator = operator;
    }

    @Override
    protected All newBinaryOperation(Expression left, Expression right) {
        return new All(left, operator, right);
    }
}