package online.kingdomkeys.kingdomkeys.entity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import online.kingdomkeys.kingdomkeys.block.ModBlocks;
import online.kingdomkeys.kingdomkeys.entity.ModEntities;

import javax.annotation.Nullable;
import java.util.UUID;

public class SoRCoreTileEntity extends BlockEntity implements TickingBlockEntity {
	UUID userUUID;
	int ticks = 0;
	
	public SoRCoreTileEntity(BlockPos blockPos, BlockState blockState) {
		super(ModEntities.TYPE_SOR_CORE_TE.get(), blockPos, blockState);
	}

	@Override
	public CompoundTag save(CompoundTag parentNBTTagCompound) {
		super.save(parentNBTTagCompound);
		if (userUUID != null)
			parentNBTTagCompound.putUUID("uuid", userUUID);
		return parentNBTTagCompound;
	}

	@Override
	public void load( CompoundTag nbt) {
		super.load(nbt);
		if(nbt.hasUUID("uuid"))
			userUUID = nbt.getUUID("uuid");
	}

	public UUID getUUID() {
		return userUUID;
	}

	public void setUUID(UUID uuid) {
		this.userUUID = uuid;
		setChanged();
	}
	
	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		CompoundTag nbt = new CompoundTag();
		this.save(nbt);
		return new ClientboundBlockEntityDataPacket(this.getBlockPos(), 1, nbt);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		this.load(pkt.getTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		this.load( tag);
	}

	@Override
	public void tick() {
		if(!level.isClientSide) {
			System.out.println(ticks);
			if(ticks == 0) {
				spawnSoR();
			}
			
			if(ticks > 12000) {
				removeSoR();
			}
			ticks++;
		}
	}

	@Override
	public BlockPos getPos() {
		return null;
	}

	//x
		int sorWidth = 25;
	    //z
	    int sorDepth = 25;
	    
	    int colHeight = 6;

	    String sorTop =
	    		"0000000000111110000000000" +
	    		"0000000011441441100000000" +
	            "0030001114444444111000300" +
	            "0000111114444444111110000" +
	            "0001414111414141114141000" +
	            "0001141111114111111411000" +
	            "0011414114414144114141100" +
	            "0011111144444444411111100" +
	            "0111111414414144141111110" +
	            "0144114441144411444114410" +
	            "1444414441144411444144441" +
	            "1444111414414144141114441" +
	            "1144444444440444444444411" + //Middle is 0 since it is already gonna be filled by the core block previously
	            "1444111414414144141114441" +
	            "1444414441144411444144441" +
	            "0144114441144411444114410" +
	            "0111111414414144141111110" +
	            "0011111144444444411111100" +
	            "0011414114414144114141100" +
	            "0001141111114111111411000" +
	            "0001414111414141114141000" +
	            "0000111114444444111110000" +
	            "0030001114444444111000300" +
	            "0000000011441441100000000" +
	    		"0000000000111110000000000";

		public void spawnSoR() {
			BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
			int startZ = worldPosition.getZ() - (sorDepth / 2);
			int startX = worldPosition.getX() - (sorWidth / 2);

			//for (int y = 0; y < baseY; ++y) {
				for (int z = startZ; z <= worldPosition.getZ() + (sorDepth / 2); ++z) {
					for (int x = startX; x <= worldPosition.getX() + (sorWidth / 2); ++x) {
						blockpos$mutable.set(x, worldPosition.getY(), z);
						int strucX = x - startX;
						int strucZ = z - startZ;
						//if (y == 1) {
							stateToPlace(sorTop.charAt(strucX + strucZ * sorWidth), blockpos$mutable);
						//}
					}
				}
			//}
			
		}

		private void stateToPlace(char c, BlockPos.MutableBlockPos pos) {
			switch (c) {
			case '0':
				return;
			case '1':
				level.setBlock(pos, Blocks.QUARTZ_BLOCK.defaultBlockState(), 2);
				break;
			case '2':
				level.setBlock(pos, ModBlocks.sorCore.get().defaultBlockState(), 2);
				break;
			case '3':
				/*for (int i = 0; i <= colHeight; i++) {
					world.setBlockState(pos, Blocks.QUARTZ_PILLAR.getDefaultState(), 2);
					pos.setY(pos.getY() + 1);
				}*/
				break;
			case '4':
				level.setBlock(pos, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), 2);
				break;
			}
		}
		
	public void removeSoR() {
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
		int startZ = this.worldPosition.getZ() - (sorDepth / 2);
		int startX = this.worldPosition.getX() - (sorWidth / 2);

		for (int z = startZ; z <= this.worldPosition.getZ() + (sorDepth / 2); ++z) {
			for (int x = startX; x <= this.worldPosition.getX() + (sorWidth / 2); ++x) {
				blockpos$mutable.set(x, this.worldPosition.getY(), z);
				level.setBlock(blockpos$mutable, Blocks.AIR.defaultBlockState(), 2);
			}
		}
	}
	
}