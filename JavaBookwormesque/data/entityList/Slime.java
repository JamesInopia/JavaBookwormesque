package data.entityList;
import data.classes.Enemy;
import data.classes.Move;
import java.util.Arrays;

public class Slime extends Enemy {
    public Slime() {
        super("Slime", "enemy", 25,"data/entityImages/slime.png",
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
