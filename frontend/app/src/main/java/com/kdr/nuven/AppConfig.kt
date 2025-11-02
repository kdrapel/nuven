package com.kdr.nuven

/**
 * Centralized application configuration.
 * Configuration values are injected at build time via BuildConfig.
 */
object AppConfig {
    
    // Data source configuration
    val dataUrl: String = BuildConfig.DATA_URL
    val githubRepo: String = BuildConfig.GITHUB_REPO
    val githubApiBase: String = BuildConfig.GITHUB_API_BASE
    val dataFilePath: String = BuildConfig.DATA_FILE_PATH
    
    // Computed URLs
    val githubRepoUrl: String 
        get() = "https://github.com/$githubRepo"
    
    val githubCommitsApiUrl: String 
        get() = "$githubApiBase/repos/$githubRepo/commits?path=$dataFilePath&page=1&per_page=1"
}
