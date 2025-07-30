package com.section11.crossclip.data.repository

import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.domain.models.User

interface SharedStringsRepository {
    suspend fun addSharedString(sharedString: SharedString): Result<String>
    suspend fun getSharedStrings(userId: String): Result<List<SharedString>>
    suspend fun deleteSharedString(id: String): Result<Unit>
    suspend fun signInWithGoogle(): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
}
