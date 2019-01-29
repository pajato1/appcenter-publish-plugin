package com.ins.gradle.plugin.android.appcenter

import com.android.build.gradle.internal.dsl.ProductFlavor
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import java.io.File

const val APP_CENTER_EXTENSION = "appCenter"

open class AppCenterPluginExtension(private val project: Project) {

    var productFlavors : NamedDomainObjectContainer<FlavorExtension> ?= null

    var defaultConfig : FlavorExtension ?= null

    fun defaultConfig(defaultDef: Closure<*>) : FlavorExtension?{
        if (defaultConfig != null) {
            throw Exception("Only one default config allowed")
        }
        defaultConfig = FlavorExtension()
        project.configure(defaultConfig, defaultDef)
        defaultDef.delegate = defaultConfig

        return defaultConfig
    }
    fun productFlavors(action: Action<in NamedDomainObjectContainer<FlavorExtension>>) {
        action.execute(productFlavors)
    }

}
