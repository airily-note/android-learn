/*
 * Copyright (c) 2020. vipyinzhiwei <vipyinzhiwei@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eyepetizer.android.ui.community.commend

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.eyepetizer.android.R
import com.eyepetizer.android.databinding.FragmentRefreshLayoutBinding
import com.eyepetizer.android.event.MessageEvent
import com.eyepetizer.android.event.RefreshEvent
import com.eyepetizer.android.extension.dp2px
import com.eyepetizer.android.extension.showToast
import com.eyepetizer.android.ui.common.ui.BaseFragment
import com.eyepetizer.android.util.GlobalUtil
import com.eyepetizer.android.util.InjectorUtil
import com.eyepetizer.android.util.ResponseHandler
import com.scwang.smart.refresh.layout.constant.RefreshState

/**
 * 社区-推荐列表界面。
 *
 * @author vipyinzhiwei
 * @since  2020/5/1
 */
class CommendFragment : BaseFragment() {

    private var _binding: FragmentRefreshLayoutBinding? = null

    private val binding
        get() = _binding!!

    /**
     * 列表左or右间距
     */
    val bothSideSpace = GlobalUtil.getDimension(R.dimen.listSpaceSize)

    /**
     * 列表中间内间距，左or右。
     */
    val middleSpace = dp2px(3f)


    /**
     * 通过获取屏幕宽度来计算出每张图片最大的宽度。
     *
     * @return 计算后得出的每张图片最大的宽度。
     */
    val maxImageWidth: Int
        get() {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay?.getMetrics(metrics)
            val columnWidth = metrics.widthPixels
            return (columnWidth - ((bothSideSpace * 2) + (middleSpace * 2))) / 2
        }

    private val viewModel by lazy { ViewModelProvider(this, InjectorUtil.getCommunityCommendViewModelFactory()).get(CommendViewModel::class.java) }

    private lateinit var adapter: CommendAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRefreshLayoutBinding.inflate(layoutInflater, container, false)
        return super.onCreateView(binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CommendAdapter(this, viewModel.dataList)
        val mainLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        mainLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        binding.recyclerView.layoutManager = mainLayoutManager
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(CommendAdapter.ItemDecoration(this))
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.itemAnimator = null
        binding.refreshLayout.setOnRefreshListener { viewModel.onRefresh() }
        binding.refreshLayout.setOnLoadMoreListener { viewModel.onLoadMore() }
        observe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun loadDataOnce() {
        super.loadDataOnce()
        startLoading()
    }

    override fun startLoading() {
        super.startLoading()
        viewModel.onRefresh()
    }

    @CallSuper
    override fun loadFailed(msg: String?) {
        super.loadFailed(msg)
        showLoadErrorView(msg ?: GlobalUtil.getString(R.string.unknown_error)) { startLoading() }
    }

    override fun onMessageEvent(messageEvent: MessageEvent) {
        super.onMessageEvent(messageEvent)
        if (messageEvent is RefreshEvent && javaClass == messageEvent.activityClass) {
            binding.refreshLayout.autoRefresh()
            if (binding.recyclerView.adapter?.itemCount ?: 0 > 0) binding.recyclerView.scrollToPosition(0)
        }
    }

    private fun observe() {
        viewModel.dataListLiveData.observe(viewLifecycleOwner, Observer { result ->
            val response = result.getOrNull()
            if (response == null) {
                ResponseHandler.getFailureTips(result.exceptionOrNull()).let { if (viewModel.dataList.isNullOrEmpty()) loadFailed(it) else it.showToast() }
                binding.refreshLayout.closeHeaderOrFooter()
                return@Observer
            }
            loadFinished()
            viewModel.nextPageUrl = response.nextPageUrl
            if (response.itemList.isNullOrEmpty() && viewModel.dataList.isEmpty()) {
                binding.refreshLayout.closeHeaderOrFooter()
                return@Observer
            }
            if (response.itemList.isNullOrEmpty() && viewModel.dataList.isNotEmpty()) {
                binding.refreshLayout.finishLoadMoreWithNoMoreData()
                return@Observer
            }
            when (binding.refreshLayout.state) {
                RefreshState.None, RefreshState.Refreshing -> {
                    viewModel.dataList.clear()
                    viewModel.dataList.addAll(response.itemList)
                    adapter.notifyDataSetChanged()
                }
                RefreshState.Loading -> {
                    val itemCount = viewModel.dataList.size
                    viewModel.dataList.addAll(response.itemList)
                    adapter.notifyItemRangeInserted(itemCount, response.itemList.size)
                }
                else -> {
                }
            }
            if (response.nextPageUrl.isNullOrEmpty()) {
                binding.refreshLayout.finishLoadMoreWithNoMoreData()
            } else {
                binding.refreshLayout.closeHeaderOrFooter()
            }
        })
    }

    companion object {

        fun newInstance() = CommendFragment()
    }

}
