package tw.maoyue.lavabilibili

import com.sedmelluq.discord.lavaplayer.tools.http.HttpContextFilter
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.HttpClientContext

class BilibiliHttpContextFilter : HttpContextFilter {
    override fun onContextOpen(context: HttpClientContext) {
        //
    }

    override fun onContextClose(context: HttpClientContext) {
        //
    }

    override fun onRequest(context: HttpClientContext, request: HttpUriRequest, isRepetition: Boolean) {
        request.setHeader("Referer", "https://www.bilibili.com/")
        request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:130.0) Gecko/20100101 Firefox/130.0")
    }

    override fun onRequestResponse(
            context: HttpClientContext,
            request: HttpUriRequest,
            response: HttpResponse
    ): Boolean {
        return false
    }

    override fun onRequestException(context: HttpClientContext?, request: HttpUriRequest, error: Throwable): Boolean {
        return false
    }
}