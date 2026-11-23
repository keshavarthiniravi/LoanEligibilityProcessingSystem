package com.loansystem.ui;

import com.loansystem.service.LoanService;
import com.loansystem.service.SampleData;

/** Tiny bridge so the dashboard does not depend directly on sample-data details. */
final class SampleLoader {
    private SampleLoader() { }

    static int run(LoanService service) {
        return SampleData.load(service);
    }
}
