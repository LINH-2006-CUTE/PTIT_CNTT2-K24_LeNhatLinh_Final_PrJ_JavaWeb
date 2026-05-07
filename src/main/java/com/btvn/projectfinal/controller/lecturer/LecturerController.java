package com.btvn.projectfinal.controller.lecturer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lecturer")
public class LecturerController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "lecturer/dashboard";
    }


}
