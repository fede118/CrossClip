package com.section11.crossclip.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.section11.crossclip.BuildConfig
import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.domain.models.User
import kotlinx.coroutines.tasks.await

/**
 * TODO: context in repository is very bad, should find another way to do it
 */
class AndroidFirebaseRepository (
    private val context: Context,
    private val credentialManager: CredentialManager,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
): SharedStringsRepository {

    private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .setAutoSelectEnabled(true)
        .build()

    private val googleCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    private val sharedStringsCollection = firestore.collection("shared_strings")

    override suspend fun addSharedString(sharedString: SharedString): Result<String> {
        return try {
            val docRef = sharedStringsCollection.add(
                hashMapOf(
                    "content" to sharedString.content,
                    "timestamp" to sharedString.timestamp,
                    "userId" to sharedString.userId,
                    "deviceInfo" to sharedString.deviceInfo
                )
            ).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSharedStrings(userId: String): Result<List<SharedString>> {
        return try {
            val querySnapshot = sharedStringsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val sharedStrings = querySnapshot.documents.map { document ->
                SharedString(
                    id = document.id,
                    content = document.getString("content") ?: "",
                    timestamp = document.getLong("timestamp") ?: 0L,
                    userId = document.getString("userId") ?: "",
                    deviceInfo = document.getString("deviceInfo") ?: ""
                )
            }

            Result.success(sharedStrings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSharedString(id: String): Result<Unit> {
        return try {
            sharedStringsCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            val credentialResponse = credentialManager.getCredential(context, googleCredentialRequest)

            val result: Result<User> = when (credentialResponse.credential) {
                is CustomCredential -> {
                    if (credentialResponse.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleUser =
                            GoogleIdTokenCredential.createFrom(credentialResponse.credential.data)
                        val credential = GoogleAuthProvider.getCredential(googleUser.idToken, null)

                        val task = firebaseAuth.signInWithCredential(credential)
                        task.await()

                        return if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val uid = user?.uid
                            val email = user?.email
                            return if (uid == null) {
                                Result.failure(Throwable("uid null"))
                            } else if (email == null) {
                                Result.failure(Throwable("email null"))
                            } else {
                                with(googleUser) {
                                    Result.success(
                                        User(
                                            idToken,
                                            uid,
                                            email,
                                            displayName,
                                            profilePictureUri.toString(),
                                        )
                                    )
                                }
                            }
                        } else {
                            Result.failure(Throwable("Firebase authentication failed"))
                        }
                    } else {
                        Result.failure(Throwable("InvalidCredentialTypeException"))
                    }
                }

                else -> Result.failure(Throwable("InvalidCredentialTypeException"))
            }
            result
        } catch (cancellationException: GetCredentialCancellationException) {
            Result.failure(Throwable("CancelledByUserException"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
