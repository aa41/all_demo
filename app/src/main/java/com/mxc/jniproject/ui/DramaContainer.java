package com.mxc.jniproject.ui;

import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.mxc.jniproject.R;
import com.mxc.jniproject.ui.callback.ILifeCycle;
import com.mxc.jniproject.ui.callback.ILifeCycleWatcher;
import com.mxc.jniproject.ui.callback.ResultCallback;
import com.mxc.jniproject.ui.callback.ResultCalledListener;
import com.mxc.jniproject.ui.utils.HandlerUtils;

import java.util.List;

public class DramaContainer<T> implements Drama<T>, ILifeCycle<T>, View.OnAttachStateChangeListener {
    private View view;
    private int inAnim;
    private int outAnim;
    private String url;
    protected ViewGroup rootView;
    private DramaManager dramaManager;
    private ResultCalledListener resultCalledListener;
    private boolean isAttach = false;
    private boolean isPushed = false;
    private boolean isHidden = false;
    private ILifeCycle<T> lifeCycle;
    protected Object params;
    protected volatile boolean mounted = false;
    private int x, y;
    private int width = ViewGroup.LayoutParams.WRAP_CONTENT, height = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int gravity;
    private boolean isOverlay;


    public DramaContainer(View view, ViewGroup rootView, String url, int inAnim, int outAnim) {
        this.view = view;
        this.inAnim = inAnim;
        this.outAnim = outAnim;
        this.url = url;
        this.rootView = rootView;
    }

    public DramaContainer(View view, ViewGroup rootView, String url) {
        this(view, rootView, url, 0, 0);
    }

    private final ILifeCycleWatcher<T> lifeCycleWatcher = new ILifeCycleWatcher<T>() {
        @Override
        public void watch(ILifeCycle<T> lc) {
            lifeCycle = lc;
        }
    };


    private ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onDramaResult(ResultCalledListener listener) {
            resultCalledListener = listener;
        }
    };


    @Override
    public T getTarget() {
        return (T) dramaManager.getTarget();
    }

    @Override
    public List<Drama> getChildDramas() {
        return Curtain.of(this).getChildren();
    }

    @Override
    public boolean isAttached() {
        return isAttach;
    }

    @Override
    public boolean isOverlay() {
        return isOverlay;
    }

    @Override
    public void setOverlay(boolean isOverlay) {
        this.isOverlay = isOverlay;
    }


    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onPush() {
        if (isPushed) return;
        Object viewTag = view.getTag(R.id.curtain_target_id);
        if (viewTag == this) return;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                initDrama(getTarget());
                innerAdd();
                if (inAnim < 0) {
                    return;
                }
                if (inAnim == 0) {
                    inAnim = R.anim.slide_in_left;
                }
                Animation animation = AnimationUtils.loadAnimation(Curtain.getContext(), inAnim);
                view.startAnimation(animation);

            }
        };


        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            HandlerUtils.getMainHandler().post(runnable);
        }
    }

    private void innerAdd() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
                lp.leftMargin = x;
                lp.topMargin = y;
                lp.gravity = gravity;

                if (!(rootView instanceof FrameLayout)) {
                    FrameLayout frameLayout = new FrameLayout(rootView.getContext());
                    rootView.addView(frameLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    rootView = frameLayout;
                }
                view.addOnAttachStateChangeListener(DramaContainer.this);
                view.setTag(R.id.curtain_target_id, DramaContainer.this);
                rootView.addView(view, lp);
                isAttach = true;
                isPushed = true;
            }
        };
        if (Looper.getMainLooper() == Looper.myLooper()) {
            r.run();
        } else {
            HandlerUtils.getMainHandler().post(r);
        }

    }

    @Override
    public void onPop(Object obj) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (resultCalledListener != null) {
                    resultCalledListener.call(obj);
                }
                if (rootView != null) {
                    if (outAnim < 0) {
                        innerPop();
                        return;
                    }
                    if (outAnim == 0) {
                        outAnim = R.anim.slide_in_right;
                    }
                    Animation animation = AnimationUtils.loadAnimation(Curtain.getContext(), outAnim);
                    view.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            rootView.post(new Runnable() {
                                @Override
                                public void run() {
                                    innerPop();
                                }
                            });
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }

                isAttach = false;
            }
        };


        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            HandlerUtils.getMainHandler().post(runnable);
        }
    }

    private void innerPop() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                dispose(getTarget());
                rootView.removeViewInLayout(view);
            }
        };
        if (Looper.getMainLooper() == Looper.myLooper()) {
            r.run();
        } else {
            HandlerUtils.getMainHandler().post(r);
        }


    }

    @Override
    public void onPop() {
        onPop(null);
    }

    @Override
    public void onHideWidthAnim(int anim) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int tmp = anim;
                if (tmp == 0) {
                    tmp = R.anim.slide_in_right;
                }

                if (tmp > 0) {
                    Animation animation = AnimationUtils.loadAnimation(Curtain.getContext(), tmp);
                    view.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            innerHide();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    innerHide();
                }


                isHidden = true;

            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            HandlerUtils.getMainHandler().post(runnable);
        }
    }

    private void innerHide() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
                onDramStateChanged(ILifeCycle.StateChanged.HIDE);
            }
        };
        if (Looper.getMainLooper() == Looper.myLooper()) {
            r.run();
        } else {
            HandlerUtils.getMainHandler().post(r);
        }


    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public void setParams(Object obj) {
        this.params = obj;
    }

    @Override
    public void onShow() {
        onShowWithAnim(-1);
    }


    public boolean isPushed() {
        return isPushed;
    }

    @Override
    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public Object getParams() {
        return params;
    }

    @Override
    public void attachDramaManager(DramaManager manager) {
        this.dramaManager = manager;
    }


    @Override
    public DramaManager getDramaManager() {
        return dramaManager;
    }

    @Override
    public ResultCallback result() {
        return resultCallback;
    }

    @Override
    public ILifeCycleWatcher<T> getLifeCycle() {
        return lifeCycleWatcher;
    }

    @Override
    public void backPress() {
        dramaManager.pop(this);
    }

    @Override
    public void setRootView(ViewGroup rootView) {
        this.rootView = rootView;
    }

    @Override
    public ViewGroup getRootView() {
        return rootView;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    @Override
    public int getGravity() {
        return gravity;
    }


    @Override
    public void onHide() {
        onHideWidthAnim(-1);
    }

    @Override
    public void onShowWithAnim(int anim) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!isHidden) return;
                int tmp = anim;
                if (tmp == 0) {
                    tmp = R.anim.slide_in_left;
                }
                innerShow();
                isHidden = false;
                if (tmp < 0) {
                    return;
                }
                Animation animation = AnimationUtils.loadAnimation(Curtain.getContext(), tmp);
                view.startAnimation(animation);
            }
        };


        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            HandlerUtils.getMainHandler().post(runnable);
        }
    }

    private void innerShow() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    int count = parent.getChildCount();
                    for (int i = count - 1; i >= 0; i--) {
                        View child = parent.getChildAt(i);
                        if (child == view) {
                            if (i == count - 1) {
                                break;
                            }
                            parent.removeViewAt(i);
                            parent.addView(view);
                            break;
                        }
                    }
                }
                view.setVisibility(View.VISIBLE);
                onDramStateChanged(StateChanged.SHOW);
            }
        };
        if (Looper.getMainLooper() == Looper.myLooper()) {
            r.run();
        } else {
            HandlerUtils.getMainHandler().post(r);
        }
    }

    @Override
    public void initDrama(T target) {
        if (lifeCycle != null) {
            lifeCycle.initDrama(target);
        }
    }

    @Override
    public void onDramStateChanged(StateChanged stateChanged) {
        if (lifeCycle != null) {
            lifeCycle.onDramStateChanged(stateChanged);
        }
    }

    @Override
    public void dispose(T target) {
        if (mounted) {
            Log.e("Drama", "drama is already dispose,please check!");
        }
        mounted = false;
        if (lifeCycle != null) {
            lifeCycle.dispose(target);
            lifeCycle = null;
        }
        if (view != null) {
            view.removeOnAttachStateChangeListener(this);
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        mounted = true;
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        if (!mounted) {
            dramaManager.pop(this);
        }
    }


    public static class Builder {
        private View view;
        private int inAnim;
        private int outAnim;
        private String url;
        private ViewGroup rootView;
        private Object params;
        private int x;
        private int y;
        private int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        private int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        private int gravity;
        private boolean isOverlay;

        public Builder() {
        }


        public Builder view(View view) {
            if (view.getParent() != null) {
                throw new IllegalArgumentException("view can not have a parent");
            }
            this.view = view;
            return this;
        }

        public Builder params(Object params) {
            this.params = params;
            return this;
        }

        public Builder inAnim(int animId) {
            this.inAnim = animId;
            return this;
        }

        public Builder outAnim(int animId) {
            this.outAnim = animId;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }


        public Builder root(ViewGroup rootView) {
            if (rootView == null) {
                throw new IllegalArgumentException("rootView can not have be null");
            }
            this.rootView = rootView;
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder gravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder isOverlay(boolean isOverlay){
            this.isOverlay = isOverlay;
            return this;
        }


        public <T> Drama<T> build() {
            DramaContainer<T> container = new DramaContainer<T>(view, rootView, url, inAnim, outAnim);
            container.setParams(params);
            container.setX(x);
            container.setY(y);
            container.setWidth(width);
            container.setHeight(height);
            container.setGravity(gravity);
            container.setOverlay(isOverlay);
            return container;
        }


    }

}
