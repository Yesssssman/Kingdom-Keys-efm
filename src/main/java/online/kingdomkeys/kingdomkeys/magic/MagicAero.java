package online.kingdomkeys.kingdomkeys.magic;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.util.Hand;
import online.kingdomkeys.kingdomkeys.capability.IPlayerCapabilities;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.entity.magic.BlizzardEntity;
import online.kingdomkeys.kingdomkeys.entity.magic.FireEntity;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;

public class MagicAero extends Magic {
	String name;

	public MagicAero(String registryName, int cost, int order) {
		super(registryName, cost, true, order);
		this.name = registryName;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onUse(PlayerEntity player) {
		//TODO Equip aero shield
		IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
		playerData.setAeroTicks(400);
		PacketHandler.syncToAllAround(player, playerData);
		player.swingArm(Hand.MAIN_HAND);

	}

}
