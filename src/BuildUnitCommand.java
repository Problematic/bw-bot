import bwapi.*;

public class BuildUnitCommand implements GameCommand {
	private UnitType unitType;

	public BuildUnitCommand (UnitType unitType) {
		this.unitType = unitType;
	}

	public boolean canExecute (Game game, Player player) {
		return player.hasUnitTypeRequirement(unitType) && player.minerals() >= unitType.mineralPrice() && player.gas() >= unitType.gasPrice() && (player.supplyTotal() - player.supplyUsed()) > unitType.supplyRequired();
	}

	public void execute (Game game, Player player) {
		for (Unit unit : player.getUnits()) {
			if (unit.getType() == unitType.whatBuilds().first) {
				if (unitType.isBuilding()) {
					TilePosition buildTile = getBuildTile(game, unit, unitType, player.getStartLocation());
					if (buildTile != null) {
						unit.build(unitType, buildTile);
					}
				} else {
					unit.build(unitType);
				}
			}
		}
	}

	private TilePosition getBuildTile(Game game, Unit builder, UnitType buildingType, TilePosition aroundTile) {
		TilePosition ret = null;
		int maxDist = 3;
		int stopDist = 40;

		// Refinery, Assimilator, Extractor
		if (buildingType.isRefinery()) {
			for (Unit n : game.neutral().getUnits()) {
				if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
					( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
					( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
					) return n.getTilePosition();
			}
		}

		while ((maxDist < stopDist) && (ret == null)) {
			for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
				for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
					if (game.canBuildHere(new TilePosition(i,j), buildingType, builder, false)) {
						// units that are blocking the tile
						boolean unitsInWay = false;
						for (Unit u : game.getAllUnits()) {
							if (u.getID() == builder.getID()) continue;
							if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
						}
						if (!unitsInWay) {
							return new TilePosition(i, j);
						}
						// creep for Zerg
						if (buildingType.requiresCreep()) {
							for (int k=i; k<=i+buildingType.tileWidth(); k++) {
								for (int l=j; l<=j+buildingType.tileHeight(); l++) {
									if (!game.hasCreep(k, l)) {
										break;
									}
								}
							}
						}
					}
				}
			}
			maxDist += 2;
		}

		if (ret == null) game.printf("Unable to find suitable build position for "+buildingType.toString());
		return ret;
	}
}
