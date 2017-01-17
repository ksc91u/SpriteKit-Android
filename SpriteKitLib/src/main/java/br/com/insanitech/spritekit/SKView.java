package br.com.insanitech.spritekit;

import android.content.Context;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import br.com.insanitech.spritekit.opengl.context.GL10ContextFactory;
import br.com.insanitech.spritekit.opengl.context.GLContextFactory;
import br.com.insanitech.spritekit.opengl.renderer.GLGenericRenderer;
import br.com.insanitech.spritekit.opengl.renderer.GLRenderer;

public class SKView extends GLSurfaceView implements GLRenderer.GLDrawer {
    protected static long beginOfTime = 0;

    private GLContextFactory factory;
    private boolean paused;
    private SKScene sceneToBePresented;
    private Thread thread;
    private SKSize viewSize = new SKSize();

    public SKView(Context context) {
        super(context);
        initView();
    }

    public SKView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void initView() {
        // initializing OpenGL ES parameters
        final GLGenericRenderer renderer = new GLGenericRenderer();
        // TODO: testing GL 1.0, change to test other versions
        factory = new GL10ContextFactory();
        factory.setRenderer(renderer);
        factory.setContextReadyListener(new GLContextFactory.GLContextReadyListener() {
            @Override
            public void onContextReady() {
                renderer.setDrawer(SKView.this);
                if (sceneToBePresented != null) {
                    requestRender();
                }
            }
        });
        setEGLContextFactory(factory);
        setRenderer(renderer);
        // end OpenGL parameters

        beginOfTime = System.currentTimeMillis();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (thread != null && !thread.isInterrupted()) {
                        synchronized (this) {
                            if (sceneToBePresented != null && factory.isReady() && !paused) {
                                sceneToBePresented.evaluateActions();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        thread.start();
    }

    protected long getCurrentTime() {
        return System.currentTimeMillis() - beginOfTime;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewSize.width = (w);
        viewSize.height = (h);
    }

    @Override
    public void onDrawFrame(GLRenderer renderer, int width, int height) {
        synchronized (this) {
            if (sceneToBePresented != null) {
                renderer.clear(sceneToBePresented.getBackgroundColor());

                renderer.saveState();

                // TODO: this is the scaling of the scene size compared to the view size.
                // TODO: it's making the Scale Aspect Fill, so the content fits the view no matter the size of the scene.
                renderer.scale(width / sceneToBePresented.getSize().width, height / sceneToBePresented.getSize().height);

                sceneToBePresented.onDrawFrame(renderer, width, height);

                renderer.restoreState();
            }
        }
    }

    public void removeFromSuperView() {
        synchronized (this) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    public void presentScene(final SKScene scene) {
        synchronized (this) {
            sceneToBePresented = scene;
            setOnTouchListener(scene);

            // needs new implementation of scene presentation
            if (factory.isReady()) {
                requestRender();
            }
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public boolean getPaused() {
        synchronized (this) {
            return paused;
        }
    }

    public void setPaused(boolean p) {
        synchronized (this) {
            paused = p;
            if (!p) {
                presentScene(sceneToBePresented);
            }
        }
    }

    public SKScene getScene() {
        synchronized (this) {
            return sceneToBePresented;
        }
    }

    public Image getTexture(SKNode node) {
        synchronized (this) {
            return null;
        }
    }

    public SKPoint convertTo(SKPoint point, SKScene scene) {
        synchronized (this) {
            return null;
        }
    }

    public SKPoint convertFrom(SKPoint point, SKScene scene) {
        synchronized (this) {
            return null;
        }
    }

    public SKSize getSize() {
        synchronized (this) {
            return viewSize;
        }
    }
}
