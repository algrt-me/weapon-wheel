package me.algrt.weaponwheel.animation

class AnimationController<TKey> {
    private val animations = mutableMapOf<TKey, Animation>()

    fun addAnimation(key: TKey, animation: Animation) {
        this.animations[key] = animation
    }

    fun isAnimating(): Boolean {
        for (entry in this.animations.iterator()) {
            if (!entry.value.isAnimating()) continue
            return true
        }

        return false
    }

    fun start(key: TKey, duration: Float, max: Float) {
        val animation = this.animations[key]
            ?: throw Exception("animation not registered")

        animation.start(duration, max)
    }

    fun getValue(key: TKey, delta: Float): Float {
        val animation = this.animations[key]
            ?: throw Exception("animation not registered")

        return animation.getValue(delta)
    }
}
