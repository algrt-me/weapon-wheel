package me.algrt.weaponwheel.animation

class Animation(
    private val easingCallback: EasingCallback,
    private var max: Float = 0f
) {
    private var totalDelta = 0f
    private var min: Float = 0f
    private var duration: Float = 0f
    private var animating = false

    fun isAnimating(): Boolean {
        return this.animating;
    }

    fun start(duration: Float, max: Float) {
        this.totalDelta = 0f
        this.min = this.max
        this.max = max
        this.duration = duration
        this.animating = true
    }

    fun getValue(delta: Float): Float {
        this.totalDelta += delta

        if (!this.animating || this.totalDelta > this.duration) {
            this.animating = false
            return this.max
        }

        return this.easingCallback(this.totalDelta, this.min, this.max, this.duration)
    }
}
