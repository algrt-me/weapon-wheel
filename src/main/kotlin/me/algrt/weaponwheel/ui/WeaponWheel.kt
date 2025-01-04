package me.algrt.weaponwheel.ui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import kotlin.math.atan2
import kotlin.math.pow

class WeaponWheel(val renderConfig: WeaponWheelRenderConfig, val slotCount: Int) {
    private var equippedSlotIndex: Int? = null;
    private var selectedSlotIndex: Int? = null;
    var isOpen = false;
    private var mouseX = 0.0
    private var mouseY = 0.0

    var innerRadius = 0f
    var outerRadius = 0f
    var itemScale = 16.0f

    private val slots: Array<WeaponWheelSlot> = Array(slotCount) {
        i -> WeaponWheelSlot(i, this)
    };

    fun open() {
        if (this.isOpen) return;

        this.equippedSlotIndex = MinecraftClient.getInstance().player!!.inventory.selectedSlot
        this.selectedSlotIndex = null
        this.isOpen = true

        this.slots.forEachIndexed { i, slot ->
            slot.setEquipped(this.equippedSlotIndex == i)
            slot.setSelected(this.selectedSlotIndex == i)
            slot.onWheelOpen()
        }
    }

    fun close() {
        if (!this.isOpen) return;
        this.isOpen = false
        this.slots.forEach { slot -> slot.onWheelClose() }
    }

    fun setMousePosition(x: Double, y: Double) {
        this.mouseX = x
        this.mouseY = y
    }

    fun render(context: DrawContext, delta: Float) {
        this.handleWindowSize()
        this.handleSlotSelection()

        slots.forEachIndexed { i, slot ->
            slot.setSelected(this.selectedSlotIndex == i)
            slot.setEquipped(this.equippedSlotIndex == i)
            slot.render(context, delta)
        }
    }

    private fun handleWindowSize() {
        val client = MinecraftClient.getInstance()
        val width = client.window.scaledWidth
        val height = client.window.scaledHeight

        val defaultInnerRadius = 30f
        val defaultOuterRadius = 100f
        val defaultItemScale = 25.0f

        val maxRadiusWidth = width * 0.5f * 0.5f
        val maxRadiusHeight = height * 0.5f * 0.5f

        val maxAllowedRadius = minOf(maxRadiusWidth, maxRadiusHeight)

        val scaleFactor: Float =
            if (defaultOuterRadius > maxAllowedRadius) { maxAllowedRadius / defaultOuterRadius } else { 1f }

        this.innerRadius = scaleFactor * defaultInnerRadius
        this.outerRadius = scaleFactor * defaultOuterRadius
        this.itemScale = scaleFactor * defaultItemScale
    }

    private fun handleSlotSelection() {
        if (!isOpen) return

        val player = MinecraftClient.getInstance().player!!

        // Handle manual selectedSlot change while wheel is open
        val currentSelectedSlot = player.inventory.selectedSlot
        if (currentSelectedSlot != this.equippedSlotIndex) {
            this.equippedSlotIndex = currentSelectedSlot
        }

        // Actual weapon slot selection
        val hoveredSlotIndex = this.calcHoveredSlot(this.mouseX, this.mouseY)
        if (hoveredSlotIndex == this.selectedSlotIndex) return

        this.selectedSlotIndex = hoveredSlotIndex

        if (hoveredSlotIndex != null) {
            player.inventory.selectedSlot = hoveredSlotIndex
        } else if (this.equippedSlotIndex != null) {
            player.inventory.selectedSlot = this.equippedSlotIndex!!
        }
    }

    private fun calcHoveredSlot(mouseX: Double, mouseY: Double): Int? {
        val client = MinecraftClient.getInstance()

        val deltaX = mouseX - (client.window.width / 2)
        val deltaY = mouseY - (client.window.height / 2)
        var angle = atan2(deltaX, deltaY)

        // Handle center deadzone
        val distanceSqr = deltaX.pow(2) + deltaY.pow(2)
        val maxDistanceScaled = (this.innerRadius + this.renderConfig.slotGap) * client.window.scaleFactor
        if (distanceSqr < maxDistanceScaled.pow(2)) return null

        val anglePerSlot = Math.toRadians(360.0) / this.slots.size
        angle -= Math.toRadians(180.0)

        if (angle < 0) angle += 2 * Math.PI

        return this.slots.size - (angle / anglePerSlot).toInt() - 1
    }
}
