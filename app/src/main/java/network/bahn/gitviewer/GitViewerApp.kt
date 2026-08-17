package network.bahn.gitviewer

import android.app.Application
import network.bahn.gitviewer.data.SshEnvironment

class GitViewerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SshEnvironment.install(filesDir)
    }
}
