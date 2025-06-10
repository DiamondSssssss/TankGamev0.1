package com.mygdx.tankgame.Screen;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.tankgame.TankGame;
import com.mygdx.tankgame.levels.Level1Screen;
import com.mygdx.tankgame.levels.Level2Screen;
import com.mygdx.tankgame.levels.Level3Screen;
import com.mygdx.tankgame.playertank.PlayerTank;

public class UpgradeScreen implements Screen {

    private TankGame game;
    private PlayerTank playerTank;
    private Stage stage;
    private int currentLevel;
    private BitmapFont titleFont;
    private BitmapFont descriptionFont;

    private String[][] upgrades = {
        {"Speed", "Increases movement speed by 50.", "speed", "1"},
        {"Dash", "Grants 1 extra dash.", "dash", "2"},
        {"Health", "Increases max health by 10.", "heart", "3"}
    };

    // Lưu trữ các texture được tạo từ file ảnh và Pixmap
    private Texture speedTexture;
    private Texture dashTexture;
    private Texture heartTexture;
    private Texture rerollIconTexture; // Biểu tượng mũi tên xoay
    private Texture rerollButtonTexture; // Hình tròn nền vàng (trạng thái bình thường)
    private Texture rerollButtonHighlightTexture; // Hình tròn sáng lên (trạng thái nhấn)
    private Texture rainbowBarTexture;

    // Tỷ lệ co dãn dựa trên kích thước màn hình
    private float scaleFactor;
    private float iconSize;
    private float rerollButtonSize;
    private float cardWidth;
    private float cardHeight;
    private float rainbowBarWidth;
    private float rainbowBarHeight;
    private float descriptionHeight;

    // Kích thước chuẩn của viewport
    private static final float BASE_WIDTH = 1920f;
    private static final float BASE_HEIGHT = 1080f;

    public UpgradeScreen(TankGame game, PlayerTank playerTank, int currentLevel) {
        this.game = game;
        this.playerTank = playerTank;
        this.currentLevel = currentLevel;

        // Sử dụng FitViewport để giữ tỷ lệ cố định
        this.stage = new Stage(new FitViewport(BASE_WIDTH, BASE_HEIGHT));

        // Tính toán tỷ lệ co dãn dựa trên kích thước màn hình
        float widthRatio = Gdx.graphics.getWidth() / BASE_WIDTH;
        float heightRatio = Gdx.graphics.getHeight() / BASE_HEIGHT;
        scaleFactor = Math.max(widthRatio, heightRatio); // Lấy tỷ lệ lớn hơn để tránh thu nhỏ quá mức
        scaleFactor = Math.max(scaleFactor, 1.0f); // Đảm bảo scaleFactor không nhỏ hơn 1.0 để tránh thu nhỏ

        // Tăng kích thước cơ số để các thành phần lớn hơn
        iconSize = 150 * scaleFactor;
        rerollButtonSize = 120 * scaleFactor;
        cardWidth = 500 * scaleFactor;
        cardHeight = 750 * scaleFactor;
        rainbowBarWidth = 450 * scaleFactor;
        rainbowBarHeight = 4 * scaleFactor;
        descriptionHeight = 500 * scaleFactor; // Tăng từ 350 lên 1050 (gấp 3 lần)

        // Tạo font và tăng kích thước font cơ số lên gấp 3 lần
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f * scaleFactor); // Tăng từ 4 lên 12 (gấp 3 lần)
        descriptionFont = new BitmapFont();
        descriptionFont.getData().setScale(4f * scaleFactor); // Tăng từ 2.5 lên 7.5 (gấp 3 lần)

        // Tải các texture từ file ảnh
        try {
            speedTexture = new Texture(Gdx.files.internal("speed.png"));
            dashTexture = new Texture(Gdx.files.internal("dash.png"));
            heartTexture = new Texture(Gdx.files.internal("heart1.png"));
        } catch (Exception e) {
            Gdx.app.error("UpgradeScreen", "Error loading textures: " + e.getMessage());
            // Tạo texture mặc định nếu không tải được
            Pixmap fallbackPixmap = new Pixmap((int) iconSize, (int) iconSize, Pixmap.Format.RGBA8888);
            fallbackPixmap.setColor(Color.RED);
            fallbackPixmap.fill();
            speedTexture = new Texture(fallbackPixmap);
            dashTexture = new Texture(fallbackPixmap);
            heartTexture = new Texture(fallbackPixmap);
            fallbackPixmap.dispose();
        }

        // Tạo texture cho biểu tượng Reroll bằng Pixmap
        rerollIconTexture = createRerollIcon();

        // Tạo texture cho nút Reroll (hình tròn nền vàng) bằng Pixmap
        Pixmap rerollPixmap = new Pixmap((int) rerollButtonSize, (int) rerollButtonSize, Pixmap.Format.RGBA8888);
        rerollPixmap.setColor(Color.valueOf("FFD700")); // Màu vàng #FFD700
        rerollPixmap.fillCircle((int) (rerollButtonSize / 2), (int) (rerollButtonSize / 2), (int) (rerollButtonSize / 2));
        rerollPixmap.setColor(Color.valueOf("DAA520")); // Viền vàng đậm (GoldenRod)
        rerollPixmap.drawCircle((int) (rerollButtonSize / 2), (int) (rerollButtonSize / 2), (int) (rerollButtonSize / 2));
        rerollButtonTexture = new Texture(rerollPixmap);
        rerollPixmap.dispose();

        // Tạo texture cho nút Reroll khi nhấn (hình tròn sáng lên)
        Pixmap rerollHighlightPixmap = new Pixmap((int) rerollButtonSize, (int) rerollButtonSize, Pixmap.Format.RGBA8888);
        rerollHighlightPixmap.setColor(Color.valueOf("FFFF99")); // Màu vàng sáng hơn
        rerollHighlightPixmap.fillCircle((int) (rerollButtonSize / 2), (int) (rerollButtonSize / 2), (int) (rerollButtonSize / 2));
        rerollHighlightPixmap.setColor(Color.valueOf("FFD700")); // Viền vàng sáng
        rerollHighlightPixmap.drawCircle((int) (rerollButtonSize / 2), (int) (rerollButtonSize / 2), (int) (rerollButtonSize / 2));
        rerollButtonHighlightTexture = new Texture(rerollHighlightPixmap);
        rerollHighlightPixmap.dispose();

        // Tạo texture cho thang ngang màu cầu vồng bằng Pixmap
        Pixmap rainbowPixmap = new Pixmap((int) rainbowBarWidth, (int) rainbowBarHeight, Pixmap.Format.RGBA8888);
        for (int x = 0; x < (int) rainbowBarWidth; x++) {
            float ratio = (float) x / rainbowBarWidth;
            Color rainbowColor;
            if (ratio < 0.2f) {
                rainbowColor = new Color(1f, ratio * 5, 0f, 1f);
            } else if (ratio < 0.4f) {
                rainbowColor = new Color(1f - (ratio - 0.2f) * 5, 1f, 0f, 1f);
            } else if (ratio < 0.6f) {
                rainbowColor = new Color(0f, 1f, (ratio - 0.4f) * 5, 1f);
            } else if (ratio < 0.8f) {
                rainbowColor = new Color(0f, 1f - (ratio - 0.6f) * 5, 1f, 1f);
            } else {
                rainbowColor = new Color((ratio - 0.8f) * 5, 0f, 1f, 1f);
            }
            rainbowPixmap.setColor(rainbowColor);
            rainbowPixmap.drawLine(x, 0, x, (int) rainbowBarHeight);
        }
        rainbowBarTexture = new Texture(rainbowPixmap);
        rainbowPixmap.dispose();

        Gdx.input.setInputProcessor(stage);

        // Background
        stage.addActor(new Image(createSolidColorTexture(Color.DARK_GRAY, (int) BASE_WIDTH, (int) BASE_HEIGHT)));

        // Table chứa các thẻ nâng cấp và nút Reroll
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Thêm từng thẻ và nút Reroll bên dưới
        Table cardWithReroll1 = createCardWithReroll(0);
        Table cardWithReroll2 = createCardWithReroll(1);
        Table cardWithReroll3 = createCardWithReroll(2);

        table.add(cardWithReroll1).pad(60 * scaleFactor);
        table.add(cardWithReroll2).pad(60 * scaleFactor);
        table.add(cardWithReroll3).pad(60 * scaleFactor);

        stage.addActor(table);
    }

    private Texture createRerollIcon() {
        Pixmap pixmap = new Pixmap((int) rerollButtonSize, (int) rerollButtonSize, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        pixmap.setColor(Color.WHITE);
        int center = (int) (rerollButtonSize / 2);
        int arrowLength = (int) (rerollButtonSize / 4);
        int arrowSize = (int) (rerollButtonSize / 8);
        pixmap.drawLine(center, center - arrowLength, center + arrowLength, center - arrowLength);
        pixmap.drawLine(center + arrowLength, center - arrowLength, center + arrowLength - arrowSize, center - arrowLength - arrowSize);
        pixmap.drawLine(center + arrowLength, center - arrowLength, center + arrowLength - arrowSize, center - arrowLength + arrowSize);
        pixmap.drawLine(center, center + arrowLength, center + arrowLength, center + arrowLength);
        pixmap.drawLine(center + arrowLength, center + arrowLength, center + arrowLength - arrowSize, center + arrowLength - arrowSize);
        pixmap.drawLine(center + arrowLength, center + arrowLength, center + arrowLength - arrowSize, center + arrowLength + arrowSize);
        pixmap.drawCircle(center, center, (int) (rerollButtonSize / 4));
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Table createCardWithReroll(int initialUpgradeIndex) {
        // Table chính chứa thẻ và nút Reroll
        Table container = new Table();

        // Tạo thẻ nâng cấp
        final Table card = new Table();

        // Tạo nền thẻ với viền gradient và nền đen bằng Pixmap
        Pixmap cardPixmap = new Pixmap((int) cardWidth, (int) cardHeight, Pixmap.Format.RGBA8888);
        cardPixmap.setColor(Color.BLACK);
        cardPixmap.fillRectangle(0, 0, (int) cardWidth, (int) cardHeight);
        cardPixmap.setColor(Color.WHITE);
        Random rand = new Random();
        for (int i = 0; i < 20; i++) { // Giảm số chấm trắng từ 40 xuống 20 để tránh nhiễu chữ
            cardPixmap.drawPixel(rand.nextInt((int) cardWidth), rand.nextInt((int) cardHeight));
        }
        for (int i = 0; i < 5; i++) {
            Color borderColor = new Color(
                    i < 2 ? 1f : 0f,
                    i < 3 ? 1f : 0f,
                    i < 4 ? 1f : 0f,
                    1f
            );
            cardPixmap.setColor(borderColor);
            cardPixmap.drawRectangle(i, i, (int) cardWidth - 2 * i, (int) cardHeight - 2 * i);
        }
        Texture cardTexture = new Texture(cardPixmap);
        cardPixmap.dispose();
        card.setBackground(new Image(cardTexture).getDrawable());

        // Lưu trữ loại nâng cấp hiện tại
        final int[] currentUpgradeIndex = {initialUpgradeIndex};

        // Hình ảnh nâng cấp
        Texture upgradeTexture;
        switch (upgrades[currentUpgradeIndex[0]][2]) {
            case "speed":
                upgradeTexture = speedTexture;
                break;
            case "dash":
                upgradeTexture = dashTexture;
                break;
            case "heart":
            default:
                upgradeTexture = heartTexture;
                break;
        }
        Image icon = new Image(upgradeTexture);

        // Tiêu đề & mô tả
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        Label.LabelStyle descriptionStyle = new Label.LabelStyle(descriptionFont, Color.valueOf("FFFF99")); // Đổi màu chữ mô tả thành vàng nhạt

        Label titleLabel = new Label(upgrades[currentUpgradeIndex[0]][0], titleStyle);
        Label descriptionLabel = new Label(upgrades[currentUpgradeIndex[0]][1], descriptionStyle);
        descriptionLabel.setWrap(true);

        // Thêm ClickListener để chọn nâng cấp khi nhấn vào thẻ
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                applyUpgrade(Integer.parseInt(upgrades[currentUpgradeIndex[0]][3]));
            }
        });

        // Thêm thang ngang màu cầu vồng
        Image rainbowBar = new Image(rainbowBarTexture);

        // Thêm nội dung vào thẻ với kích thước động
        card.add(icon).size(iconSize, iconSize).pad(25 * scaleFactor).row();
        card.add(rainbowBar).width(rainbowBarWidth).height(rainbowBarHeight).pad(15 * scaleFactor).row();
        card.add(titleLabel).pad(15 * scaleFactor).row();
        card.add(descriptionLabel).width(rainbowBarWidth).height(descriptionHeight).pad(15 * scaleFactor).row();

        // Nút Reroll
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = descriptionFont;
        final TextButton rerollButton = new TextButton("", buttonStyle);
        rerollButton.setBackground(new Image(rerollButtonTexture).getDrawable());
        rerollButton.setSize(rerollButtonSize, rerollButtonSize);
        rerollButton.add(new Image(rerollIconTexture));

        // Thêm hiệu ứng sáng lên khi nhấn
        rerollButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                rerollButton.setBackground(new Image(rerollButtonHighlightTexture).getDrawable());
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                rerollButton.setBackground(new Image(rerollButtonTexture).getDrawable());
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                Random random = new Random();
                int newIndex;
                do {
                    newIndex = random.nextInt(upgrades.length);
                } while (newIndex == currentUpgradeIndex[0]);

                currentUpgradeIndex[0] = newIndex;

                Texture newUpgradeTexture;
                switch (upgrades[currentUpgradeIndex[0]][2]) {
                    case "speed":
                        newUpgradeTexture = speedTexture;
                        break;
                    case "dash":
                        newUpgradeTexture = dashTexture;
                        break;
                    case "heart":
                    default:
                        newUpgradeTexture = heartTexture;
                        break;
                }
                icon.setDrawable(new Image(newUpgradeTexture).getDrawable());
                titleLabel.setText(upgrades[currentUpgradeIndex[0]][0]);
                descriptionLabel.setText(upgrades[currentUpgradeIndex[0]][1]);
            }
        });

        // Thêm thẻ và nút Reroll vào container
        container.add(card).row();
        container.add(rerollButton).size(rerollButtonSize, rerollButtonSize).padTop(25 * scaleFactor).row();

        return container;
    }

    private Texture createSolidColorTexture(Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void applyUpgrade(int type) {
        switch (type) {
            case 1:
                playerTank.applyUpgrade(0, 0, 50f); // Speed
                break;
            case 2:
                playerTank.applyUpgrade(1, 0, 0); // Dash
                break;
            case 3:
                playerTank.applyUpgrade(0, 1, 0); // Health
                break;
        }
        returnToLevel();
    }

    private void returnToLevel() {
        Gdx.input.setInputProcessor(null);
        switch (currentLevel) {
            case 1:
                game.setScreen(new Level2Screen(game, playerTank));
                break;
            case 2:
                game.setScreen(new Level3Screen(game, playerTank));
                break;
            default:
                game.setScreen(new Level1Screen(game, playerTank));
                break;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        titleFont.dispose();
        descriptionFont.dispose();
        speedTexture.dispose();
        dashTexture.dispose();
        heartTexture.dispose();
        rerollIconTexture.dispose();
        rerollButtonTexture.dispose();
        rerollButtonHighlightTexture.dispose();
        rainbowBarTexture.dispose();
    }
}
