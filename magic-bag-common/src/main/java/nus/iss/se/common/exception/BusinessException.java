package nus.iss.se.common.exception;

import lombok.Getter;
import nus.iss.se.common.constant.ResultStatus;
import org.apache.commons.lang3.StringUtils;

public class BusinessException extends RuntimeException {
    @Getter
    private final ResultStatus errInfo;

    @Getter
    private final String supplementMessage;

    public BusinessException(ResultStatus errInfo, String supplementMessage) {
        super(buildMessage(errInfo,supplementMessage));
        this.errInfo = errInfo;
        this.supplementMessage = supplementMessage;
    }

    public BusinessException(ResultStatus errInfo) {
        this(errInfo,null);
    }

    private static String buildMessage(ResultStatus errInfo, String supplement){
        return StringUtils.isBlank(supplement) ? errInfo.getMessage() : errInfo.getMessage() + ": " + supplement;
    }
}
