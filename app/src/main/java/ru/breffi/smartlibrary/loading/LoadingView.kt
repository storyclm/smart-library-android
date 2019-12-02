package ru.breffi.clm.ui.loading

interface LoadingView {

    fun showProgress(progress: Float)

    fun close()
}