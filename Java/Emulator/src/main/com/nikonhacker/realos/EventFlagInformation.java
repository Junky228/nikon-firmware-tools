package com.nikonhacker.realos;

import com.nikonhacker.Format;

public class EventFlagInformation extends RealOsObject {

    private int waitTaskInformation;
    private int flagPattern;

    public EventFlagInformation(ErrorCode errorCode, int extendedInformation, int waitTaskInformation, int flagPattern) {
        super(extendedInformation, errorCode);
        this.waitTaskInformation = waitTaskInformation;
        this.flagPattern = flagPattern;
    }

    public int getWaitTaskInformation() {
        return waitTaskInformation;
    }

    @Override
    public String toString() {
        if (getErrorCode() != ErrorCode.E_OK) {
            return getErrorCode().toString();
        }
        return "Pattern 0b" + Format.asBinary(flagPattern,32) + ", " + ((waitTaskInformation==0)?"no waiting task":"first waiting task=0x" + Format.asHex(waitTaskInformation, 2)) + ", extendedInformation=0x" + Format.asHex(getExtendedInformation(), 8);
    }
}
