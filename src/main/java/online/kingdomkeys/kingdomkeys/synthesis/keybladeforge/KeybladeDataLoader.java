package online.kingdomkeys.kingdomkeys.synthesis.keybladeforge;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.item.KeybladeItem;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncKeybladeData;

public class KeybladeDataLoader extends SimpleJsonResourceReloadListener {

    //GSON builder with custom deserializer for keyblade data
    public static final Gson GSON_BUILDER = new GsonBuilder().registerTypeAdapter(KeybladeData.class, new KeybladeDataDeserializer()).create();
    
    public static List<String> names = new LinkedList<>();
    public static List<String> dataList = new LinkedList<>();

    public KeybladeDataLoader() {
        super(GSON_BUILDER, "keyblades");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        KingdomKeys.LOGGER.info("Loading keyblade data");
        loadData(resourceManagerIn);
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                PacketHandler.sendTo(new SCSyncKeybladeData(KeybladeDataLoader.names, KeybladeDataLoader.dataList), player);
            }
        }
    }

    /**
     * Method searches the keyblades folder in the datapack for all json files inside it.
     * Loaded data is assigned to the keyblade with the same name as the json file
     * @param manager Resource manager from the server
     */
    public void loadData(ResourceManager manager) {
        String folder = "keyblades";
        String extension = ".json";
        
        names.clear();
        dataList.clear();
        
        System.out.print("Loading Keyblades: ");
        for (ResourceLocation file : manager.listResources(folder, n -> n.toString().endsWith(extension)).keySet()) { //Get all .json files
        	System.out.print(file.getNamespace()+":"+file.getPath()+" ");
            ResourceLocation keybladeDataID = new ResourceLocation(file.getNamespace(), file.getPath().substring(folder.length() + 1, file.getPath().length() - extension.length()));
            KeybladeItem keyblade = (KeybladeItem) ForgeRegistries.ITEMS.getValue(keybladeDataID);
            try {
            	BufferedReader br = manager.getResource(file).get().openAsReader();
            	BufferedReader br2 = manager.getResource(file).get().openAsReader();
            	String data = "";
            	while(br.ready()) {
            		data += br.readLine();
            	}
            	dataList.add(data);
            	KeybladeData result;
                try {
                    result = GSON_BUILDER.fromJson(br2, KeybladeData.class);
                    names.add(keybladeDataID.toString());
                   
                } catch (JsonParseException e) {
                    KingdomKeys.LOGGER.error("Error parsing json file {}: {}", manager.getResource(file).get().sourcePackId(), e);
                    continue;
                }
                keyblade.setKeybladeData(result);
                if(result.keychain != null)
                	result.keychain.setKeyblade(keyblade);
                IOUtils.closeQuietly(br);
                IOUtils.closeQuietly(br2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
    }
}
