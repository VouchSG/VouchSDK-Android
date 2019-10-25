package sg.vouch.vouchsdk.ui

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-03
 */

open class VouchChatUpdateEvent(val type: VouchChatEnum, val startPosition: Int, val endPosition: Int? = null)

enum class VouchChatEnum {
    TYPE_INSERTED,
    TYPE_UPDATE,
    TYPE_REMOVE,
    TYPE_FORCE_UPDATE,
}