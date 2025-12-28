package com.bismilahexpo.habitseed.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.bismilahexpo.habitseed.data.Supabase
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

object StorageHelper {

    suspend fun uploadFile(
        context: Context,
        bucketName: String,
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    withContext(Dispatchers.Main) {
                        onError("Gagal membuka file: InputStream null")
                        Toast.makeText(context, "Gagal membuka file", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                val bytes = inputStream.readBytes()
                inputStream.close()

                if (bytes.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onError("File kosong (0 bytes)")
                        Toast.makeText(context, "File kosong", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                val fileName = "${System.currentTimeMillis()}.jpg"
                val bucket = Supabase.client.storage.from(bucketName)

                // Optional: Show starting toast on Main
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Mulai upload (${bytes.size} bytes)...", Toast.LENGTH_SHORT).show()
                }

                bucket.upload(fileName, bytes) {
                    upsert = true
                }

                val publicUrl = bucket.publicUrl(fileName)

                withContext(Dispatchers.Main) {
                    onSuccess(publicUrl)
                    Toast.makeText(context, "Upload berhasil!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error")
                    Toast.makeText(context, "Error upload: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
    }
}
