package dev.tucanu.wordle.item.custom.wordle;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tucanu.wordle.Wordle;
import dev.tucanu.wordle.util.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class WordleScreen extends AbstractContainerScreen<WordleMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Wordle.MOD_ID, "textures/gui/container/wordle.png");
    private final StringBuilder currentGuess = new StringBuilder();
    private final int cols = 5;
    private Button shareButton;
    private Button leaderboardButton;

    // Flags
    private boolean invalidGuess = false;
    private boolean showingLeaderboard = false;

    public WordleScreen(WordleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 199;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 120;
        int buttonHeight = 20;

        // Share button
        shareButton = Button.builder(Component.literal("Share Results"), b -> shareToChat())
                .bounds(0, 0, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.literal("Share your results with everyone else.")))
                .build();
        shareButton.visible = false;

        // Leaderboard button
        leaderboardButton = Button.builder(Component.literal("Leaderboard"), b -> openLeaderboard())
                .bounds(0, 0, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.literal("See today's leaderboard.")))
                .build();

        addRenderableWidget(shareButton);
        addRenderableWidget(leaderboardButton);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {

        int bgWidth = 256;
        int bgHeight = 199;
        int startX = (this.width - bgWidth) / 2;
        int startY = (this.height - bgHeight) / 2;

        // Draw background
        RenderSystem.setShaderTexture(0, TEXTURE);
        g.blit(TEXTURE, startX, startY, 0, 0, bgWidth, bgHeight);

        // Wordle grid
        int cellSize = 18;
        int gridStartX = startX + (bgWidth - (cols * cellSize)) / 2;
        int gridStartY = startY + 14;
        int rows = 6;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = gridStartX + col * cellSize;
                int y = gridStartY + row * cellSize;

                if (row < menu.getGuesses().size()) {
                    String guess = menu.getGuesses().get(row);
                    WordleLogic.LetterColor[] colors = WordleLogic.checkGuess(guess, menu.getTargetWord());

                    int fillTop, fillBottom;
                    switch (colors[col]) {
                        case GREEN -> { fillTop = 0xAA2ECC71; fillBottom = 0xAA27AE60; }
                        case YELLOW -> { fillTop = 0xAAFFF1C4; fillBottom = 0xAAD4AC0D; }
                        default -> { fillTop = 0x00000000; fillBottom = 0x00000000; }
                    }

                    g.fillGradient(x, y, x + cellSize, y + cellSize, fillTop, fillBottom);
                    drawShadowedCentered(g, String.valueOf(guess.charAt(col)), x + cellSize / 2, y + cellSize / 2 - 4);
                } else if (menu.getState() == WordleMenu.GameState.PLAYING && row == menu.getCurrentRow()) {
                    if (col < currentGuess.length()) {
                        g.fillGradient(x, y, x + cellSize, y + cellSize, 0, 0);
                        drawShadowedCentered(g, String.valueOf(currentGuess.charAt(col)), x + cellSize / 2, y + cellSize / 2 - 4);
                    } else {
                        g.fillGradient(x, y, x + cellSize, y + cellSize, 0, 0);
                    }
                }
            }
        }

        // Red border for invalid guess
        if (invalidGuess) {
            int borderY = gridStartY + menu.getCurrentRow() * cellSize;
            int borderWidth = cols * cellSize;
            int red = 0xFFFF0000;

            g.fill(gridStartX - 1, borderY - 1, gridStartX + borderWidth + 1, borderY, red);
            g.fill(gridStartX - 1, borderY + cellSize, gridStartX + borderWidth + 1, borderY + cellSize + 1, red);
            g.fill(gridStartX - 1, borderY - 1, gridStartX, borderY + cellSize + 1, red);
            g.fill(gridStartX + borderWidth, borderY - 1, gridStartX + borderWidth + 1, borderY + cellSize + 1, red);
        }

        // Keyboard
        int keyboardY = startY + 158 - 24;
        renderKeyboard(g, gridStartX, keyboardY, cellSize);

        // Buttons
        boolean gameOver = menu.getState() != WordleMenu.GameState.PLAYING;
        shareButton.visible = gameOver;
        leaderboardButton.visible = true;

        shareButton.setX(startX + bgWidth / 2 - shareButton.getWidth());
        shareButton.setY(startY - shareButton.getHeight() - 5);

        leaderboardButton.setX(shareButton.getX() + shareButton.getWidth() + 10);
        leaderboardButton.setY(shareButton.getY());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTicks);
    }


    private void shareToChat() {
        if (minecraft == null || minecraft.player == null) return;

        String msg = generateShareMessage();
        ModNetwork.INSTANCE.sendToServer(new WordleSharePacket(msg));
        playSound(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F);
    }

    private String generateShareMessage() {
        int guessCount = menu.getGuesses().size();
        boolean won = menu.getState() == WordleMenu.GameState.WON;

        String playerName = minecraft.player != null ? minecraft.player.getName().getString() : "Player";

        if (won) {
            return "§r" + playerName + " §asolved today's Wordle in §e" + guessCount + "/6 " + getGuessSymbol(guessCount);
        } else {
            return "§r" + playerName + " §ccouldn't solve today's Wordle!";
        }
    }

    private void openLeaderboard() {
        if (minecraft != null && minecraft.player != null) {
            ModNetwork.INSTANCE.sendToServer(new WordleLeaderboardRequestPacket());
            playSound(SoundEvents.UI_LOOM_SELECT_PATTERN);
        }
    }


    private String getGuessSymbol(int guessCount) {
        return switch (guessCount) {
            case 1 -> " §6★";
            case 2 -> " §a✦";
            case 3 -> " §b♦";
            case 4 -> " §e●";
            case 5 -> " §7○";
            case 6 -> " §c~";
            default -> "";
        };
    }

    private void drawShadowedCentered(GuiGraphics g, String text, int x, int y) {
        g.drawCenteredString(font, text, x + 1, y + 1, 0xFF000000);
        g.drawCenteredString(font, text, x, y, -1);
    }

    private void renderKeyboard(GuiGraphics g, int baseX, int baseY, int keySize) {
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};

        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int rowWidth = row.length() * keySize;
            int rowX = baseX + (cols * keySize - rowWidth) / 2;
            int rowY = baseY + r * (keySize + 2);

            for (int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);
                int x = rowX + i * keySize;

                int fillTop = 0x00000000;
                int fillBottom = 0x00000000;

                for (String guess : menu.getGuesses()) {
                    if (guess.indexOf(c) != -1) {
                        WordleLogic.LetterColor[] colors = WordleLogic.checkGuess(guess, menu.getTargetWord());
                        for (int j = 0; j < guess.length(); j++) {
                            if (guess.charAt(j) == c) {
                                switch (colors[j]) {
                                    case GREEN -> { fillTop = 0xAA2ECC71; fillBottom = 0xAA27AE60; }
                                    case YELLOW -> { fillTop = 0xAAFFF1C4; fillBottom = 0xAAD4AC0D; }
                                    case GRAY -> { fillTop = 0xAA444444; fillBottom = 0xAA222222; }
                                }
                            }
                        }
                    }
                }

                g.fillGradient(x, rowY, x + keySize, rowY + keySize, fillTop, fillBottom);
                drawShadowedCentered(g, String.valueOf(c), x + keySize / 2, rowY + keySize / 2 - 4);
            }
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) { return false; }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showingLeaderboard) {
            // ESC closes leaderboard
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                showingLeaderboard = false;
                return true;
            }
            return true; // block input while leaderboard is visible
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        if (menu.getState() != WordleMenu.GameState.PLAYING) {
            return keyCode == GLFW.GLFW_KEY_E;
        }

        // Backspace
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!currentGuess.isEmpty()) {
                currentGuess.deleteCharAt(currentGuess.length() - 1);
                invalidGuess = false;
                playSound(SoundEvents.UI_LOOM_TAKE_RESULT);
            }
            return true;
        }

        // Enter
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (currentGuess.length() == cols && minecraft.player != null) {
                boolean valid = menu.submitGuess(currentGuess.toString(), minecraft.player);
                if (valid) {
                    currentGuess.setLength(0);
                    invalidGuess = false;
                } else {
                    invalidGuess = true;
                    playSound(SoundEvents.VINDICATOR_HURT, 0.4F);
                }
            }
            return true;
        }

        // Letters A..Z
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            if (currentGuess.length() < cols) {
                char ch = (char) ('A' + (keyCode - GLFW.GLFW_KEY_A));
                currentGuess.append(ch);
                invalidGuess = false;
                playSound(SoundEvents.UI_LOOM_SELECT_PATTERN);
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void playSound(SoundEvent sound, float volume) {
        if (minecraft.player != null)
            minecraft.player.playSound(sound, volume, 1.0F);
    }

    private void playSound(SoundEvent sound) { playSound(sound, 0.3F); }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) { }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
