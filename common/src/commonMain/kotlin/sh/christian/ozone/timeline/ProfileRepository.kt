package sh.christian.ozone.timeline

import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.ProfileView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.app.Supervisor

class ProfileRepository(
  private val apiProvider: ApiProvider,
) : Supervisor {
  private val profileFlow = MutableStateFlow<ProfileView?>(null)

  override suspend fun CoroutineScope.onStart() {
    apiProvider.auth().map { auth ->
      auth?.handle?.let { handle ->
        apiProvider.api
          .getProfile(GetProfileQueryParams(handle))
          .maybeResponse()
      }
    }.collect(profileFlow)
  }

  fun profile(): Flow<ProfileView?> = profileFlow
}
