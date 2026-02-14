// app/src/main/java/com/gabrielafonso/ipb/castelobranco/data/local/TokenStorage.kt
package com.gabrielafonso.ipb.castelobranco.features.auth.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gabrielafonso.ipb.castelobranco.core.di.AuthPrefs
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage @Inject constructor(
    @param: AuthPrefs private val dataStore: DataStore<Preferences>,
    private val json: Json
) {
    private object Keys {
        val TOKENS = stringPreferencesKey("auth_tokens")
    }

    val tokensFlow: Flow<AuthTokens?> =
        dataStore.data.map { prefs ->
            val raw = prefs[Keys.TOKENS] ?: return@map null
            runCatching { json.decodeFromString<AuthTokens>(raw) }.getOrNull()
        }

    suspend fun save(tokens: AuthTokens) {
        dataStore.edit { prefs ->
            prefs[Keys.TOKENS] = json.encodeToString(tokens)
        }
    }

    suspend fun loadOrNull(): AuthTokens? {
        return tokensFlow.first()
    }

    suspend fun clear() {
        dataStore.edit { it.remove(Keys.TOKENS) }
    }
}
