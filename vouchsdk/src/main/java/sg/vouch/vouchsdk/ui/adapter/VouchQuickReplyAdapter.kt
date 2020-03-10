package sg.vouch.vouchsdk.ui.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.data.model.message.response.QuickReplyModel
import sg.vouch.vouchsdk.ui.VouchChatClickListener
import sg.vouch.vouchsdk.ui.VouchChatViewModel
import sg.vouch.vouchsdk.utils.parseColor
import sg.vouch.vouchsdk.utils.safe
import sg.vouch.vouchsdk.utils.setFontFamily
import kotlinx.android.synthetic.main.item_quick_reply.view.*

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-05
 */
class VouchQuickReplyAdapter(val mViewModel: VouchChatViewModel, val mListener: VouchChatClickListener) :
    RecyclerView.Adapter<VouchQuickReplyAdapter.VouchQuickReplyItem>() {

    val mData: MutableList<QuickReplyModel> = mutableListOf()

    override fun onBindViewHolder(holder: VouchQuickReplyItem, position: Int) {
        holder.bind(mData[position], mViewModel, mListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VouchQuickReplyItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quick_reply, null, false)
        return VouchQuickReplyItem(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }


    class VouchQuickReplyItem(val mView: View) : RecyclerView.ViewHolder(mView) {

        fun bind(data: QuickReplyModel, viewModel: VouchChatViewModel, mListener: VouchChatClickListener) {
            mView.quickReplyContent.background.setColorFilter(viewModel.loadConfiguration.value?.headerBgColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
            mView.quickReplyContent.setTextColor(viewModel.loadConfiguration.value?.headerBgColor.parseColor(Color.BLACK))
            mView.quickReplyContent.text = if(data.contentType.equals("location", true)) "Send Location" else data.title.safe()
            mView.quickReplyContent.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
            mView.quickReplyContent.setOnClickListener {
                mListener.onClickQuickReply(MessageBodyModel(type = "quick_reply", msgType = data.contentType.safe("text"), text = data.title, payload = data.payload.safe()))
            }
        }

    }

}