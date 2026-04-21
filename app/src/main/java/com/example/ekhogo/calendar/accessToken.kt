package com.example.ekhogo.calendar

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


suspend fun getAccessToken(
    context: Context,
    account: GoogleSignInAccount
): String? {
    return withContext(Dispatchers.IO) {
        try {
            val googleAccount = account.account ?: return@withContext null

            val scope = "oauth2:https://www.googleapis.com/auth/calendar.events"
            GoogleAuthUtil.getToken(context, googleAccount, scope)

        } catch (e: Exception) {
            null
        }
    }
}
