package com.ins.gradle.plugin.android.appcenter

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

const val APP_CENTER_EXTENSION = "appCenter"

open class AppCenterPluginExtension(private val project: Project) {

    var productFlavors : NamedDomainObjectContainer<BuildVariantExtension> ?= null
    var buildTypes : NamedDomainObjectContainer<BuildVariantExtension> ?= null

    var defaultConfig : BuildVariantExtension ?= null

    /**
     * Default Configuration when not defined for a buildType or flavor
     */
    fun defaultConfig(defaultDef: Closure<*>) : BuildVariantExtension?{
        if (defaultConfig != null) {
            throw Exception("Only one default config allowed")
        }
        defaultConfig = BuildVariantExtension()
        project.configure(defaultConfig, defaultDef)
        defaultDef.delegate = defaultConfig

        return defaultConfig
    }

    /**
     * All buildType config can be defined here
     */
    fun buildTypes(action: Action<in NamedDomainObjectContainer<BuildVariantExtension>>) {
        action.execute(buildTypes)
    }

    /**
     * All flavor config can be defined here
     */
    fun productFlavors(action: Action<in NamedDomainObjectContainer<BuildVariantExtension>>) {
        action.execute(productFlavors)
    }
}
