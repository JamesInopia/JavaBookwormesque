package controller;
import java.util.List;
import model.entity.Enemy;
import model.entity.Player;
import view.GameUI;

public class BattleHandler extends TurnHandler {
    private final Player hero;
    private final Enemy enemy;

    // Passes the data for player/enemy
    public BattleHandler(GameUI ui, List<String> enemies, Player hero, Enemy enemy) {
        super(ui, enemies, hero);
        this.hero = hero;
        this.enemy = enemy;
    }

    // Handles action enemy will perform and checks if player has been defeated
    @Override
    protected void enemyTurn() {
        showTurnOverlay("Opponent's Turn", () -> {
            enemy.performMove(hero);

            getUI().updatePlayerHP(hero.getCurrentHP());

            if (!hero.isAlive()) {
                System.out.println(hero.getName() + " has been defeated!");
                getUI().showDefeatOverlay(hero.getName());
                return;
            }
            nextPlayerTurn();
        });
    }
}
