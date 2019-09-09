package id.gits.vouchsdk.ui

import id.gits.vouchsdk.ui.model.VouchChatModel

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-06
 */
interface VouchChatClickListener {

    fun onClickChatButton(type: String, data: VouchChatModel)

}