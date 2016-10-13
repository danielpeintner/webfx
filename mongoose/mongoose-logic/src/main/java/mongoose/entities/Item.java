package mongoose.entities;

import mongoose.entities.markers.EntityHasCode;
import mongoose.entities.markers.EntityHasLabel;
import mongoose.entities.markers.EntityHasName;
import mongoose.entities.markers.HasItemFamilyType;
import naga.framework.orm.entity.Entity;
import naga.framework.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface Item extends Entity, EntityHasCode, EntityHasName, EntityHasLabel, HasItemFamilyType {

    //// Domain fields

    default void setFamily(Object family) {
        setForeignField("family", family);
    }

    default EntityId getFamilyId() {
        return getForeignEntityId("family");
    }

    default ItemFamily getFamily() {
        return getForeignEntity("family");
    }

    default void setRateAliasItem(Object rateAliasItem) {
        setForeignField("rateAliasItem", rateAliasItem);
    }

    default EntityId getRateAliasItemId() {
        return getForeignEntityId("rateAliasItem");
    }

    default Item getRateAliasItem() {
        return getForeignEntity("rateAliasItem");
    }

    default void setShare_mate(Boolean share_mate) {
        setFieldValue("share_mate", share_mate);
    }

    default Boolean isShare_mate() {
        return getBooleanFieldValue("forceSoldout");
    }

    //// Enriched fields and methods

    @Override
    default ItemFamilyType getItemFamilyType() {
        ItemFamily family = getFamily();
        return family == null ? ItemFamilyType.UNKNOWN : family.getItemFamilyType();
    }
}
