package sg.vouch.vouchsdk.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import sg.vouch.vouchsdk.ui.VouchChatClickListener
import sg.vouch.vouchsdk.ui.VouchChatViewModel


// create by deannrchsnl on 2019-12-06
// contact me at deannurchusnulchotimah@gmail.com



class VouchListButtonAdapter(
    val mViewModel: VouchChatViewModel,
    private val mListener: VouchChatClickListener,
    private val mViewPool: RecyclerView.RecycledViewPool
) : RecyclerView.Adapter<VouchListButtonAdapter.VouchListButtonItem>() {

    class VouchListButtonItem(private val mView: View) : RecyclerView.ViewHolder(mView) {

    }

    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): VouchListButtonAdapter.VouchListButtonItem {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(p0: VouchListButtonAdapter.VouchListButtonItem, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}