package com.section11.crossclip.data.repository
import com.section11.crossclip.data.repository.SharedStringsRepository
import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.domain.models.User
import kotlinx.coroutines.await
import kotlin.js.Promise

// External Firebase interfaces for Wasm-JS
@JsName("Object")
external class JSObject : JsAny

external interface FirebaseApp : JsAny
external interface FirebaseAuth : JsAny
external interface Firestore : JsAny
external interface GoogleAuthProvider : JsAny
external interface UserCredential : JsAny
external interface FirebaseUser : JsAny
external interface DocumentReference : JsAny
external interface QuerySnapshot : JsAny
external interface DocumentSnapshot : JsAny
external interface CollectionReference : JsAny
external interface Query : JsAny
external interface QueryConstraint : JsAny

// Firebase global object
external interface FirebaseGlobal : JsAny {
    val app: FirebaseApp
    val auth: FirebaseAuth
    val firestore: Firestore
    fun GoogleAuthProvider(): GoogleAuthProvider
}

// Window interface to access Firebase
external interface WindowWithFirebase : JsAny {
    val firebase: FirebaseGlobal
}

// External function declarations
@JsName("signInWithPopup")
external fun signInWithPopup(auth: FirebaseAuth, provider: GoogleAuthProvider): Promise<UserCredential>

@JsName("signOut")
external fun signOutFromAuth(): Promise<JsAny>

@JsName("collection")
external fun collection(path: String): CollectionReference

@JsName("addDoc")
external fun addDoc(collection: CollectionReference, data: JsAny): Promise<DocumentReference>

@JsName("getDocs")
external fun getDocs(query: Query): Promise<QuerySnapshot>

@JsName("deleteDoc")
external fun deleteDoc(docRef: DocumentReference): Promise<JsAny>

@JsName("doc")
external fun doc(path: String): DocumentReference

@JsName("query")
external fun query(collection: CollectionReference, vararg constraints: QueryConstraint): Query

@JsName("where")
external fun where(field: String, operator: String, value: JsAny): QueryConstraint

@JsName("orderBy")
external fun orderBy(field: String, direction: String = definedExternally): QueryConstraint

// Extension functions to access JS object properties safely
external interface JsUser : JsAny {
    val uid: String
    val email: String?
    val displayName: String?
    val photoURL: String?
}

external interface JsUserCredential : JsAny {
    val user: JsUser
}

external interface JsDocumentData : JsAny {
    val content: String?
    val timestamp: Double?
    val userId: String?
    val deviceInfo: String?
}

external interface JsDocumentSnapshot : JsAny {
    val id: String
    fun data(): JsDocumentData
}

external interface JsQuerySnapshot : JsAny {
    fun forEach(callback: (JsDocumentSnapshot) -> Unit)
    val size: Int
}

external interface JsDocumentReference : JsAny {
    val id: String
}

// Global window access - only allowed usage of js()
@JsName("getWindow")
external fun getWindow(): WindowWithFirebase

// Helper functions using external declarations instead of js()
@JsName("createEmptyObject")
external fun createEmptyObject(): JsAny

@JsName("setObjectProperty")
external fun setObjectProperty(obj: JsAny, key: String, value: JsAny)

@JsName("setObjectStringProperty")
external fun setObjectStringProperty(obj: JsAny, key: String, value: String)

@JsName("setObjectNumberProperty")
external fun setObjectNumberProperty(obj: JsAny, key: String, value: Double)

@JsName("getCurrentFirebaseUser")
external fun getCurrentFirebaseUser(): JsUser?

@JsName("stringToJsAny")
external fun stringToJsAny(value: String): JsAny

// Helper function to create a JS object with properties
fun createDocumentData(
    content: String,
    timestamp: Long,
    userId: String,
    deviceInfo: String
): JsAny {
    val obj = createEmptyObject()
    setObjectStringProperty(obj, "content", content)
    setObjectNumberProperty(obj, "timestamp", timestamp.toDouble())
    setObjectStringProperty(obj, "userId", userId)
    setObjectStringProperty(obj, "deviceInfo", deviceInfo)
    return obj
}

// Get Firebase from global window
fun getFirebase(): FirebaseGlobal {
    return getWindow().firebase
}

class WasmJsFirebaseRepository : SharedStringsRepository {

    private val firebase = getFirebase()

    override suspend fun addSharedString(sharedString: SharedString): Result<String> {
        return try {
            val docData = createDocumentData(
                content = sharedString.content,
                timestamp = sharedString.timestamp,
                userId = sharedString.userId,
                deviceInfo = sharedString.deviceInfo
            )

            val collectionRef = collection("shared_strings")
            val docRef = addDoc(collectionRef, docData).await<DocumentReference>()
            val jsDocRef = docRef.unsafeCast<JsDocumentReference>()

            Result.success(jsDocRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSharedStrings(userId: String): Result<List<SharedString>> {
        return try {
            val collectionRef = collection("shared_strings")
            val userIdJS = stringToJsAny(userId)
            val q = query(
                collectionRef,
                where("userId", "==", userIdJS),
                orderBy("timestamp", "desc")
            )

            val querySnapshot = getDocs(q).await<QuerySnapshot>()
            val jsQuerySnapshot = querySnapshot.unsafeCast<JsQuerySnapshot>()

            val sharedStrings = mutableListOf<SharedString>()
            jsQuerySnapshot.forEach { doc ->
                val data = doc.data()
                sharedStrings.add(
                    SharedString(
                        id = doc.id,
                        content = data.content ?: "",
                        timestamp = data.timestamp?.toLong() ?: 0L,
                        userId = data.userId ?: "",
                        deviceInfo = data.deviceInfo ?: ""
                    )
                )
            }

            Result.success(sharedStrings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSharedString(id: String): Result<Unit> {
        return try {
            val docRef = doc("shared_strings/$id")
            deleteDoc(docRef).await<JsAny>()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            val provider = firebase.GoogleAuthProvider()
            val result = signInWithPopup(firebase.auth, provider).await<UserCredential>()
            val jsResult = result.unsafeCast<JsUserCredential>()
            val firebaseUser = jsResult.user

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoURL
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            signOutFromAuth().await<JsAny>()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val currentUserJS = getCurrentFirebaseUser()

            if (currentUserJS != null) {
                val user = User(
                    id = currentUserJS.uid,
                    email = currentUserJS.email ?: "",
                    displayName = currentUserJS.displayName ?: "",
                    photoUrl = currentUserJS.photoURL
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
