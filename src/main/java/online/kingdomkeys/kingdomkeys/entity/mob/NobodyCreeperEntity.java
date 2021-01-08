package online.kingdomkeys.kingdomkeys.entity.mob;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.entity.EntityHelper;
import online.kingdomkeys.kingdomkeys.entity.ModEntities;
import online.kingdomkeys.kingdomkeys.entity.mob.goal.ShadowGoal;

public class NobodyCreeperEntity extends CreatureEntity implements IKHMob {

    public NobodyCreeperEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public NobodyCreeperEntity(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        super(ModEntities.TYPE_NOBODY_CREEPER.get(), world);
    }
    
    @Override
    public boolean canSpawn(IWorld worldIn, SpawnReason spawnReasonIn) {
    	return ModCapabilities.getWorld((World)worldIn).getHeartlessSpawnLevel() > 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(0, new NobodyCreeperGoal(this));
        this.goalSelector.addGoal(1, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MobEntity.registerAttributes()
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 35.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.17D)
                .createMutableAttribute(Attributes.MAX_HEALTH, 40.0D)
                .createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 1000.0D)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 0.0D)
                ;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 4;
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(EntityHelper.STATE, 0);
    }

    @Override
    public EntityHelper.MobType getMobType() {
        return EntityHelper.MobType.NOBODY;
    }

    @Override
    public boolean onLivingFall(float distance, float damageMultiplier) {
        return false;
    }

    class NobodyCreeperGoal extends Goal {
        private NobodyCreeperEntity theEntity;	// the entity who holds this AI
        private boolean canUseAttack = true;	// true/false value to decide if we can use this ability or if the cooldown starts
        private int attackTimer = 5, whileAttackTimer; // attackTimer is the cooldown while whileAttackTimer is the amount of ticks the attack runned for
        private double[] posToFall; // an double array that holds the pos values (x,y,z) for where to drop the entity

        public NobodyCreeperGoal(NobodyCreeperEntity e)
        {
            this.theEntity = e;
        }

        /*
            Should be self-explanatory but:
            - return false means this AI would not be executed, return true means it will be
            - First we check if the target is not null (most important thing cuz otherwise why would we need this AI ?)
            - we then check if the AI is usable, if yes we start the AI, otherwise we start the "cooldown"..aka this line : attackTimer--
            Assuming we return true startExecuting() will be called next
         */
        @Override
        public boolean shouldExecute()
        {
            if(theEntity.getAttackTarget() != null)
            {
                if(!canUseAttack)
                {
                    if(attackTimer > 0)
                    {
                        attackTimer--;
                        return false;
                    }
                    else return true;
                }
                else return true;
            }
            else return false;
        }

        /*
            "Legacy code" from Shadows and older entities but basically if the ability is usable it's execution is not interrupted
         */
        @Override
        public boolean shouldContinueExecuting()
        {
            boolean flag = canUseAttack;

            return flag;
        }

        /*
            This is the initialization part after each cooldown

            It's also where we initialize where the pos for the spear attack will be placed
         */
        @Override
        public void startExecuting() {
            canUseAttack = true;
            if(EntityHelper.getState(theEntity) > 2)
                attackTimer = 10 + world.rand.nextInt(5);
            else
                attackTimer = 20 + world.rand.nextInt(5);
            EntityHelper.setState(theEntity, 0);
            this.theEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.17D);
            whileAttackTimer = 0;
        }

        /*
            The actual meat of this AI
         */
        @Override
        public void tick() {
            if(theEntity.getAttackTarget() != null && canUseAttack) { // Like above we check again to see if the target is still alive (maybe it died so we need to check again)
                whileAttackTimer++;
                LivingEntity target = this.theEntity.getAttackTarget(); // Creates a new variable that holds the target

                if(EntityHelper.getState(theEntity) == 0) { // if the state of the entity is 0 (meaning it does not executes any attack)
                    this.theEntity.getLookController().setLookPositionWithEntity(target, 30F, 30F); // we turn the entity to face the target

                    if(world.rand.nextInt(100) + world.rand.nextDouble() <= 75) { // some sort of primitive (could've looked better) percentage system..but if the random number is under or equal with 75 (so a 75% chance)
                        //MORPHING PHASE
                        if(world.rand.nextInt(100) + world.rand.nextDouble() <= 50) { // again but for another randomized number to see which morph to run, there's a 50/50 chance for both
                            //SWORD
                            if(theEntity.getDistance(theEntity.getAttackTarget()) < 8) { // for the sword one we need to check if the target is 4 blocks or less away from the entity (just because it wouldn't make much sense for a close-ranged attack to occur when the target is 5 miles away)
                                EntityHelper.setState(theEntity, 1); // setting the state to 1 (sword morphing)

		            			/*
		            			   Kinda optional for this one but I thought it's a nice touch...we set the movement speed and attack damage to 0
		            			   But why the attack damage ? Becase we don't want the use to be hit by the actual entity being to close (vanilla attack, which deals 1 heart)
		            			   we want to deal special damage (4 hearts) to every entity around 2 block
		            			 */
                                this.theEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);

                                for(LivingEntity enemy : EntityHelper.getEntitiesNear(this.theEntity, 4))
                                    enemy.attackEntityFrom(DamageSource.causeMobDamage(this.theEntity), 8);
                            } else {
                                return;
                            }
                        } else {
                            //SPEAR
	            			/*
	            			 Same as with the sword, the only difference being we move the entity 4 blocks above the target location (for that sweet "falling from sky" effect)
	            			 Also deals only 3 hearts for entities around 2 blocks around it (cuz it's a spear not a sword, smaller range)
	            			 */
                            EntityHelper.setState(theEntity, 2);
                            this.theEntity.setPositionAndUpdate(target.getPosX(), target.getPosY() + 4, target.getPosZ());
                            this.theEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);

                            for(LivingEntity enemy : EntityHelper.getEntitiesNear(this.theEntity, 3))
                                enemy.attackEntityFrom(DamageSource.causeMobDamage(this.theEntity), 6);
                        }
                    }
                    else {
                        if(theEntity.getDistance(theEntity.getAttackTarget()) < 5) {
                            //LEG SWIPE
                            EntityHelper.setState(theEntity, 3);

                            this.theEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);

                            for(LivingEntity enemy : EntityHelper.getEntitiesNear(this.theEntity, 2.5))
                                enemy.attackEntityFrom(DamageSource.causeMobDamage(this.theEntity), 4);
                        }
                        else
                            return;
                    }

                }

                if(EntityHelper.getState(theEntity) == 1 && whileAttackTimer > 20) { // special check if the sword AI is active and if it's been more than 1 second since the attack started,
                // did this because I want the sword attack to last 1 second and the other 2 attacks more than 1 second
        			/*
        			 	start the cooldown and reset the entity attributes
        			 */
                    canUseAttack = false;
                    EntityHelper.setState(theEntity, 0);
                    this.theEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.17D);
                }
                else if(EntityHelper.getState(theEntity) != 1 && whileAttackTimer > 30)
                {
                    canUseAttack = false;
                    EntityHelper.setState(theEntity, 0);
                    this.theEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.17D);
                }
            }
        }

    }
}
