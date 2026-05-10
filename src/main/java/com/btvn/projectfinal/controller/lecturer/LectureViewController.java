package com.btvn.projectfinal.controller.lecturer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lecturer")
public class LectureViewController {
    @GetMapping("/dashboardLecture")
    public String dashboardLecture() {
        return "lecturer/dashboardLecture";
    }
}
