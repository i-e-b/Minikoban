package ib.mkb;

import static android.content.res.Configuration.UI_MODE_NIGHT_NO;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class vw extends View {

    private final Paint mPaint = new Paint();
    private int lastHeight, lastWidth; // dimensions of screen last time we did a paint.

    private int px,py; // player position
    private float vx,vy; // view position (should move toward player)
    private float moveX, moveY; // touch position when moving
    private float touchX, touchY; // touch start position
    private float upX, upY; // touch lift position

    private boolean touchDown; // are we currently touching the screen?
    private boolean didScroll; // did we scroll with this touch? Prevents move on lift.
    private boolean darkColors; // night mode if true.

    private static final int levelWidth = 20;
    private static final int levelHeight = 11;
    private final byte[/*row*/][/*col*/] level = new byte[levelHeight][levelWidth];
    private boolean levelComplete; // true once there are no tiles just box or just goal.

    // flags
    private static final byte fWall = 1;
    private static final byte fPlayer = 1<<1;
    private static final byte fGoal = 1<<2;
    private static final byte fBox = 1<<3;

    private static final int tileScale = 110; // size of tile grid (tune to fit font)

    public vw(final Context context) {
        super(context);
        // todo: make this way tighter (3bit -> 8 tiles in 3 bytes)
        loadLevel( 0, "----#####");
        loadLevel( 1, "----#---#");
        loadLevel( 2, "----#$--#");
        loadLevel( 3, "--###--$##");
        loadLevel( 4, "--#--$-$-#");
        loadLevel( 5, "###-#-##-#---######");
        loadLevel( 6, "#---#-##-#####--..#");
        loadLevel( 7, "#-$--$----------..#");
        loadLevel( 8, "#####-###-#@##--..#");
        loadLevel( 9, "----#-----#########");
        loadLevel(10, "----#######");


        // Check for dark mode.
        int uiMode = getResources().getConfiguration().uiMode;
        if ((uiMode & UI_MODE_NIGHT_YES) > 0){
            darkColors = true;
        } else if ((uiMode & UI_MODE_NIGHT_NO) > 0){
            darkColors = false;
        }

        levelComplete = false;
        touchDown = false;
        mPaint.setAntiAlias(true);
    }

    private void loadLevel(int row, String s) {
        for (int i = 0; i < s.length(); i++){
            byte f = charToFlags(s.charAt(i));
            level[row][i] = f;
            if ((f & fPlayer) > 0){
                px = i; py = row;
                vx=px; vy=py;
            }
        }
    }

    private byte charToFlags(char c) {
        switch (c){
            case '#': return fWall;
            case '@': return fPlayer;
            case '+': return fPlayer+fGoal;
            case '$': return fBox;
            case '*': return fBox+fGoal;
            case '.': return fGoal;
            default: return 0; // no flags is floor
        }
    }

    public void drawLevel(final Canvas canvas){
        mPaint.setTextSize(100);

        lastWidth = canvas.getWidth();
        lastHeight = canvas.getHeight();

        int cx = lastWidth / 2;
        int cy = lastHeight / 2;

        for (int y=0; y < levelHeight; y++){
            for (int x=0; x < levelWidth; x++){
                // draw level centred around the view point as grid coords
                // view will drift toward player over time
                float tx = cx + ((x - vx) * tileScale);
                float ty = cy + ((y - vy) * tileScale);
                drawStack(level[y][x], tx,ty, canvas);
            }
        }
    }

    private void drawStack(byte flags, float x, float y, Canvas canvas) {
        if ((flags&fGoal)>0) canvas.drawText("\uD83D\uDD73", x, y, mPaint);
        if ((flags&fBox)>0) canvas.drawText("\uD83D\uDCE6", x, y, mPaint);
        if ((flags&fPlayer)>0) canvas.drawText("\uD83E\uDDCD", x, y, mPaint);
        if ((flags&fWall)>0) canvas.drawText("\uD83E\uDDF1", x, y, mPaint);
    }

    @Override
    public void onDrawForeground(final Canvas canvas) {
        // clear background
        if (darkColors){
            canvas.drawARGB(255, 50,50,50);
        } else {
            canvas.drawARGB(255, 200,200,200);
        }

        drawLevel(canvas);

        // show crappy win screen. TODO: level transitions etc.
        if (levelComplete) {
            mPaint.setTextSize(200);
            canvas.drawText("WIN \uD83E\uDD38", 60, 200, mPaint);
        }

        if ((!touchDown) && (Math.abs(px - vx) > 0.01f || Math.abs(py - vy) > 0.01f)) {
            // view is not aligned to player. drift in
            float dx = (px - vx) / 3.0f;
            float dy = (py - vy) / 3.0f;
            vx+=dx;
            vy+=dy;
            invalidate(); // draw a frame
        }
        /*
        stand  "\uD83E\uDDCD";
        brick  "\uD83E\uDDF1";
        cartwheel  "\uD83E\uDD38";

        run  "\uD83C\uDFC3";
        fog  "\uD83C\uDF2B️";

        box  "\uD83D\uDCE6";
        gem  "\uD83D\uDC8E";
        diamond  "\uD83D\uDD37";
        hole  "\uD83D\uDD73";
        walk  "\uD83D\uDEB6";
        block  "\uD83D\uDEA7";
        purple  "\uD83D\uDFEA";
        */
    }

    public boolean TouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown = true;
                didScroll = false;
                touchX = event.getAxisValue(0);
                touchY = event.getAxisValue(1);
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getAxisValue(0);
                moveY = event.getAxisValue(1);
                touchMoved();
                break;
            case MotionEvent.ACTION_UP:
                touchDown = false;
                upX = event.getAxisValue(0);
                upY = event.getAxisValue(1);
                touchLift();
                break;
            default:
        }

        invalidate(); // draw a frame
        return true; // event handled
    }

    private void touchLift() {
        // if we didn't scroll, then see what side of the player we tapped. move, check state etc.
        if (didScroll) return;

        // split screen into 9 like #
        // centre and corners do nothing (to save ambiguous inputs)
        int xi = (int)((upX*3) / lastWidth);
        int yi = (int)((upY*3) / lastHeight);

        if (xi == 0 && yi == 1) movePlayer(-1, 0);
        if (xi == 2 && yi == 1) movePlayer(1, 0);

        if (xi == 1 && yi == 0) movePlayer(0, -1);
        if (xi == 1 && yi == 2) movePlayer(0, 1);

        // TODO: have a reset button (should be tight bounds & have a visual)
    }

    private void movePlayer(int dx, int dy) {
        int newX = px+dx;
        int newY = py+dy;

        if (newX < 0 || newY < 0 || newX >= levelWidth || newY >= levelHeight) {
            // would try to move off play field.
            // we bump the view to show we got the input, but we don't move
            vx = newX; vy = newY;
            return;
        }

        byte target = level[newY][newX];

        if ((target & fWall) > 0){
            // target is totally blocked. Bump the view, don't move
            vx = newX; vy = newY;
            return;
        }

        if ((target & fBox) == 0){
            // either empty floor or unfilled goal. Just move
            // we don't update the *view* position, so it will animate to catch up
            level[py][px] &= ~fPlayer; // remove from old position
            px+=dx; py+=dy;
            level[py][px] |= fPlayer; // add to new position
            return;
        }

        // here, we're bumping into a box.
        // need to check the box's target is empty or goal.
        // pushing two boxes isn't allowed

        int newBoxX = newX+dx;
        int newBoxY = newY+dy;

        if (newBoxX < 0 || newBoxY < 0 || newBoxX >= levelWidth || newBoxY >= levelHeight) {
            // would try to move box off the play field.
            // we bump the view, don't move
            vx = newX; vy = newY;
            return;
        }

        target = level[newBoxY][newBoxX]; // box's new space
        if ((target & (fWall|fBox)) > 0) {
            // there is a wall or another box behind the box.
            // bump the view, don't move
            vx = newX; vy = newY;
            return;
        }

        // YAY! we can push a box.
        // move the box and the player.
        // we don't update the *view* position, so it will animate to catch up
        level[py][px] &= ~fPlayer; // remove player from old position
        px+=dx; py+=dy;            // update player pointer
        level[py][px] |= fPlayer;  // add player to new position

        level[py][px] &= ~fBox;             // remove box from old position
        level[newBoxY][newBoxX] |= fBox;    // add box to new position

        checkLevelState();
    }

    private void checkLevelState() {
        levelComplete = true; // optimism! we'll turn it off if we see a lone box or goal
        for (int y=0; y < levelHeight; y++){
            for (int x=0; x < levelWidth; x++){
                byte tile = level[y][x];
                if (tile == fGoal || tile == fBox) { // a box on a goal would not equal either.
                    levelComplete = false;
                    return;
                }
            }
        }
    }

    // if it's a small move, ignore. Otherwise, look around
    private void touchMoved() {
        float dx = touchX-moveX;
        float dy = touchY-moveY;
        float dist = (float)Math.sqrt((dx*dx)+(dy*dy));

        if (!didScroll && dist < 100) return;

        // look around the player
        didScroll = true;
        vx = px + (dx / tileScale);
        vy = py + (dy / tileScale);
    }
}
