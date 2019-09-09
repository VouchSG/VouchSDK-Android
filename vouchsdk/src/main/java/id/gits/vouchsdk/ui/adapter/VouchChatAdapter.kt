package id.gits.vouchsdk.ui.adapter

import android.graphics.PorterDuff
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devbrackets.android.exomedia.ui.widget.VideoView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import id.gits.vouchsdk.R
import id.gits.vouchsdk.ui.VouchChatClickListener
import id.gits.vouchsdk.ui.VouchChatViewModel
import id.gits.vouchsdk.ui.model.VouchChatModel
import id.gits.vouchsdk.ui.model.VouchChatType.*
import id.gits.vouchsdk.utils.parseColor
import id.gits.vouchsdk.utils.reformatFullDate
import id.gits.vouchsdk.utils.setImageUrl
import kotlinx.android.synthetic.main.item_vouch_button.view.*
import kotlinx.android.synthetic.main.item_vouch_my_chat.view.*
import kotlinx.android.synthetic.main.item_vouch_other_chat.view.*
import kotlinx.android.synthetic.main.item_vouch_quick_reply.view.*


/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-03
 */
class VouchChatAdapter(private val mViewModel: VouchChatViewModel, private val mListener: VouchChatClickListener) : RecyclerView.Adapter<VouchChatAdapter.VouchChatItem>() {

    private var mData: List<VouchChatModel> = mViewModel.bDataChat

    override fun onBindViewHolder(holder: VouchChatItem, position: Int) {
        holder.bind(mData[position], mViewModel, mListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VouchChatItem {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return VouchChatItem(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onViewDetachedFromWindow(holder: VouchChatItem) {
        super.onViewDetachedFromWindow(holder)
        holder.recycledVideo()
    }

    override fun onViewRecycled(holder: VouchChatItem) {
        super.onViewRecycled(holder)
        holder.recycledVideo()
    }

    override fun getItemViewType(position: Int): Int {
        return when (mData[position].type) {
            TYPE_BUTTON -> {
                return R.layout.item_vouch_button
            }
            TYPE_QUICK_REPLY -> {
                return R.layout.item_vouch_quick_reply
            }
            else -> {
                if (mData[position].isMyChat) R.layout.item_vouch_my_chat else R.layout.item_vouch_other_chat
            }
        }
    }

    class VouchChatItem(private val mView: View) : RecyclerView.ViewHolder(mView) {

        fun bind(data: VouchChatModel, viewModel: VouchChatViewModel, mListener: VouchChatClickListener) {
            mView.apply {
                if (data.type == TYPE_QUICK_REPLY) {
                    recyclerQuickReply.layoutManager = FlexboxLayoutManager(mView.context).apply { flexDirection = FlexDirection.ROW; justifyContent = JustifyContent.CENTER; }
                    recyclerQuickReply.adapter = VouchQuickReplyAdapter(viewModel).apply { mData.addAll(data.quickReplies); notifyDataSetChanged() }
                } else if (data.type == TYPE_BUTTON){
                    vouchButtonItem.text = data.content
                    vouchButtonItem.setTextColor(viewModel.loadConfiguration.value?.headerBgColor.parseColor())
                    borderButton.setColorFilter(viewModel.loadConfiguration.value?.headerBgColor.parseColor(), PorterDuff.Mode.SRC_IN)
                    buttonItem.setOnClickListener { mListener.onClickChatButton(data.typeValue, data) }
                } else {


                    if (data.isMyChat) {
                        myChatContent.text = data.content
                        myDateTime.text = data.createdAt.reformatFullDate("EEE, dd MMM HH:mm:ss")
                        myCardBubble.setCardBackgroundColor(viewModel.loadConfiguration.value?.rightBubbleBgColor.parseColor())
                        myChatContent.setTextColor(viewModel.loadConfiguration.value?.rightBubbleColor.parseColor())
                    } else {

                        cardBubble.visibility = View.GONE
                        imageContent.visibility = View.GONE
                        videoView.visibility = View.GONE

                        when {
                            data.type == TYPE_IMAGE -> {
                                imageContent.setImageUrl(data.mediaUrl)
                                imageContent.visibility = View.VISIBLE
                            }
                            data.type == TYPE_VIDEO -> {
                                videoView.setVideoURI(Uri.parse(data.mediaUrl))
                                videoView.visibility = View.VISIBLE
                                videoView.setReleaseOnDetachFromWindow(false)
                                videoView.setOnPreparedListener { viewModel.start() }
                            }
                            else -> {
                                cardBubble.visibility = View.VISIBLE
                            }
                        }

                        chatContent.text = data.content
                        dateTime.text = data.createdAt.reformatFullDate("EEE, dd MMM HH:mm:ss")
                        cardBubble.setCardBackgroundColor(viewModel.loadConfiguration.value?.leftBubbleBgColor.parseColor())
                        chatContent.setTextColor(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor())
                    }
                }

            }
        }

        fun recycledVideo() {
            mView.findViewById<VideoView?>(R.id.videoView)?.setReleaseOnDetachFromWindow(false)
        }

    }

}