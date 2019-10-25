package sg.vouch.vouchsdk.utils.widget

import android.content.Context
import android.util.AttributeSet
import com.makeramen.roundedimageview.RoundedImageView

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-12
 */
class SquareRoundedImageView : RoundedImageView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        setMeasuredDimension(width, width)
    }
}
