package online.kingdomkeys.kingdomkeys.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.SwordItem;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import online.kingdomkeys.kingdomkeys.network.PacketAttackOffhand;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.synthesis.keybladeforge.KeybladeData;

public class KeybladeItem extends SwordItem {

	// Level 0 = no upgrades, will use base stats in the data file
	private int level = 0;
	private KeybladeData data;

	private Item.Properties properties;

	// TODO remove attack damage
	public KeybladeItem(Item.Properties properties) {
		super(new KeybladeItemTier(0), 0, 1, properties);
		this.properties = properties;
	}

	// Get strength from the data based on the specified level
	public int getStrength(int level) {
		return data.getStrength(level);
	}

	// Get magic from the data based on the specified level
	public int getMagic(int level) {
		return data.getMagic(level);
	}

	// Get strength from the data based on level
	public int getStrength() {
		return data.getStrength(level);
	}

	// Get magic from the data based on level
	public int getMagic() {
		return data.getMagic(level);
	}

	public String getDescription() {
		return data.getDescription();
	}

	public void setKeybladeData(KeybladeData data) {
		this.data = data;
	}

	public int getKeybladeLevel() {
		return level;
	}

	public void setKeybladeLevel(int level) {
		this.level = level;
	}

	public Item.Properties getProperties() {
		return properties;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (player.isSneaking()) {

		} else {
			if (world.isRemote) {
				RayTraceResult rtr = Minecraft.getInstance().objectMouseOver;
				if (rtr != null) {
					player.swingArm(Hand.OFF_HAND);

					if (rtr.getType() == Type.ENTITY) {
						EntityRayTraceResult ertr = (EntityRayTraceResult) rtr;
						if (!ItemStack.areItemStacksEqual(player.getHeldItem(Hand.OFF_HAND), ItemStack.EMPTY) && player.getHeldItem(Hand.OFF_HAND).getItem() instanceof KeybladeItem && hand == Hand.OFF_HAND) {
							if (ertr.getEntity() != null) {
								PacketHandler.sendToServer(new PacketAttackOffhand(ertr.getEntity().getEntityId()));
								return ActionResult.resultSuccess(itemstack);
							}
							return ActionResult.resultFail(itemstack);
						}
					}
				}

			}
		}
		return super.onItemRightClick(world, player, hand);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		PlayerEntity player = context.getPlayer();
		
		SoundEvent sound;
		if (world.getBlockState(pos).getBlock() instanceof DoorBlock) {
		      DoubleBlockHalf doubleblockhalf = world.getBlockState(pos).get(DoorBlock.HALF);

			if (doubleblockhalf == DoubleBlockHalf.UPPER) {
				world.setBlockState(pos.down(), world.getBlockState(pos.down()).with(DoorBlock.OPEN, !world.getBlockState(pos.down()).get(DoorBlock.OPEN)));
				sound = world.getBlockState(pos.down()).get(DoorBlock.OPEN) ? SoundEvents.BLOCK_IRON_DOOR_CLOSE : SoundEvents.BLOCK_IRON_DOOR_OPEN;
			} else {
				world.setBlockState(pos, world.getBlockState(pos).with(DoorBlock.OPEN, !world.getBlockState(pos).get(DoorBlock.OPEN)));
				sound = world.getBlockState(pos).get(DoorBlock.OPEN) ? SoundEvents.BLOCK_IRON_DOOR_CLOSE : SoundEvents.BLOCK_IRON_DOOR_OPEN;
			}
			world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
			return ActionResultType.SUCCESS;

		} else if(world.getBlockState(pos).getBlock() instanceof TrapDoorBlock) {
			world.setBlockState(pos, world.getBlockState(pos).with(TrapDoorBlock.OPEN, !world.getBlockState(pos).get(TrapDoorBlock.OPEN)));
			sound = world.getBlockState(pos).get(TrapDoorBlock.OPEN) ? SoundEvents.BLOCK_IRON_DOOR_CLOSE : SoundEvents.BLOCK_IRON_DOOR_OPEN;
			world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
			return ActionResultType.SUCCESS;

		}
		return ActionResultType.PASS;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		// TODO make better tooltip (translations and looks)
		if (data != null) {
			tooltip.add(new TranslationTextComponent("Level %s", getKeybladeLevel()));
			tooltip.add(new TranslationTextComponent("Strength %s", getStrength(getKeybladeLevel())));
			tooltip.add(new TranslationTextComponent("Magic %s", getMagic(getKeybladeLevel())));
			tooltip.add(new TranslationTextComponent(TextFormatting.ITALIC + getDescription()));
		}
	}

}
