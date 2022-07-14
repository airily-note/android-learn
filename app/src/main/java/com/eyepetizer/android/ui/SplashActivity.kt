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

package com.eyepetizer.android.ui

import android.Manifest
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.eyepetizer.android.R
import com.eyepetizer.android.databinding.ActivitySplashBinding
import com.eyepetizer.android.ui.common.ui.BaseActivity
import com.eyepetizer.android.util.DataStoreUtils
import com.eyepetizer.android.util.GlobalUtil
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.*


/**
 * 闪屏页面，应用程序首次启动入口。
 *
 * @author vipyinzhiwei
 * @since  2020/5/15
 */
class SplashActivity : BaseActivity() {

    var _binding: ActivitySplashBinding? = null

    val binding: ActivitySplashBinding
        get() = _binding!!

    private val job by lazy { Job() }
    //启动屏持续时间
    private val splashDuration = 3 * 1000L

    /**
     * 窗口的透明度动画效果
     */
    private val alphaAnimation by lazy {
        AlphaAnimation(0.5f, 1.0f).apply {
            duration = splashDuration
            fillAfter = true
        }
    }

    /**
     * 缩放动画
     */
    private val scaleAnimation by lazy {
        ScaleAnimation(1f, 1.05f, 1f, 1.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
            duration = splashDuration
            fillAfter = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //调用请求写入存储授权
        requestWriteExternalStoragePermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        _binding = null
    }

    override fun setupViews() {
        super.setupViews()
        binding.ivSlogan.startAnimation(alphaAnimation)
        binding.ivSplashPicture.startAnimation(scaleAnimation)
        //协程处理
        CoroutineScope(job).launch {
            delay(splashDuration)
            MainActivity.start(this@SplashActivity)
            finish()
        }
        isFirstEntryApp = false
    }

    /**
     * 请求写入存储授权
     */
    private fun requestWriteExternalStoragePermission() {
        PermissionX.init(this@SplashActivity).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .onExplainRequestReason { scope, deniedList ->
                val message = GlobalUtil.getString(R.string.request_permission_picture_processing)
                scope.showRequestReasonDialog(deniedList, message, GlobalUtil.getString(R.string.ok), GlobalUtil.getString(R.string.cancel))
            }
            .onForwardToSettings { scope, deniedList ->
                val message = GlobalUtil.getString(R.string.request_permission_picture_processing)
                scope.showForwardToSettingsDialog(deniedList, message, GlobalUtil.getString(R.string.settings), GlobalUtil.getString(R.string.cancel))
            }
            .request { allGranted, grantedList, deniedList ->
                //请求读取手机信息授权
                requestReadPhoneStatePermission()
            }
    }

    /**
     * 请求读取手机信息授权
     */
    private fun requestReadPhoneStatePermission() {
        PermissionX.init(this@SplashActivity).permissions(Manifest.permission.READ_PHONE_STATE)
            .onExplainRequestReason { scope, deniedList ->
                val message = GlobalUtil.getString(R.string.request_permission_access_phone_info)
                scope.showRequestReasonDialog(deniedList, message, GlobalUtil.getString(R.string.ok), GlobalUtil.getString(R.string.cancel))
            }
            .onForwardToSettings { scope, deniedList ->
                val message = GlobalUtil.getString(R.string.request_permission_access_phone_info)
                scope.showForwardToSettingsDialog(deniedList, message, GlobalUtil.getString(R.string.settings), GlobalUtil.getString(R.string.cancel))
            }
            .request { allGranted, grantedList, deniedList ->
                _binding = ActivitySplashBinding.inflate(layoutInflater)
                setContentView(binding.root)
            }
    }

    companion object {

        /**
         * 是否首次进入APP应用
         */
        var isFirstEntryApp: Boolean
            get() = DataStoreUtils.readBooleanData("is_first_entry_app", true)
            set(value) {
                CoroutineScope(Dispatchers.IO).launch { DataStoreUtils.saveBooleanData("is_first_entry_app", value) }
            }
    }
}
