package com.gabrielafonso.ipb.castelobranco.features.auth.data.api

import com.gabrielafonso.ipb.castelobranco.core.network.ApiConstants

object AuthEndpoins {
    const val AUTH_LOGIN_PATH = "${ApiConstants.BASE_PATH}auth/login/"
    const val AUTH_REGISTER_PATH = "${ApiConstants.BASE_PATH}auth/register/"
    const val AUTH_REFRESH_PATH = "${ApiConstants.BASE_PATH}auth/refresh/"
}