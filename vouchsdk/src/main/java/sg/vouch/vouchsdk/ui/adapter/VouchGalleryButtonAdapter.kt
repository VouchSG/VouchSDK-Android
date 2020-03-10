package sg.vouch.vouchsdk.ui.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.data.model.message.response.ButtonModel
import sg.vouch.vouchsdk.ui.VouchChatClickListener
import sg.vouch.vouchsdk.ui.VouchChatViewModel
import sg.vouch.vouchsdk.ui.model.VouchChatModel
import sg.vouch.vouchsdk.ui.model.VouchChatType
import sg.vouch.vouchsdk.utils.parseColor
import sg.vouch.vouchsdk.utils.safe
import sg.vouch.vouchsdk.utils.setFontFamily
import kotlinx.android.synthetic.main.item_gallery_element_button.view.*

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-10
 */
class VouchGalleryButtonAdapter(val mViewModel: VouchChatViewModel, private val mListener: VouchChatClickListener) : RecyclerView.Adapter<VouchGalleryButtonAdapter.VouchGalleryButtonItem>() {

    val mData: MutableList<ButtonModel> = mutableListOf()

    override fun onBindViewHolder(holder: VouchGalleryButtonItem, position: Int) {
        holder.bind(mData[position], mViewModel, mListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VouchGalleryButtonItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_element_button, null, false)
        return VouchGalleryButtonItem(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }


    class VouchGalleryButtonItem(private val mView: View) : RecyclerView.ViewHolder(mView) {

        fun bind(data: ButtonModel, viewModel: VouchChatViewModel, listener: VouchChatClickListener) {
            val contentData = VouchChatModel(data.title.safe(), "", false, VouchChatType.TYPE_BUTTON, "", payload = data.payload?:data.url.safe(), typeValue = data.type.safe())

            mView.apply {
                vouchButtonItem.text = contentData.title
                vouchButtonItem.setTextColor(viewModel.loadConfiguration.value?.headerBgColor.parseColor(Color.BLACK))
                vouchButtonItem.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
                borderButton.setColorFilter(viewModel.loadConfiguration.value?.headerBgColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
                buttonItem.setOnClickListener { listener.onClickChatButton(contentData.typeValue, contentData) }
            }
        }

    }

}