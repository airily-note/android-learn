package com.eyepetizer.android.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.eyepetizer.android.R
import com.eyepetizer.android.event.MessageEvent
import com.eyepetizer.android.event.RefreshEvent
import com.eyepetizer.android.event.SwitchPagesEvent
import com.eyepetizer.android.logic.model.TabEntity
import com.eyepetizer.android.ui.common.ui.BaseViewPagerFragment
import com.eyepetizer.android.ui.community.commend.CommendFragment
import com.eyepetizer.android.ui.community.follow.FollowFragment
import com.eyepetizer.android.util.GlobalUtil
import com.flyco.tablayout.listener.CustomTabEntity
import org.greenrobot.eventbus.EventBus

/**
 * 社区主界面。
 *
 * @author vipyinzhiwei
 * @since  2020/5/1
 */
class CommunityFragment : BaseViewPagerFragment() {

    override val createTitles = ArrayList<CustomTabEntity>().apply {
        add(TabEntity(GlobalUtil.getString(R.string.commend)))
        add(TabEntity(GlobalUtil.getString(R.string.follow)))
    }

    override val createFragments: Array<Fragment> = arrayOf(CommendFragment.newInstance(), FollowFragment.newInstance())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater.inflate(R.layout.fragment_main_container, container, false))
    }

    override fun onMessageEvent(messageEvent: MessageEvent) {
        super.onMessageEvent(messageEvent)
        if (messageEvent is RefreshEvent && this::class.java == messageEvent.activityClass) {
            //事件总线 发送通知刷新界面消息
            when (viewPager?.currentItem) {
                0 -> EventBus.getDefault().post(RefreshEvent(CommendFragment::class.java))
                1 -> EventBus.getDefault().post(RefreshEvent(FollowFragment::class.java))
                else -> {
                }
            }
        } else if (messageEvent is SwitchPagesEvent) {
            when (messageEvent.activityClass) {
                CommendFragment::class.java -> viewPager?.currentItem = 0
                else -> {
                }
            }
        }
    }

    companion object {

        fun newInstance() = CommunityFragment()
    }
}
