package com.eyepetizer.android.event

/**
 * EventBus事件总线 通知刷新界面消息
 */
open class RefreshEvent(var activityClass: Class<*>? = null) : MessageEvent()