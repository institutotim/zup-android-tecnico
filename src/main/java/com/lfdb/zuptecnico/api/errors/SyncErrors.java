package com.lfdb.zuptecnico.api.errors;

/**
 * Created by Renan on 10/03/2016.
 */
public class SyncErrors {
    public static final int NOT_FOUND_ERROR_CODE = 404;
    public static final int FORBIDDEN_ERROR_CODE = 403;
    public static final int BAD_REQUEST_ERROR_CODE = 400;

    public static Exception build(int type, Exception originalError) {
        switch (type){
            case NOT_FOUND_ERROR_CODE:
                return new NotFoundError();
            case FORBIDDEN_ERROR_CODE:
                return new ForbiddenError();
            case BAD_REQUEST_ERROR_CODE:
                return new BadRequestError();
            default:
                return originalError;
        }
    }
}
