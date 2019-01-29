package com.ins.gradle.plugin.android.appcenter.entities

import com.ins.gradle.plugin.android.appcenter.STATUS_COMMITTED

data class CommitRequest(
        val status: String = STATUS_COMMITTED
)
