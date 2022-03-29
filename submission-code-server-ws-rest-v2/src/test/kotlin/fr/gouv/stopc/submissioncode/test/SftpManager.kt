package fr.gouv.stopc.submissioncode.test

import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import java.util.Base64
import kotlin.io.path.toPath

class SftpManager : TestExecutionListener {

    companion object {

        private val PRIVATE_KEY_PATH = SftpManager::class.java.getResource("/sftp/id_rsa")!!.toURI().toPath().toAbsolutePath()
        private val PUBLIC_KEY_PATH = SftpManager::class.java.getResource("/sftp/id_rsa.pub")!!.toURI().toPath().toAbsolutePath()

        private val SFTP_CONTAINER = GenericContainer<Nothing>("atmoz/sftp").apply {
            withExposedPorts(22)
            withFileSystemBind(PUBLIC_KEY_PATH.toString(), "/home/user/.ssh/keys/id_rsa.pub", BindMode.READ_ONLY)
            withCommand("user::1001")
            start()
            execInContainer("mkdir", "/home/user/upload")
            execInContainer("chown", "-R", "user:", "/home/user/upload")

            System.setProperty("submission.code.server.sftp.host", host)
            System.setProperty("submission.code.server.sftp.host.port", getMappedPort(22).toString())
            System.setProperty("submission.code.server.sftp.key", PRIVATE_KEY_PATH.toString())
            System.setProperty(
                "submission.code.server.sftp.host.passphrase", Base64.getEncoder().encodeToString("passphrase".toByteArray())
            )
        }

        fun listFiles() = SFTP_CONTAINER.execInContainer("find", "/home/user/upload", "-type", "f")
            .stdout
            .trim()
            .split("\n")
            .sorted()
            .map { containerPath ->
                SFTP_CONTAINER.copyFileFromContainer(containerPath) { inputStream ->
                    NamedContentFile(containerPath.removePrefix("/home/user/upload/"), inputStream.readAllBytes())
                }
            }
    }

    override fun beforeTestMethod(testContext: TestContext) {
        SFTP_CONTAINER.execInContainer("find", "/home/user/upload", "-type", "f", "-delete")
    }
}
