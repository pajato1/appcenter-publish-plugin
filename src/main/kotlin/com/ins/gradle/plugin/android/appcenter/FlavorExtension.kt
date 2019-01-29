package com.ins.gradle.plugin.android.appcenter

import java.io.File

open class FlavorExtension {
    var apiToken: String? = null
    var appOwner: String? = null
    var appName: String? = null

    var destination: String? = null
    var releaseNotes: String? = null
    var verbose: Boolean = false

     lateinit var name: String

    constructor(){}


    constructor(name : String){
        this.name = name
    }
    constructor(name : String, appName : String?, destination: String?, releaseNotes : String?, appOwner : String?, verbose : Boolean = false){

        this.name = name
        this.appName = appName
        this.destination = destination
        this.releaseNotes = releaseNotes
        this.appOwner = appOwner
        this.verbose = verbose


    }


    fun mergeWith(extension2 : FlavorExtension) : FlavorExtension{

        if(this.apiToken == null) this.apiToken= extension2.apiToken
        if(this.appOwner == null) this.appOwner= extension2.appOwner
        if(this.appName == null) this.appName = extension2.appName
        if(this.destination == null) this.destination =extension2.destination
        if(this.releaseNotes == null) this.releaseNotes =  extension2.releaseNotes

        return this

    }
}
