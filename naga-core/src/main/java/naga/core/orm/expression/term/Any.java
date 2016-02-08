package naga.core.orm.expression.term;

/**
 * @author Bruno Salmon
 */
public class Any extends BooleanExpression {

    public Any(Expression left, String operator, Expression right) {
        super(left, operator + " any ", right, 5);
    }

    public boolean evaluateCondition(Object a, Object b) {
        throw new UnsupportedOperationException();
    }

}
