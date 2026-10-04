package io.github.chandu4221.m3stage.model

@JvmInline
value class PropId(val value: String)

sealed class PropKey<V : PropVal>(open val id: PropId) {
    data class StrKey(override val id: PropId) : PropKey<PropVal.Str>(id)
    data class BoolKey(override val id: PropId) : PropKey<PropVal.Bool>(id)
    data class NumKey(override val id: PropId) : PropKey<PropVal.Num>(id)
    data class DpKey(override val id: PropId) : PropKey<PropVal.DpVal>(id)
    data class SpKey(override val id: PropId) : PropKey<PropVal.SpVal>(id)
    data class EnumKey(override val id: PropId) : PropKey<PropVal.EnumVal>(id)
    data class ColorKey(override val id: PropId) : PropKey<PropVal.ColorVal>(id)
    data class TypographyKey(override val id: PropId) : PropKey<PropVal.TypographyVal>(id)
    data class ShapeKey(override val id: PropId) : PropKey<PropVal.ShapeVal>(id)
}