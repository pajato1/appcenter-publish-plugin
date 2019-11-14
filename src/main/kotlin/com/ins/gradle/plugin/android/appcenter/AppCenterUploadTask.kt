package com.ins.gradle.plugin.android.appcenter

import com.google.gson.Gson
import com.ins.gradle.plugin.android.appcenter.entities.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class AppCenterUploadTask : DefaultTask() {

    init {
        group = "appCenter"
        description = "Upload artifacts to App Center"
    }


    @get:Internal  lateinit internal  var extension : BuildVariantExtension

    @get:Internal internal lateinit var artifact: File



    private val baseUrl by lazy { HttpUrl.get(BASE_URL) }
    private lateinit var okHttpClient: OkHttpClient
    private var stageLogger: (String) -> Unit = { print(it) }

    @TaskAction
    fun appCenterUpload() {
        val config = checkConfig(extension)
        initializeClient(config)
        println("DONE")
        val (uploadId, uploadUrl) = initializeUpload(config)
        println("DONE")
        uploadArtifact(uploadUrl, config)
        println("DONE")
        val (_, releasePath) = commitUploadedArtifact(uploadId, config)
        println("DONE")
        distributeArtifact(releasePath, config)
        println("DONE")
    }

    private fun checkConfig(configExtension: BuildVariantExtension): AppCenterConfig {
        stageLogger("Checking configuration...")
        return AppCenterConfig(
                verbose = configExtension.verbose,
                apiToken = configExtension.apiToken
                    ?: throw IllegalArgumentException(
                            "No API token specified as \"apiToken\" in appCenter config."),
                appName = (configExtension.appName
                    ?: throw IllegalArgumentException(
                            "No App name specified as \"appName\" in appCenter config.")) + configExtension.appNameSuffix,

                appOwner = configExtension.appOwner
                    ?: throw IllegalArgumentException(
                            "No user/organization specified as \"appOwner\" in appCenter config."),
                artifact = artifact?.takeIf { it.isFile }
                    ?: throw IllegalArgumentException(
                            "No or invalid artifact specified as \"artifact\" in appCenter config."),
                destination = configExtension.destination
                    ?: throw IllegalArgumentException(
                            "No destination(Group) specified as \"destination\" in appCenter config."),
                releaseNotes = configExtension.releaseNotes ?: "",
                mandatoryUpdate = configExtension.mandatoryUpdate,
                notifyTesters = configExtension.notifyTesters
        )
    }

    private fun initializeClient(config: AppCenterConfig) {
        val httpLogger = HttpLoggingInterceptor.Logger {
            println(it)
        }

        if (config.verbose) {
            stageLogger = { println(it) }
        }

        okHttpClient = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor(httpLogger)
                        .setLevel(if (config.verbose) BASIC else NONE))
                .build()
    }

    private fun initializeUpload(config: AppCenterConfig): InitResponse {
        stageLogger("Initializing upload...")
        val request = Request.Builder()
                .url(baseUrl.newBuilder()
                        .addEncodedPathSegments(PATH_BASE_API)
                        .addEncodedPathSegments(config.appOwner)
                        .addEncodedPathSegments(config.appName)
                        .addEncodedPathSegments(PATH_RELEASE_UPLOAD)
                        .build())
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_TYPE_JSON)
                .addHeader(HEADER_ACCEPT, MEDIA_TYPE_JSON)
                .addHeader(HEADER_TOKEN, config.apiToken)
                .post(RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON), ""))
                .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IllegalStateException(response.message())
        }

        return response.body()?.use {
            Gson().fromJson(it.string(), InitResponse::class.java)
        } ?: throw IllegalStateException("Empty response body!")
    }

    private fun uploadArtifact(uploadUrl: String, config: AppCenterConfig) {
        stageLogger("Uploading artifact...")
        val request = Request.Builder()
                .url(uploadUrl)
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_TYPE_MULTI_FORM)
                .post(MultipartBody.Builder()
                        .addFormDataPart(PART_KEY_ARTIFACT, config.artifact.name,
                                RequestBody.create(
                                        MediaType.parse(MEDIA_TYPE_OCTET),
                                        config.artifact))
                        .build())
                .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IllegalStateException(response.message())
        } else {
            response.close()
        }
    }

    private fun commitUploadedArtifact(uploadId: String, config: AppCenterConfig): CommitResponse {
        stageLogger("Commit uploaded artifact...")
        val request = Request.Builder()
                .url(baseUrl.newBuilder()
                        .addEncodedPathSegments(PATH_BASE_API)
                        .addEncodedPathSegments(config.appOwner)
                        .addEncodedPathSegments(config.appName)
                        .addEncodedPathSegments(PATH_RELEASE_UPLOAD)
                        .addEncodedPathSegments(uploadId)
                        .build())
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_TYPE_JSON)
                .addHeader(HEADER_ACCEPT, MEDIA_TYPE_JSON)
                .addHeader(HEADER_TOKEN, config.apiToken)
                .patch(RequestBody.create(
                        MediaType.parse(MEDIA_TYPE_JSON),
                        Gson().toJson(CommitRequest())))
                .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IllegalStateException(response.message())
        }

        return response.body()?.use {
            Gson().fromJson(it.string(), CommitResponse::class.java)
        } ?: throw IllegalStateException("Empty response body!")
    }

    private fun distributeArtifact(releasePath: String, config: AppCenterConfig) {
        stageLogger("Distribute uploaded artifact...")
        val request = Request.Builder()
                .url(baseUrl.newBuilder()
                        .addEncodedPathSegments(releasePath)
                        .build())
                .addHeader(HEADER_CONTENT_TYPE, MEDIA_TYPE_JSON)
                .addHeader(HEADER_ACCEPT, MEDIA_TYPE_JSON)
                .addHeader(HEADER_TOKEN, config.apiToken)
                .patch(RequestBody.create(
                        MediaType.parse(MEDIA_TYPE_JSON),
                        Gson().toJson(DistributionRequest(
                                destination_name = config.destination,
                                release_notes = config.releaseNotes,
                                mandatory_update = config.mandatoryUpdate,
                                notify_testers = config.notifyTesters))))
                .build()

                            System.err.println(request.toString())
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            System.err.println(response.toString())
            throw IllegalStateException(response.message())
        } else {
            response.close()
        }
    }
}
