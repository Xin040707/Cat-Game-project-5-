/*
*I couldn't do much with the visual because these are free
* @author Dung Nguyen
* @version 27/4
 */

package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 1500f;
    private static final float WORLD_HEIGHT = 1000f;
    private static final Color GAME_SCENE_COLOR = new Color(0xFEEED4FF);
    private static final float TOY_BOARD_WIDTH = 300f;
    private static final float FLOOR_Y = 155f;
    private static final float FLOOR_HEIGHT = 8f;
    private static final float FLOOR_LEFT_MARGIN = 40f;
    private static final float FLOOR_RIGHT_MARGIN = 40f;
    private static final Color FLOOR_COLOR = new Color(0x8B6F47FF);
    private static final float CAT_TABLE_TOP_PADDING = 130f;
    private static final float CAT_TABLE_SIDE_PADDING = 26f;
    private static final float CAT_TABLE_BUTTON_SIZE = 70f;
    private static final float CAT_TABLE_IMAGE_SIZE = 48f;
    private static final float CAT_TABLE_CELL_PADDING = 4f;
    private static final int CAT_TABLE_COLUMNS = 3;
    private static final float BOARD_HEADER_TOP_PADDING = 26f;
    private static final float BOARD_HEADER_SIDE_PADDING = 22f;
    private static final float BOARD_HEADER_HEIGHT = 56f;
    private static final float TOY_CURSOR_SIZE = 72f;
    private static final String[] CLASSICAL_CAT_VARIANTS = {
        "BlackCollor",
        "BlueCollar",
        "GreenCollor",
        "OrangeCollor",
        "PinkCollor",
        "PurpleCollor",
        "RedCollor",
        "YellowCollor"
    };
    private static final String[] CLASSICAL_CAT_LABELS = {
        "Black",
        "Blue",
        "Green",
        "Orange",
        "Pink",
        "Purple",
        "Red",
        "Yellow"
    };

    // path for start scene
    private static final String START_BACK_GROUND =
        "output/New folder/CatUI/startBackGround.jpg";
    // path for game scene
    private static final String TOY_CHOOSING_BOARD =
        "output/New folder/toyChoosingBoard.png";
    // assets
    private AssetManager assetManager;
    // game scene assets
    private TextureRegion[][] catToyTRArray;
    //texture
    private Texture pixelTexture;
    private Texture toyBoard;
    // start scene assets
    private Texture startBackground;
    // button
    private ImageButton startButton;
    private Cat cat;
    private Cursor invisibleCursor;

    // scenes boolean
    private boolean start;
    private boolean assetsReady;
    private boolean catToyMode;

    // init window
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private BitmapFont font;

    // init UI
    private Stage startSceneStage;
    private Stage gameSceneStage;

    // start button
    private Skin startButtonSkin;
    private ImageButton catToyButton;
    private TextButton sleepButton;
    private TextButton sitButton;
    private TextButton lickButton;
    private TextButton idleButton;
    private TextButton dieButton;
    private TextButton[] classicalCatButtons;

    //atlas
    private static final String catBonusUIAtlasFormat = "sprite/BONUSPastelUI - sprite";
    private TextureAtlas catUIBonusAtlas;
    private float toyCursorStateTime;
    private final Vector2 toyCursorWorld = new Vector2();
    private int selectedCatIndex = 1;
    @Override
    public void create() {
        //initialize the game window
        start = false;
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        font = new BitmapFont();
        font.getData().setScale(2f);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixelTexture = new Texture(pixmap);
        pixmap.dispose();
        invisibleCursor = createInvisibleCursor();

        assetManager = new AssetManager();
        queueAssetsFromList();
        // set up asset
         catToyTRArray = creatTextureRegionArrayFromSpriteSheet("CatMegaBundle/CatMegaBundle/CatItems/CatToys/CatToy.png",6,1);

        // UI
        // atlas
        catUIBonusAtlas = new TextureAtlas(Gdx.files.internal("output/Atlas/CatUIAtlas/CatUIBonus.atlas"));
        // start scene
        // Ui setup
        startSceneStage = new Stage(viewport);
        Gdx.input.setInputProcessor(startSceneStage);
        // start button
        startButton = createImageButton(catUIBonusAtlas, 85, 106,catBonusUIAtlasFormat, 300, 100);
        startButton.getImageCell().size(startButton.getWidth(), startButton.getHeight());
        startButton.setPosition((WORLD_WIDTH/2) - (startButton.getWidth()/2),(WORLD_HEIGHT/2) - (startButton.getHeight()/2));
        // add listener for start button
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Start button clicked");
                start = true;
                changeStageToInGame();
            }
        });

        // add start button to game scene
        startSceneStage.addActor(startButton);
    }

    private void changeStageToInGame() {
        gameSceneStage = new Stage(viewport);
        Gdx.input.setInputProcessor(gameSceneStage);
        cat = new Cat(assetManager, 520f, FLOOR_Y, 8f);

        Stack choosingBoard = createChoosingBoard();
        choosingBoard.setPosition(0f, 0f);
        gameSceneStage.addActor(choosingBoard);
    }


    private Stack createChoosingBoard() {
        Stack boardStack = new Stack();
        boardStack.setSize(TOY_BOARD_WIDTH, WORLD_HEIGHT);

        Image boardBackground = new Image(new TextureRegionDrawable(new TextureRegion(toyBoard)));
        boardBackground.setFillParent(true);

        Table decorationLayer = new Table();
        decorationLayer.setFillParent(true);
        decorationLayer.top();
        decorationLayer.padTop(BOARD_HEADER_TOP_PADDING);
        decorationLayer.padLeft(BOARD_HEADER_SIDE_PADDING);
        decorationLayer.padRight(BOARD_HEADER_SIDE_PADDING);

        // decoration layer
        TextureRegion toyTableTextBoxImage = catUIBonusAtlas.findRegion(catBonusUIAtlasFormat, 391);
        Label.LabelStyle catLabelStyle =  new Label.LabelStyle(new BitmapFont(), Color.BLACK);
        catLabelStyle.background = new TextureRegionDrawable(toyTableTextBoxImage);
        Label catBoardLabel = new Label("Cat items :3", catLabelStyle);
        catBoardLabel.setAlignment(Align.center);

        decorationLayer.add(catBoardLabel).growX().height(BOARD_HEADER_HEIGHT);


        Table contentLayer = new Table();
        contentLayer.setFillParent(true);
        contentLayer.left().top();
        contentLayer.padTop(CAT_TABLE_TOP_PADDING);
        contentLayer.padLeft(CAT_TABLE_SIDE_PADDING);
        contentLayer.padRight(CAT_TABLE_SIDE_PADDING);
        contentLayer.defaults()
            .size(CAT_TABLE_BUTTON_SIZE)
            .pad(CAT_TABLE_CELL_PADDING)
            .top()
            .left();
        // now add toy cell to the content layer
        classicalCatButtons = createClassicalCatButtons();
        catToyButton = createButtonForCatTable(catToyTRArray[0][0]);
        sleepButton = createTextButtonForCatTable("Sleep");
        sitButton = createTextButtonForCatTable("Sit");
        lickButton = createTextButtonForCatTable("Lick");
        idleButton = createTextButtonForCatTable("Idle");
        dieButton = createTextButtonForCatTable("Die");

        // add listener
        addCatTableButtonListeners();
        updateClassicalCatButtonLabels();

        // add buttons to table
        int index = 0;
        for (TextButton catButton : classicalCatButtons) {
            addCatTableCell(contentLayer, catButton, index++);
        }
        addCatTableCell(contentLayer, catToyButton, index++);
        addCatTableCell(contentLayer, sleepButton, index++);
        addCatTableCell(contentLayer, sitButton, index++);
        addCatTableCell(contentLayer, lickButton, index++);
        addCatTableCell(contentLayer, idleButton, index++);
        addCatTableCell(contentLayer, dieButton, index);

        boardStack.add(boardBackground);
        boardStack.add(contentLayer);
        boardStack.add(decorationLayer);

        return boardStack;
    }

    private ImageButton createButtonForCatTable(TextureRegion textureRegion) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(textureRegion);

        ImageButton button = new ImageButton(style);
        button.setSize(CAT_TABLE_BUTTON_SIZE, CAT_TABLE_BUTTON_SIZE);
        button.getImageCell().size(CAT_TABLE_IMAGE_SIZE, CAT_TABLE_IMAGE_SIZE);
        button.getImage().setScaling(com.badlogic.gdx.utils.Scaling.fit);
        return button;
    }

    private void addCatTableButtonListeners() {
        for (int i = 0; i < classicalCatButtons.length; i++) {
            final int catIndex = i;
            classicalCatButtons[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onClassicalCatClicked(catIndex);
                }
            });
        }

        catToyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onCatToyButtonClicked();
            }
        });

        sleepButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onSleepButtonClicked();
            }
        });

        sitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onSitButtonClicked();
            }
        });

        lickButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onLickButtonClicked();
            }
        });

        idleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onIdleButtonClicked();
            }
        });

        dieButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onDieButtonClicked();
            }
        });
    }

    private void onCatToyButtonClicked() {
        System.out.println("Cat toy clicked");
        enableCatToyMode();
    }

    private void onClassicalCatClicked(int catIndex) {
        selectedCatIndex = catIndex;
        applySelectedCatVariant();
    }

    private void onSleepButtonClicked() {
        System.out.println("Sleep clicked");
        disableCatToyMode();
        cat.setState(CatStates.SLEEP);
    }

    private void onSitButtonClicked() {
        System.out.println("Sit clicked");
        disableCatToyMode();
        cat.setState(CatStates.SIT);
    }

    private void onLickButtonClicked() {
        System.out.println("Lick clicked");
        disableCatToyMode();
        cat.setState(CatStates.LICK);
    }

    private void onIdleButtonClicked() {
        System.out.println("Idle clicked");
        disableCatToyMode();
        cat.setState(CatStates.IDLE);
    }

    private void onDieButtonClicked() {
        System.out.println("Die clicked");
        disableCatToyMode();
        cat.setState(CatStates.DIE);
    }

    private void addCatTableCell(Table table, Actor actor, int index) {
        table.add(actor);

        if ((index + 1) % CAT_TABLE_COLUMNS == 0) {
            table.row();
        }
    }

    private Label createLabelForCatTable(String text) {
        Label.LabelStyle style = new Label.LabelStyle(font, Color.BLACK);
        Label label = new Label(text, style);
        label.setAlignment(Align.center);
        return label;
    }

    private TextButton createTextButtonForCatTable(String text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.BLACK;
        style.downFontColor = Color.DARK_GRAY;

        TextButton button = new TextButton(text, style);
        button.getLabel().setAlignment(Align.center);
        button.getLabel().setWrap(true);
        button.getLabel().setFontScale(1f);
        return button;
    }

    private TextButton[] createClassicalCatButtons() {
        TextButton[] buttons = new TextButton[CLASSICAL_CAT_LABELS.length];
        for (int i = 0; i < CLASSICAL_CAT_LABELS.length; i++) {
            buttons[i] = createTextButtonForCatTable(CLASSICAL_CAT_LABELS[i]);
        }
        return buttons;
    }

    private void applySelectedCatVariant() {
        cat.setVariant(CLASSICAL_CAT_VARIANTS[selectedCatIndex]);
        updateClassicalCatButtonLabels();
    }

    private void updateClassicalCatButtonLabels() {
        for (int i = 0; i < classicalCatButtons.length; i++) {
            String label = CLASSICAL_CAT_LABELS[i];
            if (i == selectedCatIndex) {
                label = "[" + label + "]";
            }
            classicalCatButtons[i].setText(label);
        }
    }

    private TextureRegionDrawable createDrawableRegion(String filePath) {
        Texture texture = new Texture(filePath);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private TextureRegion[][] creatTextureRegionArrayFromSpriteSheet(String ImagePath, int length, int height) {
        Texture sheet = new Texture(ImagePath);
        return TextureRegion.split(sheet,sheet.getWidth() / length, sheet.getHeight()/ height);
    }

    private Table creatTable(TextureAtlas atlas, int index, String format, Float width, Float height ) {
        Table table = new Table();
        table.setBackground(new TextureRegionDrawable(atlas.findRegion(format, index)));
        table.setSize(width, height);

        return table;
    }

    private ImageButton createImageButton(TextureAtlas atlas, int upIndex, int downIndex, String format, int width, int height ) {
        TextureRegion up = atlas.findRegion(format, upIndex);
        TextureRegion down = atlas.findRegion(format, downIndex);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(up);
        style.imageDown = new TextureRegionDrawable(down);

        ImageButton button = new ImageButton(style);
        button.setSize(width,height);
        return button;
   }

    private ImageButton createToyBoardCellButton(TextureAtlas atlas, int upIndex, int downIndex) {
        ImageButton button = createImageButton(atlas, upIndex, downIndex, catBonusUIAtlasFormat, 50, 50);
        return button;
    }

    private void queueAssetsFromList() {
        String[] assetPaths = Gdx.files.internal("assets.txt").readString().split("\\R");
        for (String rawPath : assetPaths) {
            String path = rawPath.trim();
            if (shouldSkip(path)) {
                continue;
            }
            if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                assetManager.load(path, Texture.class);
            }
        }
    }

    private boolean shouldSkip(String path) {
        return path.isEmpty()
            || path.equals("assets.txt")
            || path.contains("/__MACOSX/")
            || path.endsWith("/.DS_Store")
            || path.contains("/._");
    }

    @Override
    public void render() {
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        if (!assetsReady) {
            ScreenUtils.clear(0.08f, 0.08f, 0.1f, 1f);
            if (assetManager.update()) {
                finishLoading();
            }
            drawLoadingScreen();
            return;
        }

        if (start) {
            ScreenUtils.clear(GAME_SCENE_COLOR);
            drawGameScene();
            return;
        }

        ScreenUtils.clear(0.08f, 0.08f, 0.1f, 1f);
        startScene();

    }

    private void finishLoading() {
        startBackground = assetManager.get(START_BACK_GROUND, Texture.class);
        toyBoard = assetManager.get(TOY_CHOOSING_BOARD, Texture.class);
        assetsReady = true;
    }

    private void drawLoadingScreen() {
        float progress = assetManager.getProgress();
        float barWidth = 1000f;
        float barHeight = 50f;
        float barX = (WORLD_WIDTH - barWidth) / 2f;
        float barY = (WORLD_HEIGHT - barHeight) / 2f - 20f;

        spriteBatch.begin();
        spriteBatch.setColor(0.2f, 0.2f, 0.24f, 1f);
        spriteBatch.draw(pixelTexture, barX, barY, barWidth, barHeight);
        spriteBatch.setColor(0.88f, 0.66f, 0.29f, 1f);
        spriteBatch.draw(pixelTexture, barX, barY, barWidth * progress, barHeight);
        spriteBatch.setColor(Color.WHITE);

            font.draw(spriteBatch, "Loading cat assets...", barX, barY + 90f);
        font.draw(spriteBatch, (int) (progress * 100) + "%", barX, barY - 18f);
        spriteBatch.end();
    }

    private void startScene() {
        //buttons
        //start button


        // draw sprite
        spriteBatch.begin();
        spriteBatch.draw(startBackground, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        spriteBatch.end();
        startSceneStage.act();
        startSceneStage.draw();
    }

    private void drawGameScene() {
        float delta = Gdx.graphics.getDeltaTime();
        updateCatToyCursor(delta);
        if (catToyMode) {
            cat.chaseToy(
                toyCursorWorld.x,
                toyCursorWorld.y,
                FLOOR_Y,
                TOY_BOARD_WIDTH,
                delta
            );
        }
        cat.update(delta);
        spriteBatch.begin();
        spriteBatch.setColor(FLOOR_COLOR);
        spriteBatch.draw(
            pixelTexture,
            TOY_BOARD_WIDTH + FLOOR_LEFT_MARGIN,
            FLOOR_Y,
            WORLD_WIDTH - TOY_BOARD_WIDTH - FLOOR_LEFT_MARGIN - FLOOR_RIGHT_MARGIN,
            FLOOR_HEIGHT
        );
        spriteBatch.setColor(Color.WHITE);
        cat.draw(spriteBatch);
        spriteBatch.end();
        gameSceneStage.act();
        gameSceneStage.draw();
        if (catToyMode) {
            spriteBatch.begin();
            TextureRegion toyFrame = catToyTRArray[0][(int) (toyCursorStateTime / 0.1f) % catToyTRArray[0].length];
            spriteBatch.draw(
                toyFrame,
                toyCursorWorld.x - TOY_CURSOR_SIZE / 2f,
                toyCursorWorld.y - TOY_CURSOR_SIZE / 2f,
                TOY_CURSOR_SIZE,
                TOY_CURSOR_SIZE
            );
            spriteBatch.end();
        }
    }

    private void enableCatToyMode() {
        catToyMode = true;
        toyCursorStateTime = 0f;
        Gdx.graphics.setCursor(invisibleCursor);
    }

    private void disableCatToyMode() {
        catToyMode = false;
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
    }

    private void updateCatToyCursor(float delta) {
        if (!catToyMode) {
            return;
        }

        toyCursorStateTime += delta;
        toyCursorWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(toyCursorWorld);
        if (toyCursorWorld.y < FLOOR_Y + TOY_CURSOR_SIZE * 0.5f) {
            toyCursorWorld.y = FLOOR_Y + TOY_CURSOR_SIZE * 0.5f;
        }
    }

    private Cursor createInvisibleCursor() {
        Pixmap transparentPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        transparentPixmap.setColor(0f, 0f, 0f, 0f);
        transparentPixmap.fill();
        Cursor cursor = Gdx.graphics.newCursor(transparentPixmap, 0, 0);
        transparentPixmap.dispose();
        return cursor;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        disableCatToyMode();
        invisibleCursor.dispose();
        spriteBatch.dispose();
        font.dispose();
        pixelTexture.dispose();
        assetManager.dispose();
    }
}
