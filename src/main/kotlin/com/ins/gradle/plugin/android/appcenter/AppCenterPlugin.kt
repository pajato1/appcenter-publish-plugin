package com.ins.gradle.plugin.android.appcenter

import com.android.build.gradle.AppExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

class AppCenterPlugin : Plugin<Project> {


    val _log = Logging.getLogger(AppCenterPlugin::class.java.simpleName)

    override fun apply(project: Project) {

        project.plugins.withType(AppCenterPlugin::class.java) {
            applyInternal(project)
        }
    }

    private fun applyInternal(project: Project) {


        val android = project.extensions.findByName("android") as AppExtension


        val extension =  project.extensions.create(APP_CENTER_EXTENSION, AppCenterPluginExtension::class.java, project)

        extension.productFlavors =  project.container(FlavorExtension::class.java)



        val uploadAllVariant = project.tasks.create("uploadAppCenter") // all apk upload task


        android.applicationVariants.whenObjectAdded { appVariant ->
            val variantName = appVariant.name.capitalize()

            if(extension.defaultConfig == null) throw GradleException("Default config is undefined")

            var extensionForTask =   getConfigForVariant(variantName, extension)



            val uploadVariant = project.tasks.create("upload${variantName}AppCenter", UploadVariant::class.java) { it ->
                it.variant = appVariant
            }

            if(uploadVariant.inputApks.size == 1) {
                uploadVariant.inputApks.forEach { file ->
                    val apkToCenterTask = project.tasks
                            .create("upload${file.nameWithoutExtension.replace("-", "")}ApkAppCenter",
                                    AppCenterUploadTask::class.java) {
                                it.artifact = file
                                it.extension = extensionForTask
                            }

                    uploadVariant.dependsOn(apkToCenterTask)
                }
            }
            else {

                var filteredFiles = uploadVariant.inputApks.filter { file -> file.nameWithoutExtension.contains("universal") }

                if(filteredFiles.isNotEmpty()) {
                    val apkToCenterTask = project.tasks
                            .create("upload${filteredFiles[0].nameWithoutExtension.replace("-", "")}ApkAppCenter", AppCenterUploadTask::class.java) {
                                it.artifact = filteredFiles[0]
                            }

                    uploadVariant.dependsOn(apkToCenterTask)
                }
            }
            // upload task depends on assemble
            try {
                appVariant.assembleProvider
            } catch (e: NoSuchMethodError) {
                @Suppress("DEPRECATION")
                appVariant.assemble
            }?.let { uploadVariant.dependsOn(it) }
                    ?: _log.warn("Assemble task not found. Publishing APKs may not work.")

            uploadAllVariant.dependsOn(uploadVariant)



        }
    }


    fun getConfigForVariant(variantName : String, extension : AppCenterPluginExtension, android: AppExtension) : FlavorExtension{

        if(extension.productFlavors?.isEmpty() != false) return extension.defaultConfig!!


        val variantFiltered = extension.productFlavors?.map{ it->
            if(variantName.toLowerCase().contains(it.name.toLowerCase())) { it } else null
        }
        if(variantFiltered?.isEmpty() != false) return extension.defaultConfig!!
        val customExtension =  variantFiltered[0]?: extension.defaultConfig

        _log.quiet("Default config :" +extension.defaultConfig)

        return customExtension!!.mergeWith(extension.defaultConfig!!)
    }
}
