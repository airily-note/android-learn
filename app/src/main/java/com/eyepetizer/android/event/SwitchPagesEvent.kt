package com.eyepetizer.android.event

/**
 * EventBus事件总线 通知Tab页切换界面
 */
open class SwitchPagesEvent(var activityClass: Class<*>? = null) : MessageEvent()