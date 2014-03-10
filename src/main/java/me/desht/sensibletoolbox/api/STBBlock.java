package me.desht.sensibletoolbox.api;

import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.STBFreezable;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.STBGUIHolder;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.RelativePosition;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface STBBlock {
	public RedstoneBehaviour getRedstoneBehaviour();

	void setRedstoneBehaviour(RedstoneBehaviour redstoneBehaviour);

	public AccessControl getAccessControl();

	void setAccessControl(AccessControl accessControl);

	public InventoryGUI getGUI();

	public BlockFace getFacing();

	void setFacing(BlockFace facing);

	public UUID getOwner();

	void setOwner(UUID owner);

	public RelativePosition[] getBlockStructure();

	public Location getLocation();

	public PersistableLocation getPersistableLocation();

	void moveTo(Location oldLoc, Location newLoc);

	void updateBlock(boolean redraw);

	public PistonMoveReaction getPistonMoveReaction();

	/**
	 * Check if this block may be interacted with by the given player, based on its
	 * security settings.
	 *
	 * @param player the player to check
	 * @return true if the block may be accessed
	 */
	public boolean hasAccessRights(Player player);
}