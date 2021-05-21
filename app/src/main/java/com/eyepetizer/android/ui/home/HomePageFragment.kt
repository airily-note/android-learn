package com.eyepetizer.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.eyepetizer.android.R
import com.eyepetizer.android.databinding.FragmentMainContainerBinding
import com.eyepetizer.android.event.MessageEvent
import com.eyepetizer.android.event.RefreshEvent
import com.eyepetizer.android.event.SwitchPagesEvent
import com.eyepetizer.android.logic.model.TabEntity
import com.eyepetizer.android.ui.common.ui.BaseViewPagerFragment
import com.eyepetizer.android.ui.home.commend.CommendFragment
import com.eyepetizer.android.ui.home.daily.DailyFragment
import com.eyepetizer.android.ui.home.discovery.DiscoveryFragment
import com.eyepetizer.android.util.GlobalUtil
import com.flyco.tablayout.listener.CustomTabEntity
import org.greenrobot.eventbus.EventBus

/**
 * 首页主界面。
 *
 * @author vipyinzhiwei
 * @since  2020/4/30
 */
class HomePageFragment : BaseViewPagerFragment() {

    private var _binding: FragmentMainContainerBinding? = null

    private val binding
        get() = _binding!!

    override val createTitles = ArrayList<CustomTabEntity>().apply {
        add(TabEntity(GlobalUtil.getString(R.string.discovery)))
        add(TabEntity(GlobalUtil.getString(R.string.commend)))
        add(TabEntity(GlobalUtil.getString(R.string.daily)))
    }

    override val createFragments: Array<Fragment> = arrayOf(DiscoveryFragment.newInstance(), CommendFragment.newInstance(), DailyFragment.newInstance())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMainContainerBinding.inflate(layoutInflater, container, false)
        return super.onCreateView(binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleBar.ivCalendar.visibility = View.VISIBLE
        viewPager?.currentItem = 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMessageEvent(messageEvent: MessageEvent) {
        super.onMessageEvent(messageEvent)
        if (messageEvent is RefreshEvent && this::class.java == messageEvent.activityClass) {
            //事件总线 发送通知刷新界面消息
            when (viewPager?.currentItem) {
                0 -> EventBus.getDefault().post(RefreshEvent(DiscoveryFragment::class.java))
                1 -> EventBus.getDefault().post(RefreshEvent(CommendFragment::class.java))
                2 -> EventBus.getDefault().post(RefreshEvent(DailyFragment::class.java))
                else -> {
                }
            }
        } else if (messageEvent is SwitchPagesEvent) {
            when (messageEvent.activityClass) {
                DiscoveryFragment::class.java -> viewPager?.currentItem = 0
                CommendFragment::class.java -> viewPager?.currentItem = 1
                DailyFragment::class.java -> viewPager?.currentItem = 2
                else -> {
                }
            }
        }
    }

    companion object {

        fun newInstance() = HomePageFragment()
    }
}
