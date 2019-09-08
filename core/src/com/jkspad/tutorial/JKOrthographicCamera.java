package com.jkspad.tutorial;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * @author John Knight
 * Copyright http://www.jkspad.com
 */
public class JKOrthographicCamera extends OrthographicCamera {

    // Zoom members
    private float elapsedZoom;
    private float maxZoomIn;
    private float maxZomOut;
    private float zoomSpeedSeconds;

    // Rotation members
    private float currentRotation;
    private float rotateSpeedSeconds;

    // Translation members
    private float worldWidth;
    private float worldheight;
    private float translateSpeedSeconds;

    // Shake members
    private float shakeTime; // a bit like "sexy time"
    private Vector3 storedPosition;
    private float storedRotation;
    private float maxShakeX;
    private float maxShakeY;
    private float maxShakeRotation;
    private float maxShakeTime;

    public JKOrthographicCamera(int width, int height) {
        super(width, height);
    }

    public void initShake(float maxShakeX, float maxShakeY, float maxShakeRotation, float maxShakeTime) {
        this.maxShakeX = maxShakeX;
        this.maxShakeY = maxShakeY;
        this.maxShakeRotation = maxShakeRotation;
        this.maxShakeTime = maxShakeTime;
    }

    public void startShakingBaby() {
        if (shakeTime > 0) {
            return; // we is already shakin' innit.
        }
        shakeTime = maxShakeTime;
        storePosition();
        storeRotation();
    }

    public void shakeItBaby(float delta) {
        if (shakeTime <= 0)
            return;

        shakeTime -= delta;

        if (shakeTime <= 0) {
            shakeTime = 0;
            this.position.x = storedPosition.x;
            this.position.y = storedPosition.y;
            this.position.z = storedPosition.z;
            rotate(-currentRotation + storedRotation);
            return;
        }

        int posModifier = 1;
        int rotModifier = 1;
        if (MathUtils.random(10) >= 5)
            posModifier = -1;
        if (MathUtils.random(10) >= 5)
            rotModifier = -1;

        float posXAmount = MathUtils.random(maxShakeX) * posModifier;
        float posYAmount = MathUtils.random(maxShakeY) * posModifier;
        float rotAmount = MathUtils.random(maxShakeRotation) * rotModifier;

        this.position.x = storedPosition.x + posXAmount;
        this.position.y = storedPosition.y + posYAmount;

        rotate(rotAmount);
    }

    public void initRotate(float rotateSpeedSeconds) {
        this.rotateSpeedSeconds = rotateSpeedSeconds;
    }

	public void resetRotation() {
		rotate(-currentRotation);
	}

    @Override
    public void rotate(float degrees) {
        super.rotate(degrees);
        currentRotation = currentRotation + degrees;
    }

    public void rotateLeft(float delta) {
        float deltaRotation = 360 * delta / rotateSpeedSeconds;
        rotate(deltaRotation);
    }

    public void rotateRight(float delta) {
        float deltaRotation = -360 * delta / rotateSpeedSeconds;
        rotate(deltaRotation);
    }

    public void initTranslate(float worldWidth, float worldHeight, float translateSpeedSeconds) {
        this.worldWidth = worldWidth;
        this.worldheight = worldHeight;
        this.translateSpeedSeconds = translateSpeedSeconds;
    }

    private boolean canPan() {
        if (this.zoom > worldWidth / this.viewportWidth) {
            return false;
        }
        return true;
    }

    public float panLeft(float delta) {
        if (canPan()) {
            translate(-translateSpeedSeconds * delta, 0);
            float scaledViewportWidth = this.viewportWidth * this.zoom;
            float maxX = worldWidth / 2 - scaledViewportWidth / 2;
            float minX = -maxX;
            this.position.x = MathUtils.clamp(this.position.x, minX, maxX);
            return minX;
        }
        return Float.MAX_VALUE;
    }

    public float panRight(float delta) {
        if (canPan()) {
            translate(translateSpeedSeconds * delta, 0);
            float scaledViewportWidth = this.viewportWidth * this.zoom;
            float maxX = worldWidth / 2 - scaledViewportWidth / 2;
            float minX = -maxX;
            this.position.x = MathUtils.clamp(this.position.x, minX, maxX);
            return maxX;
        }
        return Float.MAX_VALUE;
    }

    public float panUp(float delta) {
        if (canPan()) {
            translate(0, translateSpeedSeconds * delta);
            float scaledViewportHeight = this.viewportHeight * this.zoom;
            float maxY = worldheight / 2 - scaledViewportHeight / 2;
            float minY = -maxY;
            this.position.y = MathUtils.clamp(this.position.y, minY, maxY);
            return maxY;
        }
        return Float.MAX_VALUE;
    }

    public float panDown(float delta) {
        if (canPan()) {
            translate(0, -translateSpeedSeconds * delta);
            float scaledViewportHeight = this.viewportHeight * this.zoom;
            float maxY = worldheight / 2 - scaledViewportHeight / 2;
            float minY = -maxY;
            this.position.y = MathUtils.clamp(this.position.y, minY, maxY);
            return minY;
        }
        return Float.MAX_VALUE;
    }

    public void initZoom(float maxZoomIn, float maxZoomOut, float zoomSpeedSeconds, float zoomLevel) {
        this.maxZoomIn = maxZoomIn;
        this.maxZomOut = maxZoomOut;
        this.zoomSpeedSeconds = zoomSpeedSeconds;
        this.zoom = zoomLevel;
        this.elapsedZoom = MathUtils.clamp(zoomLevel / (maxZoomOut - maxZoomIn) * zoomSpeedSeconds, 0, zoomSpeedSeconds);
    }

    public void zoomOut(float delta) {
        elapsedZoom = MathUtils.clamp(elapsedZoom + delta, 0, zoomSpeedSeconds);
        this.zoom = MathUtils.lerp(maxZoomIn, maxZomOut, elapsedZoom / zoomSpeedSeconds);
    }

    public void zoomIn(float delta) {
        elapsedZoom = MathUtils.clamp(elapsedZoom - delta, 0, zoomSpeedSeconds);
        this.zoom = MathUtils.lerp(maxZoomIn, maxZomOut, elapsedZoom / zoomSpeedSeconds);
    }

    private void storePosition() {
        this.storedPosition = new Vector3(this.position);
    }

    private void storeRotation() {
        this.storedRotation = currentRotation;
    }

    public void setStoredRotation(float storedRotation) {
        this.storedRotation = storedRotation;
    }

    public float getShakeTime() {
        return shakeTime;
    }

    public float getCurrentRotation() {
        return currentRotation;
    }
}
