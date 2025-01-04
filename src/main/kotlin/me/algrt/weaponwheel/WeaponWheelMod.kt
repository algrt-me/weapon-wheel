package me.algrt.weaponwheel

import me.algrt.weaponwheel.ui.WeaponWheel
import me.algrt.weaponwheel.ui.WeaponWheelRenderConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class WeaponWheelMod : ModInitializer {
    override fun onInitialize() {
        val weaponWheel = WeaponWheel(object : WeaponWheelRenderConfig {
            override val color: Long = 0x101010
            override val secondaryColor: Long = 0x101010
            override val hoverColor: Long = 0xFAFAFA
            override val equippedColor: Long = 0xDADADA
            override val slotGap = 3f;
            override val segmentsPerSlot = 50
            override val itemScale = 20.0f
       }, 9);

        val releaseMouseKey = KeyBindingHelper.registerKeyBinding(KeyBinding(
            "key.weaponwheel.show",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            "category.weaponwheel.show"
        ))

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.player == null || client.currentScreen != null)
                return@register

            if (releaseMouseKey.isPressed) {
                weaponWheel.setMousePosition(client.mouse.x, client.mouse.y)
                weaponWheel.open()
                client.mouse.unlockCursor()
            } else {
                weaponWheel.close()
                client.mouse.lockCursor()
            }
        }

        HudRenderCallback.EVENT.register { context, delta ->
            weaponWheel.render(context, delta);
        }
    }
}
