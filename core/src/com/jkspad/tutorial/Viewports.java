package com.jkspad.tutorial;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * @author John Knight
 * Copyright http://www.jkspad.com
 */
public class Viewports extends ApplicationAdapter {

    private static final int NUM_VIEWPORTS = 4;
    private static final float WORLD_WIDTH = 1024;
    private static final float WORLD_HEIGHT = 768;
    private static final float MAX_ZOOM_IN = 0.02f;
    private static final float MAX_ZOOM_OUT = 4f;
    private static final float SPEED_ZOOM_SECONDS = 4f;
    private static final float SPEED_ROTATE_SECONDS = 1.5f;
    private static final float SPEED_TRANSLATE_SECONDS = 400f;
    private static float MAX_SHAKE_X = 10;
    private static float MAX_SHAKE_Y = 10;
    private static float MAX_SHAKE_ROTATION = 4;
    private static final float MAX_SHAKE_TIME = 0.5f;

    private static final int STATUS_TEXT_X = 10;
    private static final int STATUS_TEXT_Y = 470;

    private int viewportWidth;
    private int viewportHeight;

    private final Color colCornflowerBlue = new Color(100f / 255f, 149f / 255f, 237f / 255f, 1);
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private ShaderProgram shader;
    private Mesh mesh;
    private Texture texture;

    private JKOrthographicCamera[] cameras = new JKOrthographicCamera[NUM_VIEWPORTS];

    enum ZoomState {
        IN,
        OUT
    }

    enum RotateState {
        LEFT,
        RIGHT
    }

    enum PanState {
        LEFT,
        RIGHT,
        UP,
        DOWN,
    }

    private ZoomState currentZoomState = ZoomState.IN;
    private RotateState currentRotateState = RotateState.LEFT;
    private PanState currentPanState = PanState.LEFT;

    private final String VERTEX_SHADER =
            "attribute vec4 a_position;\n"
                    + "attribute vec2 a_texCoord; \n"
                    + "uniform mat4 u_projTrans;\n"
                    + "varying vec2 v_texCoord; \n"
                    + "void main() {\n"
                    + " gl_Position = u_projTrans * a_position;\n"
                    + " v_texCoord =  a_texCoord; \n" +
                    "}";

    private final String FRAGMENT_SHADER =
            "#ifdef GL_ES\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "varying vec2 v_texCoord; \n" +
                    "uniform sampler2D u_texture; \n" +
                    "void main() \n" +
                    "{ \n" +
                    " gl_FragColor = texture2D( u_texture, v_texCoord );\n" +
                    "} \n";

    protected void createMeshShader() {
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        String log = shader.getLog();
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException(log);
        }
        if (log != null && log.length() != 0) {
            Gdx.app.log("shader log", log);
        }
    }

    private void createTexture() {
        texture = new Texture("test.png"); // 1024 x 768
    }

    private void initCameras(int width, int height) {

        for (int i = 0; i < NUM_VIEWPORTS; i++) {
            cameras[i] = new JKOrthographicCamera(width, height);
            cameras[i].zoom = (MAX_ZOOM_OUT - MAX_ZOOM_IN) / 2;
            cameras[i].initZoom(MAX_ZOOM_IN, MAX_ZOOM_OUT, SPEED_ZOOM_SECONDS, (MAX_ZOOM_OUT - MAX_ZOOM_IN) / 2);
        }

        // zoom in a bit for panning
        cameras[2].initZoom(MAX_ZOOM_IN, MAX_ZOOM_OUT, SPEED_ZOOM_SECONDS, (MAX_ZOOM_OUT - MAX_ZOOM_IN) / 4);

        // zoom in a bit for shaking
        cameras[3].initZoom(MAX_ZOOM_IN, MAX_ZOOM_OUT, SPEED_ZOOM_SECONDS, (MAX_ZOOM_OUT - MAX_ZOOM_IN) / 4);

        //init camera for zooming
        cameras[0].initZoom(MAX_ZOOM_IN, MAX_ZOOM_OUT, SPEED_ZOOM_SECONDS, 1f);

        // init camera for rotation
        cameras[1].initRotate(SPEED_ROTATE_SECONDS);

        // init camera for translation
        cameras[2].initTranslate(WORLD_WIDTH, WORLD_HEIGHT, SPEED_TRANSLATE_SECONDS);

        // init camera for shaking
        cameras[3].initShake(MAX_SHAKE_X, MAX_SHAKE_Y, MAX_SHAKE_ROTATION, MAX_SHAKE_TIME);
    }

    @Override
    public void create() {

        float halfWidth = WORLD_WIDTH / 2;
        float halfHeight = WORLD_HEIGHT / 2;

        mesh = new Mesh(true, 4, 0,
                new VertexAttribute(Usage.Position, 2, "a_position"),
                new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord")
        );

        float[] vertices = {
                -halfWidth, -halfHeight,    // quad bottom left
                0.0f, 1.0f,                // texture bottom left
                halfWidth, -halfHeight,    // quad bottom right
                1f, 1.0f,                  // texture bottom right
                -halfWidth, halfHeight,    // quad top left
                0.0f, 0.0f,                // texture top left
                halfWidth, halfHeight,     // quad top right
                1.0f, 0.0f                 // texture top-right

        };

        mesh.setVertices(vertices);
        createTexture();
        createMeshShader();
        font = new BitmapFont();
        spriteBatch = new SpriteBatch();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewportWidth = width / 2;
        viewportHeight = height / 2;
        initCameras(width, height);
    }

    private void showMessage(JKOrthographicCamera camera) {
        StringBuilder sb = new StringBuilder();
        sb.append("Pos: " + camera.position.x + "," + camera.position.y);
        sb.append("\nZoom: " + camera.zoom);
        sb.append("\nRotation (degrees): " + camera.getCurrentRotation());
        spriteBatch.begin();
        font.draw(spriteBatch, sb.toString(), STATUS_TEXT_X, STATUS_TEXT_Y);
        spriteBatch.end();
    }

    private void renderCamera(JKOrthographicCamera camera) {
        Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texture.bind();

        shader.begin();
        shader.setUniformi("u_texture", 0);
        shader.setUniformMatrix("u_projTrans", camera.combined);
        mesh.render(shader, GL20.GL_TRIANGLE_STRIP);
        shader.end();
        showMessage(camera);
    }

    private void updateCameraZoom(float delta, JKOrthographicCamera camera) {
        if (currentZoomState == ZoomState.IN) {
            if (camera.zoom <= MAX_ZOOM_IN) {
                currentZoomState = ZoomState.OUT;
            } else {
                camera.zoomIn(delta);
            }
        } else {
            if (camera.zoom >= MAX_ZOOM_OUT) {
                currentZoomState = ZoomState.IN;
            } else {
                camera.zoomOut(delta);
            }
        }
    }

    private void updateCameraRotate(float delta, JKOrthographicCamera camera) {
        if (currentRotateState == RotateState.LEFT) {
            if (camera.getCurrentRotation() >= 360) {
                currentRotateState = RotateState.RIGHT;
            } else {
                camera.rotateLeft(delta);
            }
        } else {
            if (camera.getCurrentRotation() <= -360) {
                currentRotateState = RotateState.LEFT;
            } else {
                camera.rotateRight(delta);
            }
        }
    }

    private void updateCameraTranslate(float delta, JKOrthographicCamera camera) {
        switch (currentPanState) {
            case LEFT:
                float minX = camera.panLeft(delta);
                if (camera.position.x <= minX) {
                    currentPanState = PanState.RIGHT;
                }
                break;
            case RIGHT:
                float maxX = camera.panRight(delta);
                if (camera.position.x >= maxX) {
                    currentPanState = PanState.UP;
                }
                break;
            case UP:
                float maxY = camera.panUp(delta);
                if (camera.position.y >= maxY) {
                    currentPanState = PanState.DOWN;
                }

                break;
            case DOWN:
                float minY = camera.panDown(delta);
                if (camera.position.y <= minY) {
                    currentPanState = PanState.LEFT;
                }
                break;
            default:
                break;

        }
    }

    private void updateCameraShake(float delta, JKOrthographicCamera camera) {
        if (camera.getShakeTime() <= 0) {
            camera.startShakingBaby();
        }
        camera.shakeItBaby(delta);
    }

    private void updateCameras() {
        for (JKOrthographicCamera camera : cameras) {
            camera.update();
        }
    }

    private void update() {
        float delta = Gdx.graphics.getDeltaTime();
        updateCameras();
        updateCameraZoom(delta, cameras[0]);
        updateCameraRotate(delta, cameras[1]);
        updateCameraTranslate(delta, cameras[2]);
        updateCameraShake(delta, cameras[3]);
    }

    @Override
    public void render() {

        update();

        Gdx.gl.glClearColor(colCornflowerBlue.r, colCornflowerBlue.g, colCornflowerBlue.b, colCornflowerBlue.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Bottom left
        Gdx.gl.glViewport(0, 0, viewportWidth, viewportHeight);
        renderCamera(cameras[0]);

        // Top left
        Gdx.gl.glViewport(0, viewportHeight, viewportWidth, viewportHeight);
        renderCamera(cameras[1]);

        // Top right
        Gdx.gl.glViewport(viewportWidth, viewportHeight, viewportWidth, viewportHeight);
        renderCamera(cameras[2]);

        // Bottom Right
        Gdx.gl.glViewport(viewportWidth, 0, viewportWidth, viewportHeight);
        renderCamera(cameras[3]);
    }
}