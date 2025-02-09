package online.kingdomkeys.kingdomkeys.client.gui.elements.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import online.kingdomkeys.kingdomkeys.KingdomKeys;


public class CheckboxButton extends AbstractButton {

    private boolean checked;

    public CheckboxButton(int xIn, int yIn, String msg, boolean checked) {
        super(xIn, yIn, 10, 10, Component.translatable(msg));
        this.checked = checked;
    }

    @Override
    public void onPress() {
        this.checked = !checked;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            RenderSystem.setShaderTexture(0, new ResourceLocation(KingdomKeys.MODID + ":textures/gui/checkbox.png"));
            blit(matrixStack, getX(), getY(), 0, 0, 10, 10);
            if (checked) {
                blit(matrixStack, getX(), getY(), 10, 0, 10, 10);
            }
            Minecraft.getInstance().font.draw(matrixStack, getMessage().getString(), getX() + width + 3, getY() + 2, 4210752);
        }
    }

    @Override
    public int getWidth() {
        return super.getWidth() + 3 + Minecraft.getInstance().font.width(getMessage());
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

	@Override
	protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
		
	}
}
