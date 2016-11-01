package naga.toolkit.drawing.spi.impl;

import naga.commons.util.collection.Collections;
import naga.toolkit.drawing.shapes.Drawable;
import naga.toolkit.drawing.shapes.DrawableParent;
import naga.toolkit.drawing.shapes.impl.DrawableParentImpl;
import naga.toolkit.drawing.spi.Drawing;
import naga.toolkit.drawing.spi.DrawingNode;
import naga.toolkit.drawing.spi.DrawingRequester;
import naga.toolkit.drawing.spi.view.DrawableView;
import naga.toolkit.drawing.spi.view.DrawableViewFactory;
import naga.toolkit.util.ObservableLists;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public abstract class DrawingImpl extends DrawableParentImpl implements Drawing {

    protected final DrawingNode drawingNode;
    private DrawableViewFactory drawableViewFactory;
    private final Map<Drawable, DrawableView> drawableViews = new HashMap<>();
    private final DrawingRequester drawingRequester = new DrawingRequester() {

        @Override
        public void requestDrawableParentAndChildrenViewsUpdate(DrawableParent drawableParent) {
            updateDrawableParentAndChildrenViews(drawableParent);
        }

        @Override
        public void requestDrawableViewUpdate(Drawable drawable) {
            updateDrawableView(drawable);
        }
    };

    protected DrawingImpl(DrawingNode drawingNode, DrawableViewFactory drawableViewFactory) {
        this.drawingNode = drawingNode;
        this.drawableViewFactory = drawableViewFactory;
        keepDrawableParentAndChildrenViewsUpdated(this);
    }

    public void setDrawableViewFactory(DrawableViewFactory drawableViewFactory) {
        if (this.drawableViewFactory != null) {
            Collections.forEach(drawableViews.values(), DrawableView::unbind);
            drawableViews.clear();
        }
        this.drawableViewFactory = drawableViewFactory;
    }

    private void keepDrawableParentAndChildrenViewsUpdated(DrawableParent drawableParent) {
        ObservableLists.runNowAndOnListChange(() -> updateDrawableParentAndChildrenViews(drawableParent), drawableParent.getDrawableChildren());
    }

    protected void updateDrawableParentAndChildrenViews(DrawableParent drawableParent) {
        updateDrawableChildrenViews(drawableParent.getDrawableChildren());
    }

    protected void updateDrawableView(Drawable drawable) {
    }

    private void updateDrawableChildrenViews(Collection<Drawable> drawables) {
        Collections.forEach(drawables, this::createAndBindDrawableViewAndChildren);
    }

    private void createAndBindDrawableViewAndChildren(Drawable drawable) {
        DrawableView drawableView = getOrCreateAndBindDrawableView(drawable);
        if (drawableView instanceof DrawableParent)
            updateDrawableChildrenViews(((DrawableParent) drawableView).getDrawableChildren());
    }

    public DrawableView getOrCreateAndBindDrawableView(Drawable drawable) {
        DrawableView drawableView = drawableViews.get(drawable);
        if (drawableView == null) {
            drawableViews.put(drawable, drawableView = drawableViewFactory.createDrawableView(drawable));
            drawableView.bind(drawable, drawingRequester);
            if (drawable instanceof DrawableParent)
                keepDrawableParentAndChildrenViewsUpdated((DrawableParent) drawable);
            updateDrawableView(drawable);
        }
        return drawableView;
    }

    protected boolean isDrawableParentRoot(DrawableParent drawableParent) {
        return drawableParent == drawingNode || drawableParent == this;
    }
}
