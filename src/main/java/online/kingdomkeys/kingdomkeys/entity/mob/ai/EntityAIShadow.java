package online.kingdomkeys.kingdomkeys.entity.mob.ai;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import online.kingdomkeys.kingdomkeys.entity.EntityHelper;

public class EntityAIShadow extends TargetGoal {
	// 1 - in Shadow ; 0 - in Overworld

	private final int MAX_DISTANCE_FOR_AI = 100, MAX_DISTANCE_FOR_LEAP = 10, MAX_DISTANCE_FOR_DASH = 25, MAX_DISTANCE_FOR_ATTACK = 5, TIME_BEFORE_NEXT_ATTACK = 70, TIME_OUTSIDE_THE_SHADOW = 70;
	private int outsideShadowMaxTicks = 70, oldAi = -1, ticksUntilNextAttack;
	private boolean canUseNextAttack = true;

	public EntityAIShadow(CreatureEntity creature) {
		super(creature, true);
		ticksUntilNextAttack = TIME_BEFORE_NEXT_ATTACK;
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.goalOwner.getAttackTarget() != null && this.goalOwner.getDistanceSq(this.goalOwner.getAttackTarget()) < MAX_DISTANCE_FOR_AI) {

			/*
			 * if(EntityHelper.getState(goalOwner) == 1) System.out.println("" +
			 * EntityHelper.getState(goalOwner));
			 */

			if (!this.goalOwner.onGround) {
				EntityHelper.setState(this.goalOwner, 0);
				this.goalOwner.setInvulnerable(false);
			} else {
				if (!isInShadow()) {
					outsideShadowMaxTicks--;
					if (outsideShadowMaxTicks <= 0) {
						EntityHelper.setState(this.goalOwner, 1);
						outsideShadowMaxTicks = TIME_OUTSIDE_THE_SHADOW;
						canUseNextAttack = false;
					}
				} else {
					this.goalOwner.setInvisible(false);
				}
			}

			if (isInShadow()) {
				this.goalOwner.setInvulnerable(true);
				this.goalOwner.setInvisible(true);
				canUseNextAttack = false;
				outsideShadowMaxTicks++;
				if (outsideShadowMaxTicks >= TIME_OUTSIDE_THE_SHADOW) {
					EntityHelper.setState(this.goalOwner, 0);
					this.goalOwner.setInvulnerable(false);
					this.goalOwner.setInvisible(false);
					canUseNextAttack = true;
				}
			}

			EntityHelper.Dir dir = EntityHelper.get8Directions(this.goalOwner);
			int currentAi = this.goalOwner.world.rand.nextInt(2);

			if (!canUseNextAttack) {
				ticksUntilNextAttack--;
				if (ticksUntilNextAttack <= 0) {
					canUseNextAttack = true;
					ticksUntilNextAttack = TIME_BEFORE_NEXT_ATTACK;
				}
			}

			if (oldAi != -1 && canUseNextAttack) {
				if (currentAi == 0 && oldAi == 0)
					currentAi = 1;
				if (currentAi == 1 && oldAi == 1)
					currentAi = 0;
			}

			// Leaping
			if (this.goalOwner.onGround && this.goalOwner.getDistanceSq(this.goalOwner.getAttackTarget()) <= MAX_DISTANCE_FOR_LEAP && currentAi == 0 && canUseNextAttack) {
				oldAi = 0;

				this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0.5, 0));

				if (dir == EntityHelper.Dir.NORTH)
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, -0.7));
				if (dir == EntityHelper.Dir.NORTH_WEST) {
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, -0.7));
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(-0.7, 0, 0));
				}
				if (dir == EntityHelper.Dir.SOUTH)
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, 0.7));
				if (dir == EntityHelper.Dir.NORTH_EAST) {
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, -0.7));
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0.7, 0, 0));
				}
				if (dir == EntityHelper.Dir.WEST)
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(-0.7, 0, 0));
				if (dir == EntityHelper.Dir.SOUTH_WEST) {
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, 0.7));
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(-0.7, 0, 0));
				}
				if (dir == EntityHelper.Dir.EAST)
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0.7, 0, 0));
				if (dir == EntityHelper.Dir.SOUTH_EAST) {
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, 0.7));
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0.7, 0, 0));
				}

				if (this.goalOwner.world.rand.nextInt(2) == 0) {
					EntityHelper.setState(this.goalOwner, 0);
					this.goalOwner.setInvulnerable(false);
				} else {
					EntityHelper.setState(this.goalOwner, 1);
					this.goalOwner.setInvulnerable(true);
				}

				canUseNextAttack = false;
			}

			// Dash
			if (this.goalOwner.onGround && this.goalOwner.getDistanceSq(this.goalOwner.getAttackTarget()) <= MAX_DISTANCE_FOR_DASH && currentAi == 1 && canUseNextAttack) {
				oldAi = 1;

				this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0.2, 0));

				if (dir == EntityHelper.Dir.NORTH)
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, -1));
				if (dir == EntityHelper.Dir.NORTH_WEST) {
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, -1));
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(-1, 0, 0));
				}
				if (dir == EntityHelper.Dir.SOUTH)
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, 1));
				if (dir == EntityHelper.Dir.NORTH_EAST) {
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, -1));
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(1, 0, 0));
				}
				if (dir == EntityHelper.Dir.WEST)
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(-1, 0, 0));
				if (dir == EntityHelper.Dir.SOUTH_WEST) {
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, 1));
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(-1, 0, 0));
				}
				if (dir == EntityHelper.Dir.EAST)
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(1, 0, 0));
				if (dir == EntityHelper.Dir.SOUTH_EAST) {
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(0, 0, 1));
					this.goalOwner.setMotion(this.goalOwner.getMotion().add(1, 0, 0));
				}

				if (this.goalOwner.world.rand.nextInt(2) == 0) {
					EntityHelper.setState(this.goalOwner, 0);
					this.goalOwner.setInvulnerable(false);
				} else {
					EntityHelper.setState(this.goalOwner, 1);
					this.goalOwner.setInvulnerable(true);
				}

				canUseNextAttack = false;
			}

			return true;
		}
		EntityHelper.setState(this.goalOwner, 0);
		this.goalOwner.setInvulnerable(false);
		return false;
	}

	@Override
	public void startExecuting() {
		EntityHelper.setState(this.goalOwner, 0);
		this.goalOwner.setInvulnerable(false);
	}

	private boolean isInShadow() {
		return EntityHelper.getState(this.goalOwner) == 1;
	}

	@Override
	public boolean shouldExecute() {
		return this.goalOwner.getAttackTarget() != null && this.goalOwner.getDistanceSq(this.goalOwner.getAttackTarget()) < MAX_DISTANCE_FOR_AI;
	}

}