package online.kingdomkeys.kingdomkeys.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.capability.IPlayerCapabilities;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncCapabilityPacket;
import online.kingdomkeys.kingdomkeys.synthesis.material.Material;
import online.kingdomkeys.kingdomkeys.synthesis.material.ModMaterials;
import online.kingdomkeys.kingdomkeys.util.Utils;

public class KKMaterialCommand extends BaseCommand { // kk_material <give/take> <material/all> <amount> [player]
														// kk_material <take> <all> [player]

	private static final SuggestionProvider<CommandSourceStack> SUGGEST_MATERIALS = (p_198296_0_, p_198296_1_) -> {
		List<String> list = new ArrayList<>();
		for (ResourceLocation actual : ModMaterials.registry.getKeys()) {
			list.add(actual.toString());
		}
		return SharedSuggestionProvider.suggest(list.stream().map(StringArgumentType::escapeIfRequired), p_198296_1_);
	};

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("kk_material").requires(source -> source.hasPermission(2));

		builder.then(Commands.literal("give")

				.then(Commands.argument("material", StringArgumentType.string()).suggests(SUGGEST_MATERIALS).then(Commands.argument("amount", IntegerArgumentType.integer(1)).then(Commands.argument("targets", EntityArgument.players()).executes(KKMaterialCommand::addMaterial)).executes(KKMaterialCommand::addMaterial))).then(Commands.literal("all").then(Commands.argument("amount", IntegerArgumentType.integer(1)).then(Commands.argument("targets", EntityArgument.players()).executes(KKMaterialCommand::addAllMaterials)).executes(KKMaterialCommand::addAllMaterials))));

		builder.then(Commands.literal("take")

				.then(Commands.argument("material", StringArgumentType.string()).suggests(SUGGEST_MATERIALS).then(Commands.argument("amount", IntegerArgumentType.integer(1)).then(Commands.argument("targets", EntityArgument.players()).executes(KKMaterialCommand::takeMaterial)).executes(KKMaterialCommand::takeMaterial))).then(Commands.literal("all").then(Commands.argument("targets", EntityArgument.players()).executes(KKMaterialCommand::takeAllMaterials)).executes(KKMaterialCommand::takeAllMaterials))

		);

		builder.then(Commands.literal("set")

				.then(Commands.argument("material", StringArgumentType.string()).suggests(SUGGEST_MATERIALS).then(Commands.argument("amount", IntegerArgumentType.integer(1)).then(Commands.argument("targets", EntityArgument.players()).executes(KKMaterialCommand::setMaterial)).executes(KKMaterialCommand::setMaterial))).then(Commands.literal("all").then(Commands.argument("amount", IntegerArgumentType.integer(1)).then(Commands.argument("targets", EntityArgument.players()).executes(KKMaterialCommand::setAllMaterials)).executes(KKMaterialCommand::setAllMaterials))));

		dispatcher.register(builder);
		KingdomKeys.LOGGER.warn("Registered command " + builder.getLiteral());
	}

	private static int addMaterial(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Collection<ServerPlayer> players = getPlayers(context, 4);
		String materialName = StringArgumentType.getString(context, "material");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		Material material = ModMaterials.registry.getValue(new ResourceLocation(materialName));

		for (ServerPlayer player : players) {
			IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
			playerData.addMaterial(material, amount);

			context.getSource().sendSuccess(new TranslatableComponent("Given x" + amount + " '" + Utils.translateToLocal(material.getMaterialName()) + "' to " + player.getDisplayName().getString()), true);

			player.sendMessage(new TranslatableComponent("You have been given x" + amount + " '" + Utils.translateToLocal(material.getMaterialName()) + "'"), Util.NIL_UUID);
			PacketHandler.sendTo(new SCSyncCapabilityPacket(playerData), (ServerPlayer) player);
		}
		return 1;
	}

	private static int takeMaterial(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Collection<ServerPlayer> players = getPlayers(context, 4);
		String materialName = StringArgumentType.getString(context, "material");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		Material material = ModMaterials.registry.getValue(new ResourceLocation(materialName));

		for (ServerPlayer player : players) {
			IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
			playerData.removeMaterial(material, amount);

			context.getSource().sendSuccess(new TranslatableComponent("Removed material '" + Utils.translateToLocal(material.getMaterialName()) + "' from " + player.getDisplayName().getString()), true);

			player.sendMessage(new TranslatableComponent("x" + amount + " '" + Utils.translateToLocal(material.getMaterialName()) + "' have been taken away from you"), Util.NIL_UUID);
			PacketHandler.sendTo(new SCSyncCapabilityPacket(playerData), (ServerPlayer) player);
		}
		return 1;
	}

	private static int addAllMaterials(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Collection<ServerPlayer> players = getPlayers(context, 4);
		int amount = IntegerArgumentType.getInteger(context, "amount");

		for (ServerPlayer player : players) {
			IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
			for (Material material : ModMaterials.registry.getValues()) {
				playerData.addMaterial(material, amount);
			}

			context.getSource().sendSuccess(new TranslatableComponent("Given all materials to " + player.getDisplayName().getString()), true);

			player.sendMessage(new TranslatableComponent("You have been given all the materials"), Util.NIL_UUID);
			PacketHandler.sendTo(new SCSyncCapabilityPacket(playerData), (ServerPlayer) player);
		}
		return 1;
	}

	private static int takeAllMaterials(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Collection<ServerPlayer> players = getPlayers(context, 3);

		for (ServerPlayer player : players) {
			IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
			playerData.clearMaterials();

			context.getSource().sendSuccess(new TranslatableComponent("Taken all materials from " + player.getDisplayName().getString()), true);

			player.sendMessage(new TranslatableComponent("Your materials have been taken away"), Util.NIL_UUID);
			PacketHandler.sendTo(new SCSyncCapabilityPacket(playerData), (ServerPlayer) player);
		}
		return 1;
	}

	private static int setMaterial(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Collection<ServerPlayer> players = getPlayers(context, 4);
		String materialName = StringArgumentType.getString(context, "material");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		Material material = ModMaterials.registry.getValue(new ResourceLocation(materialName));

		for (ServerPlayer player : players) {
			IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
			playerData.setMaterial(material, amount);

			context.getSource().sendSuccess(new TranslatableComponent("Set x" + amount + " '" + Utils.translateToLocal(material.getMaterialName()) + "' to " + player.getDisplayName().getString()), true);

			player.sendMessage(new TranslatableComponent("Your '" + Utils.translateToLocal(material.getMaterialName()) + "' have been set to x" + amount), Util.NIL_UUID);
			PacketHandler.sendTo(new SCSyncCapabilityPacket(playerData), (ServerPlayer) player);
		}
		return 1;
	}

	private static int setAllMaterials(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Collection<ServerPlayer> players = getPlayers(context, 4);
		int amount = IntegerArgumentType.getInteger(context, "amount");

		for (ServerPlayer player : players) {
			IPlayerCapabilities playerData = ModCapabilities.getPlayer(player);
			for (Material material : ModMaterials.registry.getValues()) {
				playerData.setMaterial(material, amount);
			}

			context.getSource().sendSuccess(new TranslatableComponent("Set all materials for " + player.getDisplayName().getString() + " to " + amount), true);

			player.sendMessage(new TranslatableComponent("You have been set all the materials to " + amount), Util.NIL_UUID);
			PacketHandler.sendTo(new SCSyncCapabilityPacket(playerData), (ServerPlayer) player);
		}
		return 1;
	}

}
