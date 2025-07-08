package com.example.tfm.ui.theme.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/* ---------- data class que usamos en la UI ---------- */
data class Friend(
    val uid: String = "",
    val name: String = "",
    val photo: String? = null,
    val points: Int = 0,
    val streak: Int = 0
)

class SocialVm : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    init { listenFriendsUidList() }

    /** Escucha mi documento y carga los UID de mis amigos */
    private fun listenFriendsUidList() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                val map = snap?.get("friends") as? Map<*, *> ?: emptyMap<Any,Any>()
                val ids = map.keys.map { it.toString() }
                fetchFriendsData(ids)
            }
    }

    /** Descarga los datos de cada amigo (nombre, foto, puntos, racha) */
    private fun fetchFriendsData(ids: List<String>) {
        if (ids.isEmpty()) { _friends.value = emptyList(); return }

        val tmpList = mutableListOf<Friend>()

        ids.chunked(10).forEach { chunk ->
            db.collection("users")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .addOnSuccessListener { res ->
                    val list = res.documents.map { doc ->
                        Friend(
                            uid    = doc.id,
                            name   = doc.getString("displayName") ?: "Sin nombre",
                            photo  = doc.getString("photoUrl"),
                            points = doc.getLong("points")?.toInt() ?: 0,
                            streak = doc.getLong("streak")?.toInt() ?: 0
                        )
                    }
                    synchronized(tmpList) {          // varios callbacks
                        tmpList += list
                        if (tmpList.size >= ids.size) {
                            // todos los chunks respondieron
                            _friends.value = tmpList.sortedBy { it.name.lowercase() }
                        }
                    }
                }
        }
    }

    /** Añadir amigo por UID */
    fun addFriendByUid(friendUid: String, onResult: (Boolean) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            db.collection("users").document(myUid)
                .update("friends.$friendUid", true)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        }
    }
}
