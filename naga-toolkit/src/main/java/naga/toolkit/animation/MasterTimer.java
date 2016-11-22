package naga.toolkit.animation;

import naga.toolkit.spi.Toolkit;

/**
 * @author Bruno Salmon
 */
class MasterTimer extends AbstractMasterTimer {

    private static MasterTimer INSTANCE;
    public static MasterTimer get() {
        if (INSTANCE == null)
            INSTANCE = new MasterTimer();
        return INSTANCE;
    }

    @Override
    protected void postUpdateAnimationRunnable(DelayedRunnable animationRunnable) {
        Toolkit.get().scheduler().requestAnimationFrame(animationRunnable.getDelay(), animationRunnable);
    }

    @Override
    protected int getPulseDuration(int precision) {
        int rate = 60;
        return precision / rate;
    }
}
