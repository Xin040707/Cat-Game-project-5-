package io.github.some_example_name;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.EnumMap;
import java.util.Map;

public class Cat extends Sprite {
    private static final String CAT_CLASSICAL_BASE_PATH =
        "CatMegaBundle/CatMegaBundle/LittleKitties/CatClassical/";
    private static final String DEFAULT_VARIANT = "BlueCollar";
    private static final float FRAME_DURATION = 0.12f;
    private static final int FRAME_SIZE = 32;
    private static final float CHASE_SPEED = 260f;
    private static final float JUMP_TRIGGER_DISTANCE = 120f;
    private static final float CATCH_DURATION = 1.1f;
    private static final float CATCH_SIDE_OFFSET = 36f;
    private static final float CATCH_ARC_HEIGHT = 75f;
    private static final float MAX_TOY_HEIGHT_OFFSET = 170f;

    private final Map<CatStates, Animation<TextureRegion>> animations;
    private final AssetManager assetManager;
    private CatStates currentState;
    private float stateTime;
    private boolean catching;
    private float catchElapsed;
    private float catchStartX;
    private float catchEndX;
    private float catchFloorY;
    private boolean facingRight;
    private String variantFolder;
    private final float scale;

    public Cat(AssetManager assetManager, float x, float y, float scale) {
        this.assetManager = assetManager;
        this.scale = scale;
        animations = new EnumMap<>(CatStates.class);
        variantFolder = DEFAULT_VARIANT;
        loadAnimations();

        currentState = CatStates.IDLE;
        stateTime = 0f;
        facingRight = true;

        TextureRegion startingFrame = getCurrentFrame();
        setRegion(startingFrame);
        setBounds(x, y, startingFrame.getRegionWidth() * scale, startingFrame.getRegionHeight() * scale);
    }

    public void setVariant(String variantFolder) {
        if (variantFolder == null || variantFolder.equals(this.variantFolder)) {
            return;
        }

        this.variantFolder = variantFolder;
        catching = false;
        catchElapsed = 0f;
        if (currentState == CatStates.CATCH) {
            currentState = CatStates.IDLE;
        }
        stateTime = 0f;
        loadAnimations();
        TextureRegion currentFrame = getCurrentFrame();
        setRegion(currentFrame);
        setSize(currentFrame.getRegionWidth() * scale, currentFrame.getRegionHeight() * scale);
    }

    public String getVariant() {
        return variantFolder;
    }

    public void setState(CatStates newState) {
        if (newState == null || newState == currentState) {
            return;
        }

        if (newState != CatStates.CATCH) {
            catching = false;
            catchElapsed = 0f;
        }
        currentState = newState;
        stateTime = 0f;
        setRegion(getCurrentFrame());
    }

    public CatStates getState() {
        return currentState;
    }

    public void update(float delta) {
        stateTime += delta;

        if (catching) {
            updateCatch(delta);
        }

        Animation<TextureRegion> animation = animations.get(currentState);
        if (currentState == CatStates.DIE && animation.isAnimationFinished(stateTime)) {
            stateTime = animation.getAnimationDuration();
        }

        setRegion(getCurrentFrame());
    }

    public void chaseToy(float targetX, float targetY, float floorY, float minX, float delta) {
        if (currentState == CatStates.DIE) {
            return;
        }

        float catCenterX = getX() + getWidth() / 2f;
        float catCenterY = getY() + getHeight() / 2f;
        float dx = targetX - catCenterX;
        float dy = targetY - catCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        updateFacing(dx);

        if (catching) {
            return;
        }

        if (targetY - catCenterY > MAX_TOY_HEIGHT_OFFSET) {
            if (currentState != CatStates.IDLE) {
                setState(CatStates.IDLE);
            }
            setY(floorY);
            setX(Math.max(getX(), minX));
            return;
        }

        if (distance <= JUMP_TRIGGER_DISTANCE) {
            startCatch(targetX, floorY);
            return;
        }

        if (distance > 1f) {
            float moveDistance = Math.min(CHASE_SPEED * delta, distance);
            float moveX = dx / distance * moveDistance;
            setX(Math.max(getX() + moveX, minX));
            setY(floorY);
            if (currentState != CatStates.PLAY) {
                setState(CatStates.PLAY);
            }
        } else if (currentState != CatStates.IDLE) {
            setState(CatStates.IDLE);
        }
    }

    private void startCatch(float toyX, float floorY) {
        catching = true;
        catchElapsed = 0f;
        catchStartX = getX();
        float leftCatchX = toyX - getWidth() - CATCH_SIDE_OFFSET;
        float rightCatchX = toyX + CATCH_SIDE_OFFSET;
        float catCenterX = getX() + getWidth() / 2f;

        if (catCenterX <= toyX) {
            catchEndX = rightCatchX;
            facingRight = true;
        } else {
            catchEndX = leftCatchX;
            facingRight = false;
        }
        catchFloorY = floorY;
        setState(CatStates.CATCH);
    }

    private void updateCatch(float delta) {
        catchElapsed += delta;
        float progress = Math.min(1f, catchElapsed / CATCH_DURATION);
        float nextX;
        nextX = catchStartX + (catchEndX - catchStartX) * progress;

        if (catchEndX >= catchStartX) {
            facingRight = true;
        } else {
            facingRight = false;
        }

        float arcOffset = 4f * CATCH_ARC_HEIGHT * progress * (1f - progress);
        float nextY = catchFloorY + arcOffset;

        setPosition(nextX, nextY);

        if (progress >= 1f) {
            catching = false;
            setY(catchFloorY);
            setState(CatStates.PLAY);
        }
    }

    public void draw(Batch batch) {
        super.draw(batch);
    }

    private TextureRegion getCurrentFrame() {
        TextureRegion frame = animations.get(currentState).getKeyFrame(stateTime);
        boolean frameFacesRight = !frame.isFlipX();
        if (frameFacesRight != facingRight) {
            frame.flip(true, false);
        }
        return frame;
    }

    private void updateFacing(float dx) {
        if (dx > 1f) {
            facingRight = true;
        } else if (dx < -1f) {
            facingRight = false;
        }
    }

    private Animation<TextureRegion> createAnimation(
        String fileName,
        boolean looping
    ) {
        Texture texture = assetManager.get(CAT_CLASSICAL_BASE_PATH + variantFolder + "/" + fileName, Texture.class);
        TextureRegion[][] splitFrames = TextureRegion.split(
            texture,
            FRAME_SIZE,
            texture.getHeight()
        );

        Array<TextureRegion> frames = new Array<>();
        for (TextureRegion frame : splitFrames[0]) {
            frames.add(frame);
        }

        Animation<TextureRegion> animation = new Animation<>(FRAME_DURATION, frames);
        animation.setPlayMode(looping ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        return animation;
    }

    private void loadAnimations() {
        animations.clear();
        animations.put(CatStates.IDLE, createAnimation(getIdleFileName(), true));
        animations.put(CatStates.PLAY, createAnimation(getRunFileName(), true));
        animations.put(CatStates.CATCH, createAnimation(getAttackFileName(), true));
        animations.put(CatStates.SLEEP, createAnimation(getSleepFileName(), true));
        animations.put(CatStates.SIT, createAnimation(getSittingFileName(), true));
        animations.put(CatStates.LICK, createAnimation("Liking.png", true));
        animations.put(CatStates.DIE, createAnimation(getDieFileName(), false));
    }

    private String getAttackFileName() {
        return isBlueVariant() ? "AttackCat.png" : "AttackCatt.png";
    }

    private String getDieFileName() {
        return isBlueVariant() ? "DieCat.png" : "DieCatt.png";
    }

    private String getIdleFileName() {
        return isBlueVariant() ? "IdleCat.png" : "IdleCatt.png";
    }

    private String getRunFileName() {
        return isBlueVariant() ? "RunCat.png" : "RunCatt.png";
    }

    private String getSittingFileName() {
        return isBlueVariant() ? "Sitting.png" : "Sittingg.png";
    }

    private String getSleepFileName() {
        return isBlueVariant() ? "SleepCat.png" : "SleepCatt.png";
    }

    private boolean isBlueVariant() {
        return DEFAULT_VARIANT.equals(variantFolder);
    }
}
