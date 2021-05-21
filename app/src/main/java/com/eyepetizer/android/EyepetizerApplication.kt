package com.eyepetizer.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import androidx.work.WorkManager
import com.eyepetizer.android.extension.preCreateSession
import com.eyepetizer.android.ui.SplashActivity
import com.eyepetizer.android.ui.common.ui.WebViewActivity
import com.eyepetizer.android.ui.common.view.NoStatusFooter
import com.eyepetizer.android.util.DialogAppraiseTipsWorker
import com.eyepetizer.android.util.GlobalUtil
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.umeng.commonsdk.UMConfigure
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * 自定义Application类，继承自android.app.Application
 * 每个Android App运行时，会首先自动创建Application 类并实例化 Application 对象，且只有一个
 * 其子类需要在AndroidManifest.xml文件中的<application>标签下使用"android:name"进行声明
 * 自定义Application，在这里进行全局的初始化操作。
 *
 * @author vipyinzhiwei
 * @since  2020/4/28
 */
class EyepetizerApplication : Application() {

    /**
     * 初始化代码块
     * Kotlin中的init代码块就相当于Java中的普通代码块，在创建对象的时候代码块会先执行。注意是每次创建都会执行一遍
     */
    init {
        //下拉刷新框架
        //设置全局默认配置（优先级最低，会被其他设置覆盖）
        SmartRefreshLayout.setDefaultRefreshInitializer { context, layout ->
            //是否开启加上拉加载功能
            layout.setEnableLoadMore(true)
            //在内容不满一页的时候，是否可以上拉加载更多
            layout.setEnableLoadMoreWhenContentNotFull(true)
        }
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            //拖动Header的时候是否同时拖动内容
            layout.setEnableHeaderTranslationContent(true)
            MaterialHeader(context).setColorSchemeResources(R.color.blue, R.color.blue, R.color.blue)
        }
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            layout.setEnableFooterFollowWhenNoMoreData(true)
            //拖动Footer的时候是否同时拖动内容
            layout.setEnableFooterTranslationContent(true)
            //Footer的标准高度
            layout.setFooterHeight(153f)
            //Footer触发加载距离 与 FooterHeight 的比率
            layout.setFooterTriggerRate(0.6f)
            //无数据加载提示
            NoStatusFooter.REFRESH_FOOTER_NOTHING = GlobalUtil.getString(R.string.footer_not_more)
            NoStatusFooter(context).apply {
                setAccentColorId(R.color.colorTextPrimary)
                setTextTitleSize(16f)
            }
        }
    }

    /**
     * 调用ContextWrapper.attachBaseContext,执行早于onCreate
     */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    /**
     *  Application 实例创建时调用
     */
    override fun onCreate() {
        super.onCreate()
        context = this
        //设置GSYVideoPlayer播放器日志级别
        IjkPlayerManager.setLogLevel(if (BuildConfig.DEBUG) IjkMediaPlayer.IJK_LOG_WARN else IjkMediaPlayer.IJK_LOG_SILENT)
        //WebView预加载H5页面
        WebViewActivity.DEFAULT_URL.preCreateSession()
        //显示启动页
        if (!SplashActivity.isFirstEntryApp && DialogAppraiseTipsWorker.isNeedShowDialog) {
            WorkManager.getInstance(this).enqueue(DialogAppraiseTipsWorker.showDialogWorkRequest)
        }
    }

    /**
     * 伴生对象
     * Kotlin中的伴生对象相当于Java中的Static关键字。
     * 伴生对象里的init代码块就相当于Java中的静态代码块。在类加载的时候会优先执行且只会执行一次
     */
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}