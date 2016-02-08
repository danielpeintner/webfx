package naga.core.orm.expression.term;

import naga.core.type.PrimType;
import naga.core.type.Type;

/**
 * @author Bruno Salmon
 */
public class Exists extends SelectExpression {

    public Exists(Select select) {
        super(select);
    }

    @Override
    public Type getType() {
        return PrimType.BOOLEAN;
    }

    @Override
    public Object evaluate(Object data) {
        return null;
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        sb.append("exists");
        return super.toString(sb);
    }

}
