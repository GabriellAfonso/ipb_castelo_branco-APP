package com.gabrielafonso.ipb.castelobranco.core.network

object Endpoints {
    // Songs / suggestions / reports
    const val SONGS_BY_SUNDAY_PATH = "${ApiConstants.BASE_PATH}songs-by-sunday/"
    const val TOP_SONGS_PATH = "${ApiConstants.BASE_PATH}top-songs/"
    const val TOP_TONES_PATH = "${ApiConstants.BASE_PATH}top-tones/"
    const val SUGGESTED_SONGS_PATH = "${ApiConstants.BASE_PATH}suggested-songs/"

    const val HYMNAL_PATH = "${ApiConstants.BASE_PATH}hymnal/"

    const val GENERATE_SCHEDULE_PATH = "${ApiConstants.BASE_PATH}generate-schedule/"

    const val AUTH_LOGIN_PATH = "${ApiConstants.BASE_PATH}auth/login/"
    const val AUTH_REGISTER_PATH = "${ApiConstants.BASE_PATH}auth/register/"

    const val AUTH_REFRESH_PATH = "${ApiConstants.BASE_PATH}auth/refresh/"
    const val ME_PROFILE_PHOTO_PATH = "${ApiConstants.BASE_PATH}me/profile/photo/"
}