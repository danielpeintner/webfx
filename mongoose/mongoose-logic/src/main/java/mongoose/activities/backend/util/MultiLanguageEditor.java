package mongoose.activities.backend.util;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import naga.framework.expression.sqlcompiler.sql.SqlCompiled;
import naga.framework.orm.domainmodel.DataSourceModel;
import naga.framework.orm.entity.Entity;
import naga.framework.orm.entity.EntityStore;
import naga.framework.orm.entity.UpdateStore;
import naga.framework.orm.mapping.QueryResultSetToEntityListGenerator;
import naga.framework.ui.graphic.controls.button.ButtonFactoryMixin;
import naga.framework.ui.layouts.LayoutUtil;
import naga.fx.properties.Properties;
import naga.fxdata.control.HtmlTextEditor;
import naga.platform.services.query.QueryArgument;
import naga.platform.services.query.spi.QueryService;
import naga.util.Objects;
import naga.util.Strings;
import naga.util.async.Handler;
import naga.util.function.Callable;
import naga.util.function.Function;
import naga.util.tuples.Pair;

import java.util.HashMap;
import java.util.Map;

import static mongoose.actions.MongooseIcons.getLanguageIcon32;
import static naga.framework.ui.action.StandardActionKeys.*;
import static naga.framework.ui.layouts.LayoutUtil.setMaxPrefSizeToInfinite;

/**
 * @author Bruno Salmon
 */
public class MultiLanguageEditor {

    private final static String[] languages = {"en", "de", "es", "fr", "pt"};
    private final static String entityListId = "loadedEntity";

    private final ButtonFactoryMixin buttonFactory; // Also acts as i18n
    private final Callable entityIdGetter;
    private final DataSourceModel dataSourceModel;
    private final EntityStore loadingStore;
    private final String loadingSelect;
    private final Function<Object, Object> bodyFieldGetter;
    private final Function<Object, Object> subjectFieldGetter;

    private final Map<Object, ToggleButton> languageButtons = new HashMap<>();
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final BorderPane borderPane = setMaxPrefSizeToInfinite(new BorderPane());
    private Handler<Entity> closeCallback;

    private final Map<Object /*entityId*/, EditedEntity> entityUpdates = new HashMap<>();
    private final Map<Pair<Object /*entityId**/, Object /*language*/>, MonoLanguageEditor> monoLanguageEditors = new HashMap<>();

    public MultiLanguageEditor(ButtonFactoryMixin buttonFactory, Entity entity, Function<Object, Object> bodyFieldGetter, Function<Object, Object> subjectFieldGetter) {
        this(buttonFactory, entity::getId, entity.getStore(), bodyFieldGetter, subjectFieldGetter, null);
        entityUpdates.put(entity.getId(), new EditedEntity(entity));
    }

    public MultiLanguageEditor(ButtonFactoryMixin buttonFactory, Callable entityIdGetter, DataSourceModel dataSourceModel, Function<Object, Object> bodyFieldGetter, Function<Object, Object> subjectFieldGetter, String domainClassIdOrLoadingSelect) {
        this(buttonFactory, entityIdGetter, EntityStore.create(dataSourceModel), bodyFieldGetter, subjectFieldGetter, domainClassIdOrLoadingSelect);
    }

    public MultiLanguageEditor(ButtonFactoryMixin buttonFactory, Callable entityIdGetter, EntityStore loadingStore, Function<Object, Object> bodyFieldGetter, Function<Object, Object> subjectFieldGetter, String domainClassIdOrLoadingSelect) {
        this.buttonFactory = buttonFactory;
        this.entityIdGetter = entityIdGetter;
        this.loadingStore = loadingStore;
        this.dataSourceModel = loadingStore.getDataSourceModel();
        this.bodyFieldGetter = bodyFieldGetter;
        this.subjectFieldGetter = subjectFieldGetter;
        StringBuilder sb = domainClassIdOrLoadingSelect == null || domainClassIdOrLoadingSelect.startsWith("select ") ? null : new StringBuilder("select ");
        for (String language : languages) {
            ToggleButton languageButton = new ToggleButton(null, getLanguageIcon32(language));
            languageButton.setUserData(language);
            languageButton.setMinWidth(50d);
            languageButtons.put(language, languageButton);
            if (sb != null) {
                if (!Objects.areEquals(language, languages[0]))
                    sb.append(',');
                sb.append(bodyFieldGetter.apply(language));
                if (subjectFieldGetter != null)
                    sb.append(',').append(subjectFieldGetter.apply(language));
            }
        }
        this.loadingSelect = sb == null ? domainClassIdOrLoadingSelect : sb.append(" from ").append(domainClassIdOrLoadingSelect).append(" where id=?").toString();
        toggleGroup.getToggles().setAll(languageButtons.values());
    }

    public MultiLanguageEditor showOkCancelButton(Handler<Entity> closeCallback) {
        this.closeCallback = closeCallback;
        return this;
    }

    public BorderPane getUiNode() {
        if (toggleGroup.getSelectedToggle() == null) {
            Properties.runOnPropertiesChange(this::onEntityChanged, toggleGroup.selectedToggleProperty());
            toggleGroup.selectToggle(languageButtons.get(buttonFactory.getLanguage()));
        }
        return borderPane;
    }

    public void onEntityChanged() {
        MonoLanguageEditor monoLanguageEditor = getCurrentMonoLanguageEditor();
        if (monoLanguageEditor == null)
            return;
        monoLanguageEditor.displayEditor();
        Object entityId = entityIdGetter.call();
        if (entityUpdates.containsKey(entityId))
            monoLanguageEditor.setEditedEntity(entityUpdates.get(entityId));
        else if (loadingSelect != null) {
            SqlCompiled sqlCompiled = dataSourceModel.getDomainModel().compileSelect(loadingSelect);
            // Then we ask the query service to execute the sql query
            QueryService.executeQuery(new QueryArgument(sqlCompiled.getSql(), new Object[]{entityId}, dataSourceModel.getId())).setHandler(ar -> {
                if (ar.succeeded()) {
                    Entity entity = QueryResultSetToEntityListGenerator.createEntityList(ar.result(), sqlCompiled.getQueryMapping(), loadingStore, entityListId).get(0);
                    EditedEntity editedEntity = new EditedEntity(entity);
                    entityUpdates.put(entityId, editedEntity);
                    monoLanguageEditor.setEditedEntity(editedEntity);
                }
            });
        }
    }

    private MonoLanguageEditor getCurrentMonoLanguageEditor() {
        Object entityId = entityIdGetter.call();
        if (entityId == null)
            return null;
        Toggle selectedLanguageButton = toggleGroup.getSelectedToggle();
        Object language = selectedLanguageButton != null ? selectedLanguageButton.getUserData() : buttonFactory.getLanguage();
        Pair<Object, Object> pair = new Pair<>(entityId, language);
        MonoLanguageEditor monoLanguageEditor = monoLanguageEditors.get(pair);
        if (monoLanguageEditor == null)
            monoLanguageEditors.put(pair, monoLanguageEditor = new MonoLanguageEditor(pair.get2()));
        return monoLanguageEditor;
    }

    private static class EditedEntity {
        private final Entity loadedEntity;
        private final UpdateStore updateStore;
        private Entity updatedEntity;

        EditedEntity(Entity loadedEntity) {
            this.loadedEntity = loadedEntity;
            updateStore = UpdateStore.createAbove(loadedEntity.getStore());
            cancelChanges();
        }

        private void cancelChanges() {
            updateStore.cancelChanges();
            updatedEntity = updateStore.updateEntity(loadedEntity);
        }
    }

    private class MonoLanguageEditor {
        private final TextField subjectTextField = new TextField();
        private final HtmlTextEditor editor = new HtmlTextEditor();
        private final Button saveButton =   buttonFactory.newButton(closeCallback != null ? OK_ACTION_KEY     : SAVE_ACTION_KEY ,  this::save);
        private final Button revertButton = buttonFactory.newButton(closeCallback != null ? CANCEL_ACTION_KEY : REVERT_ACTION_KEY, this::revert);
        private final Object subjectField;
        private final Object bodyField;
        private EditedEntity editedEntity;

        MonoLanguageEditor(Object lang) {
            subjectField = subjectFieldGetter == null ? null : subjectFieldGetter.apply(lang);
            bodyField = bodyFieldGetter.apply(lang);
            Properties.runOnPropertiesChange(this::syncEntityFromUi, subjectTextField.textProperty(), editor.textProperty());
        }

        void syncEntityFromUi() {
            if (editedEntity != null) {
                Entity updatedEntity = editedEntity.updatedEntity;
                if (subjectField != null) {
                    String uiSubject = subjectTextField.getText();
                    String entitySubject = updatedEntity.getStringFieldValue(subjectField);
                    if (!Objects.areEquals(uiSubject, entitySubject))
                        updatedEntity.setFieldValue(subjectField, uiSubject);
                }
                String uiBody = format(editor.getText());
                String entityBody = updatedEntity.getStringFieldValue(bodyField);
                if (uiBody != null && !Objects.areEquals(uiBody, entityBody))
                    updatedEntity.setFieldValue(bodyField, uiBody);
                updateButtonsDisable();
            }
        }

        String format(String editorText) {
            if (editorText != null) {
                editorText = Strings.replaceAll(editorText,"\n", "");
                if (editorText.startsWith("<p>") && editorText.indexOf("</p>") == editorText.length() - 4)
                    editorText = editorText.substring(3, editorText.length() - 4);
            }
            return editorText;
        }

        void syncUiFromEntity() {
            if (editedEntity != null) {
                Entity updatedEntity = editedEntity.updatedEntity;
                if (subjectField != null) {
                    String uiSubject = subjectTextField.getText();
                    String entitySubject = updatedEntity.getStringFieldValue(subjectField);
                    if (!Objects.areEquals(uiSubject, entitySubject))
                        subjectTextField.setText(entitySubject);
                }
                String uiBody = editor.getText();
                String entityBody = updatedEntity.getStringFieldValue(bodyField);
                if (!Objects.areEquals(uiBody, entityBody))
                    editor.setText(entityBody);
                updateButtonsDisable();
            }
        }

        void setEditedEntity(EditedEntity editedEntity) {
            if (this.editedEntity != editedEntity) {
                this.editedEntity = editedEntity;
                syncUiFromEntity();
            }
        }

        void revert() {
            if (editedEntity != null) {
                editedEntity.cancelChanges();
                syncUiFromEntity();
            }
            callCloseCallback(false);
        }

        void save() {
            if (editedEntity != null)
                editedEntity.updateStore.executeUpdate().setHandler(ar -> {
                    if (ar.succeeded()) {
                        updateButtonsDisable();
                        callCloseCallback(true);
                    }
                });
        }

        void callCloseCallback(boolean saved) {
            if (closeCallback != null)
                closeCallback.handle(saved ? editedEntity.loadedEntity : null);
        }

        void updateButtonsDisable() {
            boolean disable = editedEntity == null || !editedEntity.updateStore.hasChanges();
            saveButton.setDisable(disable);
            revertButton.setDisable(disable && closeCallback == null);
        }

        void displayEditor() {
            if (borderPane != null && borderPane.getCenter() != editor) {
                syncUiFromEntity();
                if (subjectField != null)
                    borderPane.setTop(subjectTextField);
                borderPane.setCenter(editor);
                BorderPane buttonsBar = new BorderPane();
                HBox hBox = new HBox();
                for (Object language : languages)
                    hBox.getChildren().add(languageButtons.get(language));
                buttonsBar.setLeft(hBox);
                buttonsBar.setCenter(new HBox(20, LayoutUtil.createHGrowable(), saveButton, revertButton, LayoutUtil.createHGrowable()));
                borderPane.setBottom(buttonsBar);
                // The following code is just a temporary workaround to make CKEditor work in html platform (to be removed once fixed)
                if (editedEntity != null) {
                    editor.resize(1, 1);
                    editor.requestLayout();
                }
            }
        }
    }
}
