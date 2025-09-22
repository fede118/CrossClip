package com.section11.crossclip.framework.di

import android.content.Context
import com.section11.crossclip.data.repository.SharedStringsRepository
import com.section11.crossclip.ui.viewmodel.MainViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val androidViewModelModule = module {
    factory<MainViewModel> { (activityContext: Context) ->
        MainViewModel(
            get<SharedStringsRepository> {
                parametersOf(activityContext)
        })
    }
}