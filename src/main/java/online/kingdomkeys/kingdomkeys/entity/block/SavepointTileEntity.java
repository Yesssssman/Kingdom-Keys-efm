package online.kingdomkeys.kingdomkeys.entity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import online.kingdomkeys.kingdomkeys.block.SavePointBlock;
import online.kingdomkeys.kingdomkeys.entity.ModEntities;
import online.kingdomkeys.kingdomkeys.world.SavePointStorage;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

public class SavepointTileEntity extends BlockEntity {
	public SavepointTileEntity(BlockPos pos, BlockState state) {
		super(ModEntities.TYPE_SAVEPOINT.get(), pos, state);
	}
	long ticks;
	//TODO Change savepoint type to tier
	private UUID id = UUID.randomUUID();

	public UUID getID() {
		return id;
	}

	public int
			tier = 0,
			heal = 20,
			hunger = 20,
			magic = 20,
			drive = 20,
			focus = 20
	;

	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
		if (((SavePointBlock)getBlockState().getBlock()).getType() != SavePointStorage.SavePointType.NORMAL) {
			id = pTag.getUUID("savepoint_id");
		}
		tier = pTag.getInt("tier");
		heal = pTag.getInt("heal");
		System.out.println("Heal load: "+heal);
		hunger = pTag.getInt("hunger");
		magic = pTag.getInt("magic");
		drive = pTag.getInt("drive");
		focus = pTag.getInt("focus");
	}

	@Override
	protected void saveAdditional(CompoundTag pTag) {
		if (((SavePointBlock)getBlockState().getBlock()).getType() != SavePointStorage.SavePointType.NORMAL) {
			pTag.putUUID("savepoint_id", id);
		}
		pTag.putInt("tier",tier);
		pTag.putInt("heal",heal);
		System.out.println("Heal save: "+heal);
		pTag.putInt("hunger",hunger);
		pTag.putInt("magic",magic);
		pTag.putInt("drive",drive);
		pTag.putInt("focus",focus);
		super.saveAdditional(pTag);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return serializeNBT();
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		this.load(tag);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		this.load(pkt.getTag());
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
		if(blockEntity instanceof SavepointTileEntity savepoint) {
			if (savepoint.ticks > 1800)
				savepoint.ticks = 0;

			// Don't do anything unless it's active
			double r = 0.7D;
			double cx = pos.getX() + 0.5;
			double cy = pos.getY() + 0.5;
			double cz = pos.getZ() + 0.5;

			savepoint.ticks += 10; // Speed and distance between particles
			double x = cx + (r * Math.cos(Math.toRadians(savepoint.ticks)));
			double z = cz + (r * Math.sin(Math.toRadians(savepoint.ticks)));

			double x2 = cx + (r * Math.cos(Math.toRadians(-savepoint.ticks)));
			double z2 = cz + (r * Math.sin(Math.toRadians(-savepoint.ticks)));

			level.addParticle(new DustParticleOptions(new Vector3f(0F, 1F, 0F), 1F), x, (cy - 0.5) - (-savepoint.ticks / 1800F), z, 0.0D, 0.0D, 0.0D);
			level.addParticle(new DustParticleOptions(new Vector3f(0.3F, 1F, 0.3F), 1F), x2, (cy + 0.5) - (savepoint.ticks / 1800F), z2, 0.0D, 0.0D, 0.0D);
		}
	}
}

