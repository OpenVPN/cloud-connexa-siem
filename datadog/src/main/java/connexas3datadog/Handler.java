package connexas3datadog;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.datadog.api.client.ApiClient;
import com.datadog.api.client.ApiException;
import com.datadog.api.client.v2.api.LogsApi;
import com.datadog.api.client.v2.model.ContentEncoding;
import com.datadog.api.client.v2.model.HTTPLogItem;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

// Handler value: connexas3datadog.Handler
public class Handler implements RequestHandler<S3Event, String> {
    private static final Logger logger = LoggerFactory.getLogger(Handler.class);
    private final String DD_SITE = "DD_SITE";
    private final String DD_API_KEY_SECRET_ARN = "DD_API_KEY_SECRET_ARN";
    private final String LOG_DD_SOURCE = "CloudConnexa";
    private final String LOG_DD_TAGS = "DD_TAGS";
    private final String LOG_HOSTNAME = "";
    private final String LOG_SERVICE = "";
    private final int MAX_SIZE_PAYLOAD = 5000000;
    private final int MAX_SIZE_SINGLE_LOG = 1000000;
    private final int LOG_MAX_ENTRIES = 1000;
    private final String OBJECT_KEY_PREFIX = "CloudConnexa";
    private final String REGEX = ".*\\.([^.]*)\\.([^.]*)";
    private final String JSONL_TYPE = "jsonl";
    private final String GZ_TYPE = "gz";

    @Override
    public String handleRequest(S3Event s3event, Context context) {
        try {
            List<String> allLogs = new ArrayList<>();

            for (S3EventNotificationRecord record : s3event.getRecords()) {
                String bucketName = record.getS3().getBucket().getName();

                S3Client s3Client = S3Client.builder().build();

                // Object key may have spaces or unicode non-ASCII characters.
                String srcKey = record.getS3().getObject().getUrlDecodedKey();

                if (!srcKey.startsWith(OBJECT_KEY_PREFIX)) {
                    logger.info("Unable to infer prefix for key " + srcKey);
                    return "";
                }

                // Infer the type.
                Matcher matcher = Pattern.compile(REGEX).matcher(srcKey);
                if (!matcher.matches()) {
                    logger.info("Unable to infer type for key " + srcKey);
                    return "";
                }
                String jsonType = matcher.group(1);
                if (!(JSONL_TYPE.equals(jsonType))) {
                    logger.info("Skipping not jsonl type " + srcKey);
                    return "";
                }
                String gzType = matcher.group(2);
                if (!(GZ_TYPE.equals(gzType))) {
                    logger.info("Skipping not gz type " + srcKey);
                    return "";
                }

                InputStream s3Object = getObject(s3Client, bucketName, srcKey);
                for (List<String> logs : getLogs(s3Object)) {
                    allLogs.addAll(logs);
                }
            }

            toDatadog(allLogs);

            return "Ok";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getObject(S3Client s3Client, String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    private List<List<String>> getLogs(InputStream inputStream) throws IOException {
        List<String> logs = new ArrayList<>();
        List<List<String>> result = new ArrayList<>();
        result.add(logs);

        try (GZIPInputStream gzis = new GZIPInputStream(inputStream);
             InputStreamReader reader = new InputStreamReader(gzis);
             BufferedReader in = new BufferedReader(reader)) {
            String readed;
            int size = 0;
            while ((readed = in.readLine()) != null) {
                byte[] readBytes = readed.getBytes();
                size += readBytes.length;
                if (size > MAX_SIZE_PAYLOAD) {
                    logs = new ArrayList<>();
                    result.add(logs);
                    size = 0;
                }

                logs.addAll(Splitter.fixedLength(MAX_SIZE_SINGLE_LOG).splitToList(readed));
            }
        }
        return result;
    }

    private String getSecret(String secretId) {
        // Create a Secrets Manager client
        SecretsManagerClient client = SecretsManagerClient.builder()
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretId)
                .build();

        GetSecretValueResponse getSecretValueResponse = null;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            logger.error(
                    "Exception when calling SecretsManagerClient#getSecretValue" +
                            " Message: " + e.getMessage()
            );
            System.exit(1);
        }

        return getSecretValueResponse.secretString();
    }

    private void toDatadog(List<String> allLogs) {
        ApiClient defaultClient = ApiClient.getDefaultApiClient();
        // Configure the Datadog site to send API calls to
        Map<String, String> serverVariables = new HashMap<>();
        serverVariables.put("site", System.getenv(DD_SITE));
        defaultClient.setServerVariables(serverVariables);
        // Configure API key authorization
        Map<String, String> secrets = new HashMap<>();
        secrets.put("apiKeyAuth", getSecret(System.getenv(DD_API_KEY_SECRET_ARN)));
        defaultClient.configureApiKeys(secrets);

        LogsApi apiInstance = new LogsApi(defaultClient);

        String ddsource = LOG_DD_SOURCE;
        String ddtags = System.getenv(LOG_DD_TAGS);
        String hostname = LOG_HOSTNAME;
        String service = LOG_SERVICE;

        for (List<String> logs : Lists.partition(allLogs, LOG_MAX_ENTRIES)) {
            List<HTTPLogItem> body = logs.stream()
                    .map(log -> new HTTPLogItem()
                            .ddsource(ddsource)
                            .ddtags(ddtags)
                            .hostname(hostname)
                            .message(log)
                            .service(service)
                    )
                    .collect(Collectors.toList());

            try {
                apiInstance.submitLog(
                        body,
                        new LogsApi.SubmitLogOptionalParameters().contentEncoding(ContentEncoding.GZIP)
                );
            } catch (ApiException e) {
                logger.error(
                        "Exception when calling LogsApi#submitLog" +
                                " Status code: " + e.getCode() +
                                " Reason: " + e.getResponseBody() +
                                " Response headers: " + e.getResponseHeaders()
                );
                System.exit(1);
            }
        }
    }
}
