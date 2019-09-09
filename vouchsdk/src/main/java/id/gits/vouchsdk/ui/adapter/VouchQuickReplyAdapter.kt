package id.gits.vouchsdk.ui.adapter

import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import id.gits.vouchsdk.R
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.data.model.message.response.QuickReplyModel
import id.gits.vouchsdk.ui.VouchChatViewModel
import id.gits.vouchsdk.utils.parseColor
import id.gits.vouchsdk.utils.safe
import kotlinx.android.synthetic.main.item_quick_reply.view.*

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-05
 */
class VouchQuickReplyAdapter(val mViewModel: VouchChatViewModel) :
    RecyclerView.Adapter<VouchQuickReplyAdapter.VouchQuickReplyItem>() {

    val mData: MutableList<QuickReplyModel> = mutableListOf()

    override fun onBindViewHolder(holder: VouchQuickReplyItem, position: Int) {
        holder.bind(mData[position], mViewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VouchQuickReplyItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quick_reply, null, false)
        return VouchQuickReplyItem(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }


    class VouchQuickReplyItem(val mView: View) : RecyclerView.ViewHolder(mView) {

        fun bind(data: QuickReplyModel, viewModel: VouchChatViewModel) {
            mView.quickReplyContent.background.setColorFilter(viewModel.loadConfiguration.value?.headerBgColor.parseColor(), PorterDuff.Mode.SRC_IN)
            mView.quickReplyContent.setTextColor(viewModel.loadConfiguration.value?.headerBgColor.parseColor())
            mView.quickReplyContent.text = data.title.safe()
            mView.quickReplyContent.setOnClickListener {
                viewModel.sendReplyMessage(MessageBodyModel(type = "quick_reply", msgType = "text", text = data.title, payload = data.payload.safe()))
            }
        }

    }

}