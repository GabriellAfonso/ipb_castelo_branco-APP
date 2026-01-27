package com.gabrielafonso.ipb.castelobranco.core.network

object Endpoints {
    // Songs / suggestions / reports
    const val SONGS_BY_SUNDAY_PATH = "${ApiConstants.BASE_PATH}songs-by-sunday/"
    const val TOP_SONGS_PATH = "${ApiConstants.BASE_PATH}top-songs/"
    const val TOP_TONES_PATH = "${ApiConstants.BASE_PATH}top-tones/"
    const val SUGGESTED_SONGS_PATH = "${ApiConstants.BASE_PATH}suggested-songs/"

    const val HYMNAL_PATH = "${ApiConstants.BASE_PATH}hymnal/"
    // Schedule generation
    const val GENERATE_SCHEDULE_PATH = "${ApiConstants.BASE_PATH}generate-schedule/"

//    // Recursos gerais (mantive nomes anteriores, caso use outras rotas)
//    const val HYMNAL_PATH = "${ApiConstants.BASE_PATH}hymnal/"
//    const val MUSIC_PATH = "${ApiConstants.BASE_PATH}music/"
//    const val SCHEDULE_PATH = "${ApiConstants.BASE_PATH}schedule/"
}