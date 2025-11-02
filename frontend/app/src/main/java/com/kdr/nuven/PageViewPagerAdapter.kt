package com.kdr.nuven

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PageViewPagerAdapter(
    private var isExpandedMode: Boolean = false,
    private val onLinkClick: (Int) -> Unit,
    private val onUrlClick: ((String) -> Unit)? = null,
    private val onCoordinateUpdate: ((Int, Int) -> Unit)? = null
) : RecyclerView.Adapter<PageViewPagerAdapter.PageViewHolder>() {

    companion object {
        private const val TAG = "PageViewPagerAdapter"
    }

    private val pages = mutableListOf<PageFormat>()
    private val formatRenderer = PageFormatRenderer()

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.pageTextView)
        val lineNumberTextView: TextView = view.findViewById(R.id.lineNumberTextView)
        val linkButton: ImageButton = view.findViewById(R.id.linkButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_view, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pages[position]
        Log.d(TAG, "onBindViewHolder: position=$position, pageId=${page.pageId}, isExpandedMode=$isExpandedMode")
        formatRenderer.setExpandedMode(isExpandedMode)
        formatRenderer.renderPage(page, holder.textView, holder.lineNumberTextView, onLinkClick, onUrlClick, onCoordinateUpdate, lastUpdated)
        
        // Show/hide link button based on URL availability
        val articleUrl = page.metadata.url
        
        if (articleUrl != null && !articleUrl.isEmpty() && onUrlClick != null) {
            holder.linkButton.visibility = View.VISIBLE
            holder.linkButton.setOnClickListener {
                onUrlClick.invoke(articleUrl)
            }
        } else {
            holder.linkButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = pages.size

    private var lastUpdated: String? = null

    fun setPages(newPages: List<PageFormat>, lastUpdated: String?) {
        pages.clear()
        pages.addAll(newPages)
        this.lastUpdated = lastUpdated
        notifyDataSetChanged()
    }

    fun setExpandedMode(expanded: Boolean) {
        Log.d(TAG, "setExpandedMode: $expanded (previous: $isExpandedMode)")
        isExpandedMode = expanded
        notifyDataSetChanged()
    }

    fun setDebugMode(debug: Boolean) {
        formatRenderer.setDebugMode(debug)
        notifyDataSetChanged()
    }

    fun getPageNumberAt(position: Int): Int {
        return pages.getOrNull(position)?.pageId?.toIntOrNull() ?: 100
    }

    fun getPositionForPageNumber(pageNumber: Int): Int {
        return pages.indexOfFirst { it.pageId == pageNumber.toString() }.coerceAtLeast(0)
    }
}