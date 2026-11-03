package com.vedatv.app

import android.app.Application
import com.vedatv.app.data.db.AppDatabase

class VedaTvApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.create(this)
    }
}
