package mongoose.entities;

import naga.framework.orm.entity.EntityId;
import naga.framework.orm.entity.EntityStore;
import naga.framework.orm.entity.impl.DynamicEntity;

import java.time.Instant;

/**
 * @author Jean-Pierre Alonso.
 */
public class LtTestSetEntityImpl extends DynamicEntity implements LtTestSetEntity {

    public LtTestSetEntityImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    @Override
    public Instant getDate() {
        return getInstantFieldValue("date");
    }

    @Override
    public void setDate(Instant date) {
        setFieldValue("date", date);
    }

    @Override
    public String getName() {
        return getStringFieldValue("name");
    }

    @Override
    public void setName(String name) {
        setFieldValue("name", name);
    }

    @Override
    public String getComment() {
        return getStringFieldValue("comment");
    }

    @Override
    public void setComment(String comment) {
        setFieldValue("comment", comment);
    }
}
