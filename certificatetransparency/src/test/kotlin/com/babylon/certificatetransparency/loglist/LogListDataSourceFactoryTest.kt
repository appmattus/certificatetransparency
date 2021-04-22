package com.babylon.certificatetransparency.loglist

import com.babylon.certificatetransparency.internal.loglist.LogListJsonFailedLoadingWithException
import com.babylon.certificatetransparency.utils.TestData
import com.babylon.certificatetransparency.utils.assertIsA
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

public class LogListDataSourceFactoryTest {

    init {
        Logger.getLogger(MockWebServer::class.java.name).level = Level.OFF
    }

    private val server = MockWebServer().apply {
        dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.path) {
                    "/log_list.json" -> MockResponse().setResponseCode(200)
                        .setBody(Buffer().write(TestData.file(TestData.TEST_LOG_LIST_JSON).readBytes()))
                    "/log_list.sig" -> MockResponse().setResponseCode(200)
                        .setBody(Buffer().write(TestData.file(TestData.TEST_LOG_LIST_SIG).readBytes()))
                    "/log_list.zip" -> MockResponse().setResponseCode(200)
                        .setBody(Buffer().write(TestData.file(TestData.TEST_LOG_LIST_ZIP).readBytes()))

                    else -> MockResponse().setResponseCode(404)
                }

                return response.apply(configuration)
            }
        }

        start()
    }

    private val baseUrl = server.url("/")

    private var configuration: MockResponse.() -> Unit = {}

    @Test
    public fun timeoutReturnsException() {
        runBlocking {
            // Given the log list service times out
            configuration = { setBodyDelay(5, TimeUnit.SECONDS) }
            val logListService = LogListDataSourceFactory.createLogListService(baseUrl = baseUrl.toString(), networkTimeoutSeconds = 1)

            // When we request the log list
            val dataSource = LogListDataSourceFactory.createDataSource(logListService = logListService)
            val result = dataSource.get()

            // Then a failure is returned with a SocketTimeoutException
            assertIsA<LogListJsonFailedLoadingWithException>(result)
            assertIsA<SocketTimeoutException>((result as LogListJsonFailedLoadingWithException).exception)
        }
    }

    @Test
    public fun successReturnsValid() {
        runBlocking {
            // Given a default log list service
            val logListService = LogListDataSourceFactory.createLogListService(baseUrl = baseUrl.toString())

            // When we request the log list
            val dataSource = LogListDataSourceFactory.createDataSource(logListService = logListService)
            val result = dataSource.get()

            // Then a valid result is returned
            assertIsA<LogListResult.Valid>(result)
        }
    }

    @Test
    public fun serverErrorReturnsException() {
        runBlocking {
            // Given the log list service times out
            configuration = {
                setResponseCode(500)
                body = Buffer()
            }

            val logListService = LogListDataSourceFactory.createLogListService(baseUrl = baseUrl.toString(), networkTimeoutSeconds = 1)

            // When we request the log list
            val dataSource = LogListDataSourceFactory.createDataSource(logListService = logListService)
            val result = dataSource.get()

            // Then a failure is returned with an IOException
            assertIsA<LogListJsonFailedLoadingWithException>(result)
            assertIsA<IOException>((result as LogListJsonFailedLoadingWithException).exception)
        }
    }
}
