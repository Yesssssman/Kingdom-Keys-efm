package online.kingdomkeys.kingdomkeys.network.stc;

import java.util.*;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.capability.IPlayerCapabilities;
import online.kingdomkeys.kingdomkeys.capability.ModCapabilities;
import online.kingdomkeys.kingdomkeys.lib.PortalData;
import online.kingdomkeys.kingdomkeys.lib.SoAState;
import online.kingdomkeys.kingdomkeys.organization.ModOrganizationUnlocks;
import online.kingdomkeys.kingdomkeys.organization.OrganizationUnlock;
import online.kingdomkeys.kingdomkeys.organization.OrganizationWeaponUnlock;
import online.kingdomkeys.kingdomkeys.util.Utils;

public class SCSyncCapabilityPacket {

	int level = 0;
	private int exp = 0;
	private int expGiven = 0;
	private int strength = 0;
	private int magic = 0;
	private int defense = 0;
	private int maxHp, maxAP;
	private int munny = 0;
	private int antipoints = 0;

	private double MP, maxMP, dp, maxDP, fp;
	
	private boolean recharge;

	List<String> messages, dfMessages;
	
    PortalData[] orgPortalCoords = {new PortalData((byte)0,0,0,0,0),new PortalData((byte)0,0,0,0,0),new PortalData((byte)0,0,0,0,0)};

    List<ResourceLocation> recipeList = new ArrayList<>();
    List<String> magicList = new ArrayList<>();
	LinkedHashMap<String,int[]> driveFormMap = new LinkedHashMap<>();
	LinkedHashMap<String,int[]> abilityMap = new LinkedHashMap<>();
	List<String> partyList = new ArrayList<>(10);
	TreeMap<String, Integer> materialMap = new TreeMap<>();
	Map<ResourceLocation, ItemStack> keychains = new HashMap<>();
	
	SoAState soAstate, choice, sacrifice;
	BlockPos choicePedestal, sacrificePedestal;
	Vec3d returnPos;
	DimensionType returnDim;

	int hearts;
	Utils.OrgMember alignment;
	OrganizationWeaponUnlock equippedWeapon;
	Set<OrganizationWeaponUnlock> unlocks;
	
	public SCSyncCapabilityPacket() {
	}

	public SCSyncCapabilityPacket(IPlayerCapabilities capability) {
		this.level = capability.getLevel();
		this.exp = capability.getExperience();
		this.expGiven = capability.getExperienceGiven();
		this.strength = capability.getStrength();
		this.magic = capability.getMagic();
		this.defense = capability.getDefense();
		
		this.MP = capability.getMP();
		this.maxMP = capability.getMaxMP();
		this.recharge = capability.getRecharge();
		this.maxHp = capability.getMaxHP();
		this.maxAP = capability.getMaxAP();
		this.dp = capability.getDP();
		this.maxDP = capability.getMaxDP();
		this.fp = capability.getFP();
		this.antipoints=capability.getAntiPoints();
		this.munny = capability.getMunny();
		
		for(byte i=0;i<3;i++) {
        	this.orgPortalCoords[i] = capability.getPortalCoords((byte)i);
        }
		
		this.recipeList = capability.getKnownRecipeList();
		this.magicList = capability.getMagicList();
		this.driveFormMap = capability.getDriveFormMap();
		this.abilityMap = capability.getAbilityMap();
		this.partyList = capability.getPartiesInvited();
		this.materialMap = capability.getMaterialMap();
		this.keychains = capability.getEquippedKeychains();
		
		this.messages = capability.getMessages();
		this.dfMessages = capability.getDFMessages();
		this.soAstate = capability.getSoAState();
		this.choice = capability.getChosen();
		this.choicePedestal = capability.getChoicePedestal();
		this.sacrifice = capability.getSacrificed();
		this.sacrificePedestal = capability.getSacrificePedestal();
		this.returnPos = capability.getReturnLocation();
		this.returnDim = capability.getReturnDimension();

		this.hearts = capability.getHearts();
		this.alignment = capability.getAlignment();
		this.equippedWeapon = capability.getEquippedWeapon();
		this.unlocks = capability.getWeaponsUnlocked();
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeInt(this.level);
		buffer.writeInt(this.exp);
		buffer.writeInt(this.expGiven);
		buffer.writeInt(this.strength);
		buffer.writeInt(this.magic);
		buffer.writeInt(this.defense);

		buffer.writeDouble(this.MP);
		buffer.writeDouble(this.maxMP);
		buffer.writeBoolean(this.recharge);
		buffer.writeInt(this.maxHp);
		buffer.writeInt(this.maxAP);
		buffer.writeDouble(this.dp);
		buffer.writeDouble(this.maxDP);
		buffer.writeDouble(this.fp);
		buffer.writeInt(this.antipoints);
		buffer.writeInt(this.munny);
		
		for(byte i=0;i<3;i++) {
        	buffer.writeByte(this.orgPortalCoords[i].getPID());
        	buffer.writeDouble(this.orgPortalCoords[i].getX());
        	buffer.writeDouble(this.orgPortalCoords[i].getY());
        	buffer.writeDouble(this.orgPortalCoords[i].getZ());
        	buffer.writeInt(this.orgPortalCoords[i].getDimID());
        }
		
		CompoundNBT recipes = new CompoundNBT();
		Iterator<ResourceLocation> recipesIt = recipeList.iterator();
		while (recipesIt.hasNext()) {
			ResourceLocation r = recipesIt.next();
			recipes.putString(r.toString(), r.toString());
		}
		buffer.writeCompoundTag(recipes);

		CompoundNBT magics = new CompoundNBT();
		Iterator<String> magicsIt = magicList.iterator();
		while (magicsIt.hasNext()) {
			String m = magicsIt.next();
			magics.putInt(m, 1);
		}
		buffer.writeCompoundTag(magics);
		
		CompoundNBT forms = new CompoundNBT();
		Iterator<Map.Entry<String, int[]>> driveFormsIt = driveFormMap.entrySet().iterator();
		while (driveFormsIt.hasNext()) {
			Map.Entry<String, int[]> pair = (Map.Entry<String, int[]>) driveFormsIt.next();
			forms.putIntArray(pair.getKey().toString(), pair.getValue());
		}
		buffer.writeCompoundTag(forms);
		
		CompoundNBT abilities = new CompoundNBT();
		Iterator<Map.Entry<String, int[]>> abilitiesIt = abilityMap.entrySet().iterator();
		while (abilitiesIt.hasNext()) {
			Map.Entry<String, int[]> pair = (Map.Entry<String, int[]>) abilitiesIt.next();
			abilities.putIntArray(pair.getKey().toString(), pair.getValue());
		}
		buffer.writeCompoundTag(abilities);

		CompoundNBT keychains = new CompoundNBT();
		this.keychains.forEach((key, value) -> keychains.put(key.toString(), value.serializeNBT()));
		buffer.writeCompoundTag(keychains);

		buffer.writeInt(partyList.size());
		for(int i=0;i<partyList.size();i++) {
			buffer.writeInt(this.partyList.get(i).length());
			buffer.writeString(this.partyList.get(i));
		}
		
		CompoundNBT materials = new CompoundNBT();
		Iterator<Map.Entry<String, Integer>> materialsIt = materialMap.entrySet().iterator();
		while (materialsIt.hasNext()) {
			Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) materialsIt.next();
			materials.putInt(pair.getKey().toString(), pair.getValue());
			if (materials.getInt(pair.getKey()) == 0 && pair.getKey().toString() != null)
				materials.remove(pair.getKey().toString());
		}
		buffer.writeCompoundTag(materials);
		
		buffer.writeInt(messages.size());
		buffer.writeInt(dfMessages.size());

		for (int i = 0; i < this.messages.size(); i++) {
			buffer.writeString(this.messages.get(i));
		}
		
		for (int i = 0; i < this.dfMessages.size(); i++) {
			buffer.writeString(this.dfMessages.get(i));
		}
		buffer.writeInt(this.returnDim.getId());
		buffer.writeDouble(this.returnPos.x);
		buffer.writeDouble(this.returnPos.y);
		buffer.writeDouble(this.returnPos.z);
		buffer.writeByte(this.soAstate.get());
		buffer.writeByte(this.choice.get());
		buffer.writeByte(this.sacrifice.get());
		buffer.writeBlockPos(this.choicePedestal);
		buffer.writeBlockPos(this.sacrificePedestal);

		buffer.writeInt(this.hearts);
		buffer.writeInt(this.alignment.ordinal());
		buffer.writeBoolean(this.equippedWeapon != null);
		if (this.equippedWeapon != null) {
			buffer.writeString(this.equippedWeapon.getRegistryName().toString());
		}
		buffer.writeInt(this.unlocks.size());
		unlocks.forEach(unlock -> buffer.writeString(unlock.getRegistryName().toString()));
	}

	public static SCSyncCapabilityPacket decode(PacketBuffer buffer) {
		SCSyncCapabilityPacket msg = new SCSyncCapabilityPacket();

		msg.level = buffer.readInt();
		msg.exp = buffer.readInt();
		msg.expGiven = buffer.readInt();
		msg.strength = buffer.readInt();
		msg.magic = buffer.readInt();
		msg.defense = buffer.readInt();

		msg.MP = buffer.readDouble();
		msg.maxMP = buffer.readDouble();
		msg.recharge = buffer.readBoolean();
		msg.maxHp = buffer.readInt();
		// msg.choice1 = buffer.readString(40);
		msg.maxAP = buffer.readInt();
		msg.dp = buffer.readDouble();
		msg.maxDP = buffer.readDouble();
		msg.fp = buffer.readDouble();
		msg.antipoints = buffer.readInt();
		msg.munny = buffer.readInt();

		for(byte i=0;i<3;i++) {
    		msg.orgPortalCoords[i].setPID(buffer.readByte());
    		msg.orgPortalCoords[i].setX(buffer.readDouble());
    		msg.orgPortalCoords[i].setY(buffer.readDouble());
    		msg.orgPortalCoords[i].setZ(buffer.readDouble());
    		msg.orgPortalCoords[i].setDimID(buffer.readInt());
        }
		
		CompoundNBT recipesTag = buffer.readCompoundTag();
		Iterator<String> recipesIt = recipesTag.keySet().iterator();
		while (recipesIt.hasNext()) {
			String key = (String) recipesIt.next();
			msg.recipeList.add(new ResourceLocation(key));
		}
		
		CompoundNBT magicsTag = buffer.readCompoundTag();
		Iterator<String> magicsIt = magicsTag.keySet().iterator();
		while (magicsIt.hasNext()) {
			String key = (String) magicsIt.next();
			msg.magicList.add(key);
		}
		
		CompoundNBT driveFormsTag = buffer.readCompoundTag();
		Iterator<String> driveFormsIt = driveFormsTag.keySet().iterator();
		while (driveFormsIt.hasNext()) {
			String driveFormName = (String) driveFormsIt.next();
			msg.driveFormMap.put(driveFormName, driveFormsTag.getIntArray(driveFormName));
		}
		
		CompoundNBT abilitiesTag = buffer.readCompoundTag();
		Iterator<String> abilitiesIt = abilitiesTag.keySet().iterator();
		while (abilitiesIt.hasNext()) {
			String abilityName = (String) abilitiesIt.next();
			msg.abilityMap.put(abilityName, abilitiesTag.getIntArray(abilityName));
		}

		CompoundNBT keychainsNBT = buffer.readCompoundTag();
		keychainsNBT.keySet().forEach(key -> msg.keychains.put(new ResourceLocation(key), ItemStack.read((CompoundNBT) keychainsNBT.get(key))));
		
		int amount = buffer.readInt();
		msg.partyList = new ArrayList<String>();

		for(int i=0;i<amount;i++) {
			int length = buffer.readInt();
			msg.partyList.add(buffer.readString(length));
		}
		
		CompoundNBT materialsTag = buffer.readCompoundTag();
		Iterator<String> materialsIt = materialsTag.keySet().iterator();
		while (materialsIt.hasNext()) {
			String matName = (String) materialsIt.next();
			msg.materialMap.put(matName, materialsTag.getInt(matName));
		}
		
		int msgSize = buffer.readInt();
		int dfMsgSize = buffer.readInt();
		
		msg.messages = new ArrayList<String>();
		for(int i = 0;i<msgSize;i++) {
			msg.messages.add(buffer.readString(100));
		}
		
		msg.dfMessages = new ArrayList<String>();
		for(int i = 0;i<dfMsgSize;i++) {
			msg.dfMessages.add(buffer.readString(100));
		}

		msg.returnDim = DimensionType.getById(buffer.readInt());
		msg.returnPos = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		msg.soAstate = SoAState.fromByte(buffer.readByte());
		msg.choice = SoAState.fromByte(buffer.readByte());
		msg.sacrifice = SoAState.fromByte(buffer.readByte());
		msg.choicePedestal = buffer.readBlockPos();
		msg.sacrificePedestal = buffer.readBlockPos();

		msg.hearts = buffer.readInt();
		msg.alignment = Utils.OrgMember.values()[buffer.readInt()];
		if (buffer.readBoolean()) {
			msg.equippedWeapon = (OrganizationWeaponUnlock) ModOrganizationUnlocks.registry.getValue(new ResourceLocation(buffer.readString(100)));
		} else {
			msg.equippedWeapon = null;
		}
		msg.unlocks = new HashSet<>();
		int unlockSize = buffer.readInt();
		for (int i = 0; i < unlockSize; ++i) {
			msg.unlocks.add((OrganizationWeaponUnlock) ModOrganizationUnlocks.registry.getValue(new ResourceLocation(buffer.readString(100))));
		}
		return msg;
	}

	public static void handle(final SCSyncCapabilityPacket message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			IPlayerCapabilities playerData = ModCapabilities.getPlayer(KingdomKeys.proxy.getClientPlayer());

			playerData.setLevel(message.level);
			playerData.setExperience(message.exp);
			playerData.setExperienceGiven(message.expGiven);
			playerData.setStrength(message.strength);
			playerData.setMagic(message.magic);
			playerData.setDefense(message.defense);
			playerData.setMP(message.MP);
			playerData.setMaxMP(message.maxMP);
			playerData.setRecharge(message.recharge);
			playerData.setMaxHP(message.maxHp);
			KingdomKeys.proxy.getClientPlayer().setHealth(playerData.getMaxHP());
			playerData.setMaxAP(message.maxAP);
			playerData.setDP(message.dp);
			playerData.setFP(message.fp);
			playerData.setMaxDP(message.maxDP);
			playerData.setMunny(message.munny);
			playerData.setMessages(message.messages);
			playerData.setDFMessages(message.dfMessages);

			playerData.setPortalCoords((byte)0, message.orgPortalCoords[0]);
			playerData.setPortalCoords((byte)1, message.orgPortalCoords[1]);
			playerData.setPortalCoords((byte)2, message.orgPortalCoords[2]);

			playerData.setKnownRecipeList(message.recipeList);
			playerData.setMagicList(message.magicList);
			playerData.setDriveFormMap(message.driveFormMap);
			playerData.setAbilityMap(message.abilityMap);
			playerData.setAntiPoints(message.antipoints);
			playerData.setPartiesInvited(message.partyList);
			playerData.setMaterialMap(message.materialMap);
			playerData.equipAllKeychains(message.keychains, false);

			playerData.setReturnDimension(message.returnDim);
			playerData.setReturnLocation(message.returnPos);
			playerData.setSoAState(message.soAstate);
			playerData.setChoice(message.choice);
			playerData.setSacrifice(message.sacrifice);
			playerData.setChoicePedestal(message.choicePedestal);
			playerData.setSacrificePedestal(message.sacrificePedestal);

			playerData.setHearts(message.hearts);
			playerData.setAlignment(message.alignment);
			playerData.equipWeapon(message.equippedWeapon);
			playerData.setWeaponsUnlocked(message.unlocks);
		});
		ctx.get().setPacketHandled(true);
	}

}
