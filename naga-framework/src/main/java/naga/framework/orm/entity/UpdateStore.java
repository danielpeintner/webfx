package naga.framework.orm.entity;

import naga.commons.util.async.Batch;
import naga.commons.util.async.Future;
import naga.framework.orm.domainmodel.DataSourceModel;
import naga.framework.orm.domainmodel.DomainClass;
import naga.framework.orm.entity.impl.UpdateStoreImpl;
import naga.framework.orm.entity.resultset.EntityChanges;
import naga.platform.services.update.UpdateArgument;
import naga.platform.services.update.UpdateResult;

/**
 * @author Bruno Salmon
 */
public interface UpdateStore extends EntityStore {

    EntityChanges getEntityChanges();

    <E extends Entity> E insertEntity(Class<E> entityClass);

    <E extends Entity> E insertEntity(DomainClass domainClass);

    <E extends Entity> E insertEntity(Object domainClassId);

    <E extends Entity> E updateEntity(E entity);

    void deleteEntity(Entity entity);

    void deleteEntity(EntityId entityId);

    boolean hasChanges();

    void cancelChanges();

    void markChangesAsCommitted();

    default Future<Batch<UpdateResult>> executeUpdate() {
        return executeUpdate(null);
    }

    Future<Batch<UpdateResult>> executeUpdate(UpdateArgument[] initialUpdates);

    // Factory

    static UpdateStore create(DataSourceModel dataSourceModel) {
        return new UpdateStoreImpl(dataSourceModel);
    }

}
