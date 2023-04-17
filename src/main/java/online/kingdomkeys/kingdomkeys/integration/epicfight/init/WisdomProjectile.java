package online.kingdomkeys.kingdomkeys.integration.epicfight.init;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.entity.organization.ArrowgunShotEntity;
import online.kingdomkeys.kingdomkeys.lib.DamageCalculation;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.swing.text.html.parser.Entity;

public class WisdomProjectile {

    public static void shoot(LivingEntityPatch ep, Joint joint)
    {

        // gets the positions of the bone somehow
        Vec3 playerPos = ep.getOriginal().position();
        Player player = (Player) ep.getOriginal();
        Level world = player.level;
        OpenMatrix4f tempMatrix = OpenMatrix4f.createTranslation((float) playerPos.x, (float) playerPos.y, (float) playerPos.z)
                .mulBack(OpenMatrix4f.createRotatorDeg(180, Vec3f.Y_AXIS)).mulBack(ep.getModelMatrix(1));
        Vec3 test2 = OpenMatrix4f.transform(Armatures.BIPED.getBindedTransformFor(ep.getArmature().getPose(1)
                , joint).mulFront(tempMatrix) , Vec3.ZERO);

        ArrowgunShotEntity shot = new ArrowgunShotEntity(player.level, player, DamageCalculation.getMagicDamage(player) * 0.1F,test2.x, test2.y, test2.z );
        shot.setShotType(1);
        shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 3F, 0);
        world.addFreshEntity(shot);
        player.level.playSound(null, player.blockPosition(), ModSounds.wisdom_shot.get(), SoundSource.PLAYERS, 1F, 1F);
    }

}
