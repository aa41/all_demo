package com.mxc.jniproject.ui;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class OverlayHelper {

    private static WeakHashMap<Drama,OverlayHelper> overlayCache = new WeakHashMap<>();


    private Drama mDrama;
    private WeakReference<View> mAnchor;
    private WeakReference<View> mAnchorRoot;
    private boolean mIsAnchorRootAttached;


    private final View.OnAttachStateChangeListener mOnAnchorDetachedListener =
            new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    alignToAnchor();
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            };

    private final View.OnAttachStateChangeListener mOnAnchorRootDetachedListener =
            new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    mIsAnchorRootAttached = false;
                }
            };

    private final ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener = this::alignToAnchor;

    private final View.OnLayoutChangeListener mOnLayoutChangeListener =
            (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> alignToAnchor();

    private int mAnchorXoff;
    private int mAnchorYoff;
    private int mAnchoredGravity;
    private boolean mIsDropdown;
    private int mGravity = Gravity.NO_GRAVITY;
    private int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int[] mTmpDrawingLocation = new int[2];
    private final int[] mTmpScreenLocation = new int[2];
    private final int[] mTmpAppLocation = new int[2];


    public static void dispose(Drama drama){
        OverlayHelper helper = overlayCache.get(drama);
        if(helper != null){
            helper.detachFromAnchor();
        }
    }

    public void showAsDropDown(Drama drama,View anchor) {
        overlayCache.put(drama,this);
        this.mDrama =drama;
        attachToAnchor(anchor, mDrama.getX(), mDrama.getY(), mDrama.getGravity());
        mIsDropdown = true;

        mWidth = drama.getWidth();
        mHeight = drama.getHeight();

        final FrameLayout.LayoutParams p =
                createLayoutParams();
        findDropDownPosition(anchor, p, mDrama.getX(), mDrama.getY(),
                p.width, p.height, mDrama.getGravity(), true);

        drama.setX(p.leftMargin);
        drama.setY(p.topMargin);
        drama.setWidth(p.width);
        drama.setHeight(p.height);
        drama.setOverlay(true);
        drama.setGravity(p.gravity);
    }

    private void alignToAnchor() {
        final View anchor = mAnchor != null ? mAnchor.get() : null;
        if (anchor != null && anchor.isAttachedToWindow() ) {
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) mDrama.getView().getLayoutParams();
            findDropDownPosition(anchor, p, mAnchorXoff, mAnchorYoff,
                    p.width, p.height, mAnchoredGravity, false);
            mDrama.getView().requestLayout();

        }
    }

    protected boolean findDropDownPosition(View anchor, FrameLayout.LayoutParams outParams,
                                           int xOffset, int yOffset, int width, int height, int gravity, boolean allowScroll) {
        final int anchorHeight = anchor.getHeight();
        final int anchorWidth = anchor.getWidth();
        final int[] appScreenLocation = mTmpAppLocation;
        final View appRootView = mDrama.getRootView();
        appRootView.getLocationInWindow(appScreenLocation);

        final int[] screenLocation = mTmpScreenLocation;
        anchor.getLocationInWindow(screenLocation);

        final int[] drawingLocation = mTmpDrawingLocation;
        drawingLocation[0] = screenLocation[0] - appScreenLocation[0];
        drawingLocation[1] = screenLocation[1] - appScreenLocation[1];
        outParams.leftMargin = drawingLocation[0] + xOffset;
        outParams.topMargin = drawingLocation[1] + anchorHeight + yOffset;

        final Rect displayFrame = new Rect();
        appRootView.getWindowVisibleDisplayFrame(displayFrame);
        if (width == MATCH_PARENT) {
            width = displayFrame.right - displayFrame.left;
        }
        if (height == MATCH_PARENT) {
            height = displayFrame.bottom - displayFrame.top;
        }

        outParams.gravity = computeGravity();
        outParams.width = width;
        outParams.height = height;


        final int hgrav = Gravity.getAbsoluteGravity(gravity, anchor.getLayoutDirection())
                & Gravity.HORIZONTAL_GRAVITY_MASK;
        if (hgrav == Gravity.RIGHT) {
            outParams.leftMargin -= width - anchorWidth;
        }

        final boolean fitsVertical = tryFitVertical(outParams, yOffset, height,
                anchorHeight, drawingLocation[1], screenLocation[1], displayFrame.top,
                displayFrame.bottom, false);

        final boolean fitsHorizontal = tryFitHorizontal(outParams, xOffset, width,
                anchorWidth, drawingLocation[0], screenLocation[0], displayFrame.left,
                displayFrame.right, false);

        if (!fitsVertical || !fitsHorizontal) {
            final int scrollX = anchor.getScrollX();
            final int scrollY = anchor.getScrollY();
            final Rect r = new Rect(scrollX, scrollY, scrollX + width + xOffset,
                    scrollY + height + anchorHeight + yOffset);
            if (allowScroll && anchor.requestRectangleOnScreen(r, true)) {
                anchor.getLocationInWindow(screenLocation);
                drawingLocation[0] = screenLocation[0] - appScreenLocation[0];
                drawingLocation[1] = screenLocation[1] - appScreenLocation[1];
                outParams.leftMargin = drawingLocation[0] + xOffset;
                outParams.topMargin = drawingLocation[1] + anchorHeight + yOffset;
                if (hgrav == Gravity.RIGHT) {
                    outParams.leftMargin -= width - anchorWidth;
                }
            }

            tryFitVertical(outParams, yOffset, height, anchorHeight, drawingLocation[1],
                    screenLocation[1], displayFrame.top, displayFrame.bottom, true);
            tryFitHorizontal(outParams, xOffset, width, anchorWidth, drawingLocation[0],
                    screenLocation[0], displayFrame.left, displayFrame.right, true);
        }

        return outParams.leftMargin < drawingLocation[1];
    }

    private boolean tryFitHorizontal(FrameLayout.LayoutParams outParams, int xOffset, int width,
                                     int anchorWidth, int drawingLocationX, int screenLocationX, int displayFrameLeft,
                                     int displayFrameRight, boolean allowResize) {
        final int winOffsetX = screenLocationX - drawingLocationX;
        final int anchorLeftInScreen = outParams.leftMargin + winOffsetX;
        final int spaceRight = displayFrameRight - anchorLeftInScreen;
        if (anchorLeftInScreen >= displayFrameLeft && width <= spaceRight) {
            return true;
        }

        if (positionInDisplayHorizontal(outParams, width, drawingLocationX, screenLocationX,
                displayFrameLeft, displayFrameRight, allowResize)) {
            return true;
        }

        return false;
    }

    private boolean positionInDisplayHorizontal(FrameLayout.LayoutParams outParams, int width,
                                                int drawingLocationX, int screenLocationX, int displayFrameLeft, int displayFrameRight,
                                                boolean canResize) {
        boolean fitsInDisplay = true;
        final int winOffsetX = screenLocationX - drawingLocationX;
        outParams.leftMargin += winOffsetX;

        final int right = outParams.leftMargin + width;
        if (right > displayFrameRight) {
            outParams.leftMargin -= right - displayFrameRight;
        }

        if (outParams.leftMargin < displayFrameLeft) {

            outParams.leftMargin = displayFrameLeft;

            final int displayFrameWidth = displayFrameRight - displayFrameLeft;
            if (canResize && width > displayFrameWidth) {
                outParams.width = displayFrameWidth;
            } else {
                fitsInDisplay = false;
            }
        }

        outParams.leftMargin -= winOffsetX;

        return fitsInDisplay;
    }

    private boolean tryFitVertical(FrameLayout.LayoutParams outParams, int yOffset, int height,
                                   int anchorHeight, int drawingLocationY, int screenLocationY, int displayFrameTop,
                                   int displayFrameBottom, boolean allowResize) {
        final int winOffsetY = screenLocationY - drawingLocationY;
        final int anchorTopInScreen = outParams.topMargin + winOffsetY;
        final int spaceBelow = displayFrameBottom - anchorTopInScreen;
        if (anchorTopInScreen >= displayFrameTop && height <= spaceBelow) {
            return true;
        }

        final int spaceAbove = anchorTopInScreen - anchorHeight - displayFrameTop;
        if (height <= spaceAbove) {
            outParams.topMargin = drawingLocationY - height + yOffset;

            return true;
        }

        if (positionInDisplayVertical(outParams, height, drawingLocationY, screenLocationY,
                displayFrameTop, displayFrameBottom, allowResize)) {
            return true;
        }

        return false;
    }

    private boolean positionInDisplayVertical(FrameLayout.LayoutParams outParams, int height,
                                              int drawingLocationY, int screenLocationY, int displayFrameTop, int displayFrameBottom,
                                              boolean canResize) {
        boolean fitsInDisplay = true;

        final int winOffsetY = screenLocationY - drawingLocationY;
        outParams.topMargin += winOffsetY;
        outParams.height = height;

        final int bottom = outParams.topMargin + height;
        if (bottom > displayFrameBottom) {
            outParams.topMargin -= bottom - displayFrameBottom;
        }

        if (outParams.topMargin < displayFrameTop) {
            outParams.topMargin = displayFrameTop;

            final int displayFrameHeight = displayFrameBottom - displayFrameTop;
            if (canResize && height > displayFrameHeight) {
                outParams.height = displayFrameHeight;
            } else {
                fitsInDisplay = false;
            }
        }

        outParams.topMargin -= winOffsetY;

        return fitsInDisplay;
    }


    protected void attachToAnchor(View anchor, int xoff, int yoff, int gravity) {
        detachFromAnchor();

        final ViewTreeObserver vto = anchor.getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(mOnScrollChangedListener);
        }
        anchor.addOnAttachStateChangeListener(mOnAnchorDetachedListener);

        final View anchorRoot = anchor.getRootView();
        anchorRoot.addOnAttachStateChangeListener(mOnAnchorRootDetachedListener);
        anchorRoot.addOnLayoutChangeListener(mOnLayoutChangeListener);

        mAnchor = new WeakReference<>(anchor);
        mAnchorRoot = new WeakReference<>(anchorRoot);
        mIsAnchorRootAttached = anchorRoot.isAttachedToWindow();
        mDrama.setRootView((ViewGroup) anchorRoot);
        mAnchorXoff = xoff;
        mAnchorYoff = yoff;
        mAnchoredGravity = gravity;
    }

    protected final FrameLayout.LayoutParams createLayoutParams() {
        final FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(mWidth, mHeight);
        return p;
    }

    private int computeGravity() {
        int gravity = mGravity == Gravity.NO_GRAVITY ? Gravity.START | Gravity.TOP : mGravity;
        if (mIsDropdown) {
            gravity |= Gravity.DISPLAY_CLIP_VERTICAL;
        }
        return gravity;
    }


    protected void detachFromAnchor() {
        final View anchor = getAnchor();
        if (anchor != null) {
            final ViewTreeObserver vto = anchor.getViewTreeObserver();
            vto.removeOnScrollChangedListener(mOnScrollChangedListener);
            anchor.removeOnAttachStateChangeListener(mOnAnchorDetachedListener);
        }

        final View anchorRoot = mAnchorRoot != null ? mAnchorRoot.get() : null;
        if (anchorRoot != null) {
            anchorRoot.removeOnAttachStateChangeListener(mOnAnchorRootDetachedListener);
            anchorRoot.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        }

        mAnchor = null;
        mAnchorRoot = null;
        mIsAnchorRootAttached = false;
    }

    protected View getAnchor() {
        return mAnchor != null ? mAnchor.get() : null;
    }

}
