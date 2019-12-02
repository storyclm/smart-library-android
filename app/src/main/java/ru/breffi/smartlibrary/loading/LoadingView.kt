package ru.breffi.smartlibrary.loading

interface LoadingView {

    fun showProgress(progress: Float)

    fun close()
}