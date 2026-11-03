package model.entity.EnemyList;

import java.util.Arrays;
import model.entity.Enemy;
import model.entity.Move;

public class Slime extends Enemy {
    public Slime() {
        super("Slime", "enemy", 25, "data/entityImages/slime.png",
              Arrays.asList(
                  new Move("Tackle", 5)
              )
        );
    }

    @Override
    public void intro() {
        System.out.println("Splat!");
    }
}
