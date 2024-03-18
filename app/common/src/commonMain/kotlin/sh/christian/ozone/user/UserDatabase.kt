package sh.christian.ozone.user

import app.bsky.actor.GetProfileQueryParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.di.SingleInApp
import sh.christian.ozone.model.FullProfile
import sh.christian.ozone.model.toProfile
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.nullablePreference
import kotlin.time.Duration.Companion.minutes

@Inject
@SingleInApp
class UserDatabase(
  private val clock: Clock,
  private val storage: PersistentStorage,
  private val apiProvider: ApiProvider,
) {
  fun profile(userReference: UserReference): Flow<FullProfile> {
    return profileOrNull(userReference).filterNotNull()
  }

  fun profileOrNull(userReference: UserReference): Flow<FullProfile?> = flow {
    val key = when (userReference) {
      is UserDid -> "did:${userReference.did}"
      is UserHandle -> "handle:${userReference.handle}"
    }
    val preference = storage.nullablePreference<CacheObject>(key, null)

    val cached = preference.get()
    if (cached != null && cached.isFresh()) {
      emit(cached.profile)
    }

    val identifier = when (userReference) {
      is UserDid -> userReference.did
      is UserHandle -> userReference.handle
    }

    val profileOrNull =
      apiProvider.api.getProfile(GetProfileQueryParams(identifier))
        .maybeResponse()
        ?.let { response ->
          val profile = response.toProfile()
          val newCacheObject = CacheObject(clock.now(), profile)
          storage.nullablePreference<CacheObject>("did:${profile.did}", null).set(newCacheObject)
          storage.nullablePreference<CacheObject>("handle:${profile.handle}", null).set(newCacheObject)
          profile
        }
    emit(profileOrNull)

    emitAll(preference.updates.mapNotNull { it?.profile })
  }.distinctUntilChanged()

  private fun CacheObject.isFresh() = (clock.now() - instant) < 5.minutes

  @Serializable
  private data class CacheObject(
    val instant: Instant,
    val profile: FullProfile,
  )
}
