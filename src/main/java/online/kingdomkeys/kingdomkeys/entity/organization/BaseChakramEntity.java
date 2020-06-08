package online.kingdomkeys.kingdomkeys.entity.organization;

import java.util.List;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import online.kingdomkeys.kingdomkeys.entity.ModEntities;
import online.kingdomkeys.kingdomkeys.entity.magic.MagnetEntity;

public class BaseChakramEntity extends ThrowableEntity{

	int maxTicks = 100;
	boolean returning = false;
	String model;
	
	public BaseChakramEntity(EntityType<? extends ThrowableEntity> type, World world) {
		super(type, world);
		this.preventEntitySpawning = true;
	}

	public BaseChakramEntity(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
		super(ModEntities.TYPE_CHAKRAM.get(), world);
	}

	public BaseChakramEntity(World world) {
		super(ModEntities.TYPE_CHAKRAM.get(), world);
		this.preventEntitySpawning = true;
	}

	public BaseChakramEntity(World world, PlayerEntity player, String model) {
		super(ModEntities.TYPE_CHAKRAM.get(), player, world);
		owner = player;
		setModel(model);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected float getGravityVelocity() {
		return 0F;
	}

	@Override
	public void tick() {
		if (this.ticksExisted > maxTicks) {
			this.remove();
		}
		
		if(ticksExisted > maxTicks / 2) {
			setReturn();
		}

		if (ticksExisted > 2)
			world.addParticle(ParticleTypes.FLAME, getPosX(), getPosY(), getPosZ(), 0, 0, 0);

		if (returning) {
		//	this.rotationYaw = (rotation + 1) % 360;
			List entityTagetList = this.world.getEntitiesWithinAABB(Entity.class, this.getBoundingBox().grow(1.0D, 1.0D, 1.0D));
			for (int i = 0; i < entityTagetList.size(); i++) {
				Entity entityTarget = (Entity) entityTagetList.get(i);
				if (entityTarget != null && entityTarget instanceof PlayerEntity) {
					PlayerEntity owner = (PlayerEntity) entityTarget;
					if (owner == this.getThrower()) {
						this.remove();
					}
				}
			}
		}
		super.tick();
	}

	public void setReturn() {
		returning = true;
		if(owner != null)
		shoot(this.getThrower().getPosX() - this.getPosX(), this.getThrower().getPosY() - this.getPosY() + 1.25, this.getThrower().getPosZ() - this.getPosZ(), 2f, 0);
	}
	
	@Override
	protected void onImpact(RayTraceResult rtRes) {
		if (!world.isRemote) {

			EntityRayTraceResult ertResult = null;
			BlockRayTraceResult brtResult = null;

			if (rtRes instanceof EntityRayTraceResult) {
				ertResult = (EntityRayTraceResult) rtRes;
			}

			if (rtRes instanceof BlockRayTraceResult) {
				brtResult = (BlockRayTraceResult) rtRes;
			}

			if (ertResult != null && ertResult.getEntity() != null && ertResult.getEntity() instanceof LivingEntity) {
				LivingEntity target = (LivingEntity) ertResult.getEntity();
				if (target != getThrower()) {
					target.setFire(5);
					target.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 10);
					setReturn();
				}
			} else { // Block (not ERTR)				
				setReturn();
			}
		}

	}

	public int getMaxTicks() {
		return maxTicks;
	}

	public void setMaxTicks(int maxTicks) {
		this.maxTicks = maxTicks;
	}
	
	private static final DataParameter<String> MODEL = EntityDataManager.createKey(BaseChakramEntity.class, DataSerializers.STRING);
	
	public String getModel() {
		return model;
	}

	public void setModel(String name) {
		this.dataManager.set(MODEL, name);
		this.model = name;
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (key.equals(MODEL)) {
			this.model = this.getModelDataManager();
		}
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
		compound.putString("Model", this.getModel());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		this.setModel(compound.getString("Model"));
	}
	

	@Override
	protected void registerData() {
		this.dataManager.register(MODEL, "");
	}

	public String getModelDataManager() {
		return this.dataManager.get(MODEL);
	}
	
}
