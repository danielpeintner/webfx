package mongoose.activities.frontend.application;

import mongoose.activities.frontend.container.FrontendContainerActivity;
import mongoose.activities.frontend.event.fees.FeesActivity;
import mongoose.activities.frontend.event.program.ProgramActivity;
import mongoose.activities.frontend.event.options.OptionsActivity;
import mongoose.activities.shared.application.MongooseApplication;
import naga.commons.util.function.Factory;
import naga.framework.activity.client.UiDomainActivityContext;
import naga.framework.ui.router.UiRouter;
import naga.platform.activity.Activity;

/**
 * @author Bruno Salmon
 */
public class MongooseFrontendApplication extends MongooseApplication {

    @Override
    protected Factory<Activity<UiDomainActivityContext>> getContainerActivityFactory() {
        return FrontendContainerActivity::new;
    }

    @Override
    protected UiRouter setupContainedRouter(UiRouter containedRouter) {
        return super.setupContainedRouter(containedRouter)
                .route("/event/:eventId/fees", FeesActivity::new)
                .route("/event/:eventId/program", ProgramActivity::new)
                .route("/event/:eventId/options", OptionsActivity::new);
    }

    @Override
    public void onStart() {
        context.getUiRouter().setDefaultInitialHistoryPath("/event/137/fees");
        super.onStart();
    }

    public static void main(String[] args) {
        launchApplication(new MongooseFrontendApplication(), args);
    }

}
