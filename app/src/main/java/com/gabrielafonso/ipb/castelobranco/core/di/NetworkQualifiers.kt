package com.gabrielafonso.ipb.castelobranco.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthLessRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthedRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthLessClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Client

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl