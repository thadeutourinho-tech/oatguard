package com.capsec.oatguard.data.api

import com.capsec.oatguard.data.api.models.SafeBrowsingRequest
import com.capsec.oatguard.data.api.models.SafeBrowsingResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SafeBrowsingService {
    @POST("v4/threatMatches:find")
    suspend fun checkThreat(
        @Query("key") apiKey: String,
        @Body request: SafeBrowsingRequest
    ): SafeBrowsingResponse
}
