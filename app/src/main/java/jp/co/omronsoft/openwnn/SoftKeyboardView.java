package jp.co.omronsoft.openwnn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class SoftKeyboardView extends KeyboardView {

    private final String TAG = SoftKeyboardView.class.getSimpleName();

    private Paint mPaint;
    private Paint.FontMetricsInt mFmi;

    private Keyboard.Key focusedKey;
    private int focusedKeyIndex = -1;
    private boolean focusFlag = true;
    private List<Keyboard.Key> keys = new ArrayList<>();

    public SoftKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public SoftKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public SoftKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mFmi = mPaint.getFontMetricsInt();
    }

    @Override
    public void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        Log.d(TAG, "onDraw");

        if (null == getKeyDispatcherState()) return;
        keys.clear();

        canvas.translate(getPaddingLeft(), getPaddingTop());

        int i = 0;
        for (Keyboard.Key key : getKeyboard().getKeys()) {
            drawSoftKey(canvas, i, key, 1, 1);
            i++;
        }
    }

    private void drawSoftKey(Canvas canvas, int index, Keyboard.Key softKey, int keyXMargin,
                             int keyYMargin) {

        keys.add(softKey);
        if (isFocusFlag() && focusedKeyIndex == -1 && (focusedKey == null && keys.size() == 2))
            setFocusedKey(keys.get(1));

        Drawable bg = null;
        if (softKey.pressed || focusedKeyIndex == index)
            bg = getResources().getDrawable(R.drawable.btn_keyboard_key_pressed_2nd);
        else
            bg = getResources().getDrawable(R.drawable.btn_keyboard_key_normal_2nd);

        if (null != bg) {
            bg.setBounds(softKey.x + keyXMargin, softKey.y + keyYMargin,
                    softKey.x + softKey.width - keyXMargin, softKey.y + softKey.height - keyYMargin);
            bg.draw(canvas);
        }

        Drawable keyIcon = softKey.icon;

        if (null != keyIcon) {
            Drawable icon = keyIcon;
            int marginLeft = (softKey.width - icon.getIntrinsicWidth()) / 2;
            int marginRight = softKey.width - icon.getIntrinsicWidth()
                    - marginLeft;
            int marginTop = (softKey.height - icon.getIntrinsicHeight()) / 2;
            int marginBottom = softKey.height - icon.getIntrinsicHeight()
                    - marginTop;
            icon.setBounds(softKey.x + marginLeft,
                    softKey.y + marginTop, softKey.x + softKey.width - marginRight,
                    softKey.y + softKey.height - marginBottom);
            icon.draw(canvas);
        } else if (null != softKey.label) {
            mPaint.setColor(Color.WHITE);
            float x = softKey.x
                    + (softKey.width - mPaint.measureText(softKey.label.toString())) / 2.0f;
            int fontHeight = mFmi.bottom - mFmi.top;
            float marginY = (softKey.height - fontHeight) / 2.0f;
            float y = softKey.y + marginY - mFmi.top + mFmi.bottom / 1.5f;
            canvas.drawText(softKey.label.toString(), x, y + 1, mPaint);
        }
    }

    public List<Keyboard.Key> getKeys() {
        return keys;
    }

    public void setFocusedKey(Keyboard.Key softKey) {
        focusedKey = softKey;
        if (softKey != null) {
            focusedKeyIndex = keys.indexOf(softKey);
            Log.d(TAG, "setFocusedKey: " + softKey.codes[0] + " - " + softKey.label + ", index: " + focusedKeyIndex);
        } else {
            focusedKeyIndex = -1;
            Log.d(TAG, "setFocusedKey: null");
        }
    }

    public Keyboard.Key getFocusedKey() {
        if (focusedKey == null)
            focusedKey = getKeys().get(0);
        return focusedKey;
    }

    public boolean isFocusFlag() {
        return focusFlag;
    }

    public void setFocusFlag(boolean focusFlag) {
        this.focusFlag = focusFlag;
    }

    public Keyboard.Key getNearestKey(Keyboard.Key softKey, int direction) {
        if (direction == KeyEvent.KEYCODE_DPAD_DOWN)
            return getBottomNearestKey(softKey);
        else if (direction == KeyEvent.KEYCODE_DPAD_UP)
            return getTopNearestKey(softKey);
        else if (direction == KeyEvent.KEYCODE_DPAD_LEFT)
            return getLeftNearestKey(softKey);
        else if (direction == KeyEvent.KEYCODE_DPAD_RIGHT)
            return getRightNearestKey(softKey);

        return null;
    }

    private Keyboard.Key getTopNearestKey(Keyboard.Key softKey) {
        int TopY = softKey.y;
        if (TopY == 0) {
            Log.d(TAG, "most top");
            return null;
        }
        List<Keyboard.Key> keys = getKeys();
        Keyboard.Key closestKey = null;
        int closestDistance = Integer.MAX_VALUE;
        for (Keyboard.Key key : keys) {
            if (key == softKey || (key.y + key.height) > TopY)
                continue;
            int distance = Math.abs(TopY - (key.y + key.height));
            if (distance == closestDistance) {
                if (Math.abs((key.x + key.width / 2) - (softKey.x + softKey.width / 2)) <
                        Math.abs((closestKey.x + closestKey.width / 2) - (softKey.x + softKey.width / 2)))
                    closestKey = key;
            } else if (distance < closestDistance) {
                closestKey = key;
                closestDistance = distance;
            }
        }
        return closestKey;
    }

    private Keyboard.Key getBottomNearestKey(Keyboard.Key softKey) {
        int bottomY = softKey.y + softKey.height;
        if (bottomY == this.getBottom()) {
            Log.d(TAG, "most bottom");
            return null;
        }

        List<Keyboard.Key> keys = getKeys();
        Keyboard.Key closestKey = null;
        int closestDistance = Integer.MAX_VALUE;
        for (Keyboard.Key key : keys) {
            if (key == softKey || key.y < bottomY)
                continue;
            int distance = Math.abs(key.y - bottomY);
            if (distance == closestDistance) {
                if (Math.abs((key.x + key.width / 2) - (softKey.x + softKey.width / 2)) <
                        Math.abs((closestKey.x + closestKey.width / 2) - (softKey.x + softKey.width / 2)))
                    closestKey = key;
            } else if (distance < closestDistance) {
                closestKey = key;
                closestDistance = distance;
            }
        }
        return closestKey;
    }

    private Keyboard.Key getRightNearestKey(Keyboard.Key softKey) {
        int rightX = softKey.x + softKey.width;
        if (rightX == this.getRight()) {
            Log.d(TAG, "most right");
            return null;
        }
        List<Keyboard.Key> keys = getKeys();
        Keyboard.Key closestKey = null;
        int closestDistance = Integer.MAX_VALUE;
        for (Keyboard.Key key : keys) {
            if (key == softKey ||
                    ((key.x + key.width) / 2 < (softKey.x + softKey.width) / 2) ||
                    key.y != softKey.y)
                continue;
            int distance = Math.abs(key.x - rightX);
            if (distance < closestDistance) {
                closestKey = key;
                closestDistance = distance;
            }
        }
        return closestKey;
    }

    private Keyboard.Key getLeftNearestKey(Keyboard.Key softKey) {
        int leftX = softKey.x;
        if (leftX == this.getLeft()) {
            Log.d(TAG, "most left");
            return null;
        }

        List<Keyboard.Key> keys = getKeys();
        Keyboard.Key closestKey = null;
        int closestDistance = Integer.MAX_VALUE;
        for (Keyboard.Key key : keys) {
            if (key == softKey ||
                    ((key.x + key.width) / 2 > (softKey.x + softKey.width) / 2) ||
                    key.y != softKey.y)
                continue;
            int distance = Math.abs(leftX - (key.x + key.width));
            if (distance < closestDistance) {
                closestKey = key;
                closestDistance = distance;
            }
        }
        return closestKey;
    }
}
