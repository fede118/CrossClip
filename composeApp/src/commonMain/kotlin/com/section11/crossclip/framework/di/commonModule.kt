package com.section11.crossclip.framework.di

import com.section11.crossclip.data.repository.SharedStringsRepository
import com.section11.crossclip.ui.viewmodel.MainViewModel
import org.koin.dsl.module

val commonModule = module {
    single<MainViewModel> { MainViewModel(get<SharedStringsRepository>()) }
}