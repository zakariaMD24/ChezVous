package com.example.chezvous.presentation.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

@Composable
fun rememberGoogleSignInHandler(
    onIdToken: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) {
        CredentialManager.create(context)
    }

    return remember(context, credentialManager, coroutineScope, onIdToken, onError) {
        {
            val webClientId = context.getGeneratedString("default_web_client_id")

            if (webClientId.isBlank()) {
                onError(
                    "Google n'est pas encore configure. Activez Google dans Firebase, ajoutez SHA-1/SHA-256, puis remplacez google-services.json."
                )
            } else {
                coroutineScope.launch {
                    try {
                        val googleOption = GetSignInWithGoogleOption.Builder(webClientId)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleOption)
                            .build()

                        val result = credentialManager.getCredential(context, request)

                        val credential = result.credential

                        if (
                            credential is CustomCredential &&
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)

                            onIdToken(googleCredential.idToken)
                        } else {
                            onError("Compte Google non reconnu")
                        }
                    } catch (_: GoogleIdTokenParsingException) {
                        onError("Impossible de lire le compte Google")
                    } catch (_: GetCredentialException) {
                        onError("Connexion Google annulee ou indisponible")
                    } catch (_: Exception) {
                        onError("Connexion Google impossible pour le moment")
                    }
                }
            }
        }
    }
}

@Composable
fun rememberCredentialLogoutHandler(
    onLogout: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) {
        CredentialManager.create(context)
    }

    return remember(context, credentialManager, coroutineScope, onLogout) {
        {
            coroutineScope.launch {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } finally {
                    onLogout()
                }
            }
        }
    }
}

private fun Context.getGeneratedString(name: String): String {
    val resourceId = resources.getIdentifier(name, "string", packageName)
    return if (resourceId == 0) {
        ""
    } else {
        runCatching { getString(resourceId) }.getOrDefault("")
    }
}
