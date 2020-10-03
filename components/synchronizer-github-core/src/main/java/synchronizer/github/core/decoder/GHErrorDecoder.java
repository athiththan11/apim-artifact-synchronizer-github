package synchronizer.github.core.decoder;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import synchronizer.github.core.exception.GHClientException;

public class GHErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499) {
            return new GHClientException(response.status(), response.reason());
        }
        if (response.status() >= 500 && response.status() <= 599) {
            return new GHClientException(response.status(), response.reason());
        }
        return FeignException.errorStatus(methodKey, response);
    }

}
