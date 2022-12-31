package ib.mkb;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public abstract class BaseView extends View {
    public BaseView(Context context) {
        super(context);
    }

    /** handle a generic motion event */
    public abstract boolean motionEvent(MotionEvent ev);

    /** handle a generic key event */
    public abstract boolean keyEvent(KeyEvent event);
}
