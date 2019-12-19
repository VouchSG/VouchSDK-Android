package sg.vouch.vouchsdk.ui.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.item_vouch_button.view.*
import kotlinx.android.synthetic.main.item_vouch_gallery.view.*
import kotlinx.android.synthetic.main.item_vouch_list.view.*
import kotlinx.android.synthetic.main.item_vouch_my_chat.view.*
import kotlinx.android.synthetic.main.item_vouch_other_chat.view.*
import kotlinx.android.synthetic.main.item_vouch_quick_reply.view.*
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.ui.VouchChatClickListener
import sg.vouch.vouchsdk.ui.VouchChatViewModel
import sg.vouch.vouchsdk.ui.model.VouchChatModel
import sg.vouch.vouchsdk.ui.model.VouchChatType.*
import sg.vouch.vouchsdk.utils.*
import sg.vouch.vouchsdk.utils.Helper.getAudioId
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-03
 */
class VouchChatAdapter(
    private val mViewModel: VouchChatViewModel,
    private val mListener: VouchChatClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mData: List<VouchChatModel> = mViewModel.bDataChat
    private var viewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
    private var mChildViewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VouchChatItem) {
            holder.bind(mData[position], mViewModel, mListener, viewPool, mChildViewPool)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return if (viewType == R.layout.item_vouch_loading) {
            VouchChatLoading(view)
        } else {
            VouchChatItem(view)
        }
    }

    override fun getItemCount(): Int {
        return mData.size + (if (!mViewModel.isLastPage && mData.isNotEmpty()) 1 else 0)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mData.size && !mViewModel.isLastPage) {
            R.layout.item_vouch_loading
        } else {
            when (mData[position].type) {
                TYPE_BUTTON -> R.layout.item_vouch_button
                TYPE_QUICK_REPLY -> R.layout.item_vouch_quick_reply
                TYPE_GALLERY -> R.layout.item_vouch_gallery
                TYPE_LIST -> R.layout.item_vouch_list
                else -> if (mData[position].isMyChat) R.layout.item_vouch_my_chat else R.layout.item_vouch_other_chat
            }
        }
    }

    inner class VouchChatItem(private val mView: View) : RecyclerView.ViewHolder(mView) {

        fun bind(
            data: VouchChatModel,
            viewModel: VouchChatViewModel,
            mListener: VouchChatClickListener,
            viewPool: RecyclerView.RecycledViewPool,
            mChildViewPool: RecyclerView.RecycledViewPool
        ) {
            mView.apply {
                when (data.type) {
                    TYPE_QUICK_REPLY -> {
                        recyclerQuickReply.layoutManager =
                            FlexboxLayoutManager(mView.context).apply {
                                flexDirection = FlexDirection.ROW
                                justifyContent = JustifyContent.CENTER
                            }
                        recyclerQuickReply.adapter = VouchQuickReplyAdapter(
                            viewModel,
                            mListener
                        ).apply { mData.addAll(data.quickReplies); notifyDataSetChanged() }
                    }
                    TYPE_BUTTON -> {
                        vouchButtonItem.text = data.title
                        vouchButtonItem.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                        vouchButtonItem.setTextColor(viewModel.loadConfiguration.value?.headerBgColor.parseColor())
                        borderButton.setColorFilter(
                            viewModel.loadConfiguration.value?.headerBgColor.parseColor(),
                            PorterDuff.Mode.SRC_IN
                        )
                        buttonItem.setOnClickListener {
                            mListener.onClickChatButton(
                                data.typeValue,
                                data
                            )
                        }
                        if (data.isLastListContent) {
                            buttonDateTime.text =
                                data.createdAt.safe().reformatFullDate("EEE, dd MMM HH:mm:ss")
                            buttonDateTime.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                            buttonDateTime.visibility = View.VISIBLE
                        }else {
                            buttonDateTime.visibility = View.GONE
                        }
                    }
                    TYPE_GALLERY -> {
                        recyclerGallery.layoutManager = LinearLayoutManager(
                            mView.context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        recyclerGallery.adapter = VouchGalleryAdapter(
                            viewModel,
                            mListener,
                            mChildViewPool
                        ).apply { mData.addAll(data.galleryElements); notifyDataSetChanged() }
                        recyclerGallery.setRecycledViewPool(viewPool)
                    }
                    TYPE_LIST -> {
                        separatorView.visibility =
                            if (data.isLastListContent) View.GONE else View.VISIBLE
                        titleList.text = data.title
                        titleList.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())

                        vouchButtonList.setTextColor(viewModel.loadConfiguration.value?.headerBgColor.parseColor())
                        borderButtonList.setColorFilter(
                            viewModel.loadConfiguration.value?.headerBgColor.parseColor(),
                            PorterDuff.Mode.SRC_IN
                        )
                        buttonList.setOnClickListener {
                            mListener.onClickChatButton(data.typeValue, data)
                        }

                        if (data.isFirstListContent) {
                            (parentListContent.layoutParams as RecyclerView.LayoutParams).topMargin = 32
                            roundedTop.visibility = View.VISIBLE
                        } else {
                            (parentListContent.layoutParams as RecyclerView.LayoutParams).topMargin = 0
                            roundedTop.visibility = View.GONE
                        }

                        if (data.isLastListContent) {
                            roundedBottom.visibility = View.VISIBLE
                        } else {
                            roundedBottom.visibility = View.GONE
                        }

                        if (!data.isHaveOutsideButton && data.isLastListContent) {
                            listDateTime.text =
                                data.createdAt.safe().reformatFullDate("EEE, dd MMM HH:mm:ss")
                            listDateTime.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                            listDateTime.visibility = View.VISIBLE
                        } else {
                            listDateTime.visibility = View.GONE
                        }

                        if (data.subTitle.isEmpty()) {
                            subTitleList.visibility = View.GONE
                        } else {
                            subTitleList.text = data.subTitle
                            subTitleList.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                            subTitleList.visibility = View.VISIBLE
                        }

                        if (data.buttonTitle.isEmpty()) {
                            buttonList.visibility = View.GONE
                        } else {
                            vouchButtonList.text = data.buttonTitle
                            vouchButtonList.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                            buttonList.visibility = View.VISIBLE
                        }

                        if (data.mediaUrl.isEmpty()) {
                            imageList.visibility = View.GONE
                        } else {
                            imageList.setImageUrl(data.mediaUrl)
                            imageList.visibility = View.VISIBLE
                        }
                    }
                    else -> if (data.isMyChat) {
                        myCardBubble.setCardBackgroundColor(viewModel.loadConfiguration.value?.rightBubbleBgColor.parseColor())
                        myChatContent.setTextColor(viewModel.loadConfiguration.value?.rightBubbleColor.parseColor())
                        pendingTime.setColorFilter(
                            viewModel.loadConfiguration.value?.sendIconColor.parseColor(),
                            PorterDuff.Mode.SRC_IN
                        )

                        when (data.type) {
                            TYPE_TEXT -> {
                                myChatContent.text = data.title
                                myChatContent.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                                myCardBubble.setContentPadding(0, 0, 0, 0)
                                retry.setOnClickListener {
                                    mListener.onClickRetryMessage(MessageBodyModel(type = data.typeValue,
                                        msgType = "text",
                                        text = data.title, payload = data.payload.safe()))
                                }
                            }
                            TYPE_IMAGE -> {
                                val density = mView.context?.resources?.displayMetrics?.density
                                val padding = (4 * density!!).toInt()
                                myCardBubble.setContentPadding(padding, padding, padding, padding)
                                myChatImage.setImageUrl(data.mediaUrl)
                                myChatImage.setOnClickListener {
                                    mListener.onClickPlayVideo(
                                        data,
                                        it as ImageView,
                                        data.type
                                    )
                                }
                                retry.setOnClickListener {
                                    mListener.onClickRetryMedia(data.msgType, data.body!!, data.path)
                                }
                            }
                            TYPE_VIDEO -> {
                                myImageVideo.setImageUrl(data.mediaUrl)
                                myImageVideo.setOnClickListener {
                                    mListener.onClickPlayVideo(
                                        data,
                                        it as ImageView,
                                        data.type
                                    )
                                }
                                retry.setOnClickListener {
                                    mListener.onClickRetryMedia(data.msgType, data.body!!, data.path)
                                }
                            }
                            else -> {}
                        }

                        myChatImage.visibility = if (TYPE_IMAGE == data.type) View.VISIBLE else View.GONE
                        myChatContent.visibility = if (TYPE_TEXT == data.type) View.VISIBLE else View.GONE
                        myPackVideo.visibility = if (TYPE_VIDEO == data.type) View.VISIBLE else View.GONE

                        myDateTime.text = data.createdAt.safe().reformatFullDate("EEE, dd MMM HH:mm:ss")
                        myDateTime.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                        if(data.isFailedMessage){
                            retry.visibility = View.VISIBLE
                            myDateTime.visibility = View.GONE
                            pendingTime.visibility = View.GONE
                        }else{
                            retry.visibility = View.GONE
                            pendingTime.visibility =
                                if (data.isPendingMessage) View.VISIBLE else View.GONE
                            myDateTime.visibility =
                                if (!data.isPendingMessage) View.VISIBLE else View.GONE
                        }


                    } else {
                        cardBubble.visibility = View.GONE
                        imageContent.visibility = View.GONE
                        imageVideo.visibility = View.GONE
                        cardAudio.visibility = View.GONE
                        packVideo.visibility = View.GONE
                        thinking.visibility = View.GONE

                        dateTime.visibility = View.VISIBLE

                        when {
                            data.type == TYPE_IMAGE -> {
                                imageContent.setImageUrl(data.mediaUrl)
                                imageContent.visibility = View.VISIBLE
                                imageContent.setOnClickListener {
                                    mListener.onClickPlayVideo(
                                        data,
                                        it as ImageView,
                                        data.type
                                    )
                                }
                            }
                            data.type == TYPE_VIDEO -> {
                                packVideo.visibility = View.VISIBLE
                                imageVideo.visibility = View.VISIBLE
                                imageVideo.setImageUrl(data.mediaUrl)
                                imageVideo.setOnClickListener {
                                    mListener.onClickPlayVideo(
                                        data,
                                        it as ImageView,
                                        data.type
                                    )
                                }
                                playVideo.setOnClickListener {
                                    mListener.onClickPlayVideo(data, it as ImageView, data.type)
                                }
                            }
                            data.type == TYPE_AUDIO && data.mediaUrl.isNotEmpty() -> {
                                cardAudio.visibility = View.VISIBLE
                                audioText.setTextColor(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor())
                                playAudio.setColorFilter(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor(), PorterDuff.Mode.SRC_IN)
                                seekbar.progressDrawable.setColorFilter(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor(), PorterDuff.Mode.SRC_ATOP)
                                seekbar.thumb.setColorFilter(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor(), PorterDuff.Mode.SRC_ATOP)

                                try {
                                    if (viewModel.mMediaPlayer?.isPlaying == true && viewModel.currentAudioMedia != VouchChatModel()
                                        && getAudioId(viewModel.currentAudioMedia) == getAudioId(data)) {
                                        seekbar.incrementProgressBy(1)
                                        seekbar.max = viewModel.mMediaPlayer?.duration ?: 0
                                        playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_pause_black_24dp))
                                        mListener.setupMediaPlayer(viewModel.mMediaPlayer ?: MediaPlayer())
                                        mListener.onClickPlayAudio("")
                                    } else if (viewModel.currentAudioMedia != VouchChatModel()
                                        && getAudioId(viewModel.currentAudioMedia) == getAudioId(data)
                                        && (viewModel.audioSeek[getAudioId(viewModel.currentAudioMedia)] ?: 0) > 0) {
                                        playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                                        val audioSeek = viewModel.audioSeek[getAudioId(viewModel.currentAudioMedia)] ?: 0
                                        audioText.text =
                                            "${Helper.timeUnitToString(TimeUnit.MILLISECONDS.toMinutes(audioSeek.toLong()))}:${Helper.timeUnitToString(
                                                TimeUnit.MILLISECONDS.toSeconds(audioSeek.toLong()) - TimeUnit.MINUTES.toSeconds(
                                                    TimeUnit.MILLISECONDS.toMinutes(audioSeek.toLong())
                                                )
                                            )}"

                                        seekbar.incrementProgressBy(1)
                                        seekbar.max = viewModel.mMediaPlayer?.duration ?: 0
                                        seekbar.progress = audioSeek
                                    } else {
                                        playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                                        seekbar.incrementProgressBy(1)
                                        seekbar.max = viewModel.mMediaPlayer?.duration ?: 0
                                        seekbar.progress = 0
                                        audioText.text = "00:00"
                                    }

                                    playAudio.setOnClickListener {
                                        if (viewModel.currentAudioMedia != VouchChatModel()
                                            && getAudioId(viewModel.currentAudioMedia) != getAudioId(data)) {
                                            mListener.resetMediaPlayer()
                                        }
                                        if (viewModel.mMediaPlayer?.isPlaying == true) {
                                            playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                                            viewModel.mMediaPlayer?.pause()
                                        } else {
                                            createMediaPlayer(context, data, this, viewModel, mListener)
                                            playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_pause_black_24dp))
                                            viewModel.mMediaPlayer?.seekTo(
                                                if (viewModel.audioSeek[getAudioId(viewModel.currentAudioMedia)] == viewModel.mMediaPlayer?.duration) {
                                                    0
                                                } else {
                                                    viewModel.audioSeek[getAudioId(viewModel.currentAudioMedia)] ?: 0
                                                }
                                            )
                                            viewModel.mMediaPlayer?.start()
                                            mListener.onClickPlayAudio("")
                                        }
                                    }

                                } catch (e: IOException) {

                                }
                            }
                            data.type == TYPE_TYPING -> {
                                thinking.visibility = View.VISIBLE
                                dateTime.visibility = View.GONE
                            }
                            else -> {
                                cardBubble.visibility = View.VISIBLE
                            }
                        }

                        chatContent.text = data.title
                        dateTime.text =
                            data.createdAt.safe().reformatFullDate("EEE, dd MMM HH:mm:ss")
                        chatContent.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                        dateTime.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())

                        cardBubble.setCardBackgroundColor(viewModel.loadConfiguration.value?.leftBubbleBgColor.parseColor())
                        chatContent.setTextColor(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor())
                    }
                }
            }
        }

        private fun createMediaPlayer(context: Context, data: VouchChatModel, view: View, mViewModel: VouchChatViewModel, mListener: VouchChatClickListener) {
            mViewModel.mMediaPlayer = MediaPlayer()
            mViewModel.mMediaPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mViewModel.mMediaPlayer?.setDataSource(data.mediaUrl)
            mViewModel.mMediaPlayer?.prepare()

            mViewModel.currentAudioMedia = data

            view.apply {
                mViewModel.mMediaPlayer?.setOnPreparedListener {
                    mViewModel.mMediaPlayer = it
                    seekbar.incrementProgressBy(1)
                    seekbar.max = mViewModel.mMediaPlayer?.duration ?: 0
                    audioText.text = "00:00"
                    mListener.setupMediaPlayer(mViewModel.mMediaPlayer ?: MediaPlayer())
                }
                mViewModel.mMediaPlayer?.setOnCompletionListener {
                    audioText.text = "00:00"
                    playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                }
            }
        }

    }

    class VouchChatLoading(private val mView: View) : RecyclerView.ViewHolder(mView)
}