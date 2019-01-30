package com.ins.gradle.plugin.android.appcenter

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

const val APP_CENTER_EXTENSION = "appCenter"

open class AppCenterPluginExtension(private val project: Project) {

    var productFlavors : NamedDomainObjectContainer<FlavorExtension> ?= null

    var defaultConfig : FlavorExtension ?= null

    /**
     * Default Configuration when not defined for a flavor
     */
    fun defaultConfig(defaultDef: Closure<*>) : FlavorExtension?{
        if (defaultConfig != null) {
            throw Exception("Only one default config allowed")
        }
        defaultConfig = FlavorExtension()
        project.configure(defaultConfig, defaultDef)
        defaultDef.delegate = defaultConfig

        return defaultConfig
    }

    /**
     * All flavor config can be defined here
     */
    fun productFlavors(action: Action<in NamedDomainObjectContainer<FlavorExtension>>) {
        action.execute(productFlavors)
    }

}
