package kr.hwan.monami.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LandingController {

    /**
     * 랜딩
     * @return
     */
    @GetMapping({"/", "/index"})
    public String index() {
        return "index.html";
    }

    
}
