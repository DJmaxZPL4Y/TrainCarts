package com.bergerkiller.bukkit.tc.rails.type;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.common.utils.MaterialUtil;
import com.bergerkiller.bukkit.tc.Util;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.rails.logic.RailLogic;
import com.bergerkiller.bukkit.tc.rails.logic.RailLogicHorizontal;

public class RailTypeCrossing extends RailTypeHorizontal {

	@Override
	public boolean isRail(int typeId, int data) {
		return MaterialUtil.ISPRESSUREPLATE.get(typeId);
	}

	@Override
	public BlockFace[] getPossibleDirections(Block trackBlock) {
		BlockFace dir = getDirection(trackBlock);
		if (dir == BlockFace.SELF) {
			return FaceUtil.RADIAL;
		} else {
			return RailTypeRegular.getPossibleDirections(dir);
		}
	}

	@Override
	public BlockFace getDirection(Block railsBlock) {
		return Util.getPlateDirection(railsBlock);
	}

	@Override
	public RailLogic getLogic(MinecartMember<?> member, Block railsBlock) {
		// Get the direction of the rails to find out the logic to use
		BlockFace dir = Util.getPlateDirection(railsBlock);
		if (dir == BlockFace.SELF) {
			//set track direction based on direction of this cart
			dir = FaceUtil.toRailsDirection(member.getDirectionTo());
		}
		return RailLogicHorizontal.get(dir);
	}
}
