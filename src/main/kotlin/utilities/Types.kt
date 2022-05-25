package utilities

import kotlin.reflect.KClass

sealed class TypedValue {
    abstract val value: Any?

    class tInt(override var value: Int?): TypedValue()
    class tFloat(override var value: Double?): TypedValue()
    class tString(override var value: String?) : TypedValue()
    class tBool(override var value: Boolean?) : TypedValue()
    class tUnit(override val value: Unit? = null): TypedValue()

    companion object {
        fun tNumber(value: Number?): TypedValue {
            return if (value is Int?) {
                tInt(value)
            } else {
                tFloat(value?.toDouble())
            }
        }
    }
}

fun KClass<out Any>.toTypedNull(): TypedValue {
    return when (this) {
        Int::class -> TypedValue.tInt(null)
        Double::class -> TypedValue.tFloat(null)
        Boolean::class -> TypedValue.tBool(null)
        String::class -> TypedValue.tString(null)
        Unit::class -> TypedValue.tUnit(null)
        else -> throw IllegalStateException()
    }
}
