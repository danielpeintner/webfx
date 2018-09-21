package webfx.platforms.core.services.querypush.spi.remote;

import webfx.platforms.core.services.buscall.BusCallService;
import webfx.platforms.core.datasource.ConnectionDetails;
import webfx.platforms.core.datasource.LocalDataSourceRegistry;
import webfx.platforms.core.services.log.Logger;
import webfx.platforms.core.services.querypush.PulseArgument;
import webfx.platforms.core.services.querypush.QueryPushArgument;
import webfx.platforms.core.services.querypush.QueryPushResult;
import webfx.platforms.core.services.querypush.QueryPushService;
import webfx.platforms.core.services.querypush.spi.QueryPushServiceProvider;
import webfx.platforms.core.util.async.Future;
import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public class RemoteQueryPushServiceProviderImpl implements QueryPushServiceProvider {

    @Override
    public Future<Object> executeQueryPush(QueryPushArgument argument) {
        QueryPushServiceProvider localConnectedProvider = getOrCreateLocalConnectedProvider(argument.getDataSourceId());
        if (localConnectedProvider != null)
            return localConnectedProvider.executeQueryPush(argument);
        return executeRemoteQueryPush(argument);
    }

    @Override
    public void requestPulse(PulseArgument argument) {
        QueryPushServiceProvider localConnectedProvider = getOrCreateLocalConnectedProvider(argument.getDataSourceId());
        if (localConnectedProvider == null)
            throw new UnsupportedOperationException("requestPulse() shouldn't be called on this RemoteQueryPushServiceProviderImpl");
        localConnectedProvider.requestPulse(argument);
    }

    protected QueryPushServiceProvider getOrCreateLocalConnectedProvider(Object dataSourceId) {
        QueryPushServiceProvider localConnectedProvider = LocalQueryPushServiceProviderRegistry.getLocalConnectedProvider(dataSourceId);
        if (localConnectedProvider == null) {
            ConnectionDetails connectionDetails = LocalDataSourceRegistry.getLocalDataSourceConnectionDetails(dataSourceId);
            if (connectionDetails != null) {
                localConnectedProvider = createLocalConnectedProvider(connectionDetails);
                LocalQueryPushServiceProviderRegistry.registerLocalConnectedProvider(dataSourceId, localConnectedProvider);
            }
        }
        return localConnectedProvider;
    }

    protected QueryPushServiceProvider createLocalConnectedProvider(ConnectionDetails connectionDetails) {
        throw new UnsupportedOperationException("This platform doesn't provide local QueryPushServiceProvider");
    }

    protected <T> Future<T> executeRemoteQueryPush(QueryPushArgument argument) {
        Consumer<QueryPushResult> queryPushResultConsumer = argument.getQueryPushResultConsumer();
        Object queryStreamId = argument.getQueryStreamId();
        boolean isConsumerRegistrationPendingCall = queryStreamId == null && queryPushResultConsumer != null;
        if (isConsumerRegistrationPendingCall)
            consumerRegistrationPendingCalls++;
        else if (queryStreamId != null && queryPushResultConsumer != null)
            queryPushResultConsumers.put(queryStreamId, queryPushResultConsumer);
        Future<T> call = BusCallService.call(QueryPushService.QUERY_PUSH_SERVICE_ADDRESS, argument);
        if (isConsumerRegistrationPendingCall)
            call = call.map(newQueryStreamId -> {
                synchronized (queryPushResultConsumers) {
                    // Registering the consumer along the returned queryStreamId
                    queryPushResultConsumers.put(newQueryStreamId, queryPushResultConsumer);
                    consumerRegistrationPendingCalls--; // Decreasing the pending calls now that the consumer is registered
                    // Retrying sending results with no consumer if any
                    for (QueryPushResult qpr : withNoConsumerReceivedResults)
                        onQueryPushResultReceived(qpr);
                    withNoConsumerReceivedResults.clear();
                    return newQueryStreamId;
                }
            });
        return call;
    }

    private final static Map<Object, Consumer<QueryPushResult>> queryPushResultConsumers = new HashMap<>();
    private static int consumerRegistrationPendingCalls;
    private final static List<QueryPushResult> withNoConsumerReceivedResults = new ArrayList<>();

    private static void onQueryPushResultReceived(QueryPushResult qpr) {
        //Logger.log("Received qpr");
        synchronized (queryPushResultConsumers) {
            Consumer<QueryPushResult> consumer = queryPushResultConsumers.get(qpr.getQueryStreamId());
            if (consumer != null)
                consumer.accept(qpr);
            else if (consumerRegistrationPendingCalls > 0) // Consumer not found but this may be because this result has been received before the registration call returns
                withNoConsumerReceivedResults.add(qpr); // we will retry on next registration call return (see executeRemoteQueryPush)
            else // Definitely no consumer registered along that queryStreamId
                Logger.log("QueryPushResult received with unregistered queryStreamId = " + qpr.getQueryStreamId());
        }
    }

    static {
        QueryPushService.registerQueryPushClientConsumer(RemoteQueryPushServiceProviderImpl::onQueryPushResultReceived);
    }

}
