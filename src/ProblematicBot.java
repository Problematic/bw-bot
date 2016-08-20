import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class ProblematicBot extends DefaultBWListener {
	private Mirror mirror = new Mirror();
	private Game game;
	private Player self;

	private GameCommand[] buildCommands = {
		new BuildUnitCommand(UnitType.Zerg_Spawning_Pool),
		new BuildUnitCommand(UnitType.Zerg_Drone),
		new BuildUnitCommand(UnitType.Zerg_Zergling),
		new BuildUnitCommand(UnitType.Zerg_Zergling),
		new BuildUnitCommand(UnitType.Zerg_Zergling),
		new BuildUnitCommand(UnitType.Zerg_Drone),
		new BuildUnitCommand(UnitType.Zerg_Drone),
		new BuildUnitCommand(UnitType.Zerg_Overlord),
		new BuildUnitCommand(UnitType.Zerg_Hatchery),
		new BuildUnitCommand(UnitType.Zerg_Zergling),
		new BuildUnitCommand(UnitType.Zerg_Zergling),
		new BuildUnitCommand(UnitType.Zerg_Zergling),
		new BuildUnitCommand(UnitType.Zerg_Extractor),
		new BuildUnitCommand(UnitType.Zerg_Drone),
		new BuildUnitCommand(UnitType.Zerg_Drone),
		new BuildUnitCommand(UnitType.Zerg_Drone),
		new BuildUnitCommand(UnitType.Zerg_Lair)
	};

	private Queue<GameCommand> buildQueue = new ArrayDeque<>(Arrays.asList(buildCommands));

	private void run () {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}

	@Override
	public void onUnitCreate(Unit unit) {
		System.out.println("New unit discovered: " + unit.getType());
	}

	@Override
	public void onStart () {
		game = mirror.getGame();
		self = game.self();

		System.out.println("Analyzing map...");
		BWTA.readMap();
		BWTA.analyze();
		System.out.println("Map data ready");
	}

	@Override
	public void onFrame() {
		if (buildQueue.peek().canExecute(game, self)) {
			buildQueue.remove().execute(game, self);
		}

		for (Unit unit : self.getUnits()) {
			if (unit.getType().isWorker() && unit.isIdle()) {
				Unit closestMineral = null;

				for (Unit neutralUnit : game.neutral().getUnits()) {
					if (neutralUnit.getType().isMineralField()) {
						if (closestMineral == null || unit.getDistance(neutralUnit) < unit.getDistance(closestMineral)) {
							closestMineral = neutralUnit;
						}
					}
				}

				if (closestMineral != null) {
					unit.gather(closestMineral, false);
				}
			}

			game.drawLineMap(unit.getPosition(), unit.getOrderTargetPosition(), Color.Yellow);
		}
	}

	public static void main (String[] args) {
		new ProblematicBot().run();
	}
}
