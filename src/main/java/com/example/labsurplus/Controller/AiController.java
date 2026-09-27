package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.Service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        String reply = aiService.ask("Answer in one sentence.", "Say hello");
        if (reply == null)
            return ResponseEntity.status(500).body(new ApiResponse("AI call failed, check the logs"));
        return ResponseEntity.status(200).body(new ApiResponse(reply));
    }
}
