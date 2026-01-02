package sh.christian.ozone.oauth.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import sh.christian.ozone.api.Language

@Serializable
internal data class OAuthAuthorizationServer(
  val issuer: String,
  @SerialName("authorization_endpoint")
  val authorizationEndpoint: String,
  @SerialName("token_endpoint")
  val tokenEndpoint: String,
  @SerialName("token_endpoint_auth_methods_supported")
  val tokenEndpointAuthMethodsSupported: List<String> = listOf("client_secret_basic"),
  @SerialName("jwks_uri")
  val jwksUri: String? = null,
  @SerialName("claims_supported")
  val claimsSupported: List<String> = emptyList(),
  @SerialName("claims_locales_supported")
  val claimsLocalesSupported: List<String> = emptyList(),
  @SerialName("claims_parameter_supported")
  val claimsParameterSupported: Boolean? = null,
  @SerialName("request_parameter_supported")
  val requestParameterSupported: Boolean? = null,
  @SerialName("request_uri_parameter_supported")
  val requestUriParameterSupported: Boolean? = null,
  @SerialName("require_request_uri_registration")
  val requireRequestUriRegistration: Boolean? = null,
  @SerialName("scopes_supported")
  val scopesSupported: List<String> = emptyList(),
  @SerialName("subject_types_supported")
  val subjectTypesSupported: List<String> = emptyList(),
  @SerialName("response_types_supported")
  val responseTypesSupported: List<String> = emptyList(),
  @SerialName("response_modes_supported")
  val responseModesSupported: List<String> = emptyList(),
  @SerialName("grant_types_supported")
  val grantTypesSupported: List<String> = emptyList(),
  @SerialName("code_challenge_methods_supported")
  val codeChallengeMethodsSupported: List<String> = emptyList(),
  @SerialName("ui_locales_supported")
  val uiLocalesSupported: List<Language> = emptyList(),
  @SerialName("id_token_signing_alg_values_supported")
  val idTokenSigningAlgValuesSupported: List<String> = emptyList(),
  @SerialName("display_values_supported")
  val displayValuesSupported: List<String> = emptyList(),
  @SerialName("request_object_signing_alg_values_supported")
  val requestObjectSigningAlgValuesSupported: List<String> = emptyList(),
  @SerialName("authorization_response_iss_parameter_supported")
  val authorizationResponseIssParameterSupported: Boolean? = null,
  @SerialName("authorization_details_types_supported")
  val authorizationDetailsTypesSupported: List<String> = emptyList(),
  @SerialName("request_object_encryption_alg_values_supported")
  val requestObjectEncryptionAlgValuesSupported: List<String> = emptyList(),
  @SerialName("request_object_encryption_enc_values_supported")
  val requestObjectEncryptionEncValuesSupported: List<String> = emptyList(),
  @SerialName("token_endpoint_auth_signing_alg_values_supported")
  val tokenEndpointAuthSigningAlgValuesSupported: List<String> = emptyList(),
  @SerialName("revocation_endpoint")
  val revocationEndpoint: String? = null,
  @SerialName("introspection_endpoint")
  val introspectionEndpoint: String? = null,
  @SerialName("pushed_authorization_request_endpoint")
  val pushedAuthorizationRequestEndpoint: String? = null,
  @SerialName("require_pushed_authorization_requests")
  val requirePushedAuthorizationRequests: Boolean? = null,
  @SerialName("userinfo_endpoint")
  val userinfoEndpoint: String? = null,
  @SerialName("end_session_endpoint")
  val endSessionEndpoint: String? = null,
  @SerialName("registration_endpoint")
  val registrationEndpoint: String? = null,
  @SerialName("dpop_signing_alg_values_supported")
  val dpopSigningAlgValuesSupported: List<String> = emptyList(),
  @SerialName("protected_resources")
  val protectedResources: List<String> = emptyList(),
  @SerialName("client_id_metadata_document_supported")
  val clientIdMetadataDocumentSupported: Boolean? = null,
  @SerialName("prompt_values_supported")
  val promptValuesSupported: List<String> = emptyList()
)
