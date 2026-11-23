package com.loansystem.ui;

import com.loansystem.service.LoanService;
import com.loansystem.service.SampleData;

final class SampleLoader {
    private SampleLoader() { }

    static int run(LoanService service) {
        return SampleData.load(service);
    }
}
