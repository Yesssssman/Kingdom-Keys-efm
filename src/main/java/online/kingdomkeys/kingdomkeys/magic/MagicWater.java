package online.kingdomkeys.kingdomkeys.magic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.entity.magic.WaterEntity;
import online.kingdomkeys.kingdomkeys.entity.magic.WateraEntity;
import online.kingdomkeys.kingdomkeys.entity.magic.WatergaEntity;
import online.kingdomkeys.kingdomkeys.entity.magic.WaterzaEntity;
import online.kingdomkeys.kingdomkeys.lib.Strings;

public class MagicWater extends Magic {

	public MagicWater(ResourceLocation registryName, int maxLevel, String gmAbility, int order) {
		super(registryName, false, maxLevel, gmAbility, order);
	}

	@Override
	protected void magicUse(Player player, Player caster, int level, float fullMPBlastMult) {
		float dmgMult = getDamageMult(level) + ModCapabilities.getPlayer(player).getNumberOfAbilitiesEquipped(Strings.waterBoost) * 0.2F;
		dmgMult *= fullMPBlastMult;

		switch(level) {
		case 0:
			WaterEntity water = new WaterEntity(player.level, player, dmgMult);
			water.setCaster(player.getDisplayName().getString());
			player.level.addFreshEntity(water);
			player.level.playSound(null, player.position().x(),player.position().y(),player.position().z(), SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 1F, 1F);
			break;
		case 1:
			WateraEntity watera = new WateraEntity(player.level, player, dmgMult);
			watera.setCaster(player.getDisplayName().getString());
			player.level.addFreshEntity(watera);
			player.level.playSound(null, player.position().x(),player.position().y(),player.position().z(), SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 1F, 1F);
			break;
		case 2:
			WatergaEntity waterga = new WatergaEntity(player.level, player, dmgMult);
			waterga.setCaster(player.getDisplayName().getString());
			player.level.addFreshEntity(waterga);
			player.level.playSound(null, player.position().x(),player.position().y(),player.position().z(), SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 1F, 1F);
			break;
		case 3:
			WaterzaEntity waterza = new WaterzaEntity(player.level, player, dmgMult);
			waterza.setCaster(player.getDisplayName().getString());
			player.level.addFreshEntity(waterza);
			player.level.playSound(null, player.position().x(),player.position().y(),player.position().z(), SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 1F, 1F);
			break;
		}
		
		if(player.isOnFire()) {
			player.clearFire();
		}
	}

}
