package mongoose.activities.shared.generic.session;

import javafx.beans.property.Property;
import mongoose.authn.MongooseUserPrincipal;
import mongoose.domainmodel.loader.DomainModelSnapshotLoader;
import naga.framework.orm.entity.Entities;
import naga.framework.orm.entity.Entity;
import naga.framework.orm.entity.EntityId;
import naga.framework.orm.entity.UpdateStore;
import naga.fx.spi.Toolkit;
import naga.platform.bus.Bus;
import naga.platform.bus.BusHook;
import naga.platform.bus.Registration;
import naga.platform.services.log.spi.Logger;
import naga.platform.services.push.client.spi.PushClientService;
import naga.platform.services.shutdown.spi.Shutdown;
import naga.platform.services.storage.spi.LocalStorage;
import naga.platform.spi.Platform;

import java.time.Instant;

/**
 * @author Bruno Salmon
 */
public class ClientSessionRecorder {

    private final static ClientSessionRecorder INSTANCE = new ClientSessionRecorder();

    public static ClientSessionRecorder get() {
        return INSTANCE;
    }

    static {
        Logger.log("User Agent = " + getUserAgent());
        Logger.log("application.name = " + getApplicationName());
        Logger.log("application.version = " + getApplicationVersion());
        Logger.log("application.build.tool = " + getApplicationBuildTool());
        Logger.log("application.build.timestamp = " + getApplicationBuildTimestampString());
        Logger.log("application.build.number = " + getApplicationBuildNumberString());
        // Registering the server push client listener. This registration is private (ie just done locally on the client
        // event bus) so not directly visible on the server event bus but the server can reach that listener by running:
        // BusCallService.call(clientBusCallServiceAddress, "serverPushClientListener", arg, ...)
        // because the client bus call service will finally pass the arg to that listener over the local client bus.
        // But of course to make this work, the client bus call service must listen server calls by running:
        // BusCallService.listenBusEntryCalls(clientBusCallServiceAddress)
        // This registration is public (so visible on the server event bus) but it will be done later by the method
        // listenServerPushCallsIfReady() because clientBusCallServiceAddress is computed from client process id which
        // is not yet known at this stage (the purpose is to have a unique address for each client that can be easily
        // computed by the server as well from the process id read from session tables).
        PushClientService.registerPushFunction("serverPushClientListener", arg -> {
            Logger.log(arg);
            return "OK";
        });
    }

    private final Bus bus;
    private Registration pushClientRegistration;

    public ClientSessionRecorder() {
        this(Platform.bus());
    }

    public ClientSessionRecorder(Bus bus) {
        this.bus = bus;
        bus.setHook(new BusHook() {
            @Override
            public void handleOpened() {
                onConnectionOpened();
            }

            @Override
            public boolean handlePreClose() {
                onConnectionClosed();
                return true;
            }

            @Override
            public void handlePostClose() {
                onConnectionClosed();
            }
        });
        if (bus.isOpen())
            onConnectionOpened();
        Shutdown.addShutdownHook(this::onShutdown);
    }

    private final UpdateStore store = UpdateStore.create(DomainModelSnapshotLoader.getDataSourceModel());
    private Entity sessionAgent, sessionApplication, sessionProcess, sessionConnection, sessionUser;

    public void setUserPrincipalProperty(Property<Object> userPrincipalProperty) {
        userPrincipalProperty.addListener((observable, oldValue, userPrincipal) -> {
            if (userPrincipal instanceof MongooseUserPrincipal)
                recordNewSessionUser(((MongooseUserPrincipal) userPrincipal).getUserPersonId());
        });
    }

    private Entity getSessionAgent() {
        if (sessionAgent == null)
            loadOrInsertSessionAgent();
        return sessionAgent;
    }

    private void loadOrInsertSessionAgent() {
        String agentString = getUserAgent();
        if (agentString.length() > 1024)
            agentString = agentString.substring(0, 1024);
        if (agentString.equals(LocalStorage.getItem("sessionAgent.agentString")))
            sessionAgent = recreateSessionEntityFromLocalStorage("SessionAgent", "sessionAgent.id");
        else {
            sessionAgent = insertSessionEntity("SessionAgent", sessionAgent);
            sessionAgent.setFieldValue("agentString", agentString);
        }
    }

    private Entity getSessionApplication() {
        if (sessionApplication == null)
            loadOrInsertSessionApplication();
        return sessionApplication;
    }

    private void loadOrInsertSessionApplication() {
        String applicationName = getApplicationName();
        String applicationVersion = getApplicationVersion();
        String applicationBuildTool = getApplicationBuildTool();
        String applicationBuildNumberString = getApplicationBuildNumberString();
        String applicationBuildTimestampString = getApplicationBuildTimestampString();
        if (applicationName.equals(LocalStorage.getItem("sessionApplication.name"))
                && applicationVersion.equals(LocalStorage.getItem("sessionApplication.version"))
                && applicationBuildTool.equals(LocalStorage.getItem("sessionApplication.buildTool"))
                && applicationBuildNumberString.equals(LocalStorage.getItem("sessionApplication.buildNumberString"))
                && applicationBuildTimestampString.equals(LocalStorage.getItem("sessionApplication.buildTimestampString")))
            sessionApplication = recreateSessionEntityFromLocalStorage("SessionApplication", "sessionApplication.id");
        else {
            sessionApplication = insertSessionEntity("SessionApplication", sessionApplication);
            sessionApplication.setForeignField("agent", getSessionAgent());
            sessionApplication.setFieldValue("name", applicationName);
            sessionApplication.setFieldValue("version", applicationVersion);
            sessionApplication.setFieldValue("buildTool", applicationBuildTool);
            sessionApplication.setFieldValue("buildNumberString", applicationBuildNumberString);
            sessionApplication.setFieldValue("buildNumber", getApplicationBuildNumber());
            sessionApplication.setFieldValue("buildTimestampString", applicationBuildTimestampString);
            sessionApplication.setFieldValue("buildTimestamp", getApplicationBuildTimestamp());
        }
    }

    private Entity getSessionProcess() {
        if (sessionProcess == null)
            insertSessionProcess();
        return sessionProcess;
    }

    private void insertSessionProcess() {
        sessionProcess = insertSessionEntity("SessionProcess", sessionProcess);
        sessionProcess.setForeignField("application", getSessionApplication());
    }

    private void insertSessionConnection() {
        sessionConnection = insertSessionEntity("SessionConnection", sessionConnection);
        sessionConnection.setForeignField("process", getSessionProcess());
    }

    private void onConnectionOpened() {
        listenServerPushCallsIfReady();
        insertSessionConnection();
        executeUpdate();
    }

    private void onConnectionClosed() {
        stopListeningServerPushCalls();
        if (sessionConnection != null) {
            touchSessionEntityEnd(store.updateEntity(sessionConnection));
            executeUpdate();
        }
    }

    private void onShutdown() {
        stopListeningServerPushCalls();
        if (sessionProcess != null) {
            touchSessionEntityEnd(store.updateEntity(sessionProcess));
            if (sessionConnection != null)
                touchSessionEntityEnd(store.updateEntity(sessionConnection));
            if (sessionUser != null)
                touchSessionEntityEnd(store.updateEntity(sessionUser));
            executeUpdate();
        }
    }

    public void recordNewSessionUser(Object userId) {
        createNewSessionUser(userId);
        executeUpdate();
    }

    private void createNewSessionUser(Object userId) {
        sessionUser = insertSessionEntity("SessionUser", sessionUser);
        sessionUser.setForeignField("process", getSessionProcess());
        sessionUser.setForeignField("user", userId);
    }

    private void executeUpdate() {
        boolean newSessionAgent = Entities.isNew(sessionAgent);
        boolean newSessionApplication =  Entities.isNew(sessionApplication);
        store.executeUpdate().setHandler(ar -> {
            if (ar.failed())
                Logger.log(ar.cause());
            else {
                if (newSessionAgent)
                    storeEntityToLocalStorage(sessionAgent, "sessionAgent", "agentString");
                if (newSessionApplication)
                    storeEntityToLocalStorage(sessionApplication, "sessionApplication", "name", "version", "buildTool", "buildNumberString", "buildTimestampString");
                listenServerPushCallsIfReady();
            }
        });
    }

    private void listenServerPushCallsIfReady() {
        if (pushClientRegistration == null && Entities.isNotNew(sessionProcess))
            pushClientRegistration = PushClientService.listenServerPushCalls(sessionProcess.getPrimaryKey());
    }

    private void stopListeningServerPushCalls() {
        if (pushClientRegistration != null && bus.isOpen())
            pushClientRegistration.unregister();
        pushClientRegistration = null;
    }

    private Entity insertSessionEntity(Object domainClassId, Entity previousEntity) {
        return chainSessionEntities(previousEntity, touchSessionEntityStart(store.insertEntity(domainClassId)));
    }

    private Entity recreateSessionEntityFromLocalStorage(Object domainClassId, String idKey) {
        return store.createEntity(EntityId.create(store.getDomainClass(domainClassId), Integer.parseInt(LocalStorage.getItem(idKey))));
    }

    private void storeEntityToLocalStorage(Entity entity, String entityName, Object... fields) {
        storeEntityFieldToLocalStorage(entityName, "id", entity.getPrimaryKey());
        for (Object field : fields)
            storeEntityFieldToLocalStorage(entityName, field, entity.getFieldValue(field));
    }

    private void storeEntityFieldToLocalStorage(String entityName, Object field, Object value) {
        LocalStorage.setItem(entityName + "." + field, value.toString());
    }

    private static Entity touchSessionEntityStart(Entity entity) {
        return touchSessionEntity(entity, true);
    }

    private static Entity touchSessionEntityEnd(Entity entity) {
        return touchSessionEntity(entity, false);
    }

    private static Entity touchSessionEntity(Entity entity, boolean start) {
        entity.setFieldValue(start ? "start" : "end", Instant.now());
        return entity;
    }

    private Entity chainSessionEntities(Entity previousEntity, Entity nextEntity) {
        if (previousEntity != null && nextEntity != null) {
            previousEntity = store.updateEntity(previousEntity);
            previousEntity.setForeignField("next", nextEntity);
            nextEntity.setForeignField("previous", previousEntity);
            touchSessionEntityEnd(previousEntity);
        }
        return nextEntity;
    }

    public static String getUserAgent() {
        return Toolkit.get().getUserAgent();
    }

    public static String getApplicationName() {
        return System.getProperty("application.name");
    }

    public static String getApplicationVersion() {
        return System.getProperty("application.version");
    }

    public static String getApplicationBuildTool() {
        return System.getProperty("application.build.tool");
    }

    public static String getApplicationBuildNumberString() {
        return System.getProperty("application.build.number", "0");
    }

    public static Number getApplicationBuildNumber() {
        try {
            return Integer.valueOf(getApplicationBuildNumberString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getApplicationBuildTimestampString() {
        String timestamp = System.getProperty("application.build.timestamp");
        if (timestamp == null)
            timestamp = Instant.now().toString();
        return timestamp;
    }

    public static Instant getApplicationBuildTimestamp() {
        try {
            return Instant.parse(getApplicationBuildTimestampString());
        } catch (Exception e) {
            return null;
        }
    }
}