package model.entity.EnemyList;

import java.util.Arrays;
import model.entity.Enemy;
import model.entity.Move;

public class Wolf extends Enemy {
    public Wolf() {
        super("Wolf", "enemy", 35, "public/entityImages/wolf.png",
              Arrays.asList(
                  new Move("Bite", 10)
              )
        );
    }

    @Override
    public void intro() {
        System.out.println("Splat!");
    }
}
