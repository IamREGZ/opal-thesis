package edu.cccdci.opal.layoutwrapper

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WrapperGridLayoutManager(
    context: Context, spanCount: Int, orientation: Int, reverseLayout: Boolean
) : GridLayoutManager(context, spanCount, orientation, reverseLayout) {

    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler?, state: RecyclerView.State?
    ) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            // Log the exception
            e.printStackTrace()
        } catch (e: RuntimeException) {
            // Log the exception
            e.printStackTrace()
        }
    }  // end of onLayoutChildren method

}  // end of WrapperGridLayoutManager class