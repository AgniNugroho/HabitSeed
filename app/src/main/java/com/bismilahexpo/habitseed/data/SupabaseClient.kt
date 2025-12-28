package com.bismilahexpo.habitseed.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object Supabase {
    private const val SUPABASE_URL = "https://iitoitisprgwjlerxcsa.supabase.co" 
    private const val SUPABASE_KEY = "sb_publishable_dKV7M2tNrGfJaCQJd5RQ0Q_46a4dLwv"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth) {
            // Optional: You can provide a custom session manager here if needed,
            // but for simple cases, default should work if it's persisting.
            // On Android, explicitly setting up persistence is better.
        }
        install(Postgrest)
        install(Realtime)
        install(io.github.jan.supabase.storage.Storage)
        
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}
