package com.gabrielafonso.ipb.castelobranco.features.profile.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.data.local.JsonSnapshotStorage
import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.features.profile.data.dto.MeProfileDto
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ProfileSnapshotRepository @Inject constructor(
    api: ProfileApi,
    json: Json,
    jsonStorage: JsonSnapshotStorage
) : BaseSnapshotRepository<MeProfileDto, MeProfileDto>(
    initialValue = null,
    json = json,
    jsonStorage = jsonStorage,
    serializer = MeProfileDto.serializer(),
    key = "me_profile",
    tag = "Profile",
    fetchNetwork = api::getMeProfile
) {
    override fun mapToDomain(dto: MeProfileDto): MeProfileDto = dto
}
