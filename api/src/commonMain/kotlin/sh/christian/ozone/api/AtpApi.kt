package sh.christian.ozone.api

import com.atproto.session.CreateRequest
import com.atproto.session.CreateResponse
import sh.christian.ozone.api.response.AtpResponse

interface AtpApi {
  fun setAuthentication(authenticationData: String?)

  suspend fun createSession(request: CreateRequest): AtpResponse<CreateResponse>
}
