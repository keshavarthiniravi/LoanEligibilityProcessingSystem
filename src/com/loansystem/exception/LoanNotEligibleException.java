package com.loansystem.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoanNotEligibleException extends Exception {
    private final List<String> reasons;

    public LoanNotEligibleException(List<String> reasons) {
        super("Not eligible: " + String.join("; ", reasons));
        this.reasons = new ArrayList<>(reasons);
    }

    public List<String> getReasons() {
        return Collections.unmodifiableList(reasons);
    }
}
