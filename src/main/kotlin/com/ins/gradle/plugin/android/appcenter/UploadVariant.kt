package com.ins.gradle.plugin.android.appcenter

import com.android.build.VariantOutput
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory


/**
 * Created by Jonathan DA ROS on 23/01/2019.
 */

open class UploadVariant : DefaultTask() {

    @get:Internal
    internal lateinit var variant: ApplicationVariant

    val _log = Logging.getLogger(UploadVariant::class.java.simpleName)

    @get:Internal
    protected val progressLogger: ProgressLogger = services[ProgressLoggerFactory::class.java]
            .newOperation(javaClass)

    @get:InputFiles
    public val inputApks by lazy {
        variant.outputs.filterIsInstance<ApkVariantOutput>().filter {
            VariantOutput.OutputType.valueOf(it.outputType) == VariantOutput.OutputType.MAIN || it.filters.isEmpty()
        }.map { it.outputFile }
    }


    @TaskAction
    fun uploadVariant() {
    }

}



