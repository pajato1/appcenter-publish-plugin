package com.ins.gradle.plugin.android.appcenter

import org.gradle.api.logging.Logging
import java.io.File

open class FlavorExtension {
    var apiToken: String? = null
    var appOwner: String? = null
    var appName: String? = null

    var appNameSuffix: String = ""
    var destination: String? = null
    var releaseNotes: String? = null
    var verbose: Boolean = false


    val _log = Logging.getLogger(FlavorExtension::class.java.simpleName)

     lateinit var name: String

    constructor(){}


    constructor(name : String){
        this.name = name
    }
    constructor(name : String, appName : String? = null, destination: String?= null, releaseNotes : String?, appOwner : String?, appNameSuffix : String  = "", verbose : Boolean = false){


        this.name = name
        this.appName = appName
        this.destination = destination
        this.releaseNotes = releaseNotes
        this.appOwner = appOwner
        this.verbose = verbose
        this.appNameSuffix =appNameSuffix

    }


    fun mergeWith(extension2 : FlavorExtension) : FlavorExtension{

        var extension = FlavorExtension()
        extension.apiToken     =   if(this.apiToken == null)        extension2.apiToken           else this.apiToken
        extension.appOwner       = if(this.appOwner == null)        extension2.appOwner           else this.appOwner
        extension.appName        = if(this.appName == null)         extension2.appName            else this.appName
        extension.destination    = if(this.destination == null)     extension2.destination        else this.destination
        extension.releaseNotes   = if(this.releaseNotes == null)    extension2.releaseNotes       else this.releaseNotes
        extension.appNameSuffix  = if(this.appNameSuffix.isBlank()) extension2.appNameSuffix      else this.appNameSuffix

        var myName = if(::name.isInitialized) this.name else "default"
        var hisName = if(extension2::name.isInitialized) extension2.name else "default"

        _log.info("mergeWith() ReleaseNotes flavor {} : {},  {} : {}", myName, this.releaseNotes,hisName, extension2.releaseNotes)

        return extension

    }


    override fun toString(): String {

        return "Flavor Extension : { \n" +
                "apiToken = " + this.apiToken + "\n" +
                "appOwner = " + this.appOwner + "\n" +
                "appName  = " + this.appName  + "\n" +
                "appNameSuffix = " + this.appNameSuffix + "\n" +
                "destination = " + this.destination + "\n" +
                "releaseNotes = " + this.releaseNotes + "\n }"

    }
}
