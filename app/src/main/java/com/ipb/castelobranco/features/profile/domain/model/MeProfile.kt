package com.ipb.castelobranco.features.profile.domain.model

data class MeProfile(
    val name: String,
    val active: Boolean,
    val isMember: Boolean,
    val isAdmin: Boolean,
    val photoUrl: String?
)