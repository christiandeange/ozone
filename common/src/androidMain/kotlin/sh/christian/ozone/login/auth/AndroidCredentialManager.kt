package sh.christian.ozone.login.auth

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.CredentialManager.Companion as AndroidxCredentialManager

class AndroidCredentialManager(
  activity: Activity,
) : CredentialManager {
  private val credentialManager = AndroidxCredentialManager.create(activity.applicationContext)
  private var foregroundActivity: Activity? = activity

  init {
    activity.application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
      override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (foregroundActivity == null || foregroundActivity!!::class == activity::class) {
          foregroundActivity = activity
        }
      }

      override fun onActivityStarted(activity: Activity) = Unit
      override fun onActivityResumed(activity: Activity) = Unit
      override fun onActivityPaused(activity: Activity) = Unit
      override fun onActivityStopped(activity: Activity) = Unit
      override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit
      override fun onActivityDestroyed(activity: Activity) {
        if (activity == foregroundActivity) {
          foregroundActivity = null
        }
      }
    })
  }

  override suspend fun getStoredCredentials(): Credentials? {
    val request = GetCredentialRequest(listOf(GetPasswordOption()))

    try {
      val credentialResponse = credentialManager.getCredential(
        context = foregroundActivity!!,
        request = request,
      )

      return (credentialResponse.credential as? PasswordCredential)?.let {
        Credentials(
          email = null,
          username = it.id,
          password = it.password,
          inviteCode = null,
        )
      }
    } catch (e: GetCredentialCancellationException) {
      // User cancelled saving the credentials.
      return null
    } catch (e: GetCredentialException) {
      e.printStackTrace()
      return null
    }
  }

  override suspend fun saveCredentials(credentials: Credentials) {
    try {
      val request = CreatePasswordRequest(credentials.username, credentials.password)
      credentialManager.createCredential(
        context = foregroundActivity!!,
        request = request,
      )
    } catch (e: CreateCredentialCancellationException) {
      // User cancelled saving the credentials.
    } catch (e: CreateCredentialException) {
      e.printStackTrace()
    }
  }
}
