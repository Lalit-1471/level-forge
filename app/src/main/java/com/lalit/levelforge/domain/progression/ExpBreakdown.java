package com.lalit.levelforge.domain.progression;

public class ExpBreakdown {

    private final int baseExp;
    private final int effortExp;
    private final int setTypeExp;
    private final int overloadExp;
    private final int totalExp;

    public ExpBreakdown(int baseExp, int effortExp, int setTypeExp, int overloadExp, int totalExp) {
        this.baseExp = baseExp;
        this.effortExp = effortExp;
        this.setTypeExp = setTypeExp;
        this.overloadExp = overloadExp;
        this.totalExp = totalExp;
    }

    public int getBaseExp() {
        return baseExp;
    }

    public int getEffortExp() {
        return effortExp;
    }

    public int getSetTypeExp() {
        return setTypeExp;
    }

    public int getOverloadExp() {
        return overloadExp;
    }

    public int getTotalExp() {
        return totalExp;
    }
}
