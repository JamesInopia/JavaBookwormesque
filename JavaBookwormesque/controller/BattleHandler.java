package controller;
import model.entity.Player;
import view.GameUI;

public class BattleHandler extends TurnHandler {
    // Passes the data for player/enemy
    public BattleHandler(GameUI ui, WaveHandler waves, Player hero, Runnable onBattleEnd) {
        super(ui, waves, hero, onBattleEnd);
    }
}
