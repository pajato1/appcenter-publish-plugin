package com.ins.gradle.plugin.android.appcenter.entities

data class DistributionRequest(
        val destination_name: String,
        val release_notes: String,
        val mandatory_update: Boolean,
        val notify_testers: Boolean
)
