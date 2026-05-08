package com.btvn.projectfinal.controller.lecturer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lecture")
public class LectureViewController {
    @GetMapping("/dashboardEcture")
    public String dashboardEcture() {
        return "lecturer/dashboardLecture";
    }
}
