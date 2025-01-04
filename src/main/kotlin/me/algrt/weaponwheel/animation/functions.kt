package me.algrt.weaponwheel.animation

typealias EasingCallback = (deltaTime: Float, min: Float, max: Float, animationDuration: Float) -> Float

val linearEase: EasingCallback = fun(deltaTime: Float, min: Float, max: Float, animationDuration: Float): Float {
    val t = deltaTime / animationDuration
    return min + t * (max - min)
}

val easeInQuad: EasingCallback = fun(deltaTime: Float, min: Float, max: Float, animationDuration: Float): Float {
    val t = deltaTime / animationDuration
    return min + (max - min) * t * t
}

val easeInOutSine: EasingCallback = fun(deltaTime: Float, min: Float, max: Float, animationDuration: Float): Float {
    val t = deltaTime / animationDuration
    return min + (max - min) * (-0.5f * (kotlin.math.cos(Math.PI * t) - 1)).toFloat()
}
