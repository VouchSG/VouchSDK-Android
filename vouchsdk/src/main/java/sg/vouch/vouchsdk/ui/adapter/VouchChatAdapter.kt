package sg.vouch.vouchsdk.ui.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import okhttp3.MultipartBody
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.ui.VouchChatClickListener
import sg.vouch.vouchsdk.ui.VouchChatViewModel
import sg.vouch.vouchsdk.ui.model.VouchChatModel
import sg.vouch.vouchsdk.ui.model.VouchChatType.*
import sg.vouch.vouchsdk.utils.*
import sg.vouch.vouchsdk.utils.Helper.getAudioId
import java.io.IOException
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
            holder.bind(mData[position], mViewModel, mListener, viewPool, mChildViewPool, position)
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
            mChildViewPool: RecyclerView.RecycledViewPool,
            position: Int
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
                        vouchButtonItem.setTextColor(viewModel.loadConfiguration.value?.headerBgColor.parseColor(
                            Color.BLACK))
                        borderButton.setColorFilter(
                            viewModel.loadConfiguration.value?.headerBgColor.parseColor(Color.BLACK),
                            PorterDuff.Mode.SRC_IN
                        )
                        buttonItem.setOnClickListener {
                            mListener.onClickChatButton(
                                data.typeValue,
                                data
                            )
                        }

                        if (data.isLastListContent) {
                            buttonDateTime.text = data.createdAt.safe().reformatFullDate("EEE, dd MMM HH:mm:ss")
                            buttonDateTime.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                            buttonDateTime.visibility = View.VISIBLE
                        } else {
                            buttonDateTime.visibility = View.GONE
                        }
                    }
                    TYPE_GALLERY -> {
                        recyclerGallery.layoutManager =
                            LinearLayoutManager(
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

                        vouchButtonList.setTextColor(viewModel.loadConfiguration.value?.headerBgColor.parseColor(Color.BLACK))
                        borderButtonList.setColorFilter(
                            viewModel.loadConfiguration.value?.headerBgColor.parseColor(Color.BLACK),
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
                        myCardBubble.setCardBackgroundColor(viewModel.loadConfiguration.value?.rightBubbleBgColor.parseColor(Color.BLACK))
                        myChatContent.setTextColor(viewModel.loadConfiguration.value?.rightBubbleColor.parseColor(Color.WHITE))

                        when (data.type) {
                            TYPE_TEXT -> {
                                retry.isEnabled = true
                                myChatContent.text = data.title
                                myChatContent.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                                myCardBubble.setContentPadding(0, 0, 0, 0)
                                retry.setOnClickListener {
                                    retry.isEnabled = false
                                    // Get real position from data chat
                                    val realPosition = mData.indexOf(data)
                                    mListener.onClickRetryMessage(MessageBodyModel(type = "text",
                                        msgType = "text",
                                        text = data.title, payload = null), realPosition)
                                }
                            }
                            TYPE_IMAGE -> {
                                retry.isEnabled = true
                                if(data.imageUri == null) {
                                    myChatImage.setImageUrlwithoutCrop(data.mediaUrl)
                                } else {
                                    myChatImage.setImageUrlwithoutCrop(data.imageUri)
                                }

                                myChatImage.setOnClickListener {
                                    mListener.onClickPlayVideo(
                                        data,
                                        it as ImageView,
                                        data.type
                                    )
                                }

                                retry.setOnClickListener {
                                    retry.isEnabled = false
                                    // Get real position from data chat
                                    val realPosition = mData.indexOf(data)
                                    mListener.onClickRetryMedia(data.msgType, data.body!!, data.path, realPosition, data.imageUri)
                                }
                            }
                            TYPE_VIDEO -> {
                                retry.isEnabled = true
                                myImageVideo.setVideoUrlwithoutCrop(data.mediaUrl, myPlayVideo, myProgressVideo)
                                myImageVideo.setOnClickListener {
                                    mListener.onClickPlayVideo(
                                        data,
                                        it as ImageView,
                                        data.type
                                    )
                                }
                                myPlayVideo.setOnClickListener {
                                    mListener.onClickPlayVideo(
                                        data,
                                        it as ImageView,
                                        data.type
                                    )
                                }
                                retry.setOnClickListener {
                                    retry.isEnabled = false
                                    // Get real position from data chat
                                    val realPosition = mData.indexOf(data)
                                    mListener.onClickRetryMedia(data.msgType, mData[realPosition].body?: MultipartBody.Part.createFormData("", ""), data.path, realPosition)
                                }
                                myPlayVideo.setColorFilter(viewModel.loadConfiguration.value?.rightBubbleBgColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
                                myPlayVideo.background.setColorFilter(viewModel.loadConfiguration.value?.leftBubbleBgColor.parseColor(Color.WHITE), PorterDuff.Mode.SRC_IN)
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
                            pendingTime.visibility = View.VISIBLE
                            checklist.visibility = View.GONE
                        }else{
                            retry.visibility = View.GONE
                            pendingTime.visibility =
                                if (data.isPendingMessage) View.VISIBLE else View.GONE
                            myDateTime.visibility =
                                if (!data.isPendingMessage) View.VISIBLE else View.GONE
                            checklist.visibility =
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
                                imageContent.setImageUrlwithoutCrop(data.mediaUrl)
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
                                imageVideo.setVideoUrlwithoutCrop(data.mediaUrl, playVideo, progressVideo)
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

                                playVideo.setColorFilter(viewModel.loadConfiguration.value?.rightBubbleColor.parseColor(Color.WHITE), PorterDuff.Mode.SRC_IN)
                                playVideo.background.setColorFilter(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
                            }
                            data.type == TYPE_AUDIO && data.mediaUrl.isNotEmpty() -> {
                                cardAudio.visibility = View.VISIBLE
                                audioText.setTextColor(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor(Color.BLACK))
                                playAudio.setColorFilter(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
                                seekbar.progressDrawable.setColorFilter(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_ATOP)
                                seekbar.thumb.setColorFilter(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_ATOP)

                                val duration = viewModel.audioDuration[data.mediaUrl] ?: 0
                                if (duration > 0) {
                                    val time = viewModel.audioDuration[data.mediaUrl]?.toLong() ?: 0L
                                    audioText.text = "${Helper.timeUnitToString(
                                        TimeUnit.MILLISECONDS.toMinutes(time)
                                    )}:${Helper.timeUnitToString(
                                        TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(
                                            TimeUnit.MILLISECONDS.toMinutes(time)
                                        )
                                    )}"
                                } else {
                                    val mMediaPlayer = MediaPlayer()
                                    mMediaPlayer.setAudioAttributes(
                                        AudioAttributes.Builder()
                                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                            .build()
                                    )
                                    mMediaPlayer.setDataSource(data.mediaUrl)
                                    mMediaPlayer.setOnPreparedListener {
                                        viewModel.audioDuration[data.mediaUrl] = mMediaPlayer.duration
                                        val time = mMediaPlayer.duration.toLong()
                                        audioText.text = "${Helper.timeUnitToString(
                                            TimeUnit.MILLISECONDS.toMinutes(time)
                                        )}:${Helper.timeUnitToString(
                                            TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(
                                                TimeUnit.MILLISECONDS.toMinutes(time)
                                            )
                                        )}"
                                        this@VouchChatAdapter.notifyItemChanged(this@VouchChatAdapter.mData.indexOf(data))
                                    }
                                    mMediaPlayer.prepareAsync()
                                }

                                try {
                                    if (viewModel.mMediaPlayer?.isPlaying == true && viewModel.currentAudioMedia != VouchChatModel()
                                        && getAudioId(viewModel.currentAudioMedia) == getAudioId(data)) {
                                        seekbar.incrementProgressBy(1)
                                        seekbar.max = viewModel.mMediaPlayer?.duration ?: 0
                                        seekbar.isEnabled = true
                                        playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_pause_black_24dp))
                                        mListener.setupMediaPlayer()
                                        mListener.onClickPlayAudio()
                                    } else if (viewModel.currentAudioMedia != VouchChatModel()
                                        && getAudioId(viewModel.currentAudioMedia) == getAudioId(data)
                                        && (viewModel.audioSeek[getAudioId(viewModel.currentAudioMedia)] ?: 0) > 0) {
                                        playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                                        val audioSeek = viewModel.audioSeek[getAudioId(viewModel.currentAudioMedia)] ?: 0
                                        audioText.text = "${Helper.timeUnitToString(TimeUnit.MILLISECONDS.toMinutes(audioSeek.toLong()))}:${Helper.timeUnitToString(
                                                TimeUnit.MILLISECONDS.toSeconds(audioSeek.toLong()) - TimeUnit.MINUTES.toSeconds(
                                                    TimeUnit.MILLISECONDS.toMinutes(audioSeek.toLong())
                                                )
                                            )}"
                                        seekbar.isEnabled = false
                                        seekbar.incrementProgressBy(1)
                                        seekbar.max = viewModel.mMediaPlayer?.duration ?: 0
                                        seekbar.progress = audioSeek
                                    } else {
                                        playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                                        seekbar.isEnabled = false
                                        seekbar.incrementProgressBy(1)
                                        seekbar.max = 0
                                        seekbar.progress = 0
                                    }

                                    playAudio.setOnClickListener {
                                        if (viewModel.currentAudioMedia != VouchChatModel()
                                            && getAudioId(viewModel.currentAudioMedia) != getAudioId(data)) {
                                            mListener.resetMediaPlayer()
                                        }
                                        if (viewModel.mMediaPlayer?.isPlaying == true) {
                                            playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                                            viewModel.mMediaPlayer?.pause()
                                            seekbar.isEnabled = false
                                        } else {
                                            playAudio.setImageDrawable(context.getDrawable(R.drawable.ic_pause_black_24dp))
                                            seekbar.isEnabled = true
                                            createMediaPlayer(context, data, this, viewModel, mListener)
                                            viewModel.mMediaPlayer?.seekTo(
                                                if (viewModel.audioSeek[getAudioId(viewModel.currentAudioMedia)] == viewModel.mMediaPlayer?.duration) {
                                                    0
                                                } else {
                                                    viewModel.audioSeek[getAudioId(viewModel.currentAudioMedia)] ?: 0
                                                }
                                            )
                                            viewModel.mMediaPlayer?.start()
                                            mListener.onClickPlayAudio()
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
                        dateTime.text = data.createdAt.safe().reformatFullDate("EEE, dd MMM HH:mm:ss")
                        dateTime.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                        dateTime.visibility = if (data.isHaveOutsideButton) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                        chatContent.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())

                        cardBubble.setCardBackgroundColor(viewModel.loadConfiguration.value?.leftBubbleBgColor.parseColor(Color.WHITE))
                        chatContent.setTextColor(viewModel.loadConfiguration.value?.leftBubbleColor.parseColor(Color.BLACK))
                    }
                }
            }
            animateView(mView, data, this@VouchChatAdapter.mData.indexOf(data))
        }

        private fun animateView(view: View, data: VouchChatModel, position: Int) {
            if ((position == 0 && mViewModel.isDataNew)) {
                view.clearAnimation()
                val animation = if (data.isMyChat) {
                    AnimationUtils.loadAnimation(view.context, R.anim.right_to_left_anim)
                } else {
                    AnimationUtils.loadAnimation(view.context, R.anim.left_to_right_anim)
                }
                if (data.isPendingMessage || !data.isMyChat) {
                    view.startAnimation(animation)
                    mViewModel.isDataNew = false
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
                    mViewModel.audioDuration[data.mediaUrl] = mViewModel.mMediaPlayer?.duration ?: 0
                    seekbar.max = mViewModel.mMediaPlayer?.duration ?: 0
                    val time = (mViewModel.mMediaPlayer?.duration ?: 0).toLong()
                    audioText.text = "${Helper.timeUnitToString(TimeUnit.MILLISECONDS.toMinutes(time))}:${Helper.timeUnitToString(
                        TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(time)
                        )
                    )}"
                    mListener.setupMediaPlayer()
                }
                mViewModel.mMediaPlayer?.setOnCompletionListener {
                    mListener.resetMediaPlayer()
                }
            }
        }

        fun clearAnimation() {
            mView.clearAnimation()
        }

    }

    class VouchChatLoading(mView: View) : RecyclerView.ViewHolder(mView)

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is VouchChatItem) {
            holder.clearAnimation()
        }
    }

}