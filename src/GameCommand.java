import bwapi.Game;
import bwapi.Player;

public interface GameCommand {
	boolean canExecute (Game game, Player player);
	void execute (Game game, Player player);
}
