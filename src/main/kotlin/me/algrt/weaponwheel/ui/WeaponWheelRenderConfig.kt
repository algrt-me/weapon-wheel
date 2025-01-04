package me.algrt.weaponwheel.ui

interface WeaponWheelRenderConfig {
    val segmentsPerSlot: Int;
    val color: Long;
    val secondaryColor: Long;
    val equippedColor: Long;
    val hoverColor: Long;
    val slotGap: Float;
    val itemScale: Float;
}
