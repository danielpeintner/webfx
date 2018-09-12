package webfx.framework.activity.base.elementals.presentation.logic.impl;

import webfx.platforms.core.util.function.Factory;

/**
 * @author Bruno Salmon
 */
public abstract class PresentationLogicActivityImpl<PM>
        extends PresentationLogicActivityBase<PresentationLogicActivityContextFinal<PM>, PM> {

    public PresentationLogicActivityImpl() {
    }

    public PresentationLogicActivityImpl(Factory<PM> presentationModelFactory) {
        super(presentationModelFactory);
    }
}