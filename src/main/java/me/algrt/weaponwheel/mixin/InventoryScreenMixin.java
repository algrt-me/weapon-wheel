package me.algrt.weaponwheel.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
//    @Accessor("x")
//    int x;

//    public static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/container/inventory.png");

//    @Inject(method = "drawBackground", at = @At("TAIL"))
//    private void drawExtraSlots(DrawContext context, float delta, int mouseX, int mouseY) {
//        InventoryScreen screen = (InventoryScreen) (Object) this;
//        int x = screen.x - 40; // Position left of the player's inventory
//        int y = screen.y;
//
//        for (int i = 0; i < 6; i++) {
//            int slotX = x + (i % 2) * 18; // Two columns
//            int slotY = y + (i / 2) * 18; // Three rows
//            context.drawTexture(BACKGROUND_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
//        }
//    }
}
