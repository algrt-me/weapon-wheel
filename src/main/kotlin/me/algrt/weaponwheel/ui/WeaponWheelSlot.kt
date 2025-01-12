package me.algrt.weaponwheel.ui

import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.VertexFormat
import com.mojang.blaze3d.systems.RenderSystem
import me.algrt.weaponwheel.animation.Animation
import me.algrt.weaponwheel.animation.AnimationController
import me.algrt.weaponwheel.animation.easeInQuad
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.model.json.ModelTransformationMode
import org.joml.Matrix4f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class SlotAnimation {
    SCALE,
    OPACITY
}

class WeaponWheelSlot(
    private val slotIndex: Int,
    private val parentWheel: WeaponWheel
) {
    private var isSelected = false
    private var isEquipped = false
    private var totalTickDelta = 0f
    private val animationController = AnimationController<SlotAnimation>()
    private var position: Pair<Float, Float> = Pair(0f, 0f)


    private val totalSegments: Int
    private val startSegment: Int
    private val endSegment: Int
    private val slotAngle: Double
    private var itemDistance: Float = 0f;

    init {
        animationController.addAnimation(SlotAnimation.SCALE, Animation(easeInQuad, 1f))
        animationController.addAnimation(SlotAnimation.OPACITY, Animation(easeInQuad, 1f))

        val renderConfig = this.parentWheel.renderConfig
        this.totalSegments = renderConfig.segmentsPerSlot * this.parentWheel.slotCount;
        this.startSegment = slotIndex * renderConfig.segmentsPerSlot
        this.endSegment = startSegment + renderConfig.segmentsPerSlot
        this.slotAngle = this.getSegmentAngle(startSegment + (renderConfig.segmentsPerSlot / 2), totalSegments)
    }

    fun onWheelOpen() {
        this.animationController.start(SlotAnimation.SCALE, 1f, 1f)
        this.animationController.start(SlotAnimation.OPACITY, 1f, 0xB0.toFloat())
    }

    fun onWheelClose() {
        this.animationController.start(SlotAnimation.SCALE, 1f, 0.5f)
        this.animationController.start(SlotAnimation.OPACITY, 1f, 0f)
    }

    fun setSelected(selected: Boolean) {
        if (this.isSelected == selected) return

        this.isSelected = selected
        if (selected) {
            this.animationController.start(SlotAnimation.SCALE, 2.5f, 1.2f)
            this.animationController.start(SlotAnimation.OPACITY, 2.5f, 0xF0.toFloat())
        } else {
            this.animationController.start(SlotAnimation.SCALE, 2.5f, 1f)
            this.animationController.start(SlotAnimation.OPACITY, 2.5f, 0xB0.toFloat())
        }
    }

    fun setEquipped(equipped: Boolean) {
        this.isEquipped = equipped
    }

    fun render(drawContext: DrawContext, delta: Float) {
        if (!this.parentWheel.isOpen && !this.animationController.isAnimating())
            return

        val client = MinecraftClient.getInstance()
        this.itemDistance = this.parentWheel.innerRadius + ((this.parentWheel.outerRadius - this.parentWheel.innerRadius) / 1.5).toFloat()
        this.position = Pair(
            (client.window.scaledWidth / 2).toFloat(),
            (client.window.scaledHeight / 2).toFloat()
        )

        totalTickDelta += delta;

        val matrices = drawContext.matrices
        matrices.push()
        matrices.translate(this.position.first, this.position.second, 0f);

        val scaleAmount = this.animationController.getValue(SlotAnimation.SCALE, delta)
        matrices.scale(scaleAmount, scaleAmount, 1f)

        this.renderBackground(drawContext, delta)
        this.renderItem(drawContext, delta)

        matrices.pop()
    }

    private fun renderBackground(drawContext: DrawContext, delta: Float) {
        val matrices = drawContext.matrices
        val transformationMatrix = matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        val renderConfig = this.parentWheel.renderConfig
        val color = this.getSlotColorOpacity(delta).toInt()

        val offsetX = renderConfig.slotGap * cos(this.slotAngle)
        val offsetY = renderConfig.slotGap * sin(this.slotAngle)

        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR)

        val innerRadius = this.parentWheel.innerRadius
        val outerRadius = this.parentWheel.outerRadius

        matrices.push()

        for (i in this.startSegment .. this.endSegment) {
            val angle = this.getSegmentAngle(i, this.totalSegments)

            this.drawSegmentVertex(buffer, transformationMatrix, offsetX, offsetY, angle, outerRadius, color)
            this.drawSegmentVertex(buffer, transformationMatrix, offsetX, offsetY, angle, innerRadius, color)
        }

        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

        tessellator.draw()
        matrices.pop()
    }

    private fun renderItem(drawContext: DrawContext, delta: Float) {
        val matrices = drawContext.matrices
        val client = MinecraftClient.getInstance()
        val stack = client.player?.inventory?.getStack(this.slotIndex)
        if (stack == null || stack.isEmpty) return

        val model = client.itemRenderer.models.getModel(stack.item) ?: return

        val bl = !model.isSideLit
        if (bl) DiffuseLighting.disableGuiDepthLighting()

        matrices.push()

        matrices.translate(this.itemDistance * cos(this.slotAngle).toFloat(), this.itemDistance * sin(this.slotAngle).toFloat(), 150f)
        // matrices.translate(8.0, 8.0, 0.0);
        matrices.multiplyPositionMatrix(Matrix4f().scaling(1.0F, -1.0F, 1.0F));

        val itemScale = this.parentWheel.itemScale
        matrices.scale(itemScale, itemScale, itemScale)

        client.itemRenderer.renderItem(
            stack,
            ModelTransformationMode.GUI,
            false,
            matrices,
            drawContext.vertexConsumers,
            0xF000F0,
            OverlayTexture.DEFAULT_UV,
            model
        )

        matrices.pop()
    }

    private fun getSlotColorOpacity(delta: Float): Long {
        val slotColor = this.getSlotColor()
        val opacity: Long = this.animationController.getValue(SlotAnimation.OPACITY, delta).toLong()
        return (opacity * 0x1000000) + slotColor
    }

    private fun getSlotColor(): Long {
        val renderConfig = this.parentWheel.renderConfig

        if (this.isSelected)
            return renderConfig.hoverColor;

        if (this.isEquipped)
            return renderConfig.equippedColor;

        if (this.slotIndex % 2 == 0)
            return renderConfig.color;

        return renderConfig.secondaryColor;
    }

    private fun getSegmentAngle(segment: Int, total: Int): Double {
        return 2 * PI * segment / total - Math.toRadians(90.0);
    }

    private fun drawSegmentVertex(
        buffer: BufferBuilder,
        transformationMatrix: Matrix4f,
        offsetX: Double,
        offsetY: Double,
        angle: Double,
        radius: Float,
        color: Int
    ) {
        val x = (offsetX + cos(angle) * radius).toFloat()
        val y = (offsetY + sin(angle) * radius).toFloat()

        buffer.vertex(transformationMatrix, x, y, 0f).color(color).next()
    }
}
