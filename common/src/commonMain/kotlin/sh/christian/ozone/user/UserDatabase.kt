package sh.christian.ozone.user

import app.bsky.actor.GetProfileQueryParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.toProfile
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.preference
import kotlin.time.Duration.Companion.minutes

class UserDatabase(
  private val clock: Clock,
  private val storage: PersistentStorage,
  private val apiProvider: ApiProvider,
) {
  fun profile(userReference: UserReference): Flow<Profile> = flow {
    val preference = storage.preference<CacheObject?>(userReference.toString(), null)

    val cached = preference.get()
    if (cached != null && cached.isFresh()) {
      emit(cached.profile)
    }

    val identifier = when (userReference) {
      is UserReference.Did -> userReference.did
      is UserReference.Handle -> userReference.handle
    }
    apiProvider.api.getProfile(GetProfileQueryParams(identifier))
      .maybeResponse()
      ?.let { response ->
        val profile = response.toProfile()
        val newCacheObject = CacheObject(clock.now(), profile)
        storage.preference<CacheObject?>(profile.did, null).set(newCacheObject)
        storage.preference<CacheObject?>(profile.handle, null).set(newCacheObject)
        emit(profile)
      }

    emitAll(preference.asFlow().mapNotNull { it?.profile })
  }.distinctUntilChanged()

  private fun CacheObject.isFresh() = (clock.now() - instant) < 5.minutes

  @Serializable
  private data class CacheObject(
    val instant: Instant,
    val profile: Profile,
  )
}
