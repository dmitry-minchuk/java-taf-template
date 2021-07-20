package configuration.listeners;

import com.epam.reportportal.service.ReportPortal;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class RestAssuredFilter implements Filter {
    protected static final Logger LOGGER = LogManager.getLogger(RestAssuredFilter.class);
    private final String emptySpace = " ";
    private final String newLine = "\n";
    private final StringBuffer prettyRequest = new StringBuffer(newLine + String.format("%1$s%1$s%2$s%3$sHTTP REQUEST%3$s%2$s", System.lineSeparator(), "-", "=")).append(newLine);
    private final StringBuffer prettyResponse = new StringBuffer(newLine + String.format("%1$s%1$s%2$s%3$sHTTP RESPONSE%3$s%2$s", System.lineSeparator(), "-", "=")).append(newLine);

    @Override
    public Response filter(FilterableRequestSpecification rq, FilterableResponseSpecification rs, FilterContext ctx) {
        Response response = ctx.next(rq, rs);
        prettyRequest
                .append(rq.getMethod()).append(emptySpace).append(rq.getURI()).append(newLine)
                .append(handleHeaders(rq.getHeaders()));
                if(rq.getBody() instanceof String) {
                    prettyRequest.append("Payload: ").append(newLine).append(rq.getBody().toString());
                }

        prettyResponse
                .append(response.getStatusCode()).append(emptySpace).append(response.getStatusLine())
                .append(handleHeaders(response.headers()));
                if(response.getBody().prettyPrint() != null) {
                    prettyRequest.append("Body: ").append(newLine).append(response.getBody().prettyPrint());
                }

//        ReportPortal.emitLog(prettyRequest.toString(),"INFO", new Date());
//        ReportPortal.emitLog(prettyResponse.toString(),"INFO", new Date());
        LOGGER.info(prettyRequest.toString());
        LOGGER.info(prettyResponse.toString());
        return response;
    }

    private StringBuffer handleHeaders(Headers headers) {
        StringBuffer prettyHeaders = new StringBuffer();
        for(Header h: headers) {
            prettyHeaders
                    .append("Header: ")
                    .append(h.getName())
                    .append(" : ")
                    .append(h.getValue())
                    .append(newLine);
        }
        return prettyHeaders;
    }
}
