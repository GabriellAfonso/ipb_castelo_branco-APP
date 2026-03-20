package com.ipb.castelobranco.features.gallery.data.api

import com.ipb.castelobranco.core.network.ApiConstants

object GalleryEndpoints {
    const val  DOWNLOAD_ALL_PHOTOS = "${ApiConstants.BASE_PATH}photos/"

    const val DOWNLOAD_ALBUM = "${ApiConstants.BASE_PATH}albums/{id}/photos/"
}