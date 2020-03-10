package sg.vouch.vouchsdk.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.data.model.message.response.GalleryElementModel
import sg.vouch.vouchsdk.ui.VouchChatClickListener
import sg.vouch.vouchsdk.ui.VouchChatViewModel
import sg.vouch.vouchsdk.utils.safe
import sg.vouch.vouchsdk.utils.setFontFamily
import sg.vouch.vouchsdk.utils.setImageUrl
import kotlinx.android.synthetic.main.item_gallery_element.view.*

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-10
 */
class VouchGalleryAdapter(
    val mViewModel: VouchChatViewModel,
    private val mListener: VouchChatClickListener,
    private val mViewPool: RecyclerView.RecycledViewPool
) : RecyclerView.Adapter<VouchGalleryAdapter.VouchGalleryItem>() {

    val mData: MutableList<GalleryElementModel> = mutableListOf()

    private var highestItem = 0

    override fun onBindViewHolder(holder: VouchGalleryItem, position: Int) {
        holder.bind(mData[position], mViewModel, mListener, mViewPool)

        holder.itemView.cardGallery.measure(0, 0)
        highestItem = if (highestItem > holder.itemView.cardGallery.measuredHeight) highestItem
        else holder.itemView.cardGallery.measuredHeight
        holder.itemView.cardGallery.layoutParams?.height = highestItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VouchGalleryItem {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_element, null, false)
        return VouchGalleryItem(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class VouchGalleryItem(private val mView: View) : RecyclerView.ViewHolder(mView) {

        fun bind(
            data: GalleryElementModel,
            viewModel: VouchChatViewModel,
            listener: VouchChatClickListener,
            mViewPool: RecyclerView.RecycledViewPool
        ) {
            mView.imageGallery.setImageUrl(data.imageUrl.safe())
            mView.titleGallery.text = data.title
            mView.descGallery.text = data.subtitle

            mView.titleGallery.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())
            mView.descGallery.setFontFamily(viewModel.loadConfiguration.value?.fontStyle.safe())

            mView.recyclerGalleryButton.layoutManager =
                LinearLayoutManager(mView.context)
            mView.recyclerGalleryButton.adapter =
                VouchGalleryButtonAdapter(viewModel, listener).apply {
                    mData.addAll(
                        data.buttons ?: emptyList()
                    )
                }
        }

    }

}