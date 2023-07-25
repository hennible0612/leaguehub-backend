package leaguehub.leaguehubbackend.exception.participant.exception;

import leaguehub.leaguehubbackend.exception.global.ExceptionCode;

import static leaguehub.leaguehubbackend.exception.participant.ParticipantExceptionCode.INVALID_PARTICIPANT_TIER_REQUEST;

public class ParticipantInvalidRankException extends RuntimeException{

    private final ExceptionCode exceptionCode;

    public ParticipantInvalidRankException(){
        super(INVALID_PARTICIPANT_TIER_REQUEST.getMessage());
        this.exceptionCode = INVALID_PARTICIPANT_TIER_REQUEST;
    }

    public ExceptionCode getExceptionCode(){
        return exceptionCode;
    }
}
