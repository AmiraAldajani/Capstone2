
package com.example.labsurplus.DTO;

import com.example.labsurplus.Model.SurplusRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// الملخص من الـ AI (اقتراح بس)
@Data
@AllArgsConstructor
public class RequestSummary {
    private Integer offerId;
    private String aiSummary;
    private List<SurplusRequest> pendingRequests;
}